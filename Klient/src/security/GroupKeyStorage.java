package security;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.file.*;
import java.util.Base64;

public class GroupKeyStorage {

    public static Path getPath(String username) {
        return Paths.get(System.getProperty("user.home"), ".KryptoChatapp/keys", username, "group.key");
    }


    public static void save(String username, SecretKey key) throws Exception {

        Files.createDirectories(getPath(username).getParent());

        Files.write(getPath(username), Base64.getEncoder().encode(key.getEncoded()));
    }

    public static SecretKey load(String username) throws Exception {

        byte[] bytes = Base64.getDecoder().decode(Files.readAllBytes(getPath(username)));

        return new SecretKeySpec(bytes, "AES");
    }

    public static boolean exists(String username) {
        return Files.exists(getPath(username));
    }

    public static void delete(String username) throws Exception {
        Files.deleteIfExists(getPath(username));
    }
}
