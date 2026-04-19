package Services;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;



public class LoginService {

    public boolean login(String username, String password) {
        try {
            LoginRequest loginRequest = new LoginRequest(username, password);
            String json = String.format(
                    "{\"username\":\"%s\",\"password\":\"%s\"}",
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
            );
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://wpiszlink.pl/api/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json)).build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }
}

class LoginRequest {
    private String username;
    private String password;
    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
    public String getUsername() { return username; }
    public String  getPassword() { return password; }
}
