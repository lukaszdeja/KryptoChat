package com.KryptoChat.Services;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import java.util.Arrays;
import java.util.Base64;

/**
 * Serwis kryptograficzny odpowiedzialny za generowanie kluczy, szyfrowanie symetryczne (AES-GCM),
 * asymetryczne (RSA-OAEP) oraz bezpieczne zabezpieczanie klucza prywatnego hasłem (PBKDF2).
 */
public class CryptoService {

    /** Maska transformacji dla algorytmu RSA z dopełnieniem OAEP */
    private static final String RSA_TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";

    /** Maska transformacji dla algorytmu AES w trybie GCM */
    private static final String AES_TRANSFORMATION = "AES/GCM/NoPadding";

    /** Długość wektora inicjalizacyjnego (IV) dla trybu GCM (w bajtach) */
    private static final int AES_IV_LENGTH = 12;

    /** Długość tagu uwierzytelniającego dla trybu GCM (w bitach) */
    private static final int AES_TAG_LENGTH = 128;

    /**
     * Zwraca ścieżkę do katalogu przechowywania kluczy danego użytkownika.
     * @param username login użytkownika
     * @return ścieżka Path do katalogu z kluczami
     */
    public static Path getDIR(String username) {
        return Paths.get(System.getProperty("user.home"), ".KryptoChatapp", "keys", username);
    }

    /**
     * Generuje nową parę kluczy RSA (2048 bitów), jeśli nie istnieje ona jeszcze na dysku.
     * @param username login użytkownika
     * @return wygenerowana para kluczy KeyPair lub null, jeśli pliki już istnieją
     * @throws Exception przy błędach generatora kluczy
     */
    public static KeyPair generateKeysIfNeeded(String username) throws Exception {
        Path dir = getDIR(username);
        Path privatePath = dir.resolve("private.key");
        Path publicPath = dir.resolve("public.key");
        if (Files.exists(privatePath) && Files.exists(publicPath)) {
            return null;
        }

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);

