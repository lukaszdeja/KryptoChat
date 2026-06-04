package com.KryptoChat.Security;

import com.KryptoChat.Models.User;
import com.KryptoChat.security.TokenStorage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class TokenStorageTest {

    @TempDir
    Path tempDir;

    private Path tempFile;
    private MockedStatic<Files> mockedFiles;

    @BeforeEach
    void setUp() {
        tempFile = tempDir.resolve("token_test.dat");
        TokenStorage.setCachedToken(null);
        TokenStorage.setUser(null);
        mockedFiles = Mockito.mockStatic(Files.class);
    }

    @AfterEach
    void tearDown() {
        if (mockedFiles != null) {
            mockedFiles.close();
        }
        TokenStorage.setCachedToken(null);
        TokenStorage.setUser(null);
    }

    @Test
    @DisplayName("saveToken zapisuje token do cache i pliku")
    void shouldSaveTokenToCacheAndFileSuccessfully() {
        String testToken = "sample.jwt.token";
        mockedFiles.when(() -> Files.createDirectories(Mockito.any(Path.class))).thenReturn(null);
        mockedFiles.when(() -> Files.writeString(Mockito.any(Path.class), Mockito.anyString())).thenReturn(null);

        TokenStorage.saveToken(testToken);

        assertEquals(testToken, TokenStorage.getCachedToken());
        mockedFiles.verify(() -> Files.writeString(Mockito.any(Path.class), Mockito.eq(testToken)));
    }

    @Test
    @DisplayName("loadUser wczytuje token z pliku gdy plik istnieje")
    void shouldLoadTokenFromFileWhenFileExists() throws Exception {
        String expectedToken = "loaded.jwt.token";
        mockedFiles.when(() -> Files.exists(Mockito.any(Path.class))).thenReturn(true);
        mockedFiles.when(() -> Files.readString(Mockito.any(Path.class))).thenReturn(expectedToken);

        String loadedToken = TokenStorage.loadUser();

        assertEquals(expectedToken, loadedToken);
        assertEquals(expectedToken, TokenStorage.getCachedToken());
    }

    @Test
    @DisplayName("loadUser zwraca null gdy plik nie istnieje")
    void shouldReturnNullWhenTokenFileDoesNotExist() {
        mockedFiles.when(() -> Files.exists(Mockito.any(Path.class))).thenReturn(false);

        String loadedToken = TokenStorage.loadUser();

        assertNull(loadedToken);
        assertNull(TokenStorage.getCachedToken());
    }

    @Test
    @DisplayName("deleteToken usuwa plik i czyści cache")
    void shouldDeleteTokenFromFileAndClearCache() {
        TokenStorage.setCachedToken("delete.me.token");
        TokenStorage.setUser(new User());
        mockedFiles.when(() -> Files.deleteIfExists(Mockito.any(Path.class))).thenReturn(true);

        TokenStorage.deleteToken();

        assertNull(TokenStorage.getCachedToken());
        assertNull(TokenStorage.getUser());
    }

    @Test
    @DisplayName("setUser i getUser przechowują użytkownika")
    void shouldGetAndSetUserInCache() {
        User user = new User();
        user.setUsername("TestUser");

        TokenStorage.setUser(user);

        assertNotNull(TokenStorage.getUser());
        assertEquals("TestUser", TokenStorage.getUser().getUsername());
    }

    @Test
    @DisplayName("getCachedToken zwraca null przed ustawieniem tokenu")
    void shouldReturnNullWhenTokenNotSet() {
        assertNull(TokenStorage.getCachedToken());
    }

    @Test
    @DisplayName("setCachedToken ustawia token bez zapisu do pliku")
    void shouldSetCachedTokenWithoutWritingFile() {
        TokenStorage.setCachedToken("in-memory-only");

        assertEquals("in-memory-only", TokenStorage.getCachedToken());
        mockedFiles.verify(() -> Files.writeString(Mockito.any(Path.class), Mockito.anyString()), Mockito.never());
    }

    @Test
    @DisplayName("setCachedToken nadpisuje poprzedni token")
    void shouldOverwritePreviousCachedToken() {
        TokenStorage.setCachedToken("first-token");
        TokenStorage.setCachedToken("second-token");

        assertEquals("second-token", TokenStorage.getCachedToken());
    }

    @Test
    @DisplayName("getUser zwraca null przed ustawieniem użytkownika")
    void shouldReturnNullUserWhenNotSet() {
        assertNull(TokenStorage.getUser());
    }

    @Test
    @DisplayName("setUser nadpisuje poprzedniego użytkownika")
    void shouldOverwritePreviousUser() {
        User first = new User();
        first.setUsername("First");
        User second = new User();
        second.setUsername("Second");

        TokenStorage.setUser(first);
        TokenStorage.setUser(second);

        assertEquals("Second", TokenStorage.getUser().getUsername());
    }

    @Test
    @DisplayName("deleteToken czyści cachedToken gdy plik nie istnieje")
    void shouldClearCacheEvenWhenFileDoesNotExist() {
        TokenStorage.setCachedToken("some-token");
        mockedFiles.when(() -> Files.deleteIfExists(Mockito.any(Path.class))).thenReturn(false);

        TokenStorage.deleteToken();

        assertNull(TokenStorage.getCachedToken());
    }

    @Test
    @DisplayName("saveToken wywołuje createDirectories przed zapisem")
    void shouldCreateDirectoriesBeforeWritingFile() {
        mockedFiles.when(() -> Files.createDirectories(Mockito.any(Path.class))).thenReturn(null);
        mockedFiles.when(() -> Files.writeString(Mockito.any(Path.class), Mockito.anyString())).thenReturn(null);

        TokenStorage.saveToken("token");

        mockedFiles.verify(() -> Files.createDirectories(Mockito.any(Path.class)));
    }
}