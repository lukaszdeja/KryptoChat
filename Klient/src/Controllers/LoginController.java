package Controllers;

import Services.LoginService;
import Views.Login;
import javafx.animation.PauseTransition;
import javafx.util.Duration;


/**
 * Klasa obsługująca controller logowania - połączenie interfejsu z backendem
 */
public class LoginController {
    private Login loginView;
    private LoginService logservice;
    Runnable goToGroups;

    /**
     * Konstruktor klasy, inicjuje pola klasy i wywoluje metode init()
     * @param view - obiekt okna logowania (UI)
     * @param service - obiekt serwisu logowania
     * @param goToGroups - callback do funkcji przelaczajacej widok na okno czatu
     */
    public LoginController(Login view, LoginService service, Runnable goToGroups)  {
        this.loginView = view;
        this.logservice = service;
        this.goToGroups = goToGroups;
        init();
    }

    /**
     * Pomocnicza metoda, ktora przypisuje do przycisku z widoku logowania reakcje na klikniecie
     * Po kliknieciu przycisku wywolana jest metoda handleLogin()
     */
    private void init() {
        loginView.getButton().setOnAction(e -> handleLogin());
    }

    /**
     * Metoda obsluguje procedurę logowania
     * Frontendowa walidacja danych
     * Przekazanie do serwisu
     * Odpowiedź z serwisu i reakcją na nią
     */
    private void handleLogin() {
        String username = String.valueOf(loginView.getLogin());
        String password = loginView.getPassword();

        if (username.isEmpty() || password.isEmpty()) {
           loginView.getLabel().setText("Pola nie mogą być puste!");
            return;
        }

        boolean success = logservice.login(username, password);
        if (success) {
            loginView.getLabel().setText("Zalogowano!");
            PauseTransition delay = new PauseTransition(Duration.seconds(2));
            delay.setOnFinished(e -> goToGroups.run());
            delay.play();

        } else {
            loginView.getLabel().setText("Wyjątek przez połączenie z serwerem");
        }
    }
}