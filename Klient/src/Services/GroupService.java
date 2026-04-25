package Services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Klasa obsługuje operacje związane z grupami (tworzenie, dołączanie)
 */
public class GroupService {

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Tworzy nową grupę wysyłając nazwę do backendu
     * @param groupName nazwa grupy
     * @return true jeśli sukces
     */
    public boolean createGroup(String groupName) {

        try {
            GroupRequest requestBody = new GroupRequest();
            requestBody.setGroupName(groupName);

            String json = mapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://wpiszlink.pl/api/creategroup"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true; //domyslnie false
    }

    /**
     * Dołącza do istniejącej grupy na podstawie kodu
     * @param code kod grupy
     * @return true jeśli sukces
     */
    public boolean joinGroup(String code) {

        try {
            GroupRequest requestBody = new GroupRequest();
            requestBody.setCode(code);

            String json = mapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://wpiszlink.pl/api/joingroup"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true; //domyslnie false
    }
}