package com.KryptoChat.Services;

/**
 * Klasa reprezentująca obiekt żądania rejestracji nowego użytkownika (DTO - Data Transfer Object).
 * Służy do serializacji danych rejestracyjnych do formatu JSON przed wysłaniem na serwer
 * oraz do ich deserializacji po stronie serwera.
 */
public class RegisterRequest {

    private String username;
    private String password;
    private String publicKey;
    private String encryptedPrivateKey;

    /**
     * Bezargumentowy konstruktor domyślny.
     * Jest on niezbędny dla poprawnego działania biblioteki Jackson podczas procesu deserializacji JSON.
     */
    public RegisterRequest() {}

    /**
     * Pobiera nazwę użytkownika.
     * * @return login użytkownika jako String
     */
    public String getUsername() { return this.username; }

    /**
     * Pobiera hasło użytkownika.
     * * @return hasło użytkownika jako String
     */
    public String getPassword() { return this.password; }

    /**
     * Ustawia nazwę użytkownika.
     * * @param us nowa nazwa użytkownika
     */
    public void setUsername(String us) { this.username = us; }

    /**
     * Ustawia hasło użytkownika.
     * * @param p nowe hasło użytkownika
     */
    public void setPassword(String p) { this.password = p; }

    /**
     * Pobiera klucz publiczny użytkownika.
     * * @return klucz publiczny zakodowany tekstowo (np. w Base64 lub HEX)
     */
    public String getPublicKey() {
        return publicKey;
    }

    /**
     * Ustawia klucz publiczny użytkownika.
     * * @param key klucz publiczny w postaci tekstowej
     */
    public void setPublicKey(String key) {
        this.publicKey = key;
    }

    /**
     * Ustawia zaszyfrowany klucz prywatny użytkownika.
     * * @param key zaszyfrowany klucz prywatny w postaci tekstowej
     */
    public void setEncryptedPrivateKey(String key) {
        encryptedPrivateKey = key;
    }

    /**
     * Pobiera zaszyfrowany klucz prywatny użytkownika.
     * * @return zaszyfrowany klucz prywatny zakodowany tekstowo
     */
    public String getEncryptedPrivateKey() { return encryptedPrivateKey; }
}