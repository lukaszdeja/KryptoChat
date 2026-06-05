package com.KryptoChat.Services;

import com.KryptoChat.Models.User;
import com.KryptoChat.security.GroupKeyStorage;
import com.KryptoChat.security.TokenStorage;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import javax.crypto.SecretKey;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.PublicKey;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GroupServiceTest {

    @Test
    void createGroup_success_returnsSuccessResponse() throws Exception {

        HttpClient client = mock(HttpClient.class);
        HttpResponse<String> response = mock(HttpResponse.class);

        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("""
            {
              "jwt": "new-token",
              "userCredentials": {
                "groupId": 123
              }
            }
        """);

        when(client.send(
                any(HttpRequest.class),
                any(HttpResponse.BodyHandler.class)
        )).thenReturn((HttpResponse) response);

        SecretKey key = mock(SecretKey.class);
        when(key.getEncoded()).thenReturn("key".getBytes());

        PublicKey publicKey = mock(PublicKey.class);

        try (
                MockedStatic<TokenStorage> ts = mockStatic(TokenStorage.class);
                MockedStatic<CryptoService> cs = mockStatic(CryptoService.class);
                MockedStatic<GroupKeyStorage> gks = mockStatic(GroupKeyStorage.class)
        ) {

            User user = new User();
            user.setUsername("test");

            ts.when(TokenStorage::getUser).thenReturn(user);
            ts.when(TokenStorage::getCachedToken).thenReturn("token");

            cs.when(() -> CryptoService.generateAESKey()).thenReturn(key);
            cs.when(() -> CryptoService.getPublicKey("test")).thenReturn(publicKey);
            cs.when(() -> CryptoService.encryptRSA(any(), any())).thenReturn("encrypted");

            GroupService service = new GroupService(client);

            ServiceResponse result = service.createGroup("group");

            assertNotNull(result);
            assertTrue(result.isSuccess());
        }
    }

    @Test
    void joinGroup_success_returnsSuccess() throws Exception {

        HttpClient client = mock(HttpClient.class);
        HttpResponse<String> response = mock(HttpResponse.class);

        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("""
            {
              "jwt": "token",
              "userCredentials": {
                "groupId": 10
              }
            }
        """);

        when(client.send(
                any(HttpRequest.class),
                any(HttpResponse.BodyHandler.class)
        )).thenReturn((HttpResponse) response);

        try (MockedStatic<TokenStorage> ts = mockStatic(TokenStorage.class)) {

            User user = new User();
            user.setUsername("test");

            ts.when(TokenStorage::getUser).thenReturn(user);
            ts.when(TokenStorage::getCachedToken).thenReturn("token");

            GroupService service = new GroupService(client);

            ServiceResponse result = service.joinGroup("ABC");

            assertTrue(result.isSuccess());
            assertEquals("Dołączono do grupy", result.getMessage());
        }
    }

    @Test
    void joinGroup_groupNotFound_returnsFalse() throws Exception {

        HttpClient client = mock(HttpClient.class);
        HttpResponse<String> response = mock(HttpResponse.class);

        when(response.statusCode()).thenReturn(500);
        when(response.body()).thenReturn("{}");

        when(client.send(
                any(HttpRequest.class),
                any(HttpResponse.BodyHandler.class)
        )).thenReturn((HttpResponse) response);

        try (MockedStatic<TokenStorage> ts = mockStatic(TokenStorage.class)) {

            User user = new User();
            user.setUsername("test");

            ts.when(TokenStorage::getUser).thenReturn(user);
            ts.when(TokenStorage::getCachedToken).thenReturn("token");

            GroupService service = new GroupService(client);

            ServiceResponse result = service.joinGroup("ABC");

            assertFalse(result.isSuccess());
            assertEquals("Nie znaleziono grupy", result.getMessage());
        }
    }

    @Test
    void saveResponse_nullGroupId_returnsFalseFlow() throws Exception {

        HttpClient client = mock(HttpClient.class);
        HttpResponse<String> response = mock(HttpResponse.class);

        when(response.body()).thenReturn("""
            {
              "jwt": "token",
              "userCredentials": {
                "groupId": null
              }
            }
        """);

        try (MockedStatic<TokenStorage> ts = mockStatic(TokenStorage.class)) {

            ts.when(TokenStorage::getUser).thenReturn(new User());

            GroupService service = new GroupService(client);

            ServiceResponse result = service.joinGroup("ABC");

            assertFalse(result.isSuccess());
        }
    }

    @Test
    void exception_returnsFailureResponse() {

        HttpClient client = mock(HttpClient.class);

        try (MockedStatic<TokenStorage> ts = mockStatic(TokenStorage.class)) {

            User user = new User();
            user.setUsername("test");

            ts.when(TokenStorage::getUser).thenReturn(user);
            ts.when(TokenStorage::getCachedToken).thenReturn("token");

            GroupService service = new GroupService(client);

            ServiceResponse result = service.createGroup("test");

            assertFalse(result.isSuccess());
            assertEquals("Brak połączenia z serwerem", result.getMessage());
        }
    }
}