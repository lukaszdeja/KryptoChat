package Views;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;


/**
 * Klasa Register
 * Frontend widoku rejestracji - zrealizowany przez VBox
 */
public class Register {
    private VBox view;
    /**
     * Konstruktor klasy Register
     * Buduje interfejs rejestracji
     * @param goToLogin - callback do metody zmieniajacej scene na logowanie (z klasy Main)
     */
    public Register(Runnable goToLogin) {
        Label label = new Label("Zarejestruj się");
        label.setAlignment(Pos.CENTER);
        label.getStyleClass().add("title");
        TextField username = new TextField();
        username.setPromptText("Login: ");
        PasswordField password = new PasswordField();
        password.setPromptText("Hasło: ");
        PasswordField password2 = new PasswordField();
        password2.setPromptText("Powtórz hasło: ");
        Button button = new Button("Zarejestruj");
        VBox inputs = new VBox(10);
        inputs.setMaxWidth(500);
        inputs.setPrefWidth(500);
        inputs.setAlignment(Pos.CENTER);
        button.getStyleClass().add("btn");
        Label loginLink = new Label("Masz już konto? Zaloguj się");
        loginLink.getStyleClass().add("link");
        loginLink.setOnMouseClicked(e -> goToLogin.run());
        inputs.getChildren().addAll(username, password, password2, loginLink);
        view = new VBox(100, label, inputs, button);
        view.getStyleClass().add("card");
    }

    /**
     * Publiczna metoda zwracająca widok do innych klas
     * @return view - prywatny VBox opakowujący interfejs rejestracji
     */
    public VBox getView() {
        return view;
    }
}