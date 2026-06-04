package com.KryptoChat.Services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.KryptoChat.Models.Message;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.javafx.css.parser.Token;
import com.KryptoChat.security.TokenStorage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import com.fasterxml.jackson.databind.JsonNode;
import com.KryptoChat.security.GroupKeyStorage;

import javax.crypto.SecretKey;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.PublicKey;
import java.security.PrivateKey;
import java.util.Map;


/**
 * Serwis odpowiedzialny za dwukierunkową komunikację cyfrową za pomocą WebSocket.
 * Obsługuje zarządzanie stanem połączenia, automatyczne wznawianie sesji,
 * dystrybucję kluczy kryptograficznych oraz bezpieczną wymianę wiadomości czatu.
 */
public class WebSocketService {

    /** Aktywne połączenie WebSocket */
    private WebSocket webSocket;

    /** Bazowy URL serwera API */
    private static final String url = "https://kryptochatserwer-production.up.railway.app";

    /** Klient HTTP używany do inicjalizacji WebSocket i zapytań REST */
    private final HttpClient client = HttpClient.newHttpClient();

    /** Flaga określająca, czy proces automatycznego reconnectu jest obecnie aktywny */
    private volatile boolean reconnecting = false;

    /** Flaga informująca, czy rozłączenie nastąpiło celowo ze strony użytkownika (np. wylogowanie) */
    private volatile boolean manuallyDisconnected = false;

    /** Mapper JSON do serializacji i deserializacji obiektów wiadomości oraz konfiguracji */
    private final ObjectMapper mapper = new ObjectMapper();

    /** Callback wywoływany w warstwie UI po pomyślnym odebraniu i odszyfrowaniu wiadomości tekstowej */
    private Consumer<Message> onMessageReceived;

    /** Callback informujący aplikację o poprawnym odebraniu klucza szyfrującego grupę */
    private Runnable onKeyReceived;

    /**
     * Rejestruje akcję zwrotną dla zdarzenia odebrania nowego klucza szyfrującego.
     * * @param callback obiekt typu Runnable do wykonania po pobraniu klucza
     */
    public void setOnKeyReceived(Runnable callback) {
        this.onKeyReceived = callback;
    }

