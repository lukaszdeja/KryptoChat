package Controllers;

import Services.RegisterService;
import Views.Register;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

public class RegisterController {
    private Register registerView;
    private RegisterService service;
    Runnable goToLogin;

    public RegisterController(Register view, RegisterService service, Runnable goToLogin)  {
        this.registerView = view;
        this.service = service;
        this.goToLogin = goToLogin;
        init();
    }

    private void init() {
        registerView.getButton().setOnAction(e -> handleLogin());
    }

    private void handleLogin() {
        String username = String.valueOf(registerView.getLogin());
        String password = registerView.getPassword();
        String password2 = registerView.getPassword2();
        if (username.isEmpty() || password.isEmpty() || password2.isEmpty()) {
            registerView.getLabel().setText("Pola nie mogą być puste!");
            return;
        } else if (!password.equals(password2)) {
            registerView.getLabel().setText("Hasła się różnią!");
            return;
        } else if (password.length() < 8) {
            registerView.getLabel().setText("Hasło powinno mieć co najmniej 8 znaków!");
            return;
        } else if (!validPassword(password)) {
            registerView.getLabel().setText("Hasło powinno zawierać co najmniej 1: wielką litere, małą litere, cyfre i znak specjalny");
            return;
        }

        boolean success = service.login(username, password, password2);
        if (success) {
            registerView.getLabel().setText("Utworzono konto");
            PauseTransition delay = new PauseTransition(Duration.seconds(2));
            delay.setOnFinished(e -> goToLogin.run());
            delay.play();
        } else {
            registerView.getLabel().setText("Nie udało się utworzyć konta");
        }
    }

    private boolean validPassword(String password) {
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).+$";
        return password.matches(regex);
    }
}