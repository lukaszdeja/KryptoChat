package security;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TokenStorage {

    private static final Path FILE = Paths.get(
            System.getProperty("user.home"),
            ".KryptoChatapp",
            "token.dat"
    );

    // ===== SAVE TOKEN =====
    public static void saveToken(String token) {
        try {
            Files.createDirectories(FILE.getParent());

            String encrypted = Crypto.encrypt(token);
            Files.writeString(FILE, encrypted);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===== LOAD TOKEN =====
    public static String loadToken() {
        try {
            if (!Files.exists(FILE)) return null;

            String encrypted = Files.readString(FILE);
            return Crypto.decrypt(encrypted);

        } catch (Exception e) {
            return null;
        }
    }

    // ===== DELETE TOKEN =====
    public static void deleteToken() {
        try {
            Files.deleteIfExists(FILE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}