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
    private TextField username;
    private PasswordField password;
    private PasswordField password2;
    private Button button;
    private Label info;

    /**
     * Konstruktor klasy Register
     * Buduje interfejs rejestracji
     * @param goToLogin - callback do metody zmieniajacej scene na logowanie (z klasy Main)
     */
    public Register(Runnable goToLogin) {
        Label label = new Label("Zarejestruj się");
        label.setAlignment(Pos.CENTER);
        label.getStyleClass().add("title");
        username = new TextField();
        username.setPromptText("Login: ");
        password = new PasswordField();
        password.setPromptText("Hasło: ");
        password2 = new PasswordField();
        password2.setPromptText("Powtórz hasło: ");
        button = new Button("Zarejestruj");
        VBox inputs = new VBox(10);
        inputs.setMaxWidth(500);
        inputs.setPrefWidth(500);
        inputs.setAlignment(Pos.CENTER);
        button.getStyleClass().add("btn");
        button.setDefaultButton(true);
        Label loginLink = new Label("Masz już konto? Zaloguj się");
        loginLink.getStyleClass().add("link");
        loginLink.setOnMouseClicked(e -> goToLogin.run());
        inputs.getChildren().addAll(username, password, password2, loginLink);
        info = new Label("");
        view = new VBox(100, label, inputs, button, info);
        view.getStyleClass().add("card");
    }

    /**
     * Publiczna metoda zwracająca widok do innych klas
     * @return view - prywatny VBox opakowujący interfejs rejestracji
     */
    public VBox getView() {
        return view;
    }

    /**
     * Publiczna metoda zwracająca tekst z pola username
     * @return String - tekst z pola loginu
     */
    public String getLogin() { return username.getText(); }

    /**
     * Publiczna metoda zwracająca tekst z pola password
     * @return String - tekst z pola hasła
     */
    public String getPassword() { return password.getText(); }

    /**
     * Publiczna metoda zwracająca tekst z pola password2
     * @return String - tekst z pola powtórz hasło
     */
    public String getPassword2() { return password2.getText();}

    /**
     * Metoda zwracająca referencję do obiektu przycisku
     * @return Button
     */
    public Button getButton() { return button; }

    /**
     * Metoda zwracająca referencję do obiektu Label - komunikatu
     * @return Label
     */
    public Label getLabel() { return  info; }
}