package com.KryptoChat.Controllers;

import com.KryptoChat.Services.RegisterService;
import com.KryptoChat.Services.ServiceResponse;
import com.KryptoChat.Views.Register;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

/**
 * Kontroler obsługujący rejestrację użytkownika.
 * Łączy widok rejestracji z serwisem autoryzacji.
 */
public class RegisterController {

    /** Widok rejestracji */
    private Register registerView;

    /** Serwis obsługujący rejestrację */
    private RegisterService service;

    /** Akcja przejścia do widoku logowania */
    Runnable goToLogin;

    /**
     * Konstruktor kontrolera rejestracji.
     * @param view widok rejestracji
     * @param service serwis obsługujący rejestrację
     * @param goToLogin akcja przełączająca widok na ekran logowania
     */
    public RegisterController(Register view, RegisterService service, Runnable goToLogin)  {
        this.registerView = view;
        this.service = service;
        this.goToLogin = goToLogin;

        init();
    }

    /**
     * Inicjalizuje obsługę zdarzeń w widoku rejestracji.
     * Po kliknięciu przycisku wykonywana jest metoda handleLogin().
     */
    private void init() {
        registerView.getButton().setOnAction(e -> handleLogin());
    }

    /**
     * Obsługuje proces rejestracji użytkownika.
     * Pobiera dane z formularza, a następnie sprawdza ich poprawność,
     * wysyła żądanie rejestracji do serwisu oraz reaguje na odpowiedź.
     *
     * Po poprawnej rejestracji użytkownik zostaje przekierowany do widoku logowania.
     */
    private void handleLogin() {

        String username = registerView.getLogin().getText().trim();
        String password = registerView.getPassword().getText();
        String password2 = registerView.getPassword2().getText();

        if (username.isEmpty() || password.isEmpty() || password2.isEmpty()) {
            registerView.getLabel().setText("Pola nie mogą być puste!");
            clearFields();
            return;
        } else if (username.length() < 3 || username.length() > 20 ) {
            registerView.getLabel().setText("Login powinien mieć od 3 do 20 znaków");
            clearFields();
            return;
        } else if (!password.equals(password2)) {
            registerView.getLabel().setText("Hasła się różnią!");
            clearFields();
            return;

        } else if (password.length() < 8 || password.length() > 30 ) {
            registerView.getLabel().setText("Hasło powinno mieć od 8 do 30 znaków!");
            clearFields();
            return;

        } else if (!validPassword(password)) {
            registerView.getLabel().setText("Hasło powinno zawierać co najmniej 1: wielką litere, małą litere, cyfre i znak specjalny");
            clearFields();
            return;
        }

        ServiceResponse response = service.register(username, password, password2);
        registerView.getLabel().setText(response.getMessage());

        if(response.isSuccess()) {
            PauseTransition delay = new PauseTransition(Duration.seconds(2));
            delay.setOnFinished(e -> goToLogin.run());
            delay.play();
        }
        clearFields();
    }

    /**
     * Sprawdza poprawność hasła przy użyciu wyrażenia regularnego.
     * Hasło musi zawierać: małą literę, wielką litere, cyfrę i znak specjalny
     * @param password hasło, które sprawdzamy
     * @return true jeśli hasło spełnia wymagania, w przeciwnym razie false
     */
    private boolean validPassword(String password) {

        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).+$";

        return password.matches(regex);
    }


    /**
     * Czyści pola do wpisywania.
     */
    private void clearFields() {
        registerView.getLogin().clear();
        registerView.getPassword().clear();
        registerView.getPassword2().clear();
    }
}