package Controllers;

import Services.LoginService;
import Services.ServiceResponse;
import Views.Login;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import security.TokenStorage;

/**
 * Kontroler obsługujący logowanie użytkownika.
 * Łączy widok logowania z serwisem autoryzacji.
 */
public class LoginController {

    /** Widok logowania */
    private Login loginView;

    /** Serwis obsługujący logowanie */
    private LoginService logservice;

    /** Akcja przejścia do widoku grup */
    Runnable goToGroups;

    /** Akcja przejścia do widoku czatu */
    Runnable goToChats;

    /**
     * Konstruktor kontrolera logowania.
     * @param view widok logowania
     * @param service serwis obsługujący logowanie
     * @param goToGroups akcja przełączająca widok na widok dołączania do/tworzenia grupy
     * @param goToChats akcja przełączająca widok na widok czatu
     */
    public LoginController(Login view, LoginService service, Runnable goToGroups, Runnable goToChats)  {
        this.loginView = view;
        this.logservice = service;
        this.goToGroups = goToGroups;
        this.goToChats = goToChats;

        init();
    }

    /**
     * Inicjalizuje obsługę zdarzeń w widoku logowania.
     * Po kliknięciu przycisku wykonywana jest metoda handleLogin().
     */
    private void init() {
        loginView.getButton().setOnAction(e -> handleLogin());
    }

    /**
     * Obsługuje proces logowania użytkownika.
     * Pobiera dane z formularza, sprawdza czy pola nie są puste,
     * wysyła żądanie logowania do serwisu oraz reaguje na odpowiedź.
     *
     * Po poprawnym logowaniu użytkownik zostaje przekierowany:
     * - do czatu, jeśli jest już w którejś grupie,
     * - do widoku grup, jeśli nie należy jeszcze do żadnej grupy.
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
        clearFields();
    }

    /**
     * Czyści pola do wpisywania.
     */
    private void clearFields() {
        loginView.getLogin().clear();
        loginView.getPassword().clear();
    }
}