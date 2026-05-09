package Models;

import java.util.List;
import java.util.ArrayList;

/**
 * Klasa reprezentująca grupę użytkowników.
 */
public class Group {

    /** Unikalny identyfikator grupy */
    private Long groupId;

    /** Nazwa grupy */
    private String groupName;

    /** Lista użytkowników należących do grupy */
    private List<User> users = new ArrayList<>();

    private String code;

    /**
     * Konstruktor klasy Group.
     *
     * @param ID - identyfikator grupy
     * @param name - nazwa grupy
     */
    public Group(Long ID, String name){
        this.groupId = ID;
        this.groupName = name;
    }

    public Group() {}

    /**
     * Zwraca identyfikator grupy (groupId).
     */
    public Long getGroupId(){
        return this.groupId;
    }

    /**
     * Zwraca nazwę grupy.
     */
    public String getGroupName(){
        return this.groupName;
    }

    public void setUsers(List<User> users) { this.users = users; }

    /**
     * Ustawia nową nazwę grupy.
     *
     * @param groupName - nowa nazwa grupy
     */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    /**
     * Zwraca listę użytkowników należących do grupy.
     */
    public List<User> getUsers() {
        return users;
    }

    /**
     * Dodaje użytkownika do grupy.
     * @param user - użytkownik do dodania
     */
    public void addUser(User user) {
        users.add(user);
    }

    public void setCode(String code) { this.code = code; }

    public String getCode() { return code; }

    /**
     * Zwraca nazwę grupy jako jej reprezentację tekstową.
     */
    @Override
    public String toString() {
        return groupName;
    }
}