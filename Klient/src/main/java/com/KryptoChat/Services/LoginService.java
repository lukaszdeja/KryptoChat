package com.KryptoChat.Services;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import com.KryptoChat.security.TokenStorage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.KryptoChat.Models.User;


/**
 * Serwis odpowiedzialny za proces uwierzytelniania użytkowników w aplikacji.
 * Obsługuje komunikację synchroniczną z API logowania, zarządza tokenami sesyjnymi JWT
 * oraz odpowiada za lokalne odzyskiwanie i odszyfrowywanie asymetrycznej pary kluczy kryptograficznych.
 */
public class LoginService {

    private final HttpClient client;
    private final ObjectMapper mapper = new ObjectMapper();

    public LoginService() {
        this(HttpClient.newHttpClient());
    }

    public LoginService(HttpClient client) {
        this.client = client;
    }

    /**
     * Loguje użytkownika do systemu i konfiguruje jego sesję.
     *
     * Proces:
     * - wysyła dane logowania (JSON) żądaniem POST na serwer,
     * - w przypadku sukcesu pobiera token JWT oraz dane użytkownika,
     * - jeśli na dysku brakuje kluczy, deszyfruje klucz prywatny hasłem i zapisuje go lokalnie,
     * - zapisuje token i sesję w TokenStorage.
     *
     * @param username login użytkownika
     * @param password hasło użytkownika
     * @return ServiceResponse wynik operacji (status sukcesu i komunikat dla UI)
     */
    public ServiceResponse login(String username, String password) {
        try {
            LoginRequest loginRequest = new LoginRequest();

            loginRequest.setUsername(username);
            loginRequest.setPassword(password);

            String json = mapper.writeValueAsString(loginRequest);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://kryptochatserwer-production.up.railway.app/api/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() == 500) {
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

                JsonNode encPrivKey = node.get("encryptedPrivateKey");
                JsonNode pubKey = node.get("publicKey");

                if (encPrivKey != null && pubKey != null) {
                    if (!CryptoService.keysExist(username)) {
                        try {
                            PrivateKey privateKey =
                                    CryptoService.decryptPrivateKeyWithPassword(encPrivKey.asText(), password);

                            PublicKey publicKey = CryptoService.stringToPublicKey(pubKey.asText());

                            CryptoService.saveKeyPair(username, new KeyPair(publicKey, privateKey));

                        } catch (Exception e) {
                            System.out.println("Nie udalo sie odzyskac kluczy");
                            e.printStackTrace();
                        }
                    }
                }

                TokenStorage.setUser(user);
                TokenStorage.saveToken(token);

                return new ServiceResponse(true, "Zalogowano pomyślnie");
            }

            return new ServiceResponse(false, "Błąd serwera");

        } catch (IOException | InterruptedException e) {
            System.out.println("Nie udalo sie zalogowac");
            return new ServiceResponse(false, "Brak połączenia z serwerem");
        }
    }
}