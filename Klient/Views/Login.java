package Views;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * Klasa Login
 * Frontend widoku logowania - zrealizowany przez VBox
 */

public class Login {

    private VBox view;
    private TextField username;
    private PasswordField password;
    private Button button;
    private Label info;

    /**
     * Konstruktor klasy Login
     * Buduje interfejs logowania
     * @param goToRegister - callback do metody zmieniajacej scene na rejestracje (z klasy Main)
     */
    public Login(Runnable goToRegister) {
        Label label = new Label("Zaloguj się");
        label.setAlignment(Pos.CENTER);
        label.getStyleClass().add("title");
        username = new TextField();
        username.setPromptText("Login: ");
        password = new PasswordField();
        password.setPromptText("Hasło: ");
        button = new Button("Zaloguj");
        VBox inputs = new VBox(10);
        inputs.setMaxWidth(500);
        inputs.setPrefWidth(500);
        inputs.setAlignment(Pos.CENTER);
        button.getStyleClass().add("btn");
        button.setDefaultButton(true);
        Label registerLink = new Label("Nie masz konta? Zarejestruj się");
        registerLink.getStyleClass().add("link");
        registerLink.setOnMouseClicked(e -> goToRegister.run());
        inputs.getChildren().addAll(username, password, registerLink);
        info = new Label("");
        view = new VBox(100, label, inputs, button, info);
        view.getStyleClass().add("card");
    }

    /**
     * Publiczna metoda zwracająca widok do innych klas
     * @return view - prywatny VBox opakowujący interfejs logowania
     */
    public VBox getView() {
        return view;
    }

    public String getLogin() { return username.getText(); }
    public String getPassword() { return password.getText(); }
    public Button getButton() { return button; }
    public Label getLabel() { return  info; }
}