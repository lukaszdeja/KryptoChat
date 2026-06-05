package com.KryptoChat.Services;

import com.KryptoChat.Models.User;
import com.KryptoChat.security.TokenStorage;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.KryptoChat.security.*;

class LoginServiceTest {

    @Test
    void login_wrongPassword_returns401Message() throws Exception {

        HttpClient client = mock(HttpClient.class);
        HttpResponse<String> response = mock(HttpResponse.class);

        when(response.statusCode()).thenReturn(500);

        when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn((HttpResponse) response);

        try (MockedStatic<TokenStorage> ts = mockStatic(TokenStorage.class)) {

            LoginService service = new LoginService(client);

            ServiceResponse result = service.login("user", "pass");

            assertFalse(result.isSuccess());
            assertEquals("Niepoprawny login lub hasło", result.getMessage());
        }
    }

    @Test
    void login_missingTokenNode_returnsError() throws Exception {

        HttpClient client = mock(HttpClient.class);
        HttpResponse<String> response = mock(HttpResponse.class);

        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("""
        {
          "jwt": "token"
        }
        """);

        when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn((HttpResponse) response);

        LoginService service = new LoginService(client);

        ServiceResponse result = service.login("user", "pass");

        assertFalse(result.isSuccess());
        assertEquals("Błąd serwera - brak tokenu", result.getMessage());
    }

    @Test
    void login_otherError_returnsServerError() throws Exception {

        HttpClient client = mock(HttpClient.class);
        HttpResponse<String> response = mock(HttpResponse.class);

        when(response.statusCode()).thenReturn(404);

        when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        LoginService service = new LoginService(client);

        ServiceResponse result = service.login("user", "pass");

        assertFalse(result.isSuccess());
        assertEquals("Błąd serwera", result.getMessage());
    }

    @Test
    void login_exception_returnsConnectionError() throws Exception {

        HttpClient client = mock(HttpClient.class);

        when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenThrow(new IOException("fail"));

        LoginService service = new LoginService(client);
        ServiceResponse result = service.login("user", "pass");

        assertFalse(result.isSuccess());
        assertEquals("Brak połączenia z serwerem", result.getMessage());
    }
}