package Services;

public class LoginService {

    public boolean login(String username, String password) {
        return username.equals("admin") && password.equals("1234");
    }
}
