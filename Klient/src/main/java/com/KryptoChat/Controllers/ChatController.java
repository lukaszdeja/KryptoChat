package com.KryptoChat.Controllers;

import com.KryptoChat.Models.Group;
import com.KryptoChat.Services.ChatService;
import com.KryptoChat.Services.CryptoService;
import com.KryptoChat.Services.WebSocketService;
import com.KryptoChat.Views.Chat;
import com.KryptoChat.Models.Message;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListCell;
import com.KryptoChat.security.GroupKeyStorage;
import com.KryptoChat.security.TokenStorage;

import javax.crypto.SecretKey;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.time.LocalDateTime;

/**
 * Kontroler obsługujący logikę czatu.
 * Łączy widok czatu z serwisami komunikacji i modelami danych.
 */
public class ChatController {

    /** Widok czatu */
    private final Chat chatView;

    /** Serwis obsługujący dane czatu */
    private ChatService chatService;

    /** Akcja powrotu do ekranu logowania */
    private Runnable goToLogin;

    /** Serwis obsługujący połączenie WebSocket */
    private final WebSocketService webSocketService;

    /** Aktualna grupa użytkownika */
    Group group;

    /**
     * Konstruktor kontrolera czatu.
     * @param chat widok czatu
     * @param chatService serwis obsługujący dane czatu
     * @param goToLogin akcja wykonywana po wylogowaniu
     */
    public ChatController(Chat chat, ChatService chatService, Runnable goToLogin, WebSocketService webSocketService){
        this.chatView = chat;
        this.chatService = chatService;
        this.goToLogin = goToLogin;
        this.webSocketService = webSocketService;
        init();
    }

    /**
     * Inicjalizuje obsługę zdarzeń oraz ładuje dane czatu.
     */
    private void init(){
        chatView.getSendButton().setOnAction(e -> handleSend());
        chatView.getMessageField().setOnAction(e -> handleSend());
        chatView.getLogoutButton().setOnAction(e -> logout());

        loadGroup();
        loadMessages();

        webSocketService.setOnMessageReceived(message -> {
            Platform.runLater(() -> { chatView.getMessages().add(message); });
        });

        webSocketService.setOnKeyReceived(() -> {
            Platform.runLater(() -> {
                chatView.getMessageField().setDisable(false);
                chatView.getSendButton().setDisable(false);
                chatView.getMessageField().setPromptText("Napisz wiadomość...");
                loadMessages();
            });
        });

        webSocketService.setOnGroupUpdated(() -> {
            Platform.runLater(this::loadGroup);
        });

        webSocketService.connect();
    }

    /**
     * Ładuje informacje o grupie użytkownika i aktualizuje dane w widoku.
     */
    public void loadGroup() {
        group = chatService.loadGroup();

        if(group == null) { return; }

        chatView.getGroupNameLabel().setText(group.getGroupName());
        chatView.getGroupCodeLabel().setText("Kod do dołączenia: "+group.getCode());
        chatView.getUserList().setItems(FXCollections.observableArrayList(group.getUsers()));
    }

    /**
     * Ładuje historię wiadomości i wyświetla ją w widoku.
     */
    public void loadMessages() {
        if (!GroupKeyStorage.exists(TokenStorage.getUser().getUsername())) {
            chatView.getMessageField().setDisable(true);
            chatView.getSendButton().setDisable(true);
            chatView.getMessageField().setPromptText("Oczekiwanie na klucz grupy...");
        }
        List<Message> messages = chatService.loadMessages();

        chatView.getMessages().clear();

        if (messages != null) {
            try {
                SecretKey aesKey = GroupKeyStorage.load(TokenStorage.getUser().getUsername());
                for (Message msg : messages) {
                    try {
                        msg.setContent(CryptoService.decryptAES(msg.getContent(), aesKey));
                    } catch (Exception e) {
                        System.out.println("Nie udalo sie odszyfrowac tej wiadomosci");
                    }
                }
            } catch (Exception e) {
                System.out.println("Brak klucza - nie udalo sie odszyfrowac wiadomosci z historii");
            }
            chatView.getMessages().addAll(messages);
        }


    }

    /**
     * Wylogowuje użytkownika, usuwa zapisany token oraz rozłącza WebSocket.
     * Umożliwia opcję usunięcia kluczy szyfrujących z urządzenia - na przykład jeżeli ktoś zalogował
     * sie na innym urządzeniu i nie chce zeby klucze na nim pozostaly
     */
    public void logout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(chatView.getScene().getWindow());
        alert.setTitle("Wylogowanie");
        alert.setHeaderText("Usunąć lokalne klucze?");
        alert.setContentText(
                "Czy chcesz usunąć wszystkie lokalnie zapisane klucze kryptograficzne?"
        );
        ButtonType keepButton = new ButtonType("Zostaw");
        ButtonType deleteButton = new ButtonType("Usuń klucze");
        ButtonType cancelButton = new ButtonType("Anuluj");
        alert.getButtonTypes().setAll(
                keepButton,
                deleteButton,
                cancelButton
        );
        var result = alert.showAndWait();
        if (result.isEmpty() || result.get() == cancelButton) {
            return;
        }
        if (result.get() == deleteButton) {
            try {
                Path userDir = GroupKeyStorage
                        .getPath(TokenStorage.getUser().getUsername())
                        .getParent();
                if (Files.exists(userDir)) {
                    Files.walk(userDir)
                            .sorted(Comparator.reverseOrder())
                            .forEach(path -> {
                                try {
                                    Files.delete(path);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        TokenStorage.setUser(null);
        TokenStorage.setCachedToken(null);
        TokenStorage.deleteToken();

        webSocketService.disconnect();
        goToLogin.run();
    }

    /**
     * Obsługuje wysyłanie wiadomości.
     * Tworzy obiekt wiadomości i wysyła go przez WebSocket.
     */
    private void handleSend() {
        String text = chatView.getMessageField().getText();

        if (text == null || text.isEmpty()) return;

        if (text.length() > 1500) {
            System.out.println("Dlugosc wiadomosci nie moze przekraczac 1500 znakow");
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.initOwner(chatView.getScene().getWindow());
            alert.setTitle("Błąd");
            alert.setHeaderText("Nie można wysłać wiadomości");
            alert.setContentText("Długość wiadomości nie może przekraczać 1500 znaków.");
            alert.showAndWait();
            chatView.getMessageField().clear();
            return;
        }

        Message message = new Message();

        message.setSender(TokenStorage.getUser().getUsername());
        message.setContent(text);
        message.setGroupId(TokenStorage.getUser().getGroupId());
        message.setSend_time(LocalDateTime.now());

        webSocketService.send(message);
        chatView.getMessageField().clear();
    }
}