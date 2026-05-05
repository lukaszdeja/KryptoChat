package Services;

import Models.Group;
import Models.User;

public class ChatService {
    public Group loadUsers() {
        Group group = new Group();
        group.setGroupName("Grupa 1");
        group.setCode("#a4vd2c");
        group.addUser(new User(1L, "lukasz", 1L));
        group.addUser(new User(2L, "nacia", 1L));
        return group;
    }
}
