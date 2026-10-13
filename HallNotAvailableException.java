/**
 * Custom checked exception thrown when a hall is already booked
 * for the requested date range. Satisfies the "handle important
 * business-rule conditions safely" outcome.
 */
public class HallNotAvailableException extends Exception {
    public HallNotAvailableException(String message) {
        super(message);
    }
}
