package Services;

/**
 * Klasa potrzebna aby tworzyc Stringi JSON na podstawie jej obiektów (serializacja)
 * oraz deserializować otrzymanego JSONA - odtwarzać obiekty
 * Dla przesłania obiektu requestu logowania
 */
public class LoginRequest {
    private String username;
    private String password;
    public LoginRequest() {}
    public void setUsername(String us) { username = us;}
    public void setPassword(String p) { password = p;}
    public String getUsername() { return username; }
    public String  getPassword() { return password; }
}
