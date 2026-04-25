package Services;

public class GroupService {

    public boolean createGroup(String groupName) {
        System.out.println("Tworzenie grupy: " + groupName);

        return groupName.length() >= 3;
    }

    public boolean joinGroup(String code) {
        System.out.println("Dołączanie do grupy: " + code);

        return code.length() >= 4; //domyslnie czy kod znajduje sie w bazie
    }
}