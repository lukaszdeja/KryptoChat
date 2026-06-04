package com.KryptoChat.security;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.file.*;
import java.util.Base64;

/**
 * Klasa GroupKeyStorage odpowiada za bezpieczne (w strukturze plików)
 * przechowywanie, zapisywanie oraz wczytywanie kluczy kryptograficznychgrup użytkownika na dysku lokalnym.
 * Klucze są kodowane do formatu Base64 przed zapisem do pliku.
 */
public class GroupKeyStorage {

    /**
     * Zwraca ścieżkę do pliku klucza grupy dla konkretnego użytkownika.
     * Ścieżka bazuje na katalogu domowym użytkownika systemu (user.home).
     * Format ścieżki: {@code {katalog_domowy}/.KryptoChatapp/keys/{username}/group.key}
     *
     * @param username nazwa zalogowanego użytkownika
     * @return obiekt Path reprezentujący ścieżkę docelową pliku klucza
     */
    public static Path getPath(String username) {
        return Paths.get(System.getProperty("user.home"), ".KryptoChatapp/keys", username, "group.key");
    }

    /**
     * Zapisuje SecretKey na dysku lokalnym w formacie Base64.
     * Jeśli katalogi nadrzędne nie istnieją, zostaną automatycznie utworzone.
     *
     * @param username nazwa użytkownika, dla którego zapisywany jest klucz
     * @param key obiekt SecretKey (klucz symetryczny) do zapisania
     * @throws Exception w przypadku błędów wejścia/wyjścia (I/O) podczas tworzenia folderów lub pliku
     */
    public static void save(String username, SecretKey key) throws Exception {
        // Tworzy strukturę katalogów nadrzędnych, jeśli jeszcze nie istnieje
        Files.createDirectories(getPath(username).getParent());

        // Pobiera surowe bajty klucza, koduje je do Base64 i zapisuje do pliku
        Files.write(getPath(username), Base64.getEncoder().encode(key.getEncoded()));
    }

    /**
     * Wczytuje SecretKey z pliku lokalnego na podstawie nazwy użytkownika.
     * Odczytane dane w formacie Base64 są dekodowane z powrotem do algorytmu AES.
     *
     * @param username nazwa użytkownika, którego klucz ma zostać wczytany
     * @return odtworzony obiekt SecretKey (AES) lub {@code null}, jeśli plik klucza jeszcze nie istnieje
     * @throws Exception w przypadku błędów wejścia/wyjścia innych niż brak pliku (np. uszkodzenie pliku)
     */
    public static SecretKey load(String username) throws Exception {
        if (!exists(username)) {
            System.out.println("Klucz jeszcze nie dotarl (brak pliku na dysku)");
            return null;
        }

        try {
            // Odczytuje bajty z pliku i dekoduje je z formatu Base64
            byte[] bytes = Base64.getDecoder().decode(Files.readAllBytes(getPath(username)));

            // Rekonstruuje klucz symetryczny dla algorytmu AES
            return new SecretKeySpec(bytes, "AES");
        } catch (NoSuchFileException e) {
            System.out.println("Klucz jeszcze nie dotarl");
        }
        return null;
    }

    /**
     * Sprawdza, czy plik z kluczem grupy istnieje fizycznie na dysku dla danego użytkownika.
     *
     * @param username nazwa sprawdzanego użytkownika
     * @return true jeśli plik istnieje
     * @return false jeśli plik nie istnieje
     */
    public static boolean exists(String username) {
        return Files.exists(getPath(username));
    }
}