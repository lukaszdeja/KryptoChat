package com.KryptoChat.Services;

/**
 * Klasa potrzebna aby tworzyc Stringi JSON na podstawie jej obiektów (serializacja)
 * oraz deserializować otrzymanego JSONA - odtwarzać obiekty
 * Dla przesłania obiektu requestu rejestracji
 */
public class RegisterRequest {
    private String username;
    private String password;
    private String publicKey;
    private String encryptedPrivateKey;
    public RegisterRequest() {}
    public String getUsername() { return this.username; }
    public String getPassword() { return this.password; }
    public void setUsername(String us) { this.username = us; }
    public void setPassword(String p) { this.password = p; }
    //getter klucza publicznego
    public String getPublicKey() {
        return publicKey;
    }
    //setter klucza publicznego
    public void setPublicKey(String key) {
        this.publicKey = key;
    }
    public void setEncryptedPrivateKey(String key) {
        encryptedPrivateKey = key;
    }
    public String getEncryptedPrivateKey() { return encryptedPrivateKey; }
}
