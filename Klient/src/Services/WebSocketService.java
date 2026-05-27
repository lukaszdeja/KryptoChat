package Services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import Models.Message;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import security.TokenStorage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

/**
 * Serwis odpowiedzialny za komunikację WebSocket z serwerem czatu.
 * Obsługuje połączenie, wysyłanie oraz odbieranie wiadomości.
 */
public class WebSocketService {

    /** Aktywne połączenie WebSocket */
    private WebSocket webSocket;


    private volatile boolean reconnecting = false;

    private volatile boolean manuallyDisconnected = false;


    /** Mapper JSON do serializacji i deserializacji wiadomości */
    private final ObjectMapper mapper = new ObjectMapper();

    /** Callback wywoływany po otrzymaniu nowej wiadomości */
    private Consumer<Message> onMessageReceived;

    /**
     * Nawiązuje połączenie WebSocket z serwerem czatu.
     * Jeśli istnieje już aktywne połączenie, zostaje ono zamknięte.
     */
    public void connect() {

        manuallyDisconnected = false;

        if (webSocket != null) {
            disconnect();
        }

        HttpClient client = HttpClient.newHttpClient();

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
                                    webSocket.request(1);
                                }

                                @Override
                                public CompletionStage<?> onText(
                                        WebSocket webSocket,
                                        CharSequence data,
                                        boolean last
                                ) {

                                    try {

                                        Message message = mapper.readValue(data.toString(), Message.class);

                                        if (onMessageReceived != null) {
                                            onMessageReceived.accept(message);
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
                                    error.printStackTrace();
                                }

                                @Override
                                public CompletionStage<?> onClose(WebSocket webSocket,
                                                                  int statusCode,
                                                                  String reason) {
                                    System.out.println("WebSocket closed: " + statusCode + " " + reason);
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
     * Wysyła wiadomość przez WebSocket do serwera.
     * @param message wiadomość do wysłania
     */
    public void send(Message message) {
        if (webSocket == null) {
            System.out.println("Brak polaczenia z serwerem");
            return;
        }
        try {

            String json = mapper.writeValueAsString(message);
            webSocket.sendText(json, true);

        } catch (NullPointerException | JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Zamyka aktywne połączenie WebSocket.
     */
    public void disconnect() {
        manuallyDisconnected = true;
        try {

            if (webSocket != null) {

                webSocket.sendClose(
                        WebSocket.NORMAL_CLOSURE, "logout").join();

                webSocket = null;
                System.out.println("WebSocket zamknięty");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void reconnect() {
        if (reconnecting || manuallyDisconnected) {
            return;
        }

        reconnecting = true;

        new Thread(() -> {
            try {
                Thread.sleep(5000);
                System.out.println("Ponowne łączenie...");
                connect();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                reconnecting = false;
            }
        }).start();
    }

    /**
     * Ustawia callback wywoływany po otrzymaniu nowej wiadomości.
     * @param consumer funkcja obsługująca wiadomość
     */
    public void setOnMessageReceived(Consumer<Message> consumer) {
        this.onMessageReceived = consumer;
    }
}