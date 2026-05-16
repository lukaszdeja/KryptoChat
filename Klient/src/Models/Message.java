package Models;
import java.time.LocalDateTime;

public class Message {

    /** Użytkownik będący nadawcą wiadomości */
    private String sender;

    private Long id;

    private Long groupId;

    /** Treść wiadomości */
    private String content;

    /** Data i czas wysłania wiadomości */
    private LocalDateTime send_time;

    public Message() {}

    /**
     * Konstruktor klasy Message.
     * Tworzy wiadomość z aktualnym czasem jako timestamp.
     *
     * @param sender - użytkownik wysyłający wiadomość
     * @param content - treść wiadomości
     */
    public Message(String sender, String content) {
        this.sender = sender;
        this.content = content;
        this.send_time = LocalDateTime.now();
    }

    /**
     * Konstruktor klasy Message.
     * Tworzy wiadomość z podanym czasem.
     *
     * @param sender - użytkownik wysyłający wiadomość
     * @param content - treść wiadomości
     * @param send_time - data i czas wysłania wiadomości
     */
    public Message(String sender, String content, LocalDateTime send_time) {
        this.sender = sender;
        this.content = content;
        this.send_time = send_time;
    }

    /**
     * Zwraca nadawcę wiadomości.
     * @return obiekt użytkownika będącego nadawcą
     */
    public String getSender() {
        return sender;
    }

    public void setSender(String sender) { this.sender = sender; }

    public void setContent(String content) { this.content = content; }

    /**
     * Zwraca treść wiadomości.
     */
    public String getContent() {
        return content;
    }

    /**
     * Zwraca date i czas wysłania wiadomości.
     */
    public LocalDateTime getSend_time() {
        return send_time;
    }

    public void setSend_time(LocalDateTime send_time) { this.send_time = send_time; }

    public void setGroupId(Long groupId) { this.groupId = groupId;}

    public Long getGroupId() { return this.groupId;}

    public void setId(Long id) { this.id = id;}

    public Long getId() { return this.id; }
}

