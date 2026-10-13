/**
 * Enumerates the lifecycle states of a booking.
 */
public enum BookingStatus {
    PENDING,      // created, awaiting payment
    CONFIRMED,    // fully paid
    PARTIALLY_PAID,
    CANCELLED,
    COMPLETED     // event date has passed
}
