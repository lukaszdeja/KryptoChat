package Services;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import security.TokenStorage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import Models.User;



public class LoginService {

    /** Metoda obsługująca logowanie
     * Metoda tworzy obiekt requesta, przesyła go przez JSON, na serwer i na podstawie odpowiedzi
     * zapisuje token JWT w pliku tekstowym ("ciasteczku")
     * @param username podany login
     * @param password podane hasło
     * @return ServiceResponse - obiekt zawierający bool czy sie udało zalogować i string z komunikatem
     */
    public ServiceResponse login(String username, String password) {
        try {
            LoginRequest loginRequest = new LoginRequest();
            ObjectMapper mapper = new ObjectMapper();

            loginRequest.setUsername(username);
            loginRequest.setPassword(password);

            String json = mapper.writeValueAsString(loginRequest);

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://kryptochatserwer-production.up.railway.app/api/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            //System.out.println("Status: " + response.statusCode());

            if ( response.statusCode() == 500 ) {
                return new ServiceResponse(false, "Niepoprawny login lub hasło");
            }

            if (response.statusCode() == 200) {

                JsonNode node = mapper.readTree(response.body());
                String token = node.get("jwt").asText();
                JsonNode tokenNode = node.get("userCredentials");

                if (tokenNode == null) {
                    return new ServiceResponse(false, "Błąd serwera - brak tokenu");
                }

                User user = mapper.treeToValue(tokenNode, User.class);

                TokenStorage.setUser(user);
                TokenStorage.saveToken(token);

                return new ServiceResponse(true, "Zalogowano pomyślnie");
            }

            return new ServiceResponse(false, "Błąd serwera");

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();

            return new ServiceResponse(false, "Brak połączenia z serwerem");
        }
    }

}


