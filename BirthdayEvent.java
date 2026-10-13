/**
 * Specialized Event subclass: Birthday/private parties get a
 * discounted day multiplier and a small decoration fee.
 */
public class BirthdayEvent extends Event {
    private static final double DECOR_FEE = 4000.0;
    private static final double DAY_MULTIPLIER = 0.90;

    public BirthdayEvent(String eventName, int expectedGuests) {
        super(eventName, expectedGuests);
    }

    @Override
    public double calculateCharges(double baseRatePerDay, int days) {
        return (baseRatePerDay * days * DAY_MULTIPLIER) + DECOR_FEE;
    }

    @Override
    public String getEventCategory() {
        return "BIRTHDAY";
    }
}
