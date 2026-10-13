import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a single payment transaction against a booking.
 * Tracking payments is a core objective of the system.
 */
public class Payment implements Serializable {
    private static int counter = 5000;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private final int paymentId;
    private final int bookingId;
    private final double amount;
    private final LocalDateTime timestamp;
    private final String mode; // CASH, CARD, UPI

    public Payment(int bookingId, double amount, String mode) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive.");
        }
        this.paymentId = counter++;
        this.bookingId = bookingId;
        this.amount = amount;
        this.mode = (mode == null || mode.isBlank()) ? "CASH" : mode.toUpperCase();
        this.timestamp = LocalDateTime.now();
    }

    public int getPaymentId() { return paymentId; }
    public int getBookingId() { return bookingId; }
    public double getAmount() { return amount; }
    public String getMode() { return mode; }

    @Override
    public String toString() {
        return String.format("Payment#%d | Booking#%d | Rs.%.2f | %s | %s",
                paymentId, bookingId, amount, mode, timestamp.format(FMT));
    }
}
