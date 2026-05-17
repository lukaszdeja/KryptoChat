package Services;


public class GroupRequest {
    public GroupRequest() {}
}

/**
 * Klasa potrzebna do tworzenia obiektów requestów związanych z grupami,
 * które mogą być serializowane do formatu JSON (np. do wysłania do backendu)
 * oraz deserializowane z JSON (odtwarzanie obiektu).
 *
 * Obsługuje:
 * - tworzenie grupy (groupName)
 * - dołączanie do grupy (code)
 */
class CreateGroupRequest extends GroupRequest {

    private String groupName;
    public CreateGroupRequest(String groupName) {
        this.groupName = groupName;
    }
    public CreateGroupRequest() {}

    // Settery
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupName() {
        return groupName;
    }
}

class JoinGroupRequest extends GroupRequest {
    private String code;
    public JoinGroupRequest() {}
    public JoinGroupRequest(String code) {
        this.code = code;
    }
    // Settery
    public void setCode(String code) {
        this.code = code;
    }

    // Gettery
    public String getCode() {
        return code;
    }
}