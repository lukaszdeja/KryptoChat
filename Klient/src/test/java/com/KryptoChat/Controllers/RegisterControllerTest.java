package com.KryptoChat.Controllers;

import com.KryptoChat.Services.RegisterService;
import com.KryptoChat.Services.ServiceResponse;
import com.KryptoChat.Views.Register;
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

class RegisterControllerTest {

    private Register registerView;
    private RegisterService registerService;
    private Runnable goToLogin;
    private RegisterController controller;

    private Button realButton;
    private Label realLabel;
    private TextField realLoginField;
    private PasswordField realPasswordField;
    private PasswordField realPassword2Field;

    @BeforeAll
    static void initJFX() {
        // Inicjalizuje toolkit JavaFX bezpośrednio, bez używania komponentów Swing/AWT
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Toolkit mógł już zostać zainicjalizowany przez inny test, ignorujemy ten błąd
        }
    }

    @BeforeEach
    void setUp() {
        registerView = Mockito.mock(Register.class);
        registerService = Mockito.mock(RegisterService.class);
        goToLogin = Mockito.mock(Runnable.class);

        realButton = new Button();
        realLabel = new Label();
        realLoginField = new TextField();
        realPasswordField = new PasswordField();
        realPassword2Field = new PasswordField();

        Mockito.when(registerView.getButton()).thenReturn(realButton);
        Mockito.when(registerView.getLabel()).thenReturn(realLabel);
        Mockito.when(registerView.getLogin()).thenReturn(realLoginField);
        Mockito.when(registerView.getPassword()).thenReturn(realPasswordField);
        Mockito.when(registerView.getPassword2()).thenReturn(realPassword2Field);

        controller = new RegisterController(registerView, registerService, goToLogin);
    }

    @Test
    void shouldShowErrorWhenAnyFieldIsEmpty() {
        realLoginField.setText("");
        realPasswordField.setText("Password123!");
        realPassword2Field.setText("Password123!");

        realButton.fire();

        assertEquals("Pola nie mogą być puste!", realLabel.getText());
        Mockito.verifyNoInteractions(registerService);
    }

    @Test
    void shouldShowErrorWhenPasswordsDoNotMatch() {
        realLoginField.setText("nacia");
        realPasswordField.setText("Password123!");
        realPassword2Field.setText("Different123!");

        realButton.fire();

        assertEquals("Hasła się różnią!", realLabel.getText());
        Mockito.verifyNoInteractions(registerService);
    }

    @Test
    void shouldShowErrorWhenPasswordIsTooShort() {
        realLoginField.setText("nacia");
        realPasswordField.setText("Pas1!");
        realPassword2Field.setText("Pas1!");

        realButton.fire();

        assertEquals("Hasło powinno mieć co najmniej 8 znaków!", realLabel.getText());
        Mockito.verifyNoInteractions(registerService);
    }

    @Test
    void shouldShowErrorWhenPasswordDoesNotMatchRegexRequirements() {
        realLoginField.setText("nacia");
        realPasswordField.setText("passwordwithoutuppercase1!");
        realPassword2Field.setText("passwordwithoutuppercase1!");

        realButton.fire();

        assertEquals("Hasło powinno zawierać co najmniej 1: wielką litere, małą litere, cyfre i znak specjalny", realLabel.getText());
        Mockito.verifyNoInteractions(registerService);
    }

    @Test
    void shouldCallServiceAndShowFailureMessageWhenRegistrationFails() {
        String user = "nacia";
        String pass = "ValidPass123!";
        realLoginField.setText(user);
        realPasswordField.setText(pass);
        realPassword2Field.setText(pass);

        ServiceResponse failureResponse = new ServiceResponse(false, "Login jest już zajęty");
        Mockito.when(registerService.register(user, pass, pass)).thenReturn(failureResponse);

        realButton.fire();

        Mockito.verify(registerService).register(user, pass, pass);
        assertEquals("Login jest już zajęty", realLabel.getText());

        assertTrue(realLoginField.getText().isEmpty());
        assertTrue(realPasswordField.getText().isEmpty());
        assertTrue(realPassword2Field.getText().isEmpty());
    }

    @Test
    void shouldCallServiceAndShowSuccessMessageWhenRegistrationSucceeds() {
        String user = "nacia";
        String pass = "ValidPass123!";
        realLoginField.setText(user);
        realPasswordField.setText(pass);
        realPassword2Field.setText(pass);

        ServiceResponse successResponse = new ServiceResponse(true, "Zarejestrowano pomyślnie!");
        Mockito.when(registerService.register(user, pass, pass)).thenReturn(successResponse);

        realButton.fire();

        Mockito.verify(registerService).register(user, pass, pass);
        assertEquals("Zarejestrowano pomyślnie!", realLabel.getText());
        assertTrue(realLoginField.getText().isEmpty());
    }
}