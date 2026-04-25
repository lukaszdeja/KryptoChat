package Views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class Start extends VBox {

    private TextField codeField;
    private TextField nameField;
    private Button joinBtn;
    private Button createBtn;

    public Start() {

        setSpacing(30);
        setAlignment(Pos.CENTER);
        setPadding(new Insets(40));
        getStyleClass().add("root");

        // Tytuł
        Label title = new Label("KryptoChat");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Dołącz do grupy lub utwórz nową");
        subtitle.getStyleClass().add("subtitle");

        // Dołączanie do grupy
        VBox joinBox = new VBox(15);
        joinBox.setAlignment(Pos.CENTER);
        joinBox.getStyleClass().add("card");

        Label joinLabel = new Label("Dołącz do grupy");

        codeField = new TextField();
        codeField.setPromptText("Wpisz kod grupy");

        joinBtn = new Button("Dołącz");
        joinBtn.getStyleClass().add("primary-btn");

        joinBox.getChildren().addAll(joinLabel, codeField, joinBtn);

        // Tworzenie grupy
        VBox createBox = new VBox(15);
        createBox.setAlignment(Pos.CENTER);
        createBox.getStyleClass().add("card");

        Label createLabel = new Label("Utwórz grupę");

        nameField = new TextField();
        nameField.setPromptText("Wpisz nazwę grupy");

        createBtn = new Button("Utwórz");
        createBtn.getStyleClass().add("secondary-btn");

        createBox.getChildren().addAll(createLabel, nameField, createBtn);

        HBox options = new HBox(30, joinBox, createBox);
        options.setAlignment(Pos.CENTER);

        getChildren().addAll(title, subtitle, options);
    }

    // Gettery
    public Button getJoinBtn() {
        return joinBtn;
    }

    public Button getCreateBtn() {
        return createBtn;
    }

    public String getCode() {
        return codeField.getText();
    }

    public String getGroupName() {
        return nameField.getText();
    }
}