package Models;

/**
 * Klasa reprezentująca użytkownika systemu.
 */
public class User {

    /** Unikalny identyfikator użytkownika */
    private Long id;

    /** Nazwa użytkownika */
    private String username;

    /** Identyfikator grupy, do której należy użytkownik */
    private Long groupId;

    /**
     * Konstruktor klasy User.
     *
     * @param id - unikalny identyfikator użytkownika
     * @param username - nazwa użytkownika
     * @param groupId - identyfikator grupy użytkownika
     */
    public User(Long id, String username, Long groupId) {
        this.id = id;
        this.username = username;
        this.groupId = groupId;
    }

    public User() {}

    /**
     * Zwraca identyfikator użytkownika.
     */
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Zwraca nazwę użytkownika.
     */
    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Zwraca identyfikator(id) grupy użytkownika.
     */
    public Long getGroupId() {
        return this.groupId;
    }

    /**
     * Ustawia identyfikator grupy użytkownika (groupId).
     */
    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    /**
     * Zwraca nazwe użytkownika.
     */
    @Override
    public String toString() {
        return username;
    }
}