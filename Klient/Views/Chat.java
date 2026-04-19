package Views;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import Models.Message;

/**
 * Frontend widoku chatów - zrealizowany przez GridPane
 * - lewa: lista użytkowników
 * - prawa: czat + pole do wpisywania
 * - top: pasek aplikacji
 */
public class Chat extends GridPane {

    private ListView<String> userList;

    private ListView<Message> chatList;
    private ObservableList<Message> messages;

    private Label appTitle;
    private Button settingsButton;

    final private TextField messageField;
    final private Button sendButton;
    final private HBox inputBar;

    public Chat() {

        // główny layout
        setPadding(new Insets(10));
        setHgap(10);
        setVgap(10);

        ColumnConstraints leftCol = new ColumnConstraints();
        leftCol.setPercentWidth(25);

        ColumnConstraints rightCol = new ColumnConstraints();
        rightCol.setPercentWidth(75);
        rightCol.setHgrow(Priority.ALWAYS);

        getColumnConstraints().addAll(leftCol, rightCol);

        RowConstraints topRow = new RowConstraints();
        topRow.setPrefHeight(60);

        RowConstraints contentRow = new RowConstraints();
        contentRow.setVgrow(Priority.ALWAYS);

        getRowConstraints().addAll(topRow, contentRow);

        // lista użytkowników
        userList = new ListView<>();
        userList.getItems().addAll("Jan", "Anna", "Kuba");
        userList.setMinHeight(30);
        userList.getStyleClass().add("user-list");

        // chat
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
                    setText(msg.getSender() + ": " + msg.getContent());
                }
            }
        });

        // góra
        appTitle = new Label("KryptoChat");
        appTitle.getStyleClass().add("title");

        ImageView icon = new ImageView(new Image(getClass().getResource("/Views/settings.png").toExternalForm()));
        icon.setFitWidth(22);
        icon.setFitHeight(22);

        settingsButton = new Button();
        settingsButton.setGraphic(icon);
        settingsButton.setStyle("-fx-background-color: transparent;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topBar = new HBox(10, appTitle, spacer, settingsButton);
        topBar.setPadding(new Insets(20));
        topBar.getStyleClass().add("top-bar");

        // pole do wpisywania
        messageField = new TextField();
        messageField.setPromptText("Napisz wiadomość...");
        HBox.setHgrow(messageField, Priority.ALWAYS);
        messageField.setMaxWidth(Double.MAX_VALUE);

        sendButton = new Button("Wyślij");
        HBox.setHgrow(sendButton, Priority.NEVER);

        inputBar = new HBox(10, messageField, sendButton);
        inputBar.setPadding(new Insets(10));
        inputBar.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(inputBar, Priority.ALWAYS);
        inputBar.getStyleClass().add("input-bar");

        // panel chatu
        VBox chatPane = new VBox(10, chatList, inputBar);
        VBox.setVgrow(chatList, Priority.ALWAYS);
        chatPane.setFillWidth(true);

        // dodawanie do grid
        add(topBar, 0, 0, 2, 1);
        add(userList, 0, 1);
        add(chatPane, 1, 1);

        GridPane.setHgrow(chatPane, Priority.ALWAYS);
        GridPane.setVgrow(chatPane, Priority.ALWAYS);
        GridPane.setVgrow(userList, Priority.ALWAYS);

        // akcje
        sendButton.setOnAction(e -> sendMessage());
        messageField.setOnAction(e -> sendMessage());
    }

    /** Ustawia sposób wyświetlania elementów ListView (Message).
     * Każdy Message jest renderowany jako tekst: "sender: content".
     * Jeśli element jest pusty, komórka jest czyszczona. */
    private void sendMessage() {
        String user = userList.getSelectionModel().getSelectedItem();
        String text = messageField.getText();

        if (text.isEmpty()) return;

        if (user == null) {
            messages.add(new Message("System", "Wybierz użytkownika!"));
        } else {
            messages.add(new Message("Ja", text));
        }

        messageField.clear();
    }

    public GridPane getView() {
        return this;
    }
}
