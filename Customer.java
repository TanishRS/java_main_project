/**
 * Customer extends Person -> INHERITANCE (specialized class from a
 * common Person entity). Registering customers is a core objective.
 */
public class Customer extends Person {
    private static int counter = 1;
    private final int customerId;
    private String loyaltyTier; // STANDARD, SILVER, GOLD - affects discount

    public Customer(String name, String phone, String email, String loyaltyTier) {
        super(name, phone, email);
        this.customerId = counter++;
        this.loyaltyTier = (loyaltyTier == null || loyaltyTier.trim().isEmpty())
                ? "STANDARD" : loyaltyTier.toUpperCase();
    }

    public int getCustomerId() { return customerId; }
    public String getLoyaltyTier() { return loyaltyTier; }

    /** Discount fraction applied to booking charges based on loyalty tier. */
    public double getDiscountRate() {
        switch (loyaltyTier) {
            case "GOLD": return 0.15;
            case "SILVER": return 0.08;
            default: return 0.0;
        }
    }

    @Override
    public String getDetails() {
        return String.format("Customer#%d | %-15s | %-10s | %-25s | Tier:%s",
                customerId, name, phone, email, loyaltyTier);
    }

    @Override
    public String toString() {
        return getDetails();
    }
}
