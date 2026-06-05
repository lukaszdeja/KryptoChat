package com.KryptoChat.Services;

import com.KryptoChat.Models.Message;
import com.KryptoChat.Models.User;
import com.KryptoChat.security.GroupKeyStorage;
import com.KryptoChat.security.TokenStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.SecretKey;
import java.lang.reflect.Field;
import java.net.http.WebSocket;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSocketServiceTest {

    @TempDir
    Path tempDir;

    private WebSocketService service;

    @BeforeEach
    void setUp() {
        service = new WebSocketService();
        System.setProperty("user.home", tempDir.toString());
    }

    private void setField(String name, Object value) throws Exception {
        Field f = WebSocketService.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(service, value);
    }

    private Object getField(String name) throws Exception {
        Field f = WebSocketService.class.getDeclaredField(name);
        f.setAccessible(true);
        return f.get(service);
    }

    private WebSocket mockWebSocket() {
        WebSocket ws = mock(WebSocket.class);
        when(ws.sendClose(anyInt(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(ws));
        return ws;
    }

    @Test
    @DisplayName("Nowy obiekt: webSocket jest null")
    void initialState_WebSocketIsNull() throws Exception {
        assertThat(getField("webSocket")).isNull();
    }

    @Test
    @DisplayName("Nowy obiekt: manuallyDisconnected jest false")
    void initialState_ManuallyDisconnectedIsFalse() throws Exception {
        assertThat(getField("manuallyDisconnected")).isEqualTo(false);
    }

    @Test
    @DisplayName("Nowy obiekt: reconnecting jest false")
    void initialState_ReconnectingIsFalse() throws Exception {
        assertThat(getField("reconnecting")).isEqualTo(false);
    }

    @Test
    @DisplayName("setOnMessageReceived rejestruje callback")
    void setOnMessageReceived_RegistersCallback() throws Exception {
        Consumer<Message> consumer = msg -> {};
        service.setOnMessageReceived(consumer);
        assertThat(getField("onMessageReceived")).isSameAs(consumer);
    }

    @Test
    @DisplayName("setOnMessageReceived nadpisuje poprzedni callback")
    void setOnMessageReceived_OverwritesPreviousCallback() throws Exception {
        Consumer<Message> first = msg -> {};
        Consumer<Message> second = msg -> {};
        service.setOnMessageReceived(first);
        service.setOnMessageReceived(second);
        assertThat(getField("onMessageReceived")).isSameAs(second);
    }

    @Test
    @DisplayName("setOnKeyReceived rejestruje callback")
    void setOnKeyReceived_RegistersCallback() throws Exception {
        Runnable r = () -> {};
        service.setOnKeyReceived(r);
        assertThat(getField("onKeyReceived")).isSameAs(r);
    }

    @Test
    @DisplayName("setOnKeyReceived nadpisuje poprzedni callback")
    void setOnKeyReceived_OverwritesPreviousCallback() throws Exception {
        Runnable first = () -> {};
        Runnable second = () -> {};
        service.setOnKeyReceived(first);
        service.setOnKeyReceived(second);
        assertThat(getField("onKeyReceived")).isSameAs(second);
    }

    @Test
    @DisplayName("disconnect ustawia manuallyDisconnected na true")
    void disconnect_SetsManuallyDisconnectedTrue() throws Exception {
        service.disconnect();
        assertThat(getField("manuallyDisconnected")).isEqualTo(true);
    }

    @Test
    @DisplayName("disconnect zeruje webSocket na null")
    void disconnect_SetsWebSocketToNull() throws Exception {
        setField("webSocket", mockWebSocket());
        service.disconnect();
        assertThat(getField("webSocket")).isNull();
    }

    @Test
    @DisplayName("disconnect wywołuje sendClose(NORMAL_CLOSURE, 'logout')")
    void disconnect_CallsSendCloseWithNormalClosure() throws Exception {
        WebSocket ws = mockWebSocket();
        setField("webSocket", ws);
        service.disconnect();
        verify(ws).sendClose(WebSocket.NORMAL_CLOSURE, "logout");
    }

    @Test
    @DisplayName("disconnect nie rzuca wyjątku gdy webSocket jest null")
    void disconnect_WebSocketNull_DoesNotThrow() {
        assertThatCode(() -> service.disconnect()).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("disconnect obsługuje wyjątek z sendClose bez propagowania")
    void disconnect_SendCloseThrows_DoesNotPropagate() throws Exception {
        WebSocket ws = mock(WebSocket.class);
        when(ws.sendClose(anyInt(), anyString())).thenThrow(new RuntimeException("reset"));
        setField("webSocket", ws);
        assertThatCode(() -> service.disconnect()).doesNotThrowAnyException();
        assertThat(getField("webSocket")).isNull();
    }

    @Test
    @DisplayName("disconnect blokuje reconnect (manuallyDisconnected = true)")
    void disconnect_BlocksReconnect() throws Exception {
        service.disconnect();
        setField("webSocket", null);
        java.lang.reflect.Method reconnect = WebSocketService.class.getDeclaredMethod("reconnect");
        reconnect.setAccessible(true);
        reconnect.invoke(service);
        assertThat(getField("reconnecting")).isEqualTo(false);
    }

    @Test
    @DisplayName("send nie rzuca wyjątku gdy webSocket jest null")
    void send_WebSocketNull_DoesNotThrow() {
        Message msg = new Message("sender", "hello");
        msg.setGroupId(1L);
        assertThatCode(() -> service.send(msg)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("send nie próbuje wysłać gdy webSocket jest null")
    void send_WebSocketNull_NoSendTextCalled() throws Exception {
        service.send(new Message("sender", "hello"));
        assertThat(getField("webSocket")).isNull();
    }

    @Test
    @DisplayName("send szyfruje i wysyła wiadomość gdy webSocket i klucz grupy istnieją")
    void send_ValidWebSocketAndGroupKey_SendsEncryptedMessage() throws Exception {
        WebSocket ws = mock(WebSocket.class);
        when(ws.sendText(anyString(), eq(true)))
                .thenReturn(CompletableFuture.completedFuture(ws));
        setField("webSocket", ws);

        User user = new User();
        user.setUsername("alice");

        SecretKey aesKey = CryptoService.generateAESKey();
        GroupKeyStorage.save("alice", aesKey);

        Message msg = new Message("alice", "Tajne!");
        msg.setGroupId(10L);

        try (MockedStatic<TokenStorage> ts = mockStatic(TokenStorage.class)) {
            ts.when(TokenStorage::getUser).thenReturn(user);

            service.send(msg);
        }

        verify(ws, times(1)).sendText(anyString(), eq(true));
    }

    @Test
    @DisplayName("send nie wysyła wiadomości gdy klucz grupy nie istnieje")
    void send_NoGroupKey_DoesNotSendText() throws Exception {
        WebSocket ws = mock(WebSocket.class);
        setField("webSocket", ws);

        User user = new User();
        user.setUsername("nokey_user");

        Message msg = new Message("nokey_user", "test");
        msg.setGroupId(1L);

        try (MockedStatic<TokenStorage> ts = mockStatic(TokenStorage.class);
             MockedStatic<GroupKeyStorage> gks = mockStatic(GroupKeyStorage.class)) {
            ts.when(TokenStorage::getUser).thenReturn(user);
            gks.when(() -> GroupKeyStorage.exists("nokey_user")).thenReturn(false);

            service.send(msg);
        }

        verify(ws, never()).sendText(anyString(), anyBoolean());
    }

    @Test
    @DisplayName("send wysyła JSON zawierający typ CHAT i groupId")
    void send_ValidState_SendsJsonWithTypeChatAndGroupId() throws Exception {
        WebSocket ws = mock(WebSocket.class);
        AtomicReference<String> sentJson = new AtomicReference<>();
        when(ws.sendText(anyString(), eq(true))).thenAnswer(inv -> {
            sentJson.set(inv.getArgument(0));
            return CompletableFuture.completedFuture(ws);
        });
        setField("webSocket", ws);

        User user = new User();
        user.setUsername("sender");
        SecretKey aesKey = CryptoService.generateAESKey();
        GroupKeyStorage.save("sender", aesKey);

        Message msg = new Message("sender", "Wiadomość testowa");
        msg.setGroupId(42L);

        try (MockedStatic<TokenStorage> ts = mockStatic(TokenStorage.class)) {
            ts.when(TokenStorage::getUser).thenReturn(user);
            service.send(msg);
        }

        assertThat(sentJson.get()).contains("\"type\":\"CHAT\"");
        assertThat(sentJson.get()).contains("\"groupId\":42");
    }

    @Test
    @DisplayName("reconnect nie uruchamia wątku gdy manuallyDisconnected=true")
    void reconnect_ManuallyDisconnected_DoesNotSetReconnecting() throws Exception {
        setField("manuallyDisconnected", true);

        java.lang.reflect.Method reconnect = WebSocketService.class.getDeclaredMethod("reconnect");
        reconnect.setAccessible(true);
        reconnect.invoke(service);

        assertThat(getField("reconnecting")).isEqualTo(false);
    }

    @Test
    @DisplayName("reconnect nie uruchamia wątku gdy reconnecting=true")
    void reconnect_AlreadyReconnecting_DoesNotStartAnotherThread() throws Exception {
        setField("reconnecting", true);

        java.lang.reflect.Method reconnect = WebSocketService.class.getDeclaredMethod("reconnect");
        reconnect.setAccessible(true);
        reconnect.invoke(service);

        assertThat(getField("reconnecting")).isEqualTo(true);
    }
}