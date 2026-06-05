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
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.KryptoChat.security.*;

class RegisterServiceTest {
    @Test
    void register_success_returnsOk() throws Exception {

        HttpClient client = mock(HttpClient.class);
        HttpResponse<String> response = mock(HttpResponse.class);

        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("{}");

        when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(response);

        try (MockedStatic<CryptoService> cs = mockStatic(CryptoService.class)) {

            KeyPair kp = mock(KeyPair.class);
            PublicKey pub = mock(PublicKey.class);
            PrivateKey priv = mock(PrivateKey.class);

            when(kp.getPublic()).thenReturn(pub);
            when(kp.getPrivate()).thenReturn(priv);
            when(pub.getEncoded()).thenReturn("pub".getBytes());

            cs.when(() -> CryptoService.generateKeysIfNeeded("user")).thenReturn(kp);
            cs.when(() -> CryptoService.encryptPrivateKeyWithPassword(any(), any()))
                    .thenReturn("encrypted");

            RegisterService service = new RegisterService(client);

            ServiceResponse result = service.register("user", "pass", "pass");

            assertTrue(result.isSuccess());
            assertEquals("Utworzono konto", result.getMessage());
        }
    }

    @Test
    void register_userExists_returnsError() throws Exception {

        HttpClient client = mock(HttpClient.class);
        HttpResponse<String> response = mock(HttpResponse.class);

        when(response.statusCode()).thenReturn(500);

        when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(response);

        try (MockedStatic<CryptoService> cs = mockStatic(CryptoService.class)) {

            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048);
            KeyPair kp = gen.generateKeyPair();

            cs.when(() -> CryptoService.generateKeysIfNeeded("user"))
                    .thenReturn(kp);

            cs.when(() -> CryptoService.encryptPrivateKeyWithPassword(any(), any()))
                    .thenReturn("encrypted");

            RegisterService service = new RegisterService(client);

            ServiceResponse result =
                    service.register("user", "pass", "pass");

            assertFalse(result.isSuccess());
            assertEquals("Użytkownik już istnieje", result.getMessage());
        }
    }

    @Test
    void register_otherError_returnsGenericError() throws Exception {

        HttpClient client = mock(HttpClient.class);
        HttpResponse<String> response = mock(HttpResponse.class);

        when(response.statusCode()).thenReturn(404);

        when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(response);

        try (MockedStatic<CryptoService> cs = mockStatic(CryptoService.class)) {

            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048);
            KeyPair kp = gen.generateKeyPair();

            cs.when(() -> CryptoService.generateKeysIfNeeded("user"))
                    .thenReturn(kp);

            cs.when(() -> CryptoService.encryptPrivateKeyWithPassword(any(), any()))
                    .thenReturn("encrypted");

            RegisterService service = new RegisterService(client);

            ServiceResponse result =
                    service.register("user", "pass", "pass");

            assertFalse(result.isSuccess());
            assertEquals("Błąd rejestracji", result.getMessage());
        }
    }

    @Test
    void register_exception_returnsConnectionError() throws Exception {

        HttpClient client = mock(HttpClient.class);

        when(client.send(any(), any()))
                .thenThrow(new IOException("fail"));

        try (MockedStatic<CryptoService> cs = mockStatic(CryptoService.class)) {

            KeyPair kp = mock(KeyPair.class);

            cs.when(() -> CryptoService.generateKeysIfNeeded("user")).thenReturn(kp);

            RegisterService service = new RegisterService(client);

            ServiceResponse result =
                    service.register("user", "pass", "pass");

            assertFalse(result.isSuccess());
            assertEquals("Brak połączenia z serwerem", result.getMessage());
        }
    }

}