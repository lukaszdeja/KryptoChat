package com.KryptoChat.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GroupKeyStorageTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        System.setProperty("user.home", tempDir.toString());
    }

    private SecretKey generateAESKey() throws Exception {
        KeyGenerator gen = KeyGenerator.getInstance("AES");
        gen.init(256);
        return gen.generateKey();
    }

    @Test
    @DisplayName("exists zwraca false gdy klucz nie został zapisany")
    void exists_KeyNotSaved_ReturnsFalse() {
        assertThat(GroupKeyStorage.exists("nobody")).isFalse();
    }

    @Test
    @DisplayName("exists zwraca true po zapisaniu klucza")
    void exists_AfterSave_ReturnsTrue() throws Exception {
        SecretKey key = generateAESKey();

        GroupKeyStorage.save("alice", key);

        assertThat(GroupKeyStorage.exists("alice")).isTrue();
    }


    @Test
    @DisplayName("save tworzy plik klucza na dysku")
    void save_CreatesKeyFile() throws Exception {
        SecretKey key = generateAESKey();

        GroupKeyStorage.save("bob", key);

        Path keyFile = GroupKeyStorage.getPath("bob");
        assertThat(keyFile.toFile().exists()).isTrue();
    }

    @Test
    @DisplayName("save tworzy katalogi nadrzędne jeśli nie istnieją")
    void save_CreatesMissingDirectories() throws Exception {
        SecretKey key = generateAESKey();

        GroupKeyStorage.save("newuser", key);

        assertThat(GroupKeyStorage.getPath("newuser").getParent().toFile().exists()).isTrue();
    }

    @Test
    @DisplayName("save nadpisuje istniejący klucz nowym")
    void save_OverwritesExistingKey() throws Exception {
        SecretKey key1 = generateAESKey();
        SecretKey key2 = generateAESKey();

        GroupKeyStorage.save("charlie", key1);
        GroupKeyStorage.save("charlie", key2);

        SecretKey loaded = GroupKeyStorage.load("charlie");
        assertThat(loaded.getEncoded()).isEqualTo(key2.getEncoded());
    }


    @Test
    @DisplayName("load zwraca null gdy klucz nie istnieje")
    void load_KeyNotExists_ReturnsNull() throws Exception {
        SecretKey result = GroupKeyStorage.load("ghost");

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("load odtwarza oryginalny klucz AES po zapisie")
    void load_AfterSave_ReturnsOriginalKey() throws Exception {
        SecretKey original = generateAESKey();

        GroupKeyStorage.save("dave", original);
        SecretKey loaded = GroupKeyStorage.load("dave");

        assertThat(loaded).isNotNull();
        assertThat(loaded.getEncoded()).isEqualTo(original.getEncoded());
        assertThat(loaded.getAlgorithm()).isEqualTo("AES");
    }

    @Test
    @DisplayName("load zwraca klucz z algorytmem AES")
    void load_ReturnsKeyWithAESAlgorithm() throws Exception {
        GroupKeyStorage.save("eve", generateAESKey());

        SecretKey loaded = GroupKeyStorage.load("eve");

        assertThat(loaded.getAlgorithm()).isEqualTo("AES");
    }

    @Test
    @DisplayName("różni użytkownicy mają niezależne klucze")
    void save_DifferentUsers_HaveIndependentKeys() throws Exception {
        SecretKey key1 = generateAESKey();
        SecretKey key2 = generateAESKey();

        GroupKeyStorage.save("user1", key1);
        GroupKeyStorage.save("user2", key2);

        assertThat(GroupKeyStorage.load("user1").getEncoded()).isEqualTo(key1.getEncoded());
        assertThat(GroupKeyStorage.load("user2").getEncoded()).isEqualTo(key2.getEncoded());
    }

    @Test
    @DisplayName("getPath zwraca ścieżkę zawierającą nazwę użytkownika i group.key")
    void getPath_ContainsUsernameAndFilename() {
        Path path = GroupKeyStorage.getPath("frank");

        assertThat(path.toString()).contains("frank");
        assertThat(path.getFileName().toString()).isEqualTo("group.key");
    }

    @Test
    @DisplayName("getPath różnych użytkowników wskazuje na różne pliki")
    void getPath_DifferentUsers_ReturnsDifferentPaths() {
        Path path1 = GroupKeyStorage.getPath("userA");
        Path path2 = GroupKeyStorage.getPath("userB");

        assertThat(path1).isNotEqualTo(path2);
    }
}