    /**
     * Nawiązuje asynchroniczne połączenie WebSocket z serwerem czatu.
     *
     * Proces:
     * - przerywa istniejące, niestabilne połączenia,
     * - konfiguruje moduł obsługi czasu Jackson (JavaTimeModule),
     * - wstrzykuje token autoryzacyjny Bearer do nagłówka,
     * - definiuje asynchroniczny nasłuchiwacz zdarzeń sieciowych (otwarcie, tekst, błąd, zamknięcie).
     */
    public void connect() {

        manuallyDisconnected = false;

        if (webSocket != null) {
            try {
                webSocket.abort();
            } catch (Exception e) {
                System.out.println("Połączenie utracone: " + e.getMessage());
            }

            webSocket = null;
        }

        mapper.registerModule(new JavaTimeModule());

        try {

            String token = TokenStorage.getCachedToken();

            client.newWebSocketBuilder()
                    .header("Authorization", "Bearer " + token)
                    .buildAsync(
                            URI.create("wss://kryptochatserwer-production.up.railway.app/ws"),
                            new WebSocket.Listener() {

                                @Override
                                public void onOpen(WebSocket webSocket) {
                                    System.out.println("Połączono z serwerem");
                                    WebSocketService.this.webSocket = webSocket;
                                    if (!GroupKeyStorage.exists(TokenStorage.getUser().getUsername())) {
                                        handleKeyReady();
                                    }

                                    webSocket.request(1);
                                }

                                @Override
                                public CompletionStage<?> onText(
                                        WebSocket webSocket,
                                        CharSequence data,
                                        boolean last
                                ) {
                                    try {
                                        JsonNode node = mapper.readTree(data.toString());

                                        String type = node.has("type") ? node.get("type").asText() : "CHAT";
                                        switch (type) {
                                            case "CHAT" -> {
                                                Message message = mapper.treeToValue(node, Message.class);
                                                try {
                                                    SecretKey aesKey = GroupKeyStorage.load(TokenStorage.getUser().getUsername());
                                                    message.setContent(CryptoService.decryptAES(message.getContent(), aesKey));
                                                } catch (Exception e) {
                                                    System.out.println("Nie udalo sie odszyfrowac");
                                                    e.printStackTrace();
                                                }
                                                if (onMessageReceived != null) {
                                                    onMessageReceived.accept(message);
                                                }
                                            }
                                            case "KEY_REQUEST" -> {
                                                long targetUserId = node.get("userId").asLong();
                                                String targetPubKey = node.get("publicKey").asText();
                                                handleKeyRequest(targetUserId, targetPubKey);
                                            }
                                            case "KEY_READY" -> {
                                                handleKeyReady();
                                            }
                                            default -> {System.out.println("Cos nie tak");}
                                        }

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    webSocket.request(1);
                                    return null;
                                }

                                @Override
                                public void onError(WebSocket webSocket, Throwable error) {
                                    reconnect();
                                    WebSocketService.this.webSocket = null;
                                    error.printStackTrace();
                                }

                                @Override
                                public CompletionStage<?> onClose(WebSocket webSocket,
                                                                  int statusCode,
                                                                  String reason) {
                                    System.out.println("WebSocket closed: " + statusCode + " " + reason);
                                    WebSocketService.this.webSocket = null;
                                    if (!manuallyDisconnected) {
                                        reconnect();
                                    }
                                    return null;
                                }
                            }
                    )
                    .thenAccept(ws -> this.webSocket = ws);

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    /**
     * Szyfruje oraz wysyła wiadomość tekstową użytkownika do serwera.
     *
     * Proces:
     * - sprawdza dostępność połączenia sieciowego,
     * - weryfikuje istnienie lokalnego klucza grupy,
     * - szyfruje treść wiadomości algorytmem AES,
     * - opakowuje dane w strukturę JSON typu CHAT i wysyła strumień tekstu.
     *
     * @param message obiekt wiadomości zawierający identyfikator grupy oraz treść jawna
     */
    public void send(Message message) {
        if (webSocket == null) {
            System.out.println("Brak polaczenia z serwerem");
            return;
        }
        try {
            if (GroupKeyStorage.exists(TokenStorage.getUser().getUsername())) {
                ObjectNode node = mapper.createObjectNode();
                SecretKey aesKey = GroupKeyStorage.load(TokenStorage.getUser().getUsername());
                String encryptedMessage = CryptoService.encryptAES(message.getContent(), aesKey);
                node.put("type", "CHAT");
                node.put("groupId", message.getGroupId());
                node.put("content", encryptedMessage);
                String json = mapper.writeValueAsString(node);
                webSocket.sendText(json, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Realizuje bezpieczne przekazanie klucza szyfrującego grupę dla nowego członka.
     *
     * Proces:
     * - wczytuje tajny klucz grupy obecnego użytkownika,
     * - konwertuje tekstowy klucz publiczny odbiorcy na obiekt PublicKey,
     * - szyfruje klucz grupy kluczem publicznym odbiorcy (asymetrycznie),
     * - wysyła zaszyfrowany pakiet żądaniem POST pod punkt końcowy /deliver-key.
     *
     * @param targetUserId identyfikator użytkownika, który wnioskuje o dostęp do klucza grupy
     * @param targetPubKeyString klucz publiczny wnioskodawcy zakodowany tekstowo
     */
    private void handleKeyRequest(Long targetUserId, String targetPubKeyString) {
        try {
            SecretKey myGroupKey = GroupKeyStorage.load(TokenStorage.getUser().getUsername());
            PublicKey targetPubKey = CryptoService.stringToPublicKey(targetPubKeyString);
            String encryptedKey = CryptoService.encryptGroupKey(myGroupKey, targetPubKey);

            Map<String, Object> body = Map.of(
                    "targetUserId", targetUserId,
                    "encryptedKey", encryptedKey
            );

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url + "/api/groups/deliver-key"))
                    .header("Content-Type", "application/json")
                    .header("Authorization",
                            "Bearer " + TokenStorage.getCachedToken())
                    .POST(HttpRequest.BodyPublishers.ofString(
                            mapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> response = client.send(req, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("Klucz dostarczony dla usera: " + targetUserId);
            } else {
                System.out.println("Blad dostarczania klucza");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Pobiera przyznany dla użytkownika klucz grupy z serwera po otrzymaniu powiadomienia.
     *
     * Proces:
     * - wysyła zapytanie GET żądające klucza powiązanego z sesją,
     * - odrzuca odpowiedzi o statusie oczekiwania (PENDING),
     * - deszyfruje pobrany klucz grupy za pomocą prywatnego klucza asymetrycznego użytkownika,
     * - zapisuje odkodowany klucz w lokalnej bezpiecznej pamięci GroupKeyStorage,
     * - uruchamia zarejestrowany callback powiadomienia o gotowości klucza.
     */
    private void handleKeyReady() {
        System.out.println("Key received");
        try {

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url + "/api/groups/my-key"))
                    .header("Authorization",
                            "Bearer " + TokenStorage.getCachedToken())
                    .GET()
                    .build();

            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() == 200 && !"PENDING".equals(resp.body())) {
                PrivateKey privateKey = CryptoService.getPrivateKey(TokenStorage.getUser().getUsername());
                SecretKey groupKey = CryptoService.decryptGroupKey(resp.body(), privateKey);
                GroupKeyStorage.save(TokenStorage.getUser().getUsername(), groupKey);
                System.out.println("Klucz grupy odebrany i zapisany");

                if (onKeyReceived != null) {
                    onKeyReceived.run();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Przeprowadza procedurę bezpiecznego i kontrolowanego zamknięcia sesji WebSocket.
     *
     * Proces:
     * - oznacza rozłączenie jako manualne (blokuje automatyczny reconnect),
     * - wysyła pakiet zamykający z kodem NORMAL_CLOSURE i statusem "logout",
     * - zwalnia zasoby i czyści referencję do obiektu WebSocket.
     */
    public void disconnect() {
        manuallyDisconnected = true;

        if (webSocket != null) {
            try {
                webSocket.sendClose(
                        WebSocket.NORMAL_CLOSURE,
                        "logout"
                );
            } catch (Exception e) {
                System.out.println("Połączenie utracone: " + e.getMessage());
            }

            webSocket = null;
        }

        System.out.println("WebSocket zamknięty");
    }

    /**
     * Odpowiada za pętlę automatycznego wznawiania połączenia w osobnym wątku roboczym.
     *
     * Proces:
     * - sprawdza, czy nie trwa już inna próba połączenia lub czy sesja nie została zamknięta ręcznie,
     * - uruchamia nowy wątek, w którym co 5 sekund wywołuje metodę connect(),
     * - działa w nieskończonej pętli dopóki referencja na obiekt webSocket pozostaje pusta (null).
     */
    private void reconnect() {
        if (reconnecting || manuallyDisconnected) {
            return;
        }

        reconnecting = true;

        new Thread(() -> {
            while (webSocket == null && !manuallyDisconnected) {
                try {
                    System.out.println("Ponowne łączenie...");
                    connect();

                    Thread.sleep(5000);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }

            reconnecting = false;
        }).start();
    }

    /**
     * Definiuje zewnętrzny odbiorca wiadomości (konsumenta), realizujący aktualizację kontrolera UI.
     *
     * @param consumer interfejs funkcjonalny akceptujący przetworzony obiekt Message
     */
    public void setOnMessageReceived(Consumer<Message> consumer) {
        this.onMessageReceived = consumer;
    }
}