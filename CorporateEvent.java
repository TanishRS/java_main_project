/**
 * Specialized Event subclass: Corporate events add an AV/equipment
 * fee but no day multiplier (standard setup).
 */
public class CorporateEvent extends Event {
    private static final double AV_EQUIPMENT_FEE = 8000.0;

    public CorporateEvent(String eventName, int expectedGuests) {
        super(eventName, expectedGuests);
    }

    @Override
    public double calculateCharges(double baseRatePerDay, int days) {
        return (baseRatePerDay * days) + AV_EQUIPMENT_FEE;
    }

    @Override
    public String getEventCategory() {
        return "CORPORATE";
    }
}
