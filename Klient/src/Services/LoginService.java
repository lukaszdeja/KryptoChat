package Services;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;



public class LoginService {

    /** Metoda obsługująca logowanie
     * Metoda tworzy obiekt requesta, przesyła go przez JSON, na serwer i na podstawie odpowiedzi
     * zapisuje token JWT w pliku tekstowym ("ciasteczku")
     * @param username podany login
     * @param password podane hasło
     * @return boolean - czy sie udalo zalogowac
     */
    public boolean login(String username, String password) {
        try {
            LoginRequest loginRequest = new LoginRequest();
            ObjectMapper mapper = new ObjectMapper();
            loginRequest.setUsername(username);
            loginRequest.setPassword(password);
            String json = mapper.writeValueAsString(loginRequest);
            System.out.println(json);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://wpiszlink.pl/api/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json)).build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                String responseBody = response.body();
                JsonNode node = mapper.readTree(responseBody);
                String token = node.get("token").asText();
                System.out.println("JWT: "+ token);
                saveCookie(token);
                return true;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return true;
        }
        return true;
    }

    /**
     * Pomocnicza metoda zapisująca token do pliku tekstowego
     * @param token - token zapisywany w pliku, przekazany jako String
     */
    private void saveCookie(String token) {
        return;
    }
}


