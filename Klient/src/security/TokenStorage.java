package security;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import Models.User;
import com.sun.javafx.css.parser.Token;

public class TokenStorage {

    private static String cachedToken;
    private static User cachedUser;
    private static final Path FILE = Paths.get(
            System.getProperty("user.home"), ".KryptoChatapp", "token.dat");

    // ===== SAVE TOKEN =====
    public static void saveToken(String token) {
        try {
            Files.createDirectories(FILE.getParent());

            String encrypted = Crypto.encrypt(token);
            cachedToken = token;
            Files.writeString(FILE, encrypted);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===== LOAD TOKEN =====
    public static void loadUser() {
        try {
            if (!Files.exists(FILE)) return;
            ObjectMapper mapper = new ObjectMapper();
            String encrypted = Files.readString(FILE);
            String json = Crypto.decrypt(encrypted);
            TokenStorage.setUser(mapper.readValue(json, User.class));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===== DELETE TOKEN =====
    public static void deleteToken() {
        try {
            Files.deleteIfExists(FILE);
            cachedToken = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void setUser(User user) {
        cachedUser = user;
    }

    public static User getUser() {
        return cachedUser;
    }

}