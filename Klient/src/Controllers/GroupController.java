package Controllers;

import Services.ServiceResponse;
import Views.CreateGroup;
import Services.GroupService;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

/**
 * Kontroler obsługujący tworzenie oraz dołączanie do grupy.
 * Łączy widok z logiką aplikacji i serwisem grup.
 */
public class GroupController {

    /** Widok tworzenia/dołączania do grupy */
    private CreateGroup groupView;

    /** Serwis obsługujący operacje na grupach */
    private GroupService groupService;

    /** Akcja przejścia do widoku czatu */
    private Runnable goToChats;

    /**
     * Konstruktor kontrolera grup.
     * @param view widok tworzenia/dołączania do grupy
     * @param service serwis obsługujący grupy
     * @param chats akcja przełączająca widok na widok czatu
     */
    public GroupController(CreateGroup view, GroupService service, Runnable chats) {
        this.groupView = view;
        this.groupService = service;
        this.goToChats = chats;

        init();
    }

    /**
     * Inicjalizuje obsługę zdarzeń przycisków w widoku.
     */
    private void init() {
        groupView.getCreate().setOnAction(e -> createGroup());
        groupView.getJoin().setOnAction(e -> joinGroup());
    }

    /**
     * Obsługuje tworzenie nowej grupy.
     * Sprawdza poprawność nazwy grupy, wysyła żądanie utworzenia grupy i po sukcesie przełącza widok na czat.
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
            PauseTransition delay = new PauseTransition(Duration.seconds(2));

            delay.setOnFinished(e -> goToChats.run());
            delay.play();
        }

        clearFields();
    }

    /**
     * Obsługuje dołączanie do istniejącej grupy na podstawie kodu.
     * Po poprawnym dołączeniu przełącza widok na widok czatu.
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

    /**
     * Czyści pola do wpisywania.
     */
    private void clearFields() {
        groupView.getGroupNameField().setText("");
        groupView.getCodeField().setText("");
    }
}