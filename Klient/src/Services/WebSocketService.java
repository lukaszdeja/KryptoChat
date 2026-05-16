package Services;

import com.fasterxml.jackson.databind.ObjectMapper;
import Models.Message;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import security.TokenStorage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

public class WebSocketService {

    private WebSocket webSocket;

    private final ObjectMapper mapper = new ObjectMapper();

    private Consumer<Message> onMessageReceived;

    public void connect() {

        HttpClient client = HttpClient.newHttpClient();
        mapper.registerModule(new JavaTimeModule());
        try {
            client.newWebSocketBuilder().buildAsync(URI.create("wss://kryptochatserwer-production.up.railway.app/ws?userid=" + TokenStorage.getUser().getId()),
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

                                        Message message = mapper.readValue(
                                                data.toString(),
                                                Message.class
                                        );

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
                                    error.printStackTrace();
                                }
                            }
                    )
                    .thenAccept(ws -> this.webSocket = ws);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void send(Message message) {

        try {

            String json = mapper.writeValueAsString(message);

            webSocket.sendText(json, true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setOnMessageReceived(Consumer<Message> consumer) {
        this.onMessageReceived = consumer;
    }
}