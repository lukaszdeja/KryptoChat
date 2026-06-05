package com.KryptoChat.Controllers;

import com.KryptoChat.Models.Group;
import com.KryptoChat.Services.ChatService;
import com.KryptoChat.Services.CryptoService;
import com.KryptoChat.Services.WebSocketService;
import com.KryptoChat.Views.Chat;
import com.KryptoChat.Models.Message;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.ListCell;
import com.KryptoChat.security.GroupKeyStorage;
import com.KryptoChat.security.TokenStorage;

import javax.crypto.SecretKey;
import java.util.ArrayList;
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
     */
    public void logout() {
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

        if (text.length() > 500) {
            System.out.println("Dlugosc wiadomosci nie moze przekraczac 500 znakow");
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