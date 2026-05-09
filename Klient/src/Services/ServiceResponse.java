package Services;

/**
 * Klasa przechowująca odpowiedź z serwisu.
 * Zawiera informacje o powodzeniu operacji oraz komunikat zwracany do kontrolera.
 */
public class ServiceResponse {

    private boolean success;
    private String message;

    public ServiceResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    /**
     * Getter pola success
     * @return boolean - czy operacja zakończyła się sukcesem
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Setter pola success
     * @param success ustawiana wartość sukcesu operacji
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * Getter pola message
     * @return String - komunikat zwracany przez serwis
     */
    public String getMessage() {
        return message;
    }

    /**
     * Setter pola message
     * @param message ustawiany komunikat
     */
    public void setMessage(String message) {
        this.message = message;
    }
}