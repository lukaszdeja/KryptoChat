package Services;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.file.*;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;

public class CryptoService {

    private static final Path PRIVATE_KEY = Paths.get(System.getProperty("user.home"), ".KryptoChatapp", "private.key");

    public static KeyPair generateRSA() throws Exception {

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");

        generator.initialize(2048);

        return generator.generateKeyPair();
    }

    public static void savePrivateKey(PrivateKey privateKey)
            throws Exception {

        Files.createDirectories(PRIVATE_KEY.getParent());

        Files.write(PRIVATE_KEY, Base64.getEncoder().encode(privateKey.getEncoded()));
    }

    public static PrivateKey loadPrivateKey()
            throws Exception {

        byte[] bytes = Base64.getDecoder().decode(
                Files.readAllBytes(PRIVATE_KEY)
        );

        PKCS8EncodedKeySpec spec =
                new PKCS8EncodedKeySpec(bytes);

        return KeyFactory.getInstance("RSA")
                .generatePrivate(spec);
    }

    public static String publicKeyToString(PublicKey key) {

        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public static PublicKey stringToPublicKey(String key)
            throws Exception {

        byte[] bytes = Base64.getDecoder().decode(key);

        X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);

        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    public static SecretKey generateAES()
            throws Exception {

        KeyGenerator generator = KeyGenerator.getInstance("AES");

        generator.init(256);

        return generator.generateKey();
    }

    public static String encryptAES(String text, SecretKey key)
            throws Exception {

        byte[] iv = new byte[12];

        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));

        byte[] encrypted = cipher.doFinal(text.getBytes());

        byte[] combined = new byte[iv.length + encrypted.length];

        System.arraycopy(iv, 0, combined, 0, iv.length);

        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    public static String decryptAES(String encrypted, SecretKey key)
            throws Exception {

        byte[] combined = Base64.getDecoder().decode(encrypted);

        byte[] iv = new byte[12];
        byte[] ciphertext = new byte[combined.length - 12];

        System.arraycopy(combined, 0, iv, 0, 12);

        System.arraycopy(combined, 12, ciphertext, 0, ciphertext.length);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));

        return new String(cipher.doFinal(ciphertext));
    }

    public static String encryptRSA(byte[] data, PublicKey key)
            throws Exception {

        Cipher cipher = Cipher.getInstance("RSA");

        cipher.init(Cipher.ENCRYPT_MODE, key);

        return Base64.getEncoder().encodeToString(cipher.doFinal(data));
    }

    public static byte[] decryptRSA(String encrypted, PrivateKey key)
            throws Exception {

        Cipher cipher = Cipher.getInstance("RSA");

        cipher.init(Cipher.DECRYPT_MODE, key);

        return cipher.doFinal(Base64.getDecoder().decode(encrypted));
    }

    public static SecretKey bytesToAES(byte[] bytes) {
        return new SecretKeySpec(bytes, "AES");
    }
}
