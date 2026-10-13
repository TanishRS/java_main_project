import java.io.Serializable;

/**
 * Represents a hall owned by the event-management company.
 * Maintaining hall information (name, capacity, rate) is one of the
 * core objectives of the system.
 */
public class Hall implements Serializable {
    private static int counter = 100;

    private final int hallId;
    private String name;
    private int capacity;
    private double baseRatePerDay; // base rental cost per booking day

    public Hall(String name, int capacity, double baseRatePerDay) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Hall name cannot be empty.");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("Hall capacity must be positive.");
        }
        if (baseRatePerDay < 0) {
            throw new IllegalArgumentException("Base rate cannot be negative.");
        }
        this.hallId = counter++;
        this.name = name;
        this.capacity = capacity;
        this.baseRatePerDay = baseRatePerDay;
    }

    public int getHallId() { return hallId; }
    public String getName() { return name; }
    public int getCapacity() { return capacity; }
    public double getBaseRatePerDay() { return baseRatePerDay; }

    public void setName(String name) { this.name = name; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public void setBaseRatePerDay(double baseRatePerDay) { this.baseRatePerDay = baseRatePerDay; }

    @Override
    public String toString() {
        return String.format("Hall#%d | %-20s | Capacity: %-4d | Rate/day: Rs.%.2f",
                hallId, name, capacity, baseRatePerDay);
    }
}
