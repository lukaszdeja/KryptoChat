package com.KryptoChat.Services;

/**
 * Klasa potrzebna aby tworzyc Stringi JSON na podstawie jej obiektów (serializacja)
 * oraz deserializować otrzymanego JSONA - odtwarzać obiekty
 * Dla przesłania obiektu requestu logowania
 */
public class LoginRequest {

    /** Login użytkownika */
    private String username;

    /** Hasło użytkownika */
    private String password;

    /**
     * Konstruktor domyślny wymagany przez bibliotekę Jackson do deserializacji JSON.
     */
    public LoginRequest() {}

    /**
     * Ustawia nazwę użytkownika.
     * @param us login użytkownika
     */
    public void setUsername(String us) { username = us;}

    /**
     * Ustawia hasło użytkownika.
     * @param p hasło użytkownika
     */
    public void setPassword(String p) { password = p;}

    /**
     * Pobiera nazwę użytkownika.
     * @return login użytkownika
     */
    public String getUsername() { return username; }

    /**
     * Pobiera hasło użytkownika.
     * @return hasło użytkownika
     */
    public String getPassword() { return password; }
}
