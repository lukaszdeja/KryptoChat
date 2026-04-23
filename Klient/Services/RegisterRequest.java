package Services;

/**
 * Klasa potrzebna aby tworzyc Stringi JSON na podstawie jej obiektów (serializacja)
 * oraz deserializować otrzymanego JSONA - odtwarzać obiekty
 * Dla przesłania obiektu requestu rejestracji
 */
public class RegisterRequest {
    private String username;
    private String password;
    public RegisterRequest() {}
    public String getUsername() { return this.username; }
    public String getPassword() { return this.password; }
    public void setUsername(String us) { this.username = us; }
    public void setPassword(String p) { this.password = p; }
}
