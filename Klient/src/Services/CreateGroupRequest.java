package Services;

/**
 * Klasa potrzebna do tworzenia obiektów requestów związanych z grupami,
 * które mogą być serializowane do formatu JSON (np. do wysłania do backendu)
 * oraz deserializowane z JSON (odtwarzanie obiektu).
 *
 * Obsługuje:
 * - tworzenie grupy (groupName)
 * - dołączanie do grupy (code)
 */
public class CreateGroupRequest {

    private String groupName;
    private String username;
    public CreateGroupRequest() {}

    // Settery
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    // Gettery
    public String getGroupName() {
        return groupName;
    }
    public String getUsername() {
        return username;
    }
}

class JoinGroupRequest {
    private String code;
    private String username;
    public JoinGroupRequest() {}

    // Settery
    public void setCode(String code) {
        this.code = code;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    // Gettery
    public String getCode() {
        return code;
    }
    public String getUsername() {
        return username;
    }
}