package Services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import Models.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import security.TokenStorage;

/**
 * Serwis odpowiedzialny za operacje na grupach:
 * tworzenie grupy oraz dołączanie do istniejącej grupy.
 * Komunikuje się z backendem poprzez HTTP JSON API.
 */
public class GroupService {

    /** Klient HTTP używany do komunikacji z backendem */
    private final HttpClient client = HttpClient.newHttpClient();

    /** Mapper JSON do serializacji i deserializacji odpowiedzi */
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Tworzy nową grupę na backendzie.
     *
     * Proces:
     * - buduje request JSON,
     * - wysyła żądanie POST,
     * - interpretuje odpowiedź serwera.
     *
     * @param groupName nazwa tworzonej grupy
     * @return ServiceResponse z informacją o wyniku operacji
     */
    public ServiceResponse createGroup(String groupName) {

        try {

            CreateGroupRequest requestBody = new CreateGroupRequest(groupName);
            String json = mapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://kryptochatserwer-production.up.railway.app/api/groups/create"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + TokenStorage.getCachedToken())
                    .POST(HttpRequest.BodyPublishers.ofString(json)).build();

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
     * Dołącza użytkownika do istniejącej grupy.
     *
     * Proces:
     * - buduje request JSON z kodem grupy,
     * - wysyła żądanie POST,
     * - analizuje odpowiedź serwera.
     *
     * @param code kod grupy
     * @return ServiceResponse z wynikiem operacji
     */
    public ServiceResponse joinGroup(String code) {

        try {

            JoinGroupRequest requestBody = new JoinGroupRequest(code);

            String json = mapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://kryptochatserwer-production.up.railway.app/api/groups/join"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + TokenStorage.getCachedToken())
                    .POST(HttpRequest.BodyPublishers.ofString(json)).build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

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

    /**
     * Przetwarza odpowiedź serwera po utworzeniu lub dołączeniu do grupy.
     *
     * Wyciąga:
     * - nowy JWT token,
     * - dane użytkownika (w tym groupId),
     * a następnie aktualizuje TokenStorage.
     *
     * @param response odpowiedź HTTP z backendu
     * @return true jeśli zapis zakończył się sukcesem, false w przeciwnym razie
     */
    private boolean saveResponse(HttpResponse<String> response) {

        try {

            JsonNode node = mapper.readTree(response.body());
            String jwt = node.get("jwt").asText();
            JsonNode userCredentials = node.get("userCredentials");
            Long groupId = userCredentials.get("groupId").isNull() ? null : userCredentials.get("groupId").asLong();

            if (groupId == null) return false;

            User user = TokenStorage.getUser();

            if (user == null) { user = new User(); }

            user.setGroupId(groupId);

            TokenStorage.setUser(user);
            TokenStorage.saveToken(jwt);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}