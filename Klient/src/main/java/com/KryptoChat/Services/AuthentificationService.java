package com.KryptoChat.Services;

import com.KryptoChat.Models.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Serwis odpowiedzialny za weryfikację tokenu użytkownika
 * oraz pobranie danych aktualnie zalogowanego użytkownika z backendu.
 */
public class AuthentificationService {

    /**
     * Sprawdza poprawność tokenu i pobiera dane użytkownika z API.
     *
     * Proces:
     * - wysyła zapytanie HTTP GET do endpointu /api/me,
     * - przekazuje token w nagłówku Authorization,
     * - mapuje odpowiedź JSON na obiekt User.
     *
     * @param token token autoryzacyjny użytkownika
     * @return obiekt User jeśli token jest poprawny, w przeciwnym razie null
     */
    public User checkUser(String token) {

        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://kryptochatserwer-production.up.railway.app/api/me"))
                    .header("Authorization", "Bearer " + token).GET().build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            ObjectMapper mapper = new ObjectMapper();

            return mapper.readValue(response.body(), User.class);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}