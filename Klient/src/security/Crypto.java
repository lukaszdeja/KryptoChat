package security;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Klasa odpowiedzialna za szyfrowanie i deszyfrowanie danych
 * przy użyciu algorytmu AES.
 */
public class Crypto {

    /**
     * Stały klucz używany do szyfrowania i deszyfrowania danych.
     * tymczasowa wersja
     */
    private static final String SECRET_KEY = "MySuperSecretKey";

    /**
     * Tworzy obiekt klucza AES na podstawie zdefiniowanego SECRET_KEY.
     * @return obiekt SecretKeySpec wykorzystywany przez Cipher
     */
    private static SecretKeySpec getKey() {
        return new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
    }

    /**
     * Szyfruje podany tekst przy użyciu algorytmu AES.
     *
     * Proces:
     * - inicjalizuje Cipher w trybie szyfrowania
     * - przekształca tekst na bajty
     * - szyfruje dane
     * - koduje wynik do Base64 (aby można było przechowywać jako tekst)
     *
     * @param value tekst do zaszyfrowania
     * @return zaszyfrowany tekst zakodowany w Base64
     * @throws RuntimeException gdy wystąpi błąd szyfrowania
     */
    public static String encrypt(String value) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, getKey());

            byte[] encrypted = cipher.doFinal(value.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);

        } catch (Exception e) {
            throw new RuntimeException("Encrypt error", e);
        }
    }

    /**
     * Deszyfruje zaszyfrowany tekst przy użyciu algorytmu AES.
     *
     * Proces:
     * - dekoduje tekst z Base64
     * - inicjalizuje Cipher w trybie deszyfrowania
     * - odszyfrowuje dane
     *
     * @param encrypted zaszyfrowany tekst w formacie Base64
     * @return odszyfrowany tekst lub null w przypadku błędu
     */
    public static String decrypt(String encrypted) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, getKey());

            byte[] decoded = Base64.getDecoder().decode(encrypted);
            return new String(cipher.doFinal(decoded));

        } catch (Exception e) {
            return null;
        }
    }
}