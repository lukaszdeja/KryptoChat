package Controllers;

import Views.Chat;
import Models.Message;
import Models.User;

/**
 * Kontroler obsługujący logikę czatu.
 * Łączy warstwę widoku (Chat) z modelem danych (Message, User).
 */
public class ChatController {

    /** Widok czatu */
    private final Chat chatView;

    /**
     * Konstruktor kontrolera czatu.
     *
     * @param chat - obiekt widoku czatu
     */
    public ChatController(Chat chat){
        this.chatView = chat;
        init();
    }

    /**
     * Inicjalizacja obsługi zdarzeń w widoku.
     */
    private void init(){
        chatView.getSendButton().setOnAction(e -> handleSend());
        chatView.getMessageField().setOnAction(e -> handleSend());
    }

    /**
     * Obsługuje wysyłanie wiadomości.
     * Pobiera tekst z pola, sprawdza wybranego użytkownika
     * i dodaje nową wiadomość do listy.
     */
    private void handleSend(){
        User user = chatView.getUserList().getSelectionModel().getSelectedItem();
        String text = chatView.getMessageField().getText();

        if (text == null || text.isEmpty()) return;

        if (user == null) {
            chatView.getMessages().add(new Message(new User(0, "System", 0), "Wybierz użytkownika!"));
        } else {
            chatView.getMessages().add(new Message(new User(0, "Ja", 0), text)); //tu trzeba wstawic zalogowanego uzytkownika
        }

        chatView.getMessageField().clear();
    }
}