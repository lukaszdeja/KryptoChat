package Controllers;

import Models.Group;
import Services.ChatService;
import Services.WebSocketService;
import Views.Chat;
import Models.Message;
import Models.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.ListCell;
import security.TokenStorage;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

/**
 * Kontroler obsługujący logikę czatu.
 * Łączy warstwę widoku (Chat) z modelem danych (Message, User).
 */
public class ChatController {

    /** Widok czatu */
    private final Chat chatView;
    private ChatService chatService;
    private Runnable goToLogin;
    private final WebSocketService webSocketService = new WebSocketService();

    Group group;

    /**
     * Konstruktor kontrolera czatu.
     *
     * @param chat - obiekt widoku czatu
     */
    public ChatController(Chat chat, ChatService chatService, Runnable goToLogin){
        this.chatView = chat;
        this.chatService = chatService;
        this.goToLogin = goToLogin;
        init();
    }

    /**
     * Inicjalizacja obsługi zdarzeń w widoku.
     */
    private void init(){
        chatView.getSendButton().setOnAction(e -> handleSend());
        chatView.getMessageField().setOnAction(e -> handleSend());
        chatView.getLogoutButton().setOnAction(e -> logout());
        loadGroup();
        loadMessages();
        webSocketService.connect();

        webSocketService.setOnMessageReceived(message -> {

            Platform.runLater(() -> {
                chatView.getMessages()
                        .add(message);
            });
        });
    }

    public void loadGroup() {
        group = chatService.loadGroup();

        if(group == null) { return;}

        chatView.getGroupNameLabel().setText(group.getGroupName());

        chatView.getGroupCodeLabel().setText(group.getCode());

        chatView.getUserList().setItems(FXCollections.observableArrayList(group.getUsers()));
    }

    public void loadMessages() {
        List<Message> messages = chatService.loadMessages();
        chatView.getMessages().clear();
        if (messages != null) {
            chatView.getMessages().addAll(messages);
        }
    }

    public void logout() {
        TokenStorage.setUser(null);
        TokenStorage.deleteToken();
        goToLogin.run();
    }

    /**
     * Obsługuje wysyłanie wiadomości.
     * Pobiera tekst z pola, sprawdza wybranego użytkownika
     * i dodaje nową wiadomość do listy.
     */
    private void handleSend() {
        String text = chatView.getMessageField().getText();

        if (text == null || text.isEmpty()) return;

        Message message = new Message();
        message.setSender(TokenStorage.getUser().getUsername());
        message.setContent(text);
        message.setGroupId(TokenStorage.getUser().getGroupId());
        message.setTimestamp(LocalDateTime.now());

        webSocketService.send(message);

        chatView.getMessageField().clear();
    }
}