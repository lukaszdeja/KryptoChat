package Services;

import Models.Group;

import com.fasterxml.jackson.databind.ObjectMapper;

import security.TokenStorage;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Klasa obsługująca operacje związane z czatem.
 */
public class ChatService {

    private final HttpClient client = HttpClient.newHttpClient();

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Pobiera dane grupy zalogowanego użytkownika.
     * @return Group - grupa wraz z użytkownikami
     * @return null, gdy się nie udało
     */
    public Group loadGroup() {

        if (TokenStorage.getUser() == null) {
            System.out.println("Brak zalogowanego użytkownika");
            return null;
        }

        try {
            Long groupId = TokenStorage.getUser().getGroupId();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://kryptochatserwer-production.up.railway.app/api/groups/" + groupId))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println(response.body());

            if(response.statusCode() == 200) {
                return mapper.readValue(response.body(), Group.class);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }
}