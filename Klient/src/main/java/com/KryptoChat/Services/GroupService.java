package com.KryptoChat.Services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.PublicKey;

import com.KryptoChat.Models.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.KryptoChat.security.GroupKeyStorage;
import com.KryptoChat.security.TokenStorage;

import javax.crypto.SecretKey;

/**
 * Serwis odpowiedzialny za operacje na grupach:
 * tworzenie grupy oraz dołączanie do istniejącej grupy.
 * Komunikuje się z backendem poprzez HTTP JSON API.
 */
public class GroupService {

    /** Klient HTTP używany do komunikacji z backendem */
    private final HttpClient client;

    /** Mapper JSON do serializacji i deserializacji odpowiedzi */
    private final ObjectMapper mapper = new ObjectMapper();

    public GroupService() {
        this(HttpClient.newHttpClient());
    }

    public GroupService(HttpClient client) {
        this.client = client;
    }
    /**
     * Tworzy nową grupę na serwerze i generuje dla niej klucz szyfrujący.
     *
     * Proces:
     * - generuje nowy klucz symetryczny AES dla grupy,
     * - szyfruje go kluczem publicznym twórcy (RSA) i wysyła POST na serwer,
     * - po udanej odpowiedzi (200) zapisuje klucz grupy lokalnie i aktualizuje sesję.
     *
     * @param groupName nazwa nowej grupy
     * @return ServiceResponse wynik operacji z komunikatem dla UI
     */
    public ServiceResponse createGroup(String groupName) {

        try {
            SecretKey key = CryptoService.generateAESKey();
            PublicKey publicKey = CryptoService.getPublicKey(TokenStorage.getUser().getUsername());
            String creatorKey = CryptoService.encryptRSA(key.getEncoded(), publicKey);
            CreateGroupRequest requestBody = new CreateGroupRequest(groupName, creatorKey);
            String json = mapper.writeValueAsString(requestBody);
            System.out.println(json);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://kryptochatserwer-production.up.railway.app/api/groups/create"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + TokenStorage.getCachedToken())
                    .POST(HttpRequest.BodyPublishers.ofString(json)).build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            switch (response.statusCode()) {

                case 200:
                    GroupKeyStorage.save(TokenStorage.getUser().getUsername(), key);
                    return new ServiceResponse(saveResponse(response), "Utworzono grupę");

                default:
                    return new ServiceResponse(false, "Błąd serwera");
            }

        } catch (Exception e) {
            System.out.println("Nie udalo sie utworzyc grupy");
            return new ServiceResponse(false, "Brak połączenia z serwerem");
        }
    }

    /**
     * Dołącza zalogowanego użytkownika do istniejącej grupy na podstawie kodu.
     *
     * Proces:
     * - wysyła kod grupy w żądaniu POST na serwer,
     * - w przypadku sukcesu (200) wywołuje aktualizację tokenu i identyfikatora grupy,
     * - obsługuje brak grupy (błąd 500).
     *
     * @param code unikalny kod grupy
     * @return ServiceResponse wynik operacji z komunikatem dla UI
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
                    return new ServiceResponse(false, "Kod nie jest prawidłowy lub wystąpił błąd");
            }

        } catch (Exception e) {
            System.out.println("Nie udalo sie dolaczyc do grupy");
            return new ServiceResponse(false, "Brak połączenia z serwerem");
        }
    }

    /**
     * Przetwarza odpowiedź serwera po operacjach na grupach.
     *
     * Proces:
     * - wyciąga z JSON-a nowy token JWT oraz identyfikator grupy (groupId),
     * - przypisuje groupId do aktualnego obiektu User,
     * - zapisuje zaktualizowane dane użytkownika i nowy token w TokenStorage.
     *
     * @param response odpowiedź HTTP z serwera
     * @return true jeśli pomyślnie zaktualizowano dane sesji; false w przeciwnym wypadku
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
            System.out.println("Nie udalo sie zapisac danych z serwera");
            return false;
        }
    }
}