package com.KryptoChat.Controllers;

import com.KryptoChat.Services.ServiceResponse;
import com.KryptoChat.Views.CreateGroup;
import com.KryptoChat.Services.GroupService;
import com.KryptoChat.security.GroupKeyStorage;
import com.KryptoChat.security.TokenStorage;
import javafx.animation.PauseTransition;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.util.Duration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

/**
 * Kontroler obsługujący tworzenie oraz dołączanie do grupy.
 * Łączy widok z logiką aplikacji i serwisem grup.
 */
public class GroupController {

    /** Widok tworzenia/dołączania do grupy */
    private CreateGroup groupView;

    /** Serwis obsługujący operacje na grupach */
    private GroupService groupService;

    /** Akcja przejścia do widoku czatu */
    private Runnable goToChats;

    /** Akcja przejścia do widoku logowania (wylogowanie) */
    private Runnable goToLogin;

    /**
     * Konstruktor kontrolera grup.
     * @param view widok tworzenia/dołączania do grupy
     * @param service serwis obsługujący grupy
     * @param chats akcja przełączająca widok na widok czatu
     * @param login akcja przełączająca widok na ekran logowania
     */
    public GroupController(CreateGroup view, GroupService service, Runnable chats, Runnable login) {
        this.groupView = view;
        this.groupService = service;
        this.goToChats = chats;
        this.goToLogin = login;

        init();
    }

    /**
     * Inicjalizuje obsługę zdarzeń przycisków w widoku.
     */
    private void init() {
        groupView.getCreate().setOnAction(e -> createGroup());
        groupView.getJoin().setOnAction(e -> joinGroup());
        groupView.getLogoutButton().setOnAction(e -> logout());
    }

    /**
     * Wylogowuje użytkownika, usuwa zapisany token i czyści pola.
     * Umożliwia także usunięcie lokalnych kluczy szyfrujących jeżeli ktoś zalogował się na
     * innym urządzeniu
     */
    public void logout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(groupView.getScene().getWindow());
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
        clearFields();
        groupView.getMessage().setText("");
        TokenStorage.setUser(null);
        TokenStorage.setCachedToken(null);
        TokenStorage.deleteToken();

        goToLogin.run();
    }

    /**
     * Obsługuje tworzenie nowej grupy.
     * Sprawdza poprawność nazwy grupy, wysyła żądanie utworzenia grupy i po sukcesie przełącza widok na czat.
     */
    private void createGroup() {
        String groupName = groupView.getGroupNameField().getText().trim();

        if (groupName.isBlank()) {
            groupView.getMessage().setText("Nazwa grupy nie może być pusta");
            clearFields();
            return;
        }

        if (groupName.length() > 20) {
            groupView.getMessage().setText("Nazwa grupy nie może mieć więcej niż 20 znaków");
            clearFields();
            return;
        }

        if (groupName.length() < 3) {
            groupView.getMessage().setText("Nazwa grupy musi mieć conajmniej 3 znaki");
            clearFields();
            return;
        }

        ServiceResponse response = groupService.createGroup(groupName);
        groupView.getMessage().setText(response.getMessage());

        clearFields();

        if (response.isSuccess()) {
            PauseTransition delay = new PauseTransition(Duration.seconds(2));

            delay.setOnFinished(e -> goToChats.run());
            delay.play();
        }

    }

    /**
     * Obsługuje dołączanie do istniejącej grupy na podstawie kodu.
     * Po poprawnym dołączeniu przełącza widok na widok czatu.
     */
    private void joinGroup() {
        String code = groupView.getCodeField().getText().trim();

        if (code.isBlank()) {
            groupView.getMessage().setText("Kod grupy nie może być pusty");
            clearFields();
            return;
        }

        if (code.length() != 6 || !isCodeValid(code)) {
            groupView.getMessage().setText("Kod grupy powinien zawierać 5 znaków poprzedzonych znakiem #");
            clearFields();
            return;
        }

        ServiceResponse response = groupService.joinGroup(code);
        groupView.getMessage().setText(response.getMessage());

        clearFields();

        if (response.isSuccess()) {
            PauseTransition delay = new PauseTransition(Duration.seconds(2));
            delay.setOnFinished(e -> goToChats.run());
            delay.play();
        }
    }

    /**
     * Czyści pola do wpisywania.
     */
    private void clearFields() {
        groupView.getGroupNameField().clear();
        groupView.getCodeField().clear();
    }

    private boolean isCodeValid(String code) {
        return code != null && code.matches("^#[a-z0-9]{5}$");
    }
}