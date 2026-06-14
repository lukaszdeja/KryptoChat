package com.KryptoChat.Services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyPair;
import java.util.Base64;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Klasa obsługuje serwis rejestracji, przesylanie na serwer danych tworzonego konta
 */
public class RegisterService {

        private final HttpClient client;
        private final ObjectMapper mapper = new ObjectMapper();

        public RegisterService() {
            this(HttpClient.newHttpClient());
        }

        RegisterService(HttpClient client) {
            this.client = client;
        }

    /**
     * Metoda obsługująca rejestrację, przesyła login oraz hasło na serwer w formacie JSON
     * @param username nazwa uzytkownika
     * @param password haslo
     * @param password2 powtorzone haslo
     * @return ServiceResponse - obiekt zawierający bool czy sie udało zarejestrować i string z komunikatem
     */
    public ServiceResponse register(String username, String password, String password2) {

        RegisterRequest register = new RegisterRequest();

        register.setUsername(username);
        register.setPassword(password);

        try {
            KeyPair keyPair = CryptoService.generateKeysIfNeeded(username);
            String publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
            register.setPublicKey(publicKey);
            register.setEncryptedPrivateKey(CryptoService.encryptPrivateKeyWithPassword(keyPair.getPrivate(), password));
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(register);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://kryptochatserwer-production.up.railway.app/api/register"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response =
                    this.client.send(request, HttpResponse.BodyHandlers.ofString());

            if(response.statusCode() == 200) {
                if (keyPair != null) {
                    CryptoService.saveKeyPair(username, keyPair);
                }
                return new ServiceResponse(true, "Utworzono konto");
            }
            if (response.statusCode() == 400) {
                return new ServiceResponse(false, "Login lub hasło są zbyt dlugie");
            }

            if(response.statusCode() == 500) {
                return new ServiceResponse(false, "Użytkownik już istnieje, nie udało się utworzyć konta");
            }

            return new ServiceResponse(false, "Błąd rejestracji");

        } catch (NullPointerException e) {
            System.out.println("Nie udało sie zarejestrować, klucz dla tego użytkownika już istnieje");
            return new ServiceResponse(false, "Nie udało sie zarejestrować, użytkownik już istnieje");
        }   catch (Exception e) {
            System.out.println("Nie udalo sie zarejestrowac");

            return new ServiceResponse(false, "Brak połączenia z serwerem");
        }
    }
}
