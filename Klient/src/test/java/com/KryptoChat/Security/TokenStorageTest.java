package com.KryptoChat.Security;

import com.KryptoChat.Models.User;
import com.KryptoChat.security.TokenStorage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
        // Przygotowujemy ścieżkę do tymczasowego pliku testowego
        tempFile = tempDir.resolve("token_test.dat");

        // Resetujemy stan pamięci podręcznej przed każdym testem
        TokenStorage.setCachedToken(null);
        TokenStorage.setUser(null);

        // Tworzymy statyczny mock dla klasy Files, aby przechwytywać odwołania do plików
        mockedFiles = Mockito.mockStatic(Files.class);
    }

    @AfterEach
    void tearDown() {
        // Zamykamy mockowanie statyczne po każdym teście (bardzo ważne!)
        if (mockedFiles != null) {
            mockedFiles.close();
        }
        TokenStorage.setCachedToken(null);
        TokenStorage.setUser(null);
    }

    @Test
    void shouldSaveTokenToCacheAndFileSuccessfully() {
        String testToken = "sample.jwt.token";

        // Symulujemy, że operacje na katalogach i pliku zakończą się sukcesem
        mockedFiles.when(() -> Files.createDirectories(Mockito.any(Path.class))).thenReturn(null);
        mockedFiles.when(() -> Files.writeString(Mockito.any(Path.class), Mockito.anyString())).thenReturn(null);

        TokenStorage.saveToken(testToken);

        assertEquals(testToken, TokenStorage.getCachedToken(), "Token powinien zostać zapisany w cache");

        // Weryfikujemy, czy klasa Files została faktycznie wywołana z poprawnym tokenem
        mockedFiles.verify(() -> Files.writeString(Mockito.any(Path.class), Mockito.eq(testToken)));
    }

    @Test
    void shouldLoadTokenFromFileWhenFileExists() throws Exception {
        String expectedToken = "loaded.jwt.token";

        // Symulujemy, że plik istnieje i zwraca konkretny token
        mockedFiles.when(() -> Files.exists(Mockito.any(Path.class))).thenReturn(true);
        mockedFiles.when(() -> Files.readString(Mockito.any(Path.class))).thenReturn(expectedToken);

        String loadedToken = TokenStorage.loadUser();

        assertEquals(expectedToken, loadedToken, "Metoda powinna zwrócić poprawny token");
        assertEquals(expectedToken, TokenStorage.getCachedToken(), "Token powinien trafić do cache");
    }

    @Test
    void shouldReturnNullWhenTokenFileDoesNotExist() {
        mockedFiles.when(() -> Files.exists(Mockito.any(Path.class))).thenReturn(false);

        String loadedToken = TokenStorage.loadUser();

        assertNull(loadedToken, "Metoda powinna zwrócić null, jeśli plik nie istnieje");
        assertNull(TokenStorage.getCachedToken(), "Cache tokenu powinien pozostać pusty");
    }

    @Test
    void shouldDeleteTokenFromFileAndClearCache() {
        TokenStorage.setCachedToken("delete.me.token");
        TokenStorage.setUser(new User());
        mockedFiles.when(() -> Files.deleteIfExists(Mockito.any(Path.class))).thenReturn(true);

        TokenStorage.deleteToken();

        assertNull(TokenStorage.getCachedToken(), "Token w pamięci powinien zostać wyczyszczony");
        assertNull(TokenStorage.getUser(), "Użytkownik w pamięci powinien zostać wyczyszczony");
    }

    @Test
    void shouldGetAndSetUserInCache() {
        User user = new User();
        user.setUsername("TestUser");

        TokenStorage.setUser(user);
        User retrievedUser = TokenStorage.getUser();

        assertNotNull(retrievedUser);
        assertEquals("TestUser", retrievedUser.getUsername());
    }
}