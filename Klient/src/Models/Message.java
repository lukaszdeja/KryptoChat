package Models;
import java.time.LocalDateTime;

public class Message {

    /** Użytkownik będący nadawcą wiadomości */
    private final User sender;

    /** Treść wiadomości */
    private final String content;

    /** Data i czas wysłania wiadomości */
    private LocalDateTime timestamp;

    /**
     * Konstruktor klasy Message.
     * Tworzy wiadomość z aktualnym czasem jako timestamp.
     *
     * @param sender - użytkownik wysyłający wiadomość
     * @param content - treść wiadomości
     */
    public Message(User sender, String content) {
        this.sender = sender;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Konstruktor klasy Message.
     * Tworzy wiadomość z podanym czasem.
     *
     * @param sender - użytkownik wysyłający wiadomość
     * @param content - treść wiadomości
     * @param timestamp - data i czas wysłania wiadomości
     */
    public Message(User sender, String content, LocalDateTime timestamp) {
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp;
    }

    /**
     * Zwraca nadawcę wiadomości.
     * @return obiekt użytkownika będącego nadawcą
     */
    public User getSender() {
        return sender;
    }

    /**
     * Zwraca treść wiadomości.
     */
    public String getContent() {
        return content;
    }

    /**
     * Zwraca date i czas wysłania wiadomości.
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}

