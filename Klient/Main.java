import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/** class Main
 * Główna klasa warstwy FrontEnd aplikacji - zbudowana w JavaFx, rozszerza interfejs Application
 * Tworzy widoki i wyświetla okno
 * Obsługuje przełączanie pomiędzy widokami
 */
public class Main extends Application {
    private Login loginPage;
    private Register registerPage;
    private Scene scene;

    private LoginController loginController;
    //private RegisterController registerController;

    /** Metoda start
     * inicjuje widoki logowania oraz rejestracji
     * wywoluje pomocnicza metode setupStage budującą okno
     * @param stage
     * @throws Exception
     */
    public void start(Stage stage) throws Exception {
        loginPage = new Login(this::showRegister);
        registerPage = new Register(this::showLogin);
        setupStage(stage);
    }

    /**
     * Metoda, która zmienia źródło wyświetlania w oknie na okno logowania
     */
    private void showLogin() {
        scene.setRoot(loginPage.getView());
    }

    /**
     * Metoda, która zmienia źródło wyświetlania w oknie na okno rejestracji
     */
    private void showRegister() {
        scene.setRoot(registerPage.getView());
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
     * @param stage -
     */
    private void setupStage(Stage stage) {
        scene = new Scene(registerPage.getView(), 1080, 720);
        stage.setTitle("KryptoChat");
        scene.getStylesheets().add("style.css");
        stage.setScene(scene);
        stage.show();
    }
}