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
     *
     * @param groupName nazwa tworzonej grupy
     * @return ServiceResponse zawierający boolean czy się udało stworzyć grupe wraz z komunikatem
     */
    public ServiceResponse createGroup(String groupName) {

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
            switch (response.statusCode()) {

                case 200:
                    return new ServiceResponse(saveResponse(response), "Utworzono grupę");

                default:
                    return new ServiceResponse(false, "Błąd serwera");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ServiceResponse(false, "Brak połączenia z serwerem");
        }
    }

    /**
     * Metoda obsługująca dołączanie do grupy
     * Metoda tworzy obiekt requesta, przesyła go w formacie JSON na serwer
     * i na podstawie odpowiedzi HTTP określa czy operacja zakończyła się sukcesem
     *
     * @param code kod grupy
     * @return ServiceResponse zawierający boolean czy się udało dołączyć do grupy wraz z komunikatem
     */
    public ServiceResponse joinGroup(String code) {

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
            System.out.println("Status: " + response.statusCode());

            switch (response.statusCode()) {

                case 200:
                    return new ServiceResponse(saveResponse(response), "Dołączono do grupy");

                case 500:
                    return new ServiceResponse(false, "Nie znaleziono grupy");

                default:
                    return new ServiceResponse(false, "Błąd serwera");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ServiceResponse(false, "Brak połączenia z serwerem");
        }

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
                //System.out.println("Nie udalo sie zapisac");
                return false;
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return false;
    }
}
