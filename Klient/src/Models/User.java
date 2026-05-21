package Models;

/**
 * Model reprezentujący użytkownika systemu.
 */
public class User {

    /** Unikalny identyfikator użytkownika */
    private Long id;

    /** Nazwa użytkownika */
    private String username;

    /** Identyfikator grupy, do której należy użytkownik */
    private Long groupId;

    /**
     * Konstruktor tworzący użytkownika z podanymi danymi.
     * @param id unikalny identyfikator użytkownika
     * @param username nazwa użytkownika
     * @param groupId identyfikator grupy użytkownika
     */
    public User(Long id, String username, Long groupId) {
        this.id = id;
        this.username = username;
        this.groupId = groupId;
    }

    /**
     * Domyślny konstruktor klasy User.
     */
    public User() {}

    /**
     * Zwraca identyfikator użytkownika.
     * @return identyfikator użytkownika
     */
    public Long getId() {
        return this.id;
    }

    /**
     * Ustawia identyfikator użytkownika.
     * @param id identyfikator użytkownika
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Zwraca nazwę użytkownika.
     * @return nazwa użytkownika
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Ustawia nazwę użytkownika.
     * @param username nazwa użytkownika
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Zwraca identyfikator grupy użytkownika.
     * @return identyfikator grupy
     */
    public Long getGroupId() {
        return this.groupId;
    }

    /**
     * Ustawia identyfikator grupy użytkownika.
     * @param groupId identyfikator grupy
     */
    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    /**
     * Nadpisuje metode toString() - zwraca tekstową reprezentację użytkownika.
     * @return nazwa użytkownika
     */
    @Override
    public String toString() {
        return username;
    }
}