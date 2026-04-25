package Views;

import Models.User;
import Models.Message;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


/**
 * Widok aplikacji czatu.
 * Zawiera:
 * - listę użytkowników (lewa strona)
 * - listę wiadomości (prawa część)
 * - pole do wpisywania wiadomości
 * - pasek górny aplikacji
 */
public class Chat extends GridPane {

    /** Lista użytkowników */
    private ListView<User> userList;

    /** Lista wyświetlanych wiadomości */
    private ListView<Message> chatList;

    /** Dane wiadomości */
    private ObservableList<Message> messages;

    /** Tytuł aplikacji */
    private Label appTitle;

    /** Przycisk ustawień */
    private Button settingsButton;

    /** Pole wpisywania wiadomości */
    private final TextField messageField;

    /** Przycisk wysyłania wiadomości */
    private final Button sendButton;

    /** Pasek wpisywania wiadomości */
    private final HBox inputBar;

    /**
     * Konstruktor widoku czatu.
     * Buduje cały layout GUI.
     */
    public Chat() {

        // główny layout
        setPadding(new Insets(10));
        setHgap(10);
        setVgap(10);

        // kolumny: użytkownicy + czat
        ColumnConstraints leftCol = new ColumnConstraints();
        leftCol.setPercentWidth(25);

        ColumnConstraints rightCol = new ColumnConstraints();
        rightCol.setPercentWidth(75);
        rightCol.setHgrow(Priority.ALWAYS);

        getColumnConstraints().addAll(leftCol, rightCol);

        // wiersze: topbar + content
        RowConstraints topRow = new RowConstraints();
        topRow.setPrefHeight(60);

        RowConstraints contentRow = new RowConstraints();
        contentRow.setVgrow(Priority.ALWAYS);

        getRowConstraints().addAll(topRow, contentRow);

        // Użytkownicy
        userList = new ListView<>();
        userList.getItems().addAll(
                new User(1, "Ania", 1),
                new User(2, "Kasia", 1)
        );
        userList.setMinHeight(30);
        userList.getStyleClass().add("user-list");


        // Wiadomości
        messages = FXCollections.observableArrayList();
        chatList = new ListView<>(messages);
        chatList.getStyleClass().add("chat-list");
        chatList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        chatList.getSelectionModel().clearSelection();
        chatList.setMouseTransparent(false);

        chatList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Message msg, boolean empty) {
                super.updateItem(msg, empty);

                if (empty || msg == null) {
                    setText(null);
                } else {
                    // User.toString() zwraca username
                    setText(msg.getSender() + ": " + msg.getContent());
                }
            }
        });


        // TOP BAR
        appTitle = new Label("KryptoChat");
        appTitle.getStyleClass().add("title");

        var url = getClass().getResource("/settings.png");

        ImageView icon = new ImageView(new Image(url.toExternalForm()));
        icon.setFitWidth(22);
        icon.setFitHeight(22);
        icon.setPreserveRatio(true);

        settingsButton = new Button();
        settingsButton.setGraphic(icon);
        settingsButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        settingsButton.setStyle("-fx-background-color: transparent;");

        settingsButton = new Button();
        settingsButton.setGraphic(icon);
        settingsButton.setStyle("-fx-background-color: transparent;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topBar = new HBox(10, appTitle, spacer, settingsButton);
        topBar.setPadding(new Insets(20));
        topBar.getStyleClass().add("top-bar");


        // Pole do wpisywania wiadomości
        messageField = new TextField();
        messageField.getStyleClass().add("message");
        messageField.setPromptText("Napisz wiadomość...");
        HBox.setHgrow(messageField, Priority.ALWAYS);
        messageField.setMaxWidth(Double.MAX_VALUE);

        sendButton = new Button("Wyślij");
        HBox.setHgrow(sendButton, Priority.NEVER);
        sendButton.setPrefSize(38, 38);
        sendButton.setMinSize(38, 38);
        sendButton.setMaxSize(38, 38);
        ImageView arrow = new ImageView(
                new Image(getClass().getResource("/arrow.png").toExternalForm())
        );
        arrow.setFitWidth(16);
        arrow.setFitHeight(16);
        arrow.setPreserveRatio(true);
        sendButton.setGraphic(arrow);
        sendButton.setText("");

        inputBar = new HBox(10, messageField, sendButton);
        inputBar.setPadding(new Insets(10));
        inputBar.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(inputBar, Priority.ALWAYS);
        inputBar.getStyleClass().add("input-bar");


        // CHAT PANE
        VBox chatPane = new VBox(0, chatList, inputBar);
        VBox.setVgrow(chatList, Priority.ALWAYS);
        chatPane.setFillWidth(true);


        // GRID LAYOUT
        add(topBar, 0, 0, 2, 1);
        add(userList, 0, 1);
        add(chatPane, 1, 1);
        messageField.setMaxWidth(Double.MAX_VALUE);
        inputBar.setMaxWidth(Double.MAX_VALUE);
        chatPane.setMaxWidth(Double.MAX_VALUE);
        chatList.setMaxWidth(Double.MAX_VALUE);

        GridPane.setHgrow(chatPane, Priority.ALWAYS);
        GridPane.setVgrow(chatPane, Priority.ALWAYS);
        GridPane.setVgrow(userList, Priority.ALWAYS);
    }

    // Gettery
    public Button getSendButton() {
        return sendButton;
    }

    public TextField getMessageField() {
        return messageField;
    }

    public ListView<User> getUserList() {
        return userList;
    }

    public ObservableList<Message> getMessages() {
        return messages;
    }

    public GridPane getView() {
        return this;
    }
}
