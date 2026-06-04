package com.KryptoChat.Models;

import java.util.List;
import java.util.ArrayList;

/**
 * Model reprezentujący grupę użytkowników.
 */
public class Group {

    /** Unikalny identyfikator grupy */
    private Long groupId;

    /** Nazwa grupy */
    private String groupName;

    /** Lista użytkowników należących do grupy */
    private List<User> users = new ArrayList<>();

    /** Kod umożliwiający dołączenie do grupy */
    private String code;

    /**
     * Konstruktor tworzący grupę z identyfikatorem i nazwą.
     * @param ID identyfikator grupy
     * @param name nazwa grupy
     */
    public Group(Long ID, String name){
        this.groupId = ID;
        this.groupName = name;
    }

    /**
     * Domyślny konstruktor klasy Group.
     */
    public Group() {}

    /**
     * Zwraca identyfikator grupy.
     * @return identyfikator grupy
     */
    public Long getGroupId(){
        return this.groupId;
    }

    /**
     * Zwraca nazwę grupy.
     * @return nazwa grupy
     */
    public String getGroupName(){
        return this.groupName;
    }

    /**
     * Ustawia listę użytkowników należących do grupy.
     * @param users lista użytkowników
     */
    public void setUsers(List<User> users) {
        this.users = users;
    }

    /**
     * Ustawia nazwę grupy.
     * @param groupName nowa nazwa grupy
     */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    /**
     * Zwraca listę użytkowników należących do grupy.
     * @return lista użytkowników
     */
    public List<User> getUsers() {
        return users;
    }

    /**
     * Dodaje użytkownika do grupy.
     * @param user użytkownik do dodania
     */
    public void addUser(User user) {
        users.add(user);
    }

    /**
     * Ustawia kod grupy.
     * @param code kod grupy
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Zwraca kod grupy.
     * @return kod grupy
     */
    public String getCode() {
        return code;
    }

    /**
     * Nadpisuje metode toString() - zwraca nazwę grupy.
     * @return nazwa grupy
     */
    @Override
    public String toString() {
        return groupName;
    }
}