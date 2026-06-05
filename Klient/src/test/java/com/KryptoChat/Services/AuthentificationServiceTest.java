package com.KryptoChat.Services;

import com.KryptoChat.Models.User;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import org.junit.jupiter.api.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testy jednostkowe dla AuthentificationService.
 */
class AuthentificationServiceTest {

    private static WireMockServer wireMock;
    private AuthentificationService service;

    @BeforeAll
    static void startWireMock() {
        wireMock = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMock.start();
        WireMock.configureFor("localhost", wireMock.port());
    }

    @AfterAll
    static void stopWireMock() {
        wireMock.stop();
    }

    @BeforeEach
    void setUp() {
        wireMock.resetAll();
        service = new AuthentificationService("http://localhost:" + wireMock.port());
    }

    @Test
    @DisplayName("checkUser: poprawny token zwraca obiekt User z danymi")
    void checkUser_whenValidToken_returnsUser() {
        // Przygotowanie – serwer zwraca poprawny JSON z danymi użytkownika
        stubFor(get(urlEqualTo("/api/me"))
                .withHeader("Authorization", equalTo("Bearer valid-token"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "username": "testUser",
                                  "groupId": 42
                                }
                                """)));

        User result = service.checkUser("valid-token");

        assertNotNull(result, "Wynik nie powinien być null dla poprawnego tokenu");
        assertEquals("testUser", result.getUsername());
        assertEquals(42, result.getGroupId());
    }

    @Test
    @DisplayName("checkUser: token jest przekazywany w nagłówku Authorization jako Bearer")
    void checkUser_sendsAuthorizationHeader() {
        stubFor(get(urlEqualTo("/api/me"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "username": "alice",
                                  "groupId": 1
                                }
                                """)));

        service.checkUser("my-secret-token");

        // Weryfikacja że nagłówek był poprawnie ustawiony
        verify(getRequestedFor(urlEqualTo("/api/me"))
                .withHeader("Authorization", equalTo("Bearer my-secret-token")));
    }

    @Test
    @DisplayName("checkUser: serwer zwraca 401 – metoda zwraca null")
    void checkUser_whenUnauthorized_returnsNull() {
        stubFor(get(urlEqualTo("/api/me"))
                .willReturn(aResponse()
                        .withStatus(401)
                        .withBody("Unauthorized")));

        User result = service.checkUser("invalid-token");

        assertNull(result, "Wynik powinien być null gdy serwer zwraca 401");
    }

    @Test
    @DisplayName("checkUser: serwer zwraca 500 – metoda zwraca null")
    void checkUser_whenServerError_returnsNull() {
        stubFor(get(urlEqualTo("/api/me"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Internal Server Error")));

        User result = service.checkUser("some-token");

        assertNull(result, "Wynik powinien być null gdy serwer zwraca 500");
    }

    @Test
    @DisplayName("checkUser: serwer zwraca nieprawidłowy JSON – metoda zwraca null")
    void checkUser_whenInvalidJson_returnsNull() {
        stubFor(get(urlEqualTo("/api/me"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("to nie jest JSON %%$#@")));

        User result = service.checkUser("valid-token");

        assertNull(result, "Wynik powinien być null gdy odpowiedź nie jest poprawnym JSON");
    }

    @Test
    @DisplayName("checkUser: serwer zwraca pustą odpowiedź – metoda zwraca null")
    void checkUser_whenEmptyBody_returnsNull() {
        stubFor(get(urlEqualTo("/api/me"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("")));

        User result = service.checkUser("valid-token");

        assertNull(result, "Wynik powinien być null gdy odpowiedź jest pusta");
    }

    @Test
    @DisplayName("checkUser: null jako token – wysyła nagłówek 'Bearer null'")
    void checkUser_whenNullToken_sendsRequest() {
        stubFor(get(urlEqualTo("/api/me"))
                .willReturn(aResponse()
                        .withStatus(401)
                        .withBody("Unauthorized")));

        // Nie rzuca wyjątku – obsłużone przez catch
        User result = service.checkUser(null);

        assertNull(result);
    }

    @Test
    @DisplayName("checkUser: serwer niedostępny – metoda zwraca null bez rzucania wyjątku")
    void checkUser_whenServerDown_returnsNull() {
        // Serwis wskazujący na port bez serwera
        AuthentificationService brokenService =
                new AuthentificationService("http://localhost:1");

        User result = brokenService.checkUser("token");

        assertNull(result, "Wynik powinien być null gdy serwer jest niedostępny");
    }

    @Test
    @DisplayName("checkUser: odpowiedź zawiera tylko username bez groupId – obiekt nadal zwracany")
    void checkUser_whenPartialJson_returnsPartialUser() {
        stubFor(get(urlEqualTo("/api/me"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "username": "partialUser"
                                }
                                """)));

        User result = service.checkUser("valid-token");

        assertNotNull(result);
        assertEquals("partialUser", result.getUsername());
    }
}