        return generator.generateKeyPair();
    }

    /**
     * Zapisuje parę kluczy RSA na dysku lokalnym w formacie Base64.
     * @param username login użytkownika
     * @param pair para kluczy do zapisania
     * @throws Exception przy błędach zapisu plików
     */
    public static void saveKeyPair(String username, KeyPair pair) throws Exception {
        Path dir = getDIR(username);
        Files.createDirectories(dir);
        Files.write(dir.resolve("private.key"), Base64.getEncoder().encode(pair.getPrivate().getEncoded()));
        Files.write(dir.resolve("public.key"), Base64.getEncoder().encode(pair.getPublic().getEncoded()));
    }

    /**
     * Wczytuje i rekonstruuje klucz publiczny RSA z dysku lokalnego.
     * @param username login użytkownika
     * @return obiekt PublicKey (RSA)
     * @throws Exception przy błędach odczytu lub niepoprawnym formacie klucza
     */
    public static PublicKey getPublicKey(String username) throws Exception {
        Path path = getDIR(username).resolve("public.key");
        byte[] bytes = Base64.getDecoder().decode(Files.readAllBytes(path));
        X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    /**
     * Wczytuje i rekonstruuje klucz prywatny RSA z dysku lokalnego.
     * @param username login użytkownika
     * @return obiekt PrivateKey (RSA)
     * @throws Exception przy błędach odczytu lub niepoprawnym formacie klucza
     */
    public static PrivateKey getPrivateKey(String username) throws Exception {
        Path path = getDIR(username).resolve("private.key");
        byte[] bytes = Base64.getDecoder().decode(Files.readAllBytes(path));
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(bytes);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    /**
     * Konwertuje tekstowy klucz publiczny Base64 na obiekt PublicKey.
     * @param key klucz publiczny w formacie Base64 jako String
     * @return obiekt PublicKey (RSA)
     * @throws Exception przy niepoprawnym formacie klucza
     */
    public static PublicKey stringToPublicKey(String key) throws Exception {
        byte[] bytes = Base64.getDecoder().decode(key);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    /**
     * Sprawdz czy pliki klucza publicznego i prywatnego istnieją na dysku.
     * @param username login użytkownika
     * @return true jeśli oba pliki istnieją, false w przeciwnym razie
     */
    public static boolean keysExist(String username) {
        Path path = getDIR(username);
        return Files.exists(path.resolve("public.key")) && Files.exists(path.resolve("private.key"));
    }

    /**
     * Generuje nowy klucz symetryczny AES o długości 256 bitów.
     * @return wygenerowany obiekt SecretKey
     * @throws Exception przy błędach generatora kluczy
     */
    public static SecretKey generateAESKey() throws Exception {
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(256);
        return generator.generateKey();
    }

    /**
     * Szyfruje tekst jawny algorytmem AES-GCM z użyciem losowego IV.
     *
     * Proces:
     * - generuje losowy 12-bajtowy wektor IV,
     * - szyfruje tekst za pomocą AES-GCM,
     * - łączy IV i szyfrogram w jedną tablicę bajtów i koduje do Base64.
     *
     * @param text tekst do zaszyfrowania
     * @param key klucz symetryczny AES
     * @return zaszyfrowany ciąg Base64 (zawierający IV + szyfrogram)
     * @throws Exception przy błędach szyfrowania
     */
    public static String encryptAES(String text, SecretKey key) throws Exception {
        byte[] iv = new byte[AES_IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(AES_TAG_LENGTH, iv));

        byte[] encrypted = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));
        byte[] combined = new byte[iv.length + encrypted.length];

        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    /**
     * Deszyfruje tekst zaszyfrowany metodą AES-GCM.
     *
     * Proces:
     * - dekoduje ciąg Base64 i rozdziela go na IV oraz właściwy szyfrogram,
     * - inicjalizuje odszyfrowywanie z wyodrębnionym IV,
     * - zwraca odszyfrowany tekst w standardzie UTF-8.
     *
     * @param encrypted zaszyfrowany tekst Base64 (IV + szyfrogram)
     * @param key klucz symetryczny AES
     * @return odszyfrowany tekst jawny
     * @throws Exception przy błędach deszyfrowania lub naruszeniu spójności danych (błędny tag)
     */
    public static String decryptAES(String encrypted, SecretKey key) throws Exception {
        byte[] combined = Base64.getDecoder().decode(encrypted);

        byte[] iv = new byte[AES_IV_LENGTH];
        byte[] ciphertext = new byte[combined.length - AES_IV_LENGTH];

        System.arraycopy(combined, 0, iv, 0, AES_IV_LENGTH);
        System.arraycopy(combined, AES_IV_LENGTH, ciphertext, 0, ciphertext.length);

        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(AES_TAG_LENGTH, iv));

        byte[] decrypted = cipher.doFinal(ciphertext);
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    /**
     * Szyfruje surowe dane asymetrycznie za pomocą klucza publicznego RSA (OAEP).
     * @param data dane do zaszyfrowania
     * @param publicKey klucz publiczny odbiorcy
     * @return zaszyfrowany ciąg Base64
     * @throws Exception przy błędach szyfrowania RSA
     */
    public static String encryptRSA(byte[] data, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance(RSA_TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encrypted = cipher.doFinal(data);
        return Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * Deszyfruje dane asymetrycznie za pomocą klucza prywatnego RSA (OAEP).
     * @param encrypted zaszyfrowany tekst Base64
     * @param privateKey klucz prywatny odbiorcy
     * @return surowe odszyfrowane bajty danych
     * @throws Exception przy błędach deszyfrowania RSA
     */
    public static byte[] decryptRSA(String encrypted, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance(RSA_TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(Base64.getDecoder().decode(encrypted));
    }

    /**
     * Szyfruje klucz grupy (AES) kluczem publicznym użytkownika docelowego.
     * @param groupKey symetryczny klucz grupy
     * @param publicKey klucz publiczny adresata
     * @return zaszyfrowany klucz grupy w formacie Base64
     * @throws Exception przy błędach szyfrowania
     */
    public static String encryptGroupKey(SecretKey groupKey, PublicKey publicKey) throws Exception {
        return encryptRSA(groupKey.getEncoded(), publicKey);
    }

    /**
     * Odszyfrowuje klucz grupy za pomocą klucza prywatnego użytkownika.
     * @param encryptedGroupKey zaszyfrowany klucz grupy w formacie Base64
     * @param privateKey klucz prywatny użytkownika
     * @return zrekonstruowany obiekt SecretKey (AES) grupy
     * @throws Exception przy błędach deszyfrowania
     */
    public static SecretKey decryptGroupKey(String encryptedGroupKey, PrivateKey privateKey) throws Exception {
        byte[] keyBytes = decryptRSA(encryptedGroupKey, privateKey);
        return new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * Zabezpiecza klucz prywatny użytkownika hasłem (do zapisu na serwerze).
     *
     * Proces:
     * - generuje losową sól i tworzy klucz AES za pomocą PBKDF2 (310 000 iteracji),
     * - szyfruje klucz prywatny algorytmem AES-GCM,
     * - łączy sól, IV oraz szyfrogram w jeden pakiet i koduje do Base64.
     *
     * @param privateKey klucz prywatny RSA do zabezpieczenia
     * @param password hasło użytkownika służące jako podstawa klucza
     * @return zabezpieczony ciąg Base64 gotowy do wysyłki na backend
     * @throws Exception przy błędach generowania klucza pochodnego lub szyfrowania
     */
    public static String encryptPrivateKeyWithPassword(PrivateKey privateKey, String password) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 310_000, 256);
        byte[] rawKey = factory.generateSecret(spec).getEncoded();
        SecretKey aesKey = new SecretKeySpec(rawKey, "AES");

        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, new GCMParameterSpec(128, iv));

        byte[] encrypted = cipher.doFinal(privateKey.getEncoded());
        byte[] combined = new byte[16 + 12 + encrypted.length];
        System.arraycopy(salt, 0, combined, 0, 16);
        System.arraycopy(iv, 0, combined, 16, 12);
        System.arraycopy(encrypted, 0, combined, 28, encrypted.length);
        return Base64.getEncoder().encodeToString(combined);
    }

    /**
     * Odszyfrowuje klucz prywatny pobrany z serwera przy użyciu hasła użytkownika.
     *
     * Proces:
     * - wyodrębnia sól, IV oraz szyfrogram z tablicy bajtów Base64,
     * - generuje identyczny klucz AES za pomocą PBKDF2 na podstawie hasła i soli,
     * - deszyfruje dane za pomocą AES-GCM i rekonstruuje obiekt PrivateKey (RSA).
     *
     * @param encrypted zabezpieczony ciąg Base64 z serwera (sól + IV + szyfrogram)
     * @param password hasło podane podczas logowania
     * @return odzyskany, pełnoprawny obiekt PrivateKey (RSA)
     * @throws Exception przy błędnym haśle (błąd autoryzacji tagu GCM) lub błędach parsowania
     */
    public static PrivateKey decryptPrivateKeyWithPassword(String encrypted, String password) throws Exception {
        byte[] combined = Base64.getDecoder().decode(encrypted);
        byte[] salt = Arrays.copyOfRange(combined, 0, 16);
        byte[] iv = Arrays.copyOfRange(combined, 16, 28);
        byte[] ciphertext = Arrays.copyOfRange(combined, 28, combined.length);

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 310_000, 256);
        SecretKey aesKey = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, aesKey, new GCMParameterSpec(128, iv));
        byte[] keyBytes = cipher.doFinal(ciphertext);
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
    }
}