package security;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.util.Base64;

public class AESUtil {

    public static String generateKey() throws Exception {

        KeyGenerator generator = KeyGenerator.getInstance("AES");

        generator.init(256);

        SecretKey key = generator.generateKey();

        return Base64.getEncoder().encodeToString(key.getEncoded());
    }
}