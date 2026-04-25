package Views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Klasa CreateGroup
 * Frontend widoku dołączania do grupy - już istniejącej lub utworzenie nowej, zrealizowany przez VBox
 */
public class CreateGroup extends VBox {

    private TextField codeField;
    private TextField nameField;
    private Button joinBtn;
    private Button createBtn;
    private Label message;

    /**
     * Konstruktor klasy CreateGroup
     * Buduje interfejs dołączanie/tworzenia grupy
     */
    public CreateGroup() {

        setSpacing(30);
        setAlignment(Pos.CENTER);
        setPadding(new Insets(40));
        getStyleClass().add("root");

        Label title = new Label("KryptoChat");
        title.getStyleClass().add("title");
        title.setMaxWidth(Double.MAX_VALUE);
        title.setAlignment(Pos.CENTER);

        Label subtitle = new Label("Nie należysz do żadnej grupy - dołącz do istniejącej lub utwórz nową");
        subtitle.getStyleClass().add("subtitle");

        // Dołącz do grupy
        VBox joinBox = new VBox(15);
        joinBox.setAlignment(Pos.CENTER);
        joinBox.getStyleClass().add("card");

        Label joinLabel = new Label("Dołącz do grupy");
        joinLabel.getStyleClass().add("join-label");

        codeField = new TextField();
        codeField.setPromptText("Wpisz kod grupy");
        codeField.getStyleClass().add("input");

        joinBtn = new Button("Dołącz");
        joinBtn.getStyleClass().add("join-btn");

        joinBox.getChildren().addAll(joinLabel, codeField, joinBtn);

        // Utwórz grupe
        VBox createBox = new VBox(15);
        createBox.setAlignment(Pos.CENTER);
        createBox.getStyleClass().add("card");

        Label createLabel = new Label("Utwórz grupę");
        createLabel.getStyleClass().add("create-label");

        nameField = new TextField();
        nameField.setPromptText("Wpisz nazwę grupy");
        nameField.getStyleClass().add("input");

        createBtn = new Button("Utwórz");
        createBtn.getStyleClass().add("create-btn");

        createBox.getChildren().addAll(createLabel, nameField, createBtn);

        // komunikat
        message = new Label();
        message.getStyleClass().add("error-text");

        HBox options = new HBox(30, joinBox, createBox);
        options.setAlignment(Pos.CENTER);

        getChildren().addAll(title, subtitle, options, message);
    }

    // Gettery
    public Button getJoin() { return joinBtn; }
    public Button getCreate() { return createBtn; }

    public TextField getCodeField() { return codeField; }
    public TextField getGroupNameField() { return nameField; }

    public Label getMessage() { return message; }

    public VBox getView() { return this; }
}