package Services;

import Models.User;

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

    private User user;
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
    public void setUser(User user) { this.user = user; }

    // Gettery
    public String getGroupName() {
        return groupName;
    }
    public String getCode() {
        return code;
    }
    public User getUser() { return this.user; }
}