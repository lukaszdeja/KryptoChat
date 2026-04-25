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
public class GroupRequest {

    private String groupName;
    private String code;
    public GroupRequest() {}

    // Settery
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
    public void setCode(String code) {
        this.code = code;
    }

    // Gettery
    public String getGroupName() {
        return groupName;
    }
    public String getCode() {
        return code;
    }
}