package com.KryptoChat.Controllers;

import com.KryptoChat.Models.User;
import com.KryptoChat.Services.LoginService;
import com.KryptoChat.Services.ServiceResponse;
import com.KryptoChat.Views.Login;
import com.KryptoChat.security.TokenStorage;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class LoginControllerTest {

    private Login loginView;
    private LoginService loginService;
    private Runnable goToGroups;
    private Runnable goToChats;
    private LoginController controller;

    private Button realButton;
    private Label realLabel;
    private TextField realLoginField;
    private PasswordField realPasswordField;

    @BeforeAll
    static void initJFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Ignorujemy błąd
        }
    }

    @BeforeEach
    void setUp() {
        loginView = Mockito.mock(Login.class);
        loginService = Mockito.mock(LoginService.class);
        goToGroups = Mockito.mock(Runnable.class);
        goToChats = Mockito.mock(Runnable.class);

        realButton = new Button();
        realLabel = new Label();
        realLoginField = new TextField();
        realPasswordField = new PasswordField();

        Mockito.when(loginView.getButton()).thenReturn(realButton);
        Mockito.when(loginView.getLabel()).thenReturn(realLabel);
        Mockito.when(loginView.getLogin()).thenReturn(realLoginField);
        Mockito.when(loginView.getPassword()).thenReturn(realPasswordField);

        TokenStorage.setUser(null);

        controller = new LoginController(loginView, loginService, goToGroups, goToChats);
    }

    @Test
    void shouldShowErrorWhenAnyFieldIsEmpty() {
        realLoginField.setText("");
        realPasswordField.setText("Password123!");

        realButton.fire();

        assertEquals("Pola nie mogą być puste!", realLabel.getText());
        Mockito.verifyNoInteractions(loginService);
    }

    @Test
    void shouldCallServiceAndShowFailureMessageWhenLoginFails() {
        String user = "nacia";
        String pass = "WrongPass123";
        realLoginField.setText(user);
        realPasswordField.setText(pass);

        ServiceResponse failureResponse = new ServiceResponse(false, "Błędny login lub hasło");
        Mockito.when(loginService.login(user, pass)).thenReturn(failureResponse);

        realButton.fire();

        Mockito.verify(loginService).login(user, pass);
        assertEquals("Błędny login lub hasło", realLabel.getText());
        assertTrue(realLoginField.getText().isEmpty());
        assertTrue(realPasswordField.getText().isEmpty());
    }

    @Test
    void shouldRedirectToChatsWhenLoginSucceedsAndUserHasGroupId() {
        String user = "nacia";
        String pass = "ValidPass123";
        realLoginField.setText(user);
        realPasswordField.setText(pass);

        // Przygotowujemy makietę użytkownika, który posiada przypisane GroupId
        User mockUser = new User();
        mockUser.setGroupId(123L);
        TokenStorage.setUser(mockUser);

        ServiceResponse successResponse = new ServiceResponse(true, "Zalogowano pomyślnie");
        Mockito.when(loginService.login(user, pass)).thenReturn(successResponse);

        realButton.fire();

        Mockito.verify(loginService).login(user, pass);
        assertEquals("Zalogowano pomyślnie", realLabel.getText());
        assertTrue(realLoginField.getText().isEmpty());
    }

    @Test
    void shouldRedirectToGroupsWhenLoginSucceedsAndUserHasNoGroupId() {
        String user = "nacia";
        String pass = "ValidPass123";
        realLoginField.setText(user);
        realPasswordField.setText(pass);

        // Przygotowujemy makietę użytkownika, który NIE posiada przypisanego GroupId
        User mockUser = new User();
        mockUser.setGroupId(null);
        TokenStorage.setUser(mockUser);

        ServiceResponse successResponse = new ServiceResponse(true, "Zalogowano pomyślnie");
        Mockito.when(loginService.login(user, pass)).thenReturn(successResponse);

        realButton.fire();

        Mockito.verify(loginService).login(user, pass);
        assertEquals("Zalogowano pomyślnie", realLabel.getText());
        assertTrue(realLoginField.getText().isEmpty());
    }
}