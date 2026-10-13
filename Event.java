import java.io.Serializable;

/**
 * Abstract base class representing a common "event" entity.
 * INHERITANCE: WeddingEvent, CorporateEvent, and BirthdayEvent are
 * specialized classes derived from this common entity.
 * POLYMORPHISM: calculateCharges() and getEventCategory() are
 * overridden differently by each subclass, but callers can treat
 * every event uniformly through this common reference type.
 */
public abstract class Event implements Serializable {
    protected String eventName;
    protected int expectedGuests;

    public Event(String eventName, int expectedGuests) {
        if (eventName == null || eventName.trim().isEmpty()) {
            throw new IllegalArgumentException("Event name cannot be empty.");
        }
        if (expectedGuests <= 0) {
            throw new IllegalArgumentException("Expected guests must be positive.");
        }
        this.eventName = eventName;
        this.expectedGuests = expectedGuests;
    }

    public String getEventName() { return eventName; }
    public int getExpectedGuests() { return expectedGuests; }

    /** POLYMORPHISM: every event type calculates its own charge multiplier/add-ons. */
    public abstract double calculateCharges(double baseRatePerDay, int days);

    /** POLYMORPHISM: every event type reports its own category label. */
    public abstract String getEventCategory();

    @Override
    public String toString() {
        return String.format("%s [%s] - Guests: %d", eventName, getEventCategory(), expectedGuests);
    }
}
