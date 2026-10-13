/**
 * Custom checked exception thrown when booking input violates a
 * business rule (e.g., guests exceed hall capacity, invalid dates).
 */
public class InvalidBookingException extends Exception {
    public InvalidBookingException(String message) {
        super(message);
    }
}
