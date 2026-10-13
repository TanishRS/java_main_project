/**
 * Specialized Event subclass: Wedding bookings carry a decor
 * surcharge and a per-day multiplier reflecting higher setup costs.
 */
public class WeddingEvent extends Event {
    private static final double DECOR_SURCHARGE = 15000.0;
    private static final double DAY_MULTIPLIER = 1.30;

    public WeddingEvent(String eventName, int expectedGuests) {
        super(eventName, expectedGuests);
    }

    @Override
    public double calculateCharges(double baseRatePerDay, int days) {
        return (baseRatePerDay * days * DAY_MULTIPLIER) + DECOR_SURCHARGE;
    }

    @Override
    public String getEventCategory() {
        return "WEDDING";
    }
}
