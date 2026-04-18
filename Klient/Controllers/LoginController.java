package Controllers;

import Services.LoginService;
import Views.Login;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

public class LoginController {
    private Login loginView;
    private LoginService logservice;
    Runnable goToChats;

    public LoginController(Login view, LoginService service, Runnable goToChats)  {
        this.loginView = view;
        this.logservice = service;
        this.goToChats = goToChats;
        init();
    }

    private void init() {
        loginView.getButton().setOnAction(e -> handleLogin());
    }

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
            delay.setOnFinished(e -> goToChats.run());
            delay.play();

        } else {
            loginView.getLabel().setText("Błędne dane");
        }
    }
}