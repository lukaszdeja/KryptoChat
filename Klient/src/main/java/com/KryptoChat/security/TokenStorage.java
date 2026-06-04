package com.KryptoChat.security;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.KryptoChat.Models.User;
import com.sun.javafx.css.parser.Token;

/**
 * Klasa odpowiedzialna za przechowywanie tokenu użytkownika
 * oraz danych zalogowanego użytkownika.
 */
public class TokenStorage {

    /** Token przechowywany w pamięci aplikacji */
    private static String cachedToken;

    /** Dane aktualnie zalogowanego użytkownika */
    private static User cachedUser;

    /** Ścieżka do pliku przechowującego token */
    private static final Path FILE = Paths.get(
            System.getProperty("user.home"), ".KryptoChatapp", "token.dat");

    /**
     * Zapisuje token do pliku oraz pamięci aplikacji.
     * @param token token użytkownika
     */
    public static void saveToken(String token) {

        try {

            Files.createDirectories(FILE.getParent());
            cachedToken = token;
            Files.writeString(FILE, token);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Odczytuje token z pliku i zapisuje go w pamięci aplikacji.
     * @return zapisany token lub null jeśli plik nie istnieje
     */
    public static String loadUser() {

        try {
            if (!Files.exists(FILE)) {
                return null;
            }

            String token = Files.readString(FILE);
            cachedToken = token;
            return cachedToken;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Usuwa zapisany token oraz czyści dane użytkownika z pamięci.
     */
    public static void deleteToken() {

        try {
            Files.deleteIfExists(FILE);
            cachedToken = null;
            cachedUser = null;

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    /**
     * Ustawia dane zalogowanego użytkownika.
     * @param user użytkownik aplikacji
     */
    public static void setUser(User user) {
        cachedUser = user;
    }

    /**
     * Zwraca dane aktualnie zalogowanego użytkownika.
     * @return użytkownik aplikacji
     */
    public static User getUser() {
        return cachedUser;
    }

    /**
     * Zwraca zapisany token z pamięci aplikacji.
     * @return token użytkownika
     */
    public static String getCachedToken() {
        return cachedToken;
    }

    /**
     * Ustawia token w pamięci aplikacji.
     * @param token token użytkownika
     */
    public static void setCachedToken(String token) {
        cachedToken = token;
    }
}