package security;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.file.*;
import java.util.Base64;

public class GroupKeyStorage {

    private static final Path KEY_FILE = Paths.get(
            System.getProperty("user.home"),
            ".KryptoChatapp",
            "group.key"
    );

    public static void save(SecretKey key) throws Exception {

        Files.createDirectories(KEY_FILE.getParent());

        Files.write(KEY_FILE, Base64.getEncoder().encode(key.getEncoded()));
    }

    public static SecretKey load() throws Exception {

        byte[] bytes = Base64.getDecoder().decode(Files.readAllBytes(KEY_FILE));

        return new SecretKeySpec(bytes, "AES");
    }

    public static boolean exists() {
        return Files.exists(KEY_FILE);
    }

    public static void delete() throws Exception {
        Files.deleteIfExists(KEY_FILE);
    }
}
