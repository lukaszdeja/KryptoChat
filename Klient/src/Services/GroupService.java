package Services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

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
            GroupRequest requestBody = new GroupRequest();
            requestBody.setGroupName(groupName);
            requestBody.setUser(TokenStorage.getUser());

            if ( TokenStorage.getUser().getGroupId() != null ){
                return false;
            }

            String json = mapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://kryptochatserwer-production.up.railway.app/api/groups/create"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
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
            GroupRequest requestBody = new GroupRequest();
            requestBody.setCode(code);
            requestBody.setUser(TokenStorage.getUser());

            if ( TokenStorage.getUser().getGroupId() != null ){
                return false;
            }

            String json = mapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://kryptochatserwer-production.up.railway.app/api/groups/join"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
        }
        //obsluga komunikatu czy sie udalo czy nie w obu

        return false;
    }
}