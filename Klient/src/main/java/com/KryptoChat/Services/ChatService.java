package com.KryptoChat.Services;

import com.KryptoChat.Models.Group;
import com.KryptoChat.Models.Message;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.KryptoChat.security.TokenStorage;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * Serwis odpowiedzialny za operacje związane z czatem, w tym pobieranie grupy oraz wiadomości użytkownika.
 */
public class ChatService {

    private final HttpClient client = HttpClient.newHttpClient();

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    /**
     * Pobiera dane grupy zalogowanego użytkownika.
     * @return obiekt Group zawierający dane grupy i użytkowników
     * @return null w przypadku błędu lub braku użytkownika
     */
    public Group loadGroup() {

        if (TokenStorage.getUser() == null) {
            System.out.println("Brak zalogowanego użytkownika");
            return null;
        }

        try {

            String token = TokenStorage.getCachedToken();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://kryptochatserwer-production.up.railway.app/api/groups/"))
                    .header("Authorization", "Bearer " + token).GET().build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println(response.body());

            if (response.statusCode() == 200) {
                return mapper.readValue(response.body(), Group.class);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Pobiera listę wiadomości użytkownika z API.
     * @return lista wiadomości lub null w przypadku błędu
     */
    public List<Message> loadMessages() {

        if (TokenStorage.getUser() == null) {
            return null;
        }

        try {

            String token = TokenStorage.getCachedToken();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://kryptochatserwer-production.up.railway.app/api/messages/"))
                    .header("Authorization", "Bearer " + token)
                    .GET().build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println(response.body());

            if (response.statusCode() == 200) {

                JsonNode root = mapper.readTree(response.body());
                JsonNode messagesNode = root.get("messages");

                List<Message> messages = mapper.readValue(messagesNode.toString(), new TypeReference<List<Message>>() {});

                return messages;
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }
}