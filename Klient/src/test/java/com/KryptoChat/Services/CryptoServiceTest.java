package com.KryptoChat.Services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.crypto.SecretKey;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CryptoServiceTest {

    @Test
    @DisplayName("generateAESKey zwraca klucz AES 256-bitowy")
    void generateAESKey_Returns256BitAESKey() throws Exception {
        SecretKey key = CryptoService.generateAESKey();

        assertThat(key).isNotNull();
        assertThat(key.getAlgorithm()).isEqualTo("AES");
        assertThat(key.getEncoded().length).isEqualTo(32); // 256 bitów = 32 bajty
    }

    @Test
    @DisplayName("generateAESKey za każdym razem generuje inny klucz")
    void generateAESKey_GeneratesUniqueKeys() throws Exception {
        SecretKey key1 = CryptoService.generateAESKey();
        SecretKey key2 = CryptoService.generateAESKey();

        assertThat(key1.getEncoded()).isNotEqualTo(key2.getEncoded());
    }

    // encryptAES / decryptAES

    @Test
    @DisplayName("encryptAES zwraca zaszyfrowany tekst różny od oryginału")
    void encryptAES_ReturnsEncryptedTextDifferentFromOriginal() throws Exception {
        SecretKey key = CryptoService.generateAESKey();
        String plaintext = "Hello KryptoChat!";

        String encrypted = CryptoService.encryptAES(plaintext, key);

        assertThat(encrypted).isNotEqualTo(plaintext);
        assertThat(encrypted).isNotBlank();
    }

    @Test
    @DisplayName("decryptAES odszyfrowuje tekst zaszyfrowany przez encryptAES")
    void decryptAES_DecryptsTextEncryptedByEncryptAES() throws Exception {
        SecretKey key = CryptoService.generateAESKey();
        String plaintext = "Tajna wiadomość 123!";

        String encrypted = CryptoService.encryptAES(plaintext, key);
        String decrypted = CryptoService.decryptAES(encrypted, key);

        assertThat(decrypted).isEqualTo(plaintext);
    }

    @Test
    @DisplayName("encryptAES zwraca różne szyfrogramy dla tego samego tekstu (losowy IV)")
    void encryptAES_SamePlaintextProducesDifferentCiphertexts() throws Exception {
        SecretKey key = CryptoService.generateAESKey();
        String plaintext = "test";

        String encrypted1 = CryptoService.encryptAES(plaintext, key);
        String encrypted2 = CryptoService.encryptAES(plaintext, key);

        assertThat(encrypted1).isNotEqualTo(encrypted2);
    }

    @Test
    @DisplayName("decryptAES z błędnym kluczem rzuca wyjątek")
    void decryptAES_WrongKey_ThrowsException() throws Exception {
        SecretKey correctKey = CryptoService.generateAESKey();
        SecretKey wrongKey = CryptoService.generateAESKey();
        String encrypted = CryptoService.encryptAES("sekret", correctKey);

        assertThrows(Exception.class, () -> CryptoService.decryptAES(encrypted, wrongKey));
    }

    @Test
    @DisplayName("encryptAES i decryptAES obsługują pusty ciąg znaków")
    void encryptDecryptAES_EmptyString_WorksCorrectly() throws Exception {
        SecretKey key = CryptoService.generateAESKey();

        String encrypted = CryptoService.encryptAES("", key);
        String decrypted = CryptoService.decryptAES(encrypted, key);

        assertThat(decrypted).isEqualTo("");
    }

    @Test
    @DisplayName("encryptAES i decryptAES obsługują długi tekst")
    void encryptDecryptAES_LongText_WorksCorrectly() throws Exception {
        SecretKey key = CryptoService.generateAESKey();
        String longText = "A".repeat(10_000);

        String encrypted = CryptoService.encryptAES(longText, key);
        String decrypted = CryptoService.decryptAES(encrypted, key);

        assertThat(decrypted).isEqualTo(longText);
    }

    @Test
    @DisplayName("encryptAES i decryptAES obsługują znaki specjalne i Unicode")
    void encryptDecryptAES_UnicodeChars_WorksCorrectly() throws Exception {
        SecretKey key = CryptoService.generateAESKey();
        String text = "Zażółć gęślą jaźń 🔐 !@#$%";

        String decrypted = CryptoService.decryptAES(CryptoService.encryptAES(text, key), key);

        assertThat(decrypted).isEqualTo(text);
    }

    @Test
    @DisplayName("generateKeysIfNeeded zwraca KeyPair gdy klucze nie istnieją")
    void generateKeysIfNeeded_NoExistingKeys_ReturnsKeyPair(@TempDir Path tempDir) throws Exception {
        String username = "testuser_new";
        // Nadpisujemy user.home na katalog tymczasowy
        System.setProperty("user.home", tempDir.toString());

        KeyPair pair = CryptoService.generateKeysIfNeeded(username);

        assertThat(pair).isNotNull();
        assertThat(pair.getPublic()).isNotNull();
        assertThat(pair.getPrivate()).isNotNull();
    }

    @Test
    @DisplayName("generateKeysIfNeeded zwraca null gdy klucze już istnieją")
    void generateKeysIfNeeded_KeysAlreadyExist_ReturnsNull(@TempDir Path tempDir) throws Exception {
        System.setProperty("user.home", tempDir.toString());
        String username = "existinguser";

        KeyPair first = CryptoService.generateKeysIfNeeded(username);
        CryptoService.saveKeyPair(username, first);

        KeyPair second = CryptoService.generateKeysIfNeeded(username);

        assertThat(second).isNull();
    }

    @Test
    @DisplayName("saveKeyPair i getPublicKey / getPrivateKey odtwarzają oryginalne klucze")
    void saveAndLoadKeyPair_ReturnsOriginalKeys(@TempDir Path tempDir) throws Exception {
        System.setProperty("user.home", tempDir.toString());
        String username = "saveloaduser";

        KeyPair pair = CryptoService.generateKeysIfNeeded(username);
        CryptoService.saveKeyPair(username, pair);

        PublicKey loadedPublic = CryptoService.getPublicKey(username);
        PrivateKey loadedPrivate = CryptoService.getPrivateKey(username);

        assertThat(loadedPublic.getEncoded()).isEqualTo(pair.getPublic().getEncoded());
        assertThat(loadedPrivate.getEncoded()).isEqualTo(pair.getPrivate().getEncoded());
    }


    @Test
    @DisplayName("keysExist zwraca false gdy klucze nie istnieją")
    void keysExist_NoKeys_ReturnsFalse(@TempDir Path tempDir) throws Exception {
        System.setProperty("user.home", tempDir.toString());

        assertThat(CryptoService.keysExist("nobody")).isFalse();
    }

    @Test
    @DisplayName("keysExist zwraca true po zapisaniu kluczy")
    void keysExist_AfterSave_ReturnsTrue(@TempDir Path tempDir) throws Exception {
        System.setProperty("user.home", tempDir.toString());
        String username = "existcheck";

        KeyPair pair = CryptoService.generateKeysIfNeeded(username);
        CryptoService.saveKeyPair(username, pair);

        assertThat(CryptoService.keysExist(username)).isTrue();
    }

    @Test
    @DisplayName("stringToPublicKey odtwarza klucz publiczny z Base64")
    void stringToPublicKey_ValidBase64_ReturnsPublicKey(@TempDir Path tempDir) throws Exception {
        System.setProperty("user.home", tempDir.toString());
        String username = "pubkeyuser";

        KeyPair pair = CryptoService.generateKeysIfNeeded(username);
        String base64 = java.util.Base64.getEncoder().encodeToString(pair.getPublic().getEncoded());

        PublicKey result = CryptoService.stringToPublicKey(base64);

        assertThat(result.getEncoded()).isEqualTo(pair.getPublic().getEncoded());
    }

    @Test
    @DisplayName("stringToPublicKey rzuca wyjątek dla nieprawidłowego Base64")
    void stringToPublicKey_InvalidBase64_ThrowsException() {
        assertThrows(Exception.class, () -> CryptoService.stringToPublicKey("nie-jest-kluczem"));
    }


    @Test
    @DisplayName("encryptGroupKey i decryptGroupKey odtwarzają oryginalny klucz AES")
    void encryptDecryptGroupKey_ReturnsOriginalKey(@TempDir Path tempDir) throws Exception {
        System.setProperty("user.home", tempDir.toString());
        String username = "groupkeyuser";

        KeyPair pair = CryptoService.generateKeysIfNeeded(username);
        SecretKey original = CryptoService.generateAESKey();

        String encrypted = CryptoService.encryptGroupKey(original, pair.getPublic());
        SecretKey decrypted = CryptoService.decryptGroupKey(encrypted, pair.getPrivate());

        assertThat(decrypted.getEncoded()).isEqualTo(original.getEncoded());
    }

    @Test
    @DisplayName("decryptGroupKey rzuca wyjątek przy błędnym kluczu prywatnym")
    void decryptGroupKey_WrongPrivateKey_ThrowsException(@TempDir Path tempDir) throws Exception {
        System.setProperty("user.home", tempDir.toString());

        KeyPair pair1 = CryptoService.generateKeysIfNeeded("user1");
        System.setProperty("user.home", tempDir.toString());
        KeyPair pair2 = CryptoService.generateKeysIfNeeded("user2");
        SecretKey aesKey = CryptoService.generateAESKey();

        String encrypted = CryptoService.encryptGroupKey(aesKey, pair1.getPublic());

        assertThrows(Exception.class, () -> CryptoService.decryptGroupKey(encrypted, pair2.getPrivate()));
    }
    

    @Test
    @DisplayName("encryptPrivateKeyWithPassword i decrypt odtwarzają oryginalny klucz prywatny")
    void encryptDecryptPrivateKeyWithPassword_ReturnsOriginalKey(@TempDir Path tempDir) throws Exception {
        System.setProperty("user.home", tempDir.toString());
        String username = "pwduser";
        String password = "MojeHaslo123!";

        KeyPair pair = CryptoService.generateKeysIfNeeded(username);
        String encrypted = CryptoService.encryptPrivateKeyWithPassword(pair.getPrivate(), password);
        PrivateKey decrypted = CryptoService.decryptPrivateKeyWithPassword(encrypted, password);

        assertThat(decrypted.getEncoded()).isEqualTo(pair.getPrivate().getEncoded());
    }

    @Test
    @DisplayName("encryptPrivateKeyWithPassword rzuca wyjątek przy błędnym haśle podczas deszyfrowania")
    void decryptPrivateKeyWithPassword_WrongPassword_ThrowsException(@TempDir Path tempDir) throws Exception {
        System.setProperty("user.home", tempDir.toString());
        String username = "pwduser2";

        KeyPair pair = CryptoService.generateKeysIfNeeded(username);
        String encrypted = CryptoService.encryptPrivateKeyWithPassword(pair.getPrivate(), "PoprawneHaslo1!");

        assertThrows(Exception.class,
                () -> CryptoService.decryptPrivateKeyWithPassword(encrypted, "BledneHaslo1!"));
    }

    @Test
    @DisplayName("encryptPrivateKeyWithPassword zwraca różne szyfrogramy dla tego samego wejścia (losowa sól)")
    void encryptPrivateKeyWithPassword_SameInput_ProducesDifferentCiphertexts(@TempDir Path tempDir) throws Exception {
        System.setProperty("user.home", tempDir.toString());
        String username = "pwduser3";
        String password = "HasloTest1!";

        KeyPair pair = CryptoService.generateKeysIfNeeded(username);
        String enc1 = CryptoService.encryptPrivateKeyWithPassword(pair.getPrivate(), password);
        String enc2 = CryptoService.encryptPrivateKeyWithPassword(pair.getPrivate(), password);

        assertThat(enc1).isNotEqualTo(enc2);
    }


    @Test
    @DisplayName("getDIR zwraca ścieżkę zawierającą nazwę użytkownika")
    void getDIR_ReturnsPathContainingUsername() {
        Path dir = CryptoService.getDIR("jankowalski");

        assertThat(dir.toString()).contains("jankowalski");
        assertThat(dir.toString()).contains(".KryptoChatapp");
    }
}