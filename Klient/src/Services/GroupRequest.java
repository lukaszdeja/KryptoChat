package Services;

/**
 * Klasa bazowa dla requestów związanych z operacjami na grupach.
 * Umożliwia serializację/deserializację obiektów do JSON przesyłanych do backendu.
 */
public class GroupRequest {
    public GroupRequest() {}
}

/**
 * Request używany przy tworzeniu nowej grupy.
 * Zawiera nazwę grupy wysyłaną do backendu.
 */
class CreateGroupRequest extends GroupRequest {

    private String groupName;
    private String creatorKey;
    public CreateGroupRequest(String groupName, String key) {
        this.creatorKey = key;
        this.groupName = groupName;
    }
    public CreateGroupRequest() {}

    /**
     * Ustawia nazwę grupy.
     * @param groupName nazwa grupy
     */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    /**
     * Zwraca nazwę grupy.
     * @return nazwa grupy
     */
    public String getGroupName() {
        return groupName;
    }

    public String getCreatorKey() { return creatorKey; }
    public void setCreatorKey(String key) { this.creatorKey = key; }
}

/**
 * Request używany przy dołączaniu do istniejącej grupy.
 * Zawiera kod grupy.
 */
class JoinGroupRequest extends GroupRequest {
    private String code;
    public JoinGroupRequest() {}
    public JoinGroupRequest(String code) {
        this.code = code;
    }

    /**
     * Ustawia kod grupy.
     * @return nic nie zwraca
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Zwraca kod grupy.
     * @return kod grupy
     */
    public String getCode() {
        return code;
    }
}