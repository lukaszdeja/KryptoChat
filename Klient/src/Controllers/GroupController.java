package Controllers;

import Views.CreateGroup;
import Services.GroupService;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

public class GroupController {

    private CreateGroup groupView;
    private GroupService groupService;
    Runnable goToChats;

    public GroupController(CreateGroup view, GroupService service, Runnable chats) {
        this.groupView = view;
        this.groupService = service;
        this.goToChats = chats;
        init();
    }

    private void init() {
        groupView.getCreate().setOnAction(e -> createGroup());
        groupView.getJoin().setOnAction(e -> joinGroup());
    }

    private void createGroup() {
        String groupName = groupView.getGroupNameField().getText();
        if (groupName.equals("")) {
            groupView.getMessage().setText("Nazwa grupy nie może być pusta");
            return;
        }
        boolean success = groupService.createGroup(groupName);
    }

    private void joinGroup() {

    }
}
