package Services;

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

public class CryptoService {


    public static Path getDIR(String username) {
        return Paths.get(System.getProperty("user.home"), ".KryptoChatapp", "keys", username);
    }


    private static final String RSA_TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";

    private static final String AES_TRANSFORMATION = "AES/GCM/NoPadding";

    private static final int AES_IV_LENGTH = 12;
    private static final int AES_TAG_LENGTH = 128;


    public static KeyPair generateKeysIfNeeded(String username) throws Exception {
        Path dir = getDIR(username);
        Path privatePath = dir.resolve("private.key");
        Path publicPath = dir.resolve("public.key");
        if (Files.exists(privatePath) && Files.exists(publicPath)) {
            return null;
        }

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);

        KeyPair pair = generator.generateKeyPair();
        return pair;
    }

    public static void saveKeyPair(String username, KeyPair pair) throws Exception {
        Path dir = getDIR(username);
        Files.createDirectories(dir);
        Files.write(dir.resolve("private.key"), Base64.getEncoder().encode(pair.getPrivate().getEncoded()));
        Files.write(dir.resolve("public.key"), Base64.getEncoder().encode(pair.getPublic().getEncoded()));
    }

    public static PublicKey getPublicKey(String username) throws Exception {
        Path path = getDIR(username).resolve("public.key");
        byte[] bytes = Base64.getDecoder().decode(Files.readAllBytes(path));

        X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);

        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    public static PrivateKey getPrivateKey(String username) throws Exception {
        Path path = getDIR(username).resolve("private.key");
        byte[] bytes = Base64.getDecoder().decode(Files.readAllBytes(path));

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(bytes);

        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    public static String getPublicKeyString(String username) throws Exception {
        return Base64.getEncoder().encodeToString(getPublicKey(username).getEncoded());
    }

    public static PublicKey stringToPublicKey(String key) throws Exception {
        byte[] bytes = Base64.getDecoder().decode(key);

        X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);

        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    public static boolean keysExist(String username) {
        Path path = getDIR(username);
        if (Files.exists(path.resolve("public.key")) && Files.exists(path.resolve("private.key"))) {
            return true;
        }
        return false;
    }


    public static SecretKey generateAESKey() throws Exception {
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(256);
        return generator.generateKey();
    }

    public static String aesKeyToString(SecretKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public static SecretKey stringToAESKey(String key) {
        byte[] bytes = Base64.getDecoder().decode(key);
        return new SecretKeySpec(bytes, "AES");
    }

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


    public static String encryptRSA(byte[] data, PublicKey publicKey) throws Exception {

        Cipher cipher = Cipher.getInstance(RSA_TRANSFORMATION);

        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        byte[] encrypted = cipher.doFinal(data);

        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static byte[] decryptRSA(String encrypted, PrivateKey privateKey) throws Exception {

        Cipher cipher = Cipher.getInstance(RSA_TRANSFORMATION);

        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        return cipher.doFinal(Base64.getDecoder().decode(encrypted));
    }

    public static String encryptGroupKey(SecretKey groupKey, PublicKey publicKey) throws Exception {

        return encryptRSA(groupKey.getEncoded(), publicKey);
    }

    public static SecretKey decryptGroupKey(String encryptedGroupKey, PrivateKey privateKey) throws Exception {

        byte[] keyBytes = decryptRSA(encryptedGroupKey, privateKey);

        return new SecretKeySpec(keyBytes, "AES");
    }

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
        System.arraycopy(salt,0, combined, 0,  16);
        System.arraycopy(iv,0, combined, 16, 12);
        System.arraycopy(encrypted,0, combined, 28, encrypted.length);
        return Base64.getEncoder().encodeToString(combined);
    }

    public static PrivateKey decryptPrivateKeyWithPassword(String encrypted, String password) throws Exception {
        byte[] combined = Base64.getDecoder().decode(encrypted);
        byte[] salt = Arrays.copyOfRange(combined, 0,  16);
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