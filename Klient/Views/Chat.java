package Views;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import Models.Message;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

/**
 * Klasa Chat
 * Frontend widoku chatów - zrealizowany przez GridPane
 */
public class Chat extends GridPane{
    private ListView<String> userList;
    private ListView<Message> chatList;
    private ObservableList<Message> messages;

    private HBox topBar;
    private Button settingsButton;
    private Label appTitle;
    private TextField messageField;
    private Button sendButton;

    public Chat() {
        this.setPadding(new Insets(10));
        this.setHgap(10);
        this.setVgap(10);

        // Lista użytkowników
        userList = new ListView<>();
        userList.getItems().addAll("Jan", "Anna", "Kuba");

        // Lista wiadomości
        messages = FXCollections.observableArrayList();
        chatList = new ListView<>(messages);


        /**
         * Ustawia sposób wyświetlania elementów ListView (Message).
         * Każdy Message jest renderowany jako tekst: "sender: content".
         * Jeśli element jest pusty, komórka jest czyszczona.
         */
        chatList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Message msg, boolean empty) {
                super.updateItem(msg, empty);

                if (empty || msg == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(msg.getSender() + ": " + msg.getContent());
                }
            }
        });

        messageField = new TextField();
        messageField.setPromptText("Napisz wiadomość...");

        sendButton = new Button("Wyślij");


        settingsButton = new Button("Ustawienia");
        appTitle = new Label("KryptoChat");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar = new HBox();
        topBar.setPadding(new Insets(10));
        topBar.setSpacing(10);
        topBar.setStyle("-fx-background-color: #2c3e50;");

        HBox.setHgrow(appTitle, Priority.ALWAYS);
        appTitle.setMaxWidth(Double.MAX_VALUE);
        appTitle.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

        topBar.getChildren().addAll(appTitle, spacer, settingsButton);

        // prawa strona
        GridPane chatPane = new GridPane();
        chatPane.setHgap(10);
        chatPane.setVgap(10);

        chatPane.add(chatList, 0, 0, 2, 1);
        chatPane.add(messageField, 0, 1);
        chatPane.add(sendButton, 1, 1);

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);

        chatPane.getColumnConstraints().addAll(c1, new ColumnConstraints());

        RowConstraints r1 = new RowConstraints();
        r1.setVgrow(Priority.ALWAYS);
        chatPane.getRowConstraints().add(r1);

        // główny layout
        this.add(topBar, 0, 0, 2, 1);
        this.add(userList, 0, 1);
        this.add(chatPane, 1, 1);

        RowConstraints top = new RowConstraints();
        top.setPrefHeight(40);

        RowConstraints content = new RowConstraints();
        content.setVgrow(Priority.ALWAYS);

        this.getRowConstraints().addAll(top, content);

        ColumnConstraints left = new ColumnConstraints();
        left.setPercentWidth(25);

        ColumnConstraints right = new ColumnConstraints();
        right.setPercentWidth(75);

        this.getColumnConstraints().addAll(left, right);

        RowConstraints row = new RowConstraints();
        row.setVgrow(Priority.ALWAYS);
        this.getRowConstraints().add(row);

        // akcje
        sendButton.setOnAction(e -> sendMessage());
        messageField.setOnAction(e -> sendMessage());
    }

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


