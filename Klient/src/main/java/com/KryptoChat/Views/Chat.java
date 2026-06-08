package com.KryptoChat.Views;

import com.KryptoChat.Models.Message;
import com.KryptoChat.Models.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.application.Platform;
import java.time.format.DateTimeFormatter;


/**
 * Widok aplikacji czatu.
 * Zawiera:
 * - listę użytkowników należących do grupy (lewa strona)
 * - listę wiadomości (prawa część)
 * - pole do wpisywania wiadomości
 * - pasek górny aplikacji
 */
public class Chat extends GridPane {

    private ListView<User> userList;
    private ListView<Message> chatList;
    private ObservableList<Message> messages;
    private Label appTitle;
    private Button logoutButton;
    private final TextField messageField;
    private final Button sendButton;
    private Label groupNameLabel;
    private Label groupCodeLabel;

    /** Pasek wpisywania wiadomości */
    private final HBox inputBar;

    /**
     * Konstruktor widoku czatu.
     * Buduje cały layout GUI.
     */
    public Chat() {

        // główny layout
        setPadding(new Insets(0));
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
        topRow.setMinHeight(50);
        topRow.setPrefHeight(Region.USE_COMPUTED_SIZE);
        topRow.setVgrow(Priority.NEVER);

        RowConstraints contentRow = new RowConstraints();
        contentRow.setVgrow(Priority.ALWAYS);

        getRowConstraints().addAll(topRow, contentRow);

        // Użytkownicy
        groupNameLabel = new Label("");
        groupCodeLabel = new Label("Kod dolaczenia:");

        VBox labele = new VBox(5, groupNameLabel, groupCodeLabel);
        labele.setPadding(new Insets(10));

        userList = new ListView<>();
        userList.setMinHeight(30);
        userList.getStyleClass().add("user-list");

        groupNameLabel.setWrapText(true);

        VBox groupInfoBox = new VBox(5, groupNameLabel, groupCodeLabel);
        groupInfoBox.setPadding(new Insets(10));
        groupInfoBox.getStyleClass().add("group-info-box");

        // USER BOX
        VBox userBox = new VBox(10, groupInfoBox, userList);

        userBox.getStyleClass().add("user-list");

        groupNameLabel.getStyleClass().add("group-name-label");
        groupCodeLabel.getStyleClass().add("group-code-label");

        // Wiadomości
        messages = FXCollections.observableArrayList();
        chatList = new ListView<>(messages);
        chatList.getStyleClass().add("chat-list");
        chatList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        chatList.getSelectionModel().clearSelection();
        chatList.setMouseTransparent(false);

        messages.addListener((javafx.collections.ListChangeListener<Message>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    Platform.runLater(() ->
                            chatList.scrollTo(messages.size() - 1)
                    );
                }
            }
        });

        chatList.setCellFactory(param -> new ListCell<>() {

            private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", new java.util.Locale("pl"));

            private final Label dateLabel = new Label();
            private final Label messageLabel = new Label();
            private final VBox container = new VBox(4);

            {
                messageLabel.setWrapText(true);
                messageLabel.setMaxWidth(Double.MAX_VALUE);

                dateLabel.getStyleClass().add("date-separator");
                dateLabel.setMaxWidth(Double.MAX_VALUE);
                dateLabel.setAlignment(Pos.CENTER);

                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                setStyle("-fx-background-color: transparent; -fx-padding: 2 0 2 0;");
            }

            @Override
            protected void updateItem(Message msg, boolean empty) {
                super.updateItem(msg, empty);
                container.getChildren().clear();

                if (empty || msg == null) {
                    setGraphic(null);
                    return;
                }

                // sprawdź czy poprzednia wiadomość ma inną datę
                int index = getIndex();
                boolean showDate = false;

                if (msg.getSend_time() != null){
                    if(index == 0){
                        showDate = true;
                    } else {
                        Message prev = messages.get(index - 1);
                        if (prev.getSend_time() != null &&
                                !prev.getSend_time().toLocalDate().equals(msg.getSend_time().toLocalDate())) {
                            showDate = true;
                        }
                    }
                }

                if (showDate) {
                    dateLabel.setText(msg.getSend_time().format(dateFormatter));
                    container.getChildren().add(dateLabel);
                }

                String time = msg.getSend_time() != null ? msg.getSend_time().format(timeFormatter) : "";
                messageLabel.setText("[" + time + "] " + msg.getSender() + ": " + msg.getContent());
                messageLabel.prefWidthProperty().bind(chatList.widthProperty().subtract(20));

                container.getChildren().add(messageLabel);
                setGraphic(container);
            }
        });


        // TOP BAR
        appTitle = new Label("KryptoChat");
        appTitle.getStyleClass().add("title");

        var url = getClass().getResource("/logout.png");

        ImageView icon = new ImageView(new Image(url.toExternalForm()));
        icon.setPreserveRatio(true);

        logoutButton = new Button();
        logoutButton.setGraphic(icon);
        logoutButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        logoutButton.setStyle("-fx-background-color: transparent;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topBar = new HBox(10, appTitle, spacer, logoutButton);
        topBar.setPadding(new Insets(20));
        topBar.getStyleClass().add("top-bar");

        icon.fitHeightProperty().bind(
                topBar.heightProperty().multiply(0.45)
        );
        icon.fitWidthProperty().bind(icon.fitHeightProperty());

        logoutButton.maxHeightProperty().bind(topBar.heightProperty().multiply(0.7));
        logoutButton.maxWidthProperty().bind(logoutButton.maxHeightProperty());


        // Pole do wpisywania wiadomości
        messageField = new TextField();
        messageField.getStyleClass().add("message");
        messageField.setPromptText("Napisz wiadomość...");
        HBox.setHgrow(messageField, Priority.ALWAYS);
        messageField.setMaxWidth(Double.MAX_VALUE);

        sendButton = new Button();
        var url1 = getClass().getResource("/arrow.png");
        ImageView ikonka = new ImageView(new Image(url1.toExternalForm()));
        ikonka.setPreserveRatio(true);

        sendButton.setGraphic(ikonka);
        sendButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        sendButton.setStyle("-fx-background-color: transparent;");

        inputBar = new HBox(10, messageField, sendButton);
        inputBar.setPadding(new Insets(10));
        inputBar.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(inputBar, Priority.ALWAYS);
        inputBar.getStyleClass().add("input-bar");

        ikonka.fitHeightProperty().bind(inputBar.heightProperty().multiply(0.55));
        ikonka.fitWidthProperty().bind(ikonka.fitHeightProperty());


        // CHAT PANE
        VBox chatPane = new VBox(0, chatList, inputBar);
        VBox.setVgrow(chatList, Priority.ALWAYS);
        chatPane.setFillWidth(true);


        // GRID LAYOUT
        add(topBar, 0, 0, 2, 1);
        add(userBox, 0, 1);
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

    public Button getLogoutButton() { return  logoutButton; }

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

    public Label getGroupNameLabel() { return groupNameLabel; }

    public Label getGroupCodeLabel() { return groupCodeLabel; }
}
