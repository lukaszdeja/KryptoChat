package com.KryptoChat;
import com.KryptoChat.Models.User;
import com.KryptoChat.Services.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.KryptoChat.Views.*;
import com.KryptoChat.Controllers.*;
import com.KryptoChat.security.TokenStorage;

/** class Main
 * Główna klasa warstwy FrontEnd aplikacji - zbudowana w JavaFx, rozszerza interfejs Application
 * Tworzy widoki i wyświetla okno
 * Obsługuje przełączanie pomiędzy widokami
 */
public class Main extends Application {
    private Login loginPage;
    private Register registerPage;
    private Chat chatPage;
    private Scene scene;
    private CreateGroup groupPage;
    private LoginController loginController;
    private LoginService loginService;
    private RegisterService registerService;
    private RegisterController registerController;
    private ChatController chatController;
    private GroupController groupController;
    private GroupService groupService;
    private ChatService chatService;
    private AuthentificationService authService;

    /**
     * Inicjalizuje komponenty aplikacji, konfiguruje wstrzykiwanie zależności (DI)
     * oraz decyduje o widoku startowym na podstawie ważności lokalnego tokenu sesji.
     *
     * @param stage główny kontener okna JavaFX
     * @throws Exception przy błędach inicjalizacji interfejsu lub sieci
     */
    @Override
    public void start(Stage stage) throws Exception {
        loginPage = new Login(this::showRegister);
        registerPage = new Register(this::showLogin);
        chatPage = new Chat();
        groupPage = new CreateGroup();
        loginService = new LoginService();
        registerService = new RegisterService();
        groupService = new GroupService();
        chatService = new ChatService();
        loginController = new LoginController(loginPage, loginService, this::showCreateGroup, this::showChats);
        registerController = new RegisterController(registerPage, registerService, this::showLogin);
        groupController = new GroupController(groupPage, groupService, this::showChats, this::showLogin);
        authService = new AuthentificationService();
        setupStage(stage);

        // Automatyczne logowanie na podstawie zapisanego tokenu
       String token =  TokenStorage.loadUser();
       if (token == null) {
            showLogin();
       } else {
           User user = authService.checkUser(token);
           TokenStorage.setUser(user);
           if (user == null) {
               showLogin();
           } else {
               TokenStorage.setUser(user);
               if (user.getGroupId() != null) {
                   showChats();
               } else {
                   showCreateGroup();
               }
           }
       }

    }

    /**
     * Przełącza główny widok aplikacji na ekran logowania i ładuje dedykowane style CSS.
     */
    private void showLogin() {
        scene.setRoot(loginPage.getView());

        scene.getStylesheets().setAll(
                getClass().getResource("/global.css").toExternalForm(),
                getClass().getResource("/login.css").toExternalForm()
        );
    }

    /**
     * Przełącza główny widok aplikacji na ekran rejestracji i ładuje dedykowane style CSS.
     */
    private void showRegister() {
        scene.setRoot(registerPage.getView());
        scene.getStylesheets().setAll(
                getClass().getResource("/global.css").toExternalForm(),
                getClass().getResource("/login.css").toExternalForm()
        );
    }

    /**
     * Przełącza główny widok aplikacji na ekran głównego czatu, dynamicznie
     * odświeżając kontroler czatu oraz odpowiednie pliki stylów.
     */
    private void showChats() {
        chatController = new ChatController(chatPage, chatService, this::showLogin);
        scene.setRoot(chatPage.getView());
        scene.getStylesheets().setAll(
                getClass().getResource("/global.css").toExternalForm(),
                getClass().getResource("/chat.css").toExternalForm()
        );
    }

    /**
     * Przełącza główny widok aplikacji na panel tworzenia/dołączania do grupy i ładuje style CSS.
     */
    private void showCreateGroup() {
        scene.setRoot(groupPage.getView());
        scene.getStylesheets().setAll(
                getClass().getResource("/global.css").toExternalForm(),
                getClass().getResource("/groups.css").toExternalForm()
        );
    }

    /**
     * Konfiguruje parametry początkowe sceny, ustawia rozmiar okna, tytuł oraz wyświetla aplikację.
     * @param stage główny obiekt Stage dostarczony przez uruchomienie JavaFX
     */
    private void setupStage(Stage stage) {
        scene = new Scene(loginPage.getView(), 1080, 720);
        stage.setTitle("KryptoChat");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Główna metoda wejściowa programu uruchamiająca proces JavaFX.
     * @param args argumenty wiersza poleceń
     */
    public static void main(String[] args) {
        launch(args);
    }
}