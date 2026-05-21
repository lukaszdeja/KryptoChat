package Models;

import java.time.LocalDateTime;

/**
 * Model reprezentujący wiadomość na czacie.
 */
public class Message {

    /** Nazwa użytkownika będącego nadawcą wiadomości */
    private String sender;

    /** Unikalny identyfikator wiadomości */
    private Long id;

    /** Identyfikator grupy, do której należy wiadomość */
    private Long groupId;

    /** Treść wiadomości */
    private String content;

    /** Data i czas wysłania wiadomości */
    private LocalDateTime send_time;

    /**
     * Domyślny konstruktor klasy Message.
     */
    public Message() {}

    /**
     * Konstruktor tworzący wiadomość z aktualnym czasem wysłania.
     * @param sender nazwa użytkownika wysyłającego wiadomość
     * @param content treść wiadomości
     */
    public Message(String sender, String content) {
        this.sender = sender;
        this.content = content;
        this.send_time = LocalDateTime.now();
    }

    /**
     * Konstruktor tworzący wiadomość z podanym czasem wysłania.
     * @param sender nazwa użytkownika wysyłającego wiadomość
     * @param content treść wiadomości
     * @param send_time data i czas wysłania wiadomości
     */
    public Message(String sender, String content, LocalDateTime send_time) {
        this.sender = sender;
        this.content = content;
        this.send_time = send_time;
    }

    /**
     * Zwraca nazwę nadawcy wiadomości.
     * @return nazwa użytkownika
     */
    public String getSender() {
        return sender;
    }

    /**
     * Ustawia nadawcę wiadomości.
     * @param sender nazwa użytkownika
     */
    public void setSender(String sender) {
        this.sender = sender;
    }

    /**
     * Ustawia treść wiadomości.
     * @param content treść wiadomości
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Zwraca treść wiadomości.
     * @return treść wiadomości
     */
    public String getContent() {
        return content;
    }

    /**
     * Zwraca datę i czas wysłania wiadomości.
     * @return data i czas wysłania
     */
    public LocalDateTime getSend_time() {
        return send_time;
    }

    /**
     * Ustawia datę i czas wysłania wiadomości.
     * @param send_time data i czas wysłania
     */
    public void setSend_time(LocalDateTime send_time) {
        this.send_time = send_time;
    }

    /**
     * Ustawia identyfikator grupy wiadomości.
     * @param groupId identyfikator grupy
     */
    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    /**
     * Zwraca identyfikator grupy wiadomości.
     * @return identyfikator grupy
     */
    public Long getGroupId() {
        return this.groupId;
    }

    /**
     * Ustawia identyfikator wiadomości.
     * @param id identyfikator wiadomości
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Zwraca identyfikator wiadomości.
     * @return identyfikator wiadomości
     */
    public Long getId() {
        return this.id;
    }
}