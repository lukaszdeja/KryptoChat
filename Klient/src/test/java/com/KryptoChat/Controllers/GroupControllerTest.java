package com.KryptoChat.Controllers;

import com.KryptoChat.Models.User;
import com.KryptoChat.Services.GroupService;
import com.KryptoChat.Services.ServiceResponse;
import com.KryptoChat.Views.CreateGroup;
import com.KryptoChat.security.TokenStorage;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class GroupControllerTest {

    private CreateGroup groupView;
    private GroupService groupService;
    private Runnable goToChats;
    private Runnable goToLogin;
    private GroupController controller;

    private Button realCreateButton;
    private Button realJoinButton;
    private Button realLogoutButton;
    private Label realMessageLabel;
    private TextField realGroupNameField;
    private TextField realCodeField;

    @BeforeAll
    static void initJFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Ignorujemy, jeśli podsystem już działa
        }
    }

    @BeforeEach
    void setUp() {
        groupView = Mockito.mock(CreateGroup.class);
        groupService = Mockito.mock(GroupService.class);
        goToChats = Mockito.mock(Runnable.class);
        goToLogin = Mockito.mock(Runnable.class);

        realCreateButton = new Button();
        realJoinButton = new Button();
        realLogoutButton = new Button();
        realMessageLabel = new Label();
        realGroupNameField = new TextField();
        realCodeField = new TextField();

        Mockito.when(groupView.getCreate()).thenReturn(realCreateButton);
        Mockito.when(groupView.getJoin()).thenReturn(realJoinButton);
        Mockito.when(groupView.getLogoutButton()).thenReturn(realLogoutButton);
        Mockito.when(groupView.getMessage()).thenReturn(realMessageLabel);
        Mockito.when(groupView.getGroupNameField()).thenReturn(realGroupNameField);
        Mockito.when(groupView.getCodeField()).thenReturn(realCodeField);

        controller = new GroupController(groupView, groupService, goToChats, goToLogin);
    }

    @Test
    void shouldShowErrorWhenGroupNameIsEmpty() {
        realGroupNameField.setText("   ");

        realCreateButton.fire();

        assertEquals("Nazwa grupy nie może być pusta", realMessageLabel.getText());
        Mockito.verifyNoInteractions(groupService);
    }

    @Test
    void shouldShowErrorWhenGroupNameIsTooLong() {
        realGroupNameField.setText("TaNazwaGrupyMaZdecydowanieZaDuzoZnakow");

        realCreateButton.fire();

        assertEquals("Nazwa grupy nie może mieć więcej niż 20 znaków", realMessageLabel.getText());
        Mockito.verifyNoInteractions(groupService);
    }

    @Test
    void shouldShowErrorWhenGroupNameIsTooShort() {
        realGroupNameField.setText("ab");

        realCreateButton.fire();

        assertEquals("Nazwa grupy musi mieć conajmniej 3 znaki", realMessageLabel.getText());
        Mockito.verifyNoInteractions(groupService);
    }

    @Test
    void shouldCallServiceAndHandleFailureWhenCreatingGroupFails() {
        String invalidGroupName = "ZajetaGrupa";
        realGroupNameField.setText(invalidGroupName);

        ServiceResponse failureResponse = new ServiceResponse(false, "Grupa o takiej nazwie już istnieje");
        Mockito.when(groupService.createGroup(invalidGroupName)).thenReturn(failureResponse);

        realCreateButton.fire();

        Mockito.verify(groupService).createGroup(invalidGroupName);
        assertEquals("Grupa o takiej nazwie już istnieje", realMessageLabel.getText());
        assertTrue(realGroupNameField.getText().isEmpty());
    }

    @Test
    void shouldCallServiceAndHandleSuccessWhenCreatingGroupSucceeds() {
        String validGroupName = "Kryptolodzy";
        realGroupNameField.setText(validGroupName);

        ServiceResponse successResponse = new ServiceResponse(true, "Grupa utworzona pomyślnie!");
        Mockito.when(groupService.createGroup(validGroupName)).thenReturn(successResponse);

        realCreateButton.fire();

        Mockito.verify(groupService).createGroup(validGroupName);
        assertEquals("Grupa utworzona pomyślnie!", realMessageLabel.getText());
        assertTrue(realGroupNameField.getText().isEmpty());
    }

    @Test
    void shouldShowErrorWhenGroupCodeIsEmpty() {
        realCodeField.setText("");

        realJoinButton.fire();

        assertEquals("Kod grupy nie może być pusty", realMessageLabel.getText());
        Mockito.verifyNoInteractions(groupService);
    }

    @Test
    void shouldCallServiceAndHandleFailureWhenJoiningGroupFails() {
        String invalidCode = "BAD_CODE";
        realCodeField.setText(invalidCode);

        ServiceResponse failureResponse = new ServiceResponse(false, "Niepoprawny kod grupy");
        Mockito.when(groupService.joinGroup(invalidCode)).thenReturn(failureResponse);

        realJoinButton.fire();

        Mockito.verify(groupService).joinGroup(invalidCode);
        assertEquals("Niepoprawny kod grupy", realMessageLabel.getText());
        assertTrue(realCodeField.getText().isEmpty());
    }

    @Test
    void shouldCallServiceAndHandleSuccessWhenJoiningGroupSucceeds() {
        String validCode = "ABC123XYZ";
        realCodeField.setText(validCode);

        ServiceResponse successResponse = new ServiceResponse(true, "Dołączono do grupy!");
        Mockito.when(groupService.joinGroup(validCode)).thenReturn(successResponse);

        realJoinButton.fire();

        Mockito.verify(groupService).joinGroup(validCode);
        assertEquals("Dołączono do grupy!", realMessageLabel.getText());
        assertTrue(realCodeField.getText().isEmpty());
    }

    @Test
    void shouldClearStorageAndRedirectToLoginOnLogout() {
        TokenStorage.setUser(new User());
        TokenStorage.setCachedToken("some.token");
        realMessageLabel.setText("Stary komunikat");

        // Mockujemy klasę Files, bo TokenStorage.deleteToken() spróbuje usunąć plik z dysku
        try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.deleteIfExists(Mockito.any(Path.class))).thenReturn(true);

            realLogoutButton.fire();

            assertNull(TokenStorage.getUser());
            assertNull(TokenStorage.getCachedToken());
            assertEquals("", realMessageLabel.getText());
            Mockito.verify(goToLogin).run();
            assertTrue(realGroupNameField.getText().isEmpty());
            assertTrue(realCodeField.getText().isEmpty());
        }
    }
}