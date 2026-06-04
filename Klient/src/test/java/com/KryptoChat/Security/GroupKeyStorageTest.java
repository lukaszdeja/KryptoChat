package com.KryptoChat.Security;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import com.KryptoChat.security.GroupKeyStorage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class GroupKeyStorageTest {

    private MockedStatic<Files> mockedFiles;
    private final String testUsername = "nacia";

    @BeforeEach
    void setUp() {
        mockedFiles = Mockito.mockStatic(Files.class);
    }

    @AfterEach
    void tearDown() {
        if (mockedFiles != null) {
            mockedFiles.close();
        }
    }

    @Test
    void shouldReturnCorrectPathStructureForGivenUser() {
        Path generatedPath = GroupKeyStorage.getPath("testUser");

        assertNotNull(generatedPath);
        assertTrue(generatedPath.toString().contains(".KryptoChatapp"));
        assertTrue(generatedPath.toString().contains("keys"));
        assertTrue(generatedPath.toString().contains("testUser"));
        assertTrue(generatedPath.toString().endsWith("group.key"));
    }

    @Test
    void shouldSaveSecretKeyByEncodingItToBase64() throws Exception {
        byte[] rawKeyBytes = new byte[16];
        SecretKey secretKey = new SecretKeySpec(rawKeyBytes, "AES");
        byte[] expectedBase64Bytes = Base64.getEncoder().encode(rawKeyBytes);

        mockedFiles.when(() -> Files.createDirectories(Mockito.any(Path.class))).thenReturn(null);
        mockedFiles.when(() -> Files.write(Mockito.any(Path.class), Mockito.any(byte[].class))).thenReturn(null);

        GroupKeyStorage.save(testUsername, secretKey);

        mockedFiles.verify(() -> Files.write(
                Mockito.eq(GroupKeyStorage.getPath(testUsername)),
                Mockito.eq(expectedBase64Bytes)
        ));
    }

    @Test
    void shouldLoadAndReconstructSecretKeySuccessfully() throws Exception {
        byte[] rawKeyBytes = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
        byte[] base64EncodedBytes = Base64.getEncoder().encode(rawKeyBytes);

        mockedFiles.when(() -> Files.exists(Mockito.any(Path.class))).thenReturn(true);
        mockedFiles.when(() -> Files.readAllBytes(Mockito.any(Path.class))).thenReturn(base64EncodedBytes);

        SecretKey loadedKey = GroupKeyStorage.load(testUsername);

        assertNotNull(loadedKey);
        assertEquals("AES", loadedKey.getAlgorithm());
        assertArrayEquals(rawKeyBytes, loadedKey.getEncoded());
    }

    @Test
    void shouldReturnNullWhenLoadingKeyAndFileDoesNotExist() throws Exception {
        mockedFiles.when(() -> Files.exists(Mockito.any(Path.class))).thenReturn(false);

        SecretKey loadedKey = GroupKeyStorage.load(testUsername);

        assertNull(loadedKey);
    }

    @Test
    void shouldReturnTrueWhenKeyFileExists() {
        mockedFiles.when(() -> Files.exists(GroupKeyStorage.getPath(testUsername))).thenReturn(true);

        boolean exists = GroupKeyStorage.exists(testUsername);

        assertTrue(exists);
    }

    @Test
    void shouldReturnFalseWhenKeyFileDoesNotExist() {
        mockedFiles.when(() -> Files.exists(GroupKeyStorage.getPath(testUsername))).thenReturn(false);

        boolean exists = GroupKeyStorage.exists(testUsername);

        assertFalse(exists);
    }
}
