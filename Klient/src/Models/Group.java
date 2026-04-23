package Models;

import java.util.List;
import java.util.ArrayList;

/**
 * Klasa reprezentująca grupę użytkowników.
 */
public class Group {

    /** Unikalny identyfikator grupy */
    private final int groupId;

    /** Nazwa grupy */
    private String groupName;

    /** Lista użytkowników należących do grupy */
    private List<User> users = new ArrayList<>();

    /**
     * Konstruktor klasy Group.
     *
     * @param ID - identyfikator grupy
     * @param name - nazwa grupy
     */
    Group(int ID, String name){
        this.groupId = ID;
        this.groupName = name;
    }

    /**
     * Zwraca identyfikator grupy (groupId).
     */
    public int getGroupId(){
        return this.groupId;
    }

    /**
     * Zwraca nazwę grupy.
     */
    public String getGroupName(){
        return this.groupName;
    }

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

    /**
     * Zwraca nazwę grupy jako jej reprezentację tekstową.
     */
    @Override
    public String toString() {
        return groupName;
    }
}