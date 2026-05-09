package Services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import security.TokenStorage;

/**
 * Klasa obsługująca operacje związane z grupami (tworzenie oraz dołączanie).
 * Realizuje komunikację z backendem poprzez wysyłanie żądań HTTP w formacie JSON.
 * Na podstawie odpowiedzi serwera (status HTTP) określa powodzenie operacji.
 */
public class GroupService {

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Metoda obsługująca tworzenie grupy
     * Metoda tworzy obiekt requesta, przesyła go w formacie JSON na serwer
     * i na podstawie odpowiedzi HTTP określa czy operacja zakończyła się sukcesem
     * @param groupName nazwa tworzonej grupy
     * @return boolean - czy udało się utworzyć grupę
     */
    public boolean createGroup(String groupName) {

        try {
            CreateGroupRequest requestBody = new CreateGroupRequest();
            requestBody.setGroupName(groupName);
            requestBody.setUsername(TokenStorage.getUser().getUsername());

            String json = mapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://kryptochatserwer-production.up.railway.app/api/groups/create"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.body());
            System.out.println(response.statusCode());

            if (response.statusCode() == 200) {
                return this.saveResponse(response);
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false; //domyslnie false
    }

    /**
     * Metoda obsługująca dołączanie do grupy
     * Metoda tworzy obiekt requesta, przesyła go w formacie JSON na serwer
     * i na podstawie odpowiedzi HTTP określa czy operacja zakończyła się sukcesem
     * @param code kod grupy
     * @return boolean - czy udało się dołączyć do grupy
     */
    public boolean joinGroup(String code) {

        try {
            JoinGroupRequest requestBody = new JoinGroupRequest();
            requestBody.setCode(code);
            requestBody.setUsername(TokenStorage.getUser().getUsername());

            String json = mapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://kryptochatserwer-production.up.railway.app/api/groups/join"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return this.saveResponse(response);
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false; //domyslnie false
    }

    private boolean saveResponse(HttpResponse<String> response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(response.body());
            Long groupId = node.get("groupId").asLong();
            System.out.println(groupId);
            if (groupId != null) {
                TokenStorage.getUser().setGroupId(groupId);
                TokenStorage.deleteToken();
                String storeUser = mapper.writeValueAsString(TokenStorage.getUser());
                TokenStorage.saveToken(storeUser);
                return true;
            } else {
                System.out.println("Nie udalo sie zapisac");
                return false;
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return false;
    }
}
