package Services;

public class LoginRequest {
    private String username;
    private String password;
    public LoginRequest() {}
    public void setUsername(String us) { username = us;}
    public void setPassword(String p) { password = p;}
    public String getUsername() { return username; }
    public String  getPassword() { return password; }
}
