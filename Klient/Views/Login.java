package Views;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import javafx.scene.control.Label;


/**
 * Klasa Login
 * Frontend widoku logowania - zrealizowany przez VBox
 */

public class Login {

    private VBox view;

    /**
     * Konstruktor klasy Login
     * Buduje interfejs logowania
     * @param goToRegister - callback do metody zmieniajacej scene na rejestracje (z klasy Main)
     */
    public Login(Runnable goToRegister) {
        Label label = new Label("Zaloguj się");
        label.setAlignment(Pos.CENTER);
        label.getStyleClass().add("title");
        TextField username = new TextField();
        username.setPromptText("Login: ");
        PasswordField password = new PasswordField();
        password.setPromptText("Hasło: ");
        Button button = new Button("Zaloguj");
        VBox inputs = new VBox(10);
        inputs.setMaxWidth(500);
        inputs.setPrefWidth(500);
        inputs.setAlignment(Pos.CENTER);
        button.getStyleClass().add("btn");
        Label registerLink = new Label("Nie masz konta? Zarejestruj się");
        registerLink.getStyleClass().add("link");
        registerLink.setOnMouseClicked(e -> goToRegister.run());
        inputs.getChildren().addAll(username, password, registerLink);
        view = new VBox(100, label, inputs, button);
        view.getStyleClass().add("card");
    }

    /**
     * Publiczna metoda zwracająca widok do innych klas
     * @return view - prywatny VBox opakowujący interfejs logowania
     */
    public VBox getView() {
        return view;
    }
}