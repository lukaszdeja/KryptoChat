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

/**
 * Klasa obsługuje serwis rejestracji, realizacja backendu
 */
public class RegisterService {

    /**
     * Metoda obsługująca rejestrację, przesyła login oraz hasło na serwer w formacie JSON
     * @param username nazwa uzytkownika
     * @param password haslo
     * @param password2 powtorzone haslo
     * @return boolean - czy udalo sie zarejestrować użytkownika
     */
    public boolean login(String username, String password, String password2) {
        RegisterRequest register = new RegisterRequest();
        register.setUsername(username);
        register.setPassword(password);
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(register);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://wpiszlink.pl/api/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }
}
