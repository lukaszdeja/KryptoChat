package Controllers;

import Services.ServiceResponse;
import Views.CreateGroup;
import Services.GroupService;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

/**
 * Klasa obsługująca controller tworzenia lub dołączania do grupy - połączenie interfejsu z backendem
 */
public class GroupController {

    private CreateGroup groupView;
    private GroupService groupService;
    private Runnable goToChats;


    /* Konstruktor klasy, inicjuje pola klasy i wywoluje metode init()
     * @param view - obiekt okna (UI)
     * @param service - obiekt serwisu obsługi grup
     * @param goToChats - callback do funkcji przelaczajacej widok na okno czatu
     */
    public GroupController(CreateGroup view, GroupService service, Runnable chats) {
        this.groupView = view;
        this.groupService = service;
        this.goToChats = chats;

        init();
    }

    /**
     * Pomocnicza metoda, która przypisuje do przycisków z widoku odpowiednie reakcje na kliknięcie.
     * Po kliknięciu przycisku wywoływana jest odpowiednia metoda obsługująca akcję:
     * - createGroup() dla tworzenia grupy
     * - joinGroup() dla dołączania do istniejącej grupy
     */
    private void init() {
        groupView.getCreate().setOnAction(e -> createGroup());
        groupView.getJoin().setOnAction(e -> joinGroup());
    }

    /**
     * Metoda odpowiedzialna za tworzenie nowej grupy.
     * Pobiera nazwę grupy z pola tekstowego, waliduje ją,a następnie wywołuje metodę serwisu tworzącą grupę.
     * W zależności od wyniku:
     * - wyświetla komunikat błędu
     * - lub przełącza widok na czat
     */
    private void createGroup() {
        String groupName = groupView.getGroupNameField().getText();

        if (groupName.isBlank()) {
            groupView.getMessage().setText("Nazwa grupy nie może być pusta");
            return;
        }

        if (groupName.length() > 20) {
            groupView.getMessage().setText("Nazwa grupy nie może mieć więcej niż 20 znaków");
            return;
        }

        if (groupName.length() <= 3) {
            groupView.getMessage().setText("Nazwa grupy musi mieć conajmniej 3 znaki");
            return;
        }

        ServiceResponse response = groupService.createGroup(groupName);

        groupView.getMessage().setText(response.getMessage());

        if (response.isSuccess()) {

            PauseTransition delay =
                    new PauseTransition(Duration.seconds(2));

            delay.setOnFinished(e -> goToChats.run());

            delay.play();
        }

        clearFields();
    }

    /**
     * Metoda odpowiedzialna za dołączanie do istniejącej grupy.
     * Pobiera kod grupy z pola tekstowego, waliduje go, a następnie wywołuje metodę serwisu dołączającą do grupy.
     * W zależności od wyniku:
     * - wyświetla komunikat błędu
     * - lub przełącza widok na czat
     */
    private void joinGroup() {
        String code = groupView.getCodeField().getText();

        if (code.isBlank()) {
            groupView.getMessage().setText("Kod grupy nie może być pusty");
            return;
        }

        ServiceResponse response = groupService.joinGroup(code);

        groupView.getMessage().setText(response.getMessage());

        if (response.isSuccess()) {

            PauseTransition delay = new PauseTransition(Duration.seconds(2));

            delay.setOnFinished(e -> goToChats.run());

            delay.play();
        }

        clearFields();
    }

    private void clearFields() {

        groupView.getGroupNameField().setText("");
        groupView.getCodeField().setText("");
    }
}
