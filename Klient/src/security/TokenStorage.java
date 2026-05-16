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


    public static void saveToken(String token) {
        try {
            Files.createDirectories(FILE.getParent());

            cachedToken = token;
            Files.writeString(FILE, token);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static String loadUser() {
        try {
            if (!Files.exists(FILE)) return null;
            String token = Files.readString(FILE);
            cachedToken = token;
            return cachedToken;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void deleteToken() {
        try {
            Files.deleteIfExists(FILE);
            cachedToken = null;
            cachedUser = null;
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

    public static String getCachedToken() { return  cachedToken; }

}