package security;

import java.io.IOException;
import java.nio.file.*;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;

public class KeyManager {

    private static final Path DIR = Paths.get(
            System.getProperty("user.home"), ".KryptoChatapp", "keys"
    );

    private static final Path PRIVATE_KEY = DIR.resolve("private.key");
    private static final Path PUBLIC_KEY = DIR.resolve("public.key");

    public static void generateKeysIfNeeded() {

        try {

            if (Files.exists(PRIVATE_KEY) && Files.exists(PUBLIC_KEY)) {
                return;
            }

            Files.createDirectories(DIR);

            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);

            KeyPair pair = generator.generateKeyPair();

            saveKey(PRIVATE_KEY, pair.getPrivate().getEncoded());
            saveKey(PUBLIC_KEY, pair.getPublic().getEncoded());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void saveKey(Path path, byte[] key) throws IOException {
        Files.write(path, Base64.getEncoder().encode(key));
    }

    public static PublicKey getPublicKey() throws Exception {

        byte[] keyBytes = Base64.getDecoder().decode(
                Files.readAllBytes(PUBLIC_KEY)
        );

        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);

        return KeyFactory.getInstance("RSA")
                .generatePublic(spec);
    }

    public static PrivateKey getPrivateKey() throws Exception {

        byte[] keyBytes = Base64.getDecoder().decode(
                Files.readAllBytes(PRIVATE_KEY)
        );

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);

        return KeyFactory.getInstance("RSA")
                .generatePrivate(spec);
    }

    public static String getPublicKeyString() throws Exception {
        return Base64.getEncoder().encodeToString(getPublicKey().getEncoded());
    }
}