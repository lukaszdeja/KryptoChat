package security;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.PrivateKey;
import java.security.spec.X509EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class RSAUtil {

    public static String encrypt(String text, String publicKeyString) throws Exception {

        byte[] publicBytes = Base64.getDecoder().decode(publicKeyString);

        X509EncodedKeySpec spec = new X509EncodedKeySpec(publicBytes);

        KeyFactory factory = KeyFactory.getInstance("RSA");

        PublicKey publicKey = factory.generatePublic(spec);

        Cipher cipher = Cipher.getInstance("RSA");

        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        byte[] encrypted = cipher.doFinal(text.getBytes());

        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static String decrypt(String text, PrivateKey privateKey) throws Exception {

        byte[] bytes = Base64.getDecoder().decode(text);

        Cipher cipher = Cipher.getInstance("RSA");

        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        return new String(cipher.doFinal(bytes));
    }
}