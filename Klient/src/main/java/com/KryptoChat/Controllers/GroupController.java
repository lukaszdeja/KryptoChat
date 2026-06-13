package com.KryptoChat.Controllers;

import com.KryptoChat.Services.ServiceResponse;
import com.KryptoChat.Views.CreateGroup;
import com.KryptoChat.Services.GroupService;
import com.KryptoChat.security.TokenStorage;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

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
     */
    public void logout() {
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