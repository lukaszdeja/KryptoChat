package Services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

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