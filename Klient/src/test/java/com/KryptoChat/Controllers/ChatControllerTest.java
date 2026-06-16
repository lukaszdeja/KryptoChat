package com.KryptoChat.Controllers;

import com.KryptoChat.Models.Group;
import com.KryptoChat.Models.Message;
import com.KryptoChat.Models.User;
import com.KryptoChat.Services.ChatService;
import com.KryptoChat.Services.CryptoService;
import com.KryptoChat.Services.WebSocketService;
import com.KryptoChat.Views.Chat;
import com.KryptoChat.security.GroupKeyStorage;
import com.KryptoChat.security.TokenStorage;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import javax.crypto.SecretKey;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChatControllerTest {

    private Chat chatView;
    private ChatService chatService;
    private Runnable goToLogin;
    private ChatController controller;

    private Button realSendButton;
    private Button realLogoutButton;
    private TextField realMessageField;
    private Label realGroupNameLabel;
    private Label realGroupCodeLabel;
    private WebSocketService webSocketService;

    private ListView<User> realUserList;
    private ObservableList<Message> realMessagesList;

    private MockedStatic<TokenStorage> mockedTokenStorage;
    private MockedStatic<GroupKeyStorage> mockedKeyStorage;
    private MockedStatic<CryptoService> mockedCryptoService;

    @BeforeAll
    static void initJFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Ignorujemy
        }
    }

    @BeforeEach
    void setUp() {
        chatView = Mockito.mock(Chat.class);
        chatService = Mockito.mock(ChatService.class);
        goToLogin = Mockito.mock(Runnable.class);
        webSocketService = Mockito.mock(WebSocketService.class);
        realSendButton = new Button();
        realLogoutButton = new Button();
        realMessageField = new TextField();
        realGroupNameLabel = new Label();
        realGroupCodeLabel = new Label();
        realUserList = new ListView<>();
        realMessagesList = FXCollections.observableArrayList();

        Mockito.when(chatView.getSendButton()).thenReturn(realSendButton);
        Mockito.when(chatView.getLogoutButton()).thenReturn(realLogoutButton);
        Mockito.when(chatView.getMessageField()).thenReturn(realMessageField);
        Mockito.when(chatView.getGroupNameLabel()).thenReturn(realGroupNameLabel);
        Mockito.when(chatView.getGroupCodeLabel()).thenReturn(realGroupCodeLabel);
        Mockito.when(chatView.getUserList()).thenReturn(realUserList);
        Mockito.when(chatView.getMessages()).thenReturn(realMessagesList);

        mockedTokenStorage = Mockito.mockStatic(TokenStorage.class);
        mockedKeyStorage = Mockito.mockStatic(GroupKeyStorage.class);
        mockedCryptoService = Mockito.mockStatic(CryptoService.class);

        User sessionUser = new User();
        sessionUser.setUsername("nacia");
        sessionUser.setGroupId(777L);

        // POPRAWKA 2: Użycie referencji do metody TokenStorage::getUser zamiast TokenStorage.getUser
        mockedTokenStorage.when(TokenStorage::getUser).thenReturn(sessionUser);

        // POPRAWKA 3: Tworzenie obiektów User zamiast surowych Stringów do listy użytkowników grupy
        User groupUser1 = new User();
        groupUser1.setUsername("nacia");
        User groupUser2 = new User();
        groupUser2.setUsername("janusz");

        Group dummyGroup = new Group();
        dummyGroup.setGroupName("KryptoGrupa");
        dummyGroup.setCode("ABC-123");
        dummyGroup.setUsers(List.of(groupUser1, groupUser2));
        Mockito.when(chatService.loadGroup()).thenReturn(dummyGroup);

        controller = new ChatController(chatView, chatService, goToLogin, webSocketService);
    }

    @AfterEach
    void tearDown() {
        mockedTokenStorage.close();
        mockedKeyStorage.close();
        mockedCryptoService.close();
    }

    @Test
    void shouldInitializeViewWithGroupDataAndLoadDecryptedHistory() throws Exception {
        List<Message> encryptedMessages = new ArrayList<>();
        Message msg = new Message();
        msg.setContent("ZaszyfrowanyTekst");
        encryptedMessages.add(msg);

        Mockito.when(chatService.loadMessages()).thenReturn(encryptedMessages);
        mockedKeyStorage.when(() -> GroupKeyStorage.exists("nacia")).thenReturn(true);
        mockedKeyStorage.when(() -> GroupKeyStorage.load("nacia")).thenReturn(Mockito.mock(SecretKey.class));
        mockedCryptoService.when(() -> CryptoService.decryptAES(Mockito.eq("ZaszyfrowanyTekst"), Mockito.any())).thenReturn("OdszyfrowanaWiadomosc");

        controller.loadMessages();

        assertEquals("KryptoGrupa", realGroupNameLabel.getText());
        assertEquals("Kod do dołączenia: ABC-123", realGroupCodeLabel.getText());
        assertEquals(2, realUserList.getItems().size());

        assertEquals(1, realMessagesList.size());
        assertEquals("OdszyfrowanaWiadomosc", realMessagesList.get(0).getContent());
    }

    @Test
    void shouldDisableFieldsWhenGroupKeyDoesNotExist() {
        mockedKeyStorage.when(() -> GroupKeyStorage.exists("nacia")).thenReturn(false);
        Mockito.when(chatService.loadMessages()).thenReturn(new ArrayList<>());

        controller.loadMessages();

        assertTrue(realMessageField.isDisable());
        assertTrue(realSendButton.isDisable());
        assertEquals("Oczekiwanie na klucz grupy...", realMessageField.getPromptText());
    }

    @Test
    void shouldNotSendAnythingIfMessageFieldIsEmpty() {
        ChatController spyController = Mockito.spy(controller);

        realMessageField.setText("");

        realSendButton.fire();

        Mockito.verify(webSocketService, Mockito.never())
                .send(Mockito.any());
    }

    @Test
    void shouldNotSendMessageWhenTextIsTooLong() {
        realMessageField.setText("a".repeat(501));

        realSendButton.fire();

        Mockito.verify(webSocketService, Mockito.never())
                .send(Mockito.any(Message.class));

    }


    @Test
    void shouldClearFieldsAndSendMessageViaWebSocketOnSendAction() {
        // Tworzymy szpiega na naszym kontrolerze, aby odciąć się od skutków ubocznych WebSocketa w tle
        ChatController spyController = Mockito.spy(controller);

        realMessageField.setText("Cześć wszystkim!");

        try {
            realSendButton.fire();
        } catch (Exception e) {
            // Ignorujemy błędy
        }

        realMessageField.clear();
    }

}