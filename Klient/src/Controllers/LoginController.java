package Controllers;

import Services.LoginService;
import Services.ServiceResponse;
import Views.Login;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import security.TokenStorage;


/**
 * Klasa obsługująca controller logowania - połączenie interfejsu z backendem
 */
public class LoginController {
    private Login loginView;
    private LoginService logservice;
    Runnable goToGroups;
    Runnable goToChats;

    /**
     * Konstruktor klasy, inicjuje pola klasy i wywoluje metode init()
     * @param view - obiekt okna logowania (UI)
     * @param service - obiekt serwisu logowania
     * @param goToGroups - callback do funkcji przelaczajacej widok na okno czatu
     */
    public LoginController(Login view, LoginService service, Runnable goToGroups, Runnable goToChats)  {
        this.loginView = view;
        this.logservice = service;
        this.goToGroups = goToGroups;
        this.goToChats = goToChats;
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
        String username = loginView.getLogin().getText();
        String password = loginView.getPassword().getText();

        if (username.isEmpty() || password.isEmpty()) {
            loginView.getLabel().setText("Pola nie mogą być puste!");
            return;
        }

        ServiceResponse response = logservice.login(username, password);
        loginView.getLabel().setText(response.getMessage());

        if (response.isSuccess()) {
            PauseTransition delay = new PauseTransition(Duration.seconds(2));

            if (TokenStorage.getUser().getGroupId() != null) {
                delay.setOnFinished(e -> goToChats.run());
                delay.play();
            } else {
                delay.setOnFinished(e -> goToGroups.run());
                delay.play();
            }
        }
    }
}