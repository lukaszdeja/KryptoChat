package Views;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.geometry.Pos;
import javafx.scene.image.ImageView;

public class CreateGroup {

    private VBox Box;

    private Button createButton;

    private Button joinButton;

    private TextField groupNameField;

    private TextField codeField;

    private Label message;

    public CreateGroup() {
        Label appTitle = new Label("KryptoChat");
        appTitle.getStyleClass().add("title");
        ImageView icon = new ImageView(
                new Image(getClass().getResource("/settings.png").toExternalForm())
        );
        icon.setFitWidth(22);
        icon.setFitHeight(22);

        Button settingsButton = new Button();
        settingsButton.setGraphic(icon);
        settingsButton.setStyle("-fx-background-color: transparent;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topBar = new HBox(10, appTitle, spacer, settingsButton);
        topBar.setPadding(new Insets(20));
        topBar.getStyleClass().add("top-bar");

        Label header = new Label("Nie należysz do żadnej grupy");
        header.getStyleClass().add("header");

        Label createLabel = new Label("Utwórz grupę");

        groupNameField = new TextField();
        groupNameField.setPromptText("Nazwa grupy");

        createButton = new Button("Utwórz");

        Label orLabel = new Label("LUB");

        Label joinLabel = new Label("Dołącz do grupy");

        codeField = new TextField();
        codeField.setPromptText("Kod grupy");

        joinButton = new Button("Dołącz");

        VBox createBox = new VBox(8, createLabel, groupNameField, createButton);
        VBox joinBox = new VBox(8, joinLabel, codeField, joinButton);
        joinBox.setAlignment(Pos.CENTER);
        createBox.setAlignment(Pos.CENTER);
        message = new Label();
        VBox content = new VBox(20, header, createBox, orLabel, joinBox, message);
        content.setPadding(new Insets(40));
        content.setMaxWidth(600);
        content.setAlignment(Pos.CENTER);

        StackPane centerWrapper = new StackPane(content);
        StackPane.setAlignment(content, Pos.CENTER);

        Box = new VBox(10, topBar, centerWrapper);
        VBox.setVgrow(centerWrapper, Priority.ALWAYS);
    }

    public VBox getView() {
        return Box;
    }

    public Button getCreate() {
        return createButton;
    }

    public Button getJoin() {
        return joinButton;
    }

    public TextField getGroupNameField() {
        return groupNameField;
    }

    public TextField getCodeField() {
        return codeField;
    }

    public Label getMessage() {
        return message;
    }
}
