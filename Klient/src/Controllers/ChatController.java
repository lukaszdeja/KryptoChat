package Controllers;

import Models.Group;
import Services.ChatService;
import Views.Chat;
import Models.Message;
import Models.User;
import javafx.collections.FXCollections;
import javafx.scene.control.ListCell;

/**
 * Kontroler obsługujący logikę czatu.
 * Łączy warstwę widoku (Chat) z modelem danych (Message, User).
 */
public class ChatController {

    /** Widok czatu */
    private final Chat chatView;
    private ChatService chatService;

    Group group;

    /**
     * Konstruktor kontrolera czatu.
     *
     * @param chat - obiekt widoku czatu
     */
    public ChatController(Chat chat, ChatService chatService){
        this.chatView = chat;
        this.chatService = chatService;
        init();
    }

    /**
     * Inicjalizacja obsługi zdarzeń w widoku.
     */
    private void init(){
        chatView.getSendButton().setOnAction(e -> handleSend());
        chatView.getMessageField().setOnAction(e -> handleSend());

        loadGroup();
        loadMessages();
    }

    public void loadGroup() {
        group = chatService.loadUsers();
        chatView.getGroupNameLabel().setText(group.getGroupName());
        chatView.getGroupCodeLabel().setText(group.getCode());
        chatView.getUserList().setItems(FXCollections.observableArrayList(group.getUsers()));
    }

    public void loadMessages() {

    }

    /**
     * Obsługuje wysyłanie wiadomości.
     * Pobiera tekst z pola, sprawdza wybranego użytkownika
     * i dodaje nową wiadomość do listy.
     */
    private void handleSend(){
        //User user = chatView.getUserList().getSelectionModel().getSelectedItem();
        String text = chatView.getMessageField().getText();

        if (text == null || text.isEmpty()) return;

        //if (user == null) {
          //  chatView.getMessages().add(new Message(new User(0L, "System", 0L), "Wybierz użytkownika!"));
        //} else {
          //  chatView.getMessages().add(new Message(new User(0L, "Ja", 0L), text)); //tu trzeba wstawic zalogowanego uzytkownika
        //}

        chatView.getMessageField().clear();
    }
}