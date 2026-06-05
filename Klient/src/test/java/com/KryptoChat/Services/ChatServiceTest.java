package com.KryptoChat.Services;

import com.KryptoChat.Models.Group;
import com.KryptoChat.Models.User;
import com.KryptoChat.security.TokenStorage;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ChatServiceTest {

    @Test
    void loadGroup_whenUserNotLoggedIn_returnsNull() {

        try (MockedStatic<TokenStorage> tokenStorage = mockStatic(TokenStorage.class)) {

            tokenStorage.when(TokenStorage::getUser).thenReturn(null);

            ChatService service = new ChatService();
            Group result = service.loadGroup();
            assertNull(result);
        }
    }

    @Test
    void loadGroup_whenApiReturns200_returnsGroup() throws Exception {

        HttpClient client = mock(HttpClient.class);
        @SuppressWarnings("unchecked")
        HttpResponse<String> response = mock(HttpResponse.class);

        when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("""
            {
              "groupName":"TestGroup",
              "code":"ABC123"
            }
            """);

        doReturn(response).when(client).send(any(), any());

        try (MockedStatic<TokenStorage> tokenStorage = mockStatic(TokenStorage.class)) {
            User user = new User();

            tokenStorage.when(TokenStorage::getUser).thenReturn(user);

            tokenStorage.when(TokenStorage::getCachedToken).thenReturn("token");

            ChatService service = new ChatService(client);

            Group group = service.loadGroup();

            assertNotNull(group);
            assertEquals("TestGroup", group.getGroupName());
            assertEquals("ABC123", group.getCode());
        }
    }

    @Test
    void loadGroup_whenApiReturns404_returnsNull() throws Exception {

        HttpClient client = mock(HttpClient.class);
        HttpResponse<String> response = mock(HttpResponse.class);

        when(response.statusCode()).thenReturn(404);

        doReturn(response).when(client).send(any(), any());

        try (MockedStatic<TokenStorage> tokenStorage = mockStatic(TokenStorage.class)) {

            tokenStorage.when(TokenStorage::getUser).thenReturn(new User());
            tokenStorage.when(TokenStorage::getCachedToken).thenReturn("token");
            ChatService service = new ChatService(client);
            assertNull(service.loadGroup());
        }
    }

    @Test
    void loadMessages_whenUserNotLoggedIn_returnsNull() {

        try (MockedStatic<TokenStorage> tokenStorage = mockStatic(TokenStorage.class)) {

            tokenStorage.when(TokenStorage::getUser).thenReturn(null);
            ChatService service = new ChatService();
            assertNull(service.loadMessages());
        }
    }

    @Test
    void loadMessages_whenApiReturns200_returnsMessages()
            throws Exception {

        HttpClient client = mock(HttpClient.class);
        HttpResponse<String> response = mock(HttpResponse.class);

        when(response.statusCode()).thenReturn(200);

        when(response.body()).thenReturn("""
            {
              "messages":[
                {
                  "sender":"Alice",
                  "content":"hello"
                },
                {
                  "sender":"Bob",
                  "content":"hi"
                }
              ]
            }
            """);

        doReturn(response)
                .when(client)
                .send(any(), any());

        try (MockedStatic<TokenStorage> tokenStorage = mockStatic(TokenStorage.class)) {

            tokenStorage.when(TokenStorage::getUser).thenReturn(new User());
            tokenStorage.when(TokenStorage::getCachedToken).thenReturn("token");
            ChatService service = new ChatService(client);
            var messages = service.loadMessages();

            assertNotNull(messages);
            assertEquals(2, messages.size());
            assertEquals("Alice", messages.get(0).getSender());
            assertEquals("Bob", messages.get(1).getSender());
        }
    }

    @Test
    void loadMessages_whenExceptionOccurs_returnsNull()
            throws Exception {

        HttpClient client = mock(HttpClient.class);

        when(client.send(any(), any())).thenThrow(new IOException());

        try (MockedStatic<TokenStorage> tokenStorage = mockStatic(TokenStorage.class)) {

            tokenStorage.when(TokenStorage::getUser).thenReturn(new User());
            tokenStorage.when(TokenStorage::getCachedToken).thenReturn("token");
            ChatService service = new ChatService(client);
            assertNull(service.loadMessages());
        }
    }
}