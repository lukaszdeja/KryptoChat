package Models;

/**
 * Klasa reprezentująca użytkownika systemu.
 */
public class User {

    /** Unikalny identyfikator użytkownika */
    private final int id;

    /** Nazwa użytkownika */
    private final String username;

    /** Identyfikator grupy, do której należy użytkownik */
    private int groupId;

    /**
     * Konstruktor klasy User.
     *
     * @param id - unikalny identyfikator użytkownika
     * @param username - nazwa użytkownika
     * @param groupId - identyfikator grupy użytkownika
     */
    public User(int id, String username, int groupId) {
        this.id = id;
        this.username = username;
        this.groupId = groupId;
    }

    /**
     * Zwraca identyfikator użytkownika.
     */
    public int getId() {
        return this.id;
    }

    /**
     * Zwraca nazwę użytkownika.
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Zwraca identyfikator(id) grupy użytkownika.
     */
    public int getGroupId() {
        return this.groupId;
    }

    /**
     * Ustawia identyfikator grupy użytkownika (groupId).
     */
    public void setGroupId(int groupId) {
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