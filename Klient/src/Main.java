import Services.GroupService;
import Services.LoginService;
import Services.RegisterService;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import Views.*;
import Controllers.*;

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

    /** Metoda start
     * inicjuje widoki logowania oraz rejestracji
     * wywoluje pomocnicza metode setupStage budującą okno
     * @param stage
     * @throws Exception
     */
    public void start(Stage stage) throws Exception {
        loginPage = new Login(this::showRegister);
        registerPage = new Register(this::showLogin);
        chatPage = new Chat();
        groupPage = new CreateGroup();
        loginService = new LoginService();
        registerService = new RegisterService();
        groupService = new GroupService();
        loginController = new LoginController(loginPage, loginService, this::showChats);
        registerController = new RegisterController(registerPage, registerService, this::showLogin);
        chatController = new ChatController(chatPage);
        groupController = new GroupController(groupPage, groupService, this::showChats);
        setupStage(stage);

        // WYBIERZ WIDOK STARTOWY:
        //showLogin();
        // showChats();
         //showCreateGroup();
        showRegister();
    }

    /**
     * Metoda, która zmienia źródło wyświetlania w oknie na okno logowania
     */
    private void showLogin() {
        scene.setRoot(loginPage.getView());

        scene.getStylesheets().setAll(
                getClass().getResource("/global.css").toExternalForm(),
                getClass().getResource("/login.css").toExternalForm()
        );
    }

    /**
     * Metoda, która zmienia źródło wyświetlania w oknie na okno rejestracji
     */
    private void showRegister() {
        scene.setRoot(registerPage.getView());

        scene.getStylesheets().setAll(
                getClass().getResource("/global.css").toExternalForm(),
                getClass().getResource("/login.css").toExternalForm()
        );
    }

    /**
     * Metoda, która zmienia źródło wyświetlania w oknie na okno czatu
     */
    private void showChats() {
        scene.setRoot(chatPage.getView());

        scene.getStylesheets().setAll(
                getClass().getResource("/global.css").toExternalForm(),
                getClass().getResource("/chat.css").toExternalForm()
        );
    }

    /**
     * Metoda, która zmienia źródło wyświetlania w oknie na okno czatu
     */
    private void showCreateGroup() {
        scene.setRoot(groupPage.getView());

        scene.getStylesheets().setAll(
                getClass().getResource("/global.css").toExternalForm(),
                getClass().getResource("/groups.css").toExternalForm()
        );
    }

    /**
     * Główna metoda, która uruchamia aplikację
     * @param args
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Metoda, która tworzy scenę - okienko
     * Ustawia tytuł oraz źródło stylów CSS
     * wyświetla okno
     * @param stage
     */
    private void setupStage(Stage stage) {
        scene = new Scene(loginPage.getView(), 1080, 720);
        stage.setTitle("KryptoChat");
        stage.setScene(scene);
        stage.show();
    }
}