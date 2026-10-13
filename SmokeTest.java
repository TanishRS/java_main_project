import java.time.LocalDate;

/**
 * Console-based smoke test that exercises BookingManager business
 * logic without needing a display (headless environments). This is
 * NOT part of the delivered GUI app; it's a verification harness.
 */
public class SmokeTest {
    static int pass = 0, fail = 0;

    public static void main(String[] args) {
        BookingManager mgr = new BookingManager();

        // 1. Hall management
        Hall grand = mgr.addHall("Grand Ballroom", 300, 50000);
        Hall garden = mgr.addHall("Garden Pavilion", 150, 30000);
        check("2 halls added", mgr.getHalls().size() == 2);

        // 2. Customer registration + validation
        Customer rohan = mgr.registerCustomer("Rohan Mehta", "9876543210", "rohan@example.com", "GOLD");
        Customer priya = mgr.registerCustomer("Priya Shah", "9123456780", "priya@example.com", "STANDARD");
        check("2 customers registered", mgr.getCustomers().size() == 2);
        check("Gold discount is 15%", rohan.getDiscountRate() == 0.15);

        try {
            mgr.registerCustomer("Bad Phone", "12345", "x@x.com", "STANDARD");
            check("invalid phone should throw", false);
        } catch (IllegalArgumentException ex) {
            check("invalid phone rejected: " + ex.getMessage(), true);
        }

        // 3. Polymorphism check: Event references, different calculateCharges()
        Event wedding = new WeddingEvent("Rohan-Anika Wedding", 250);
        Event corporate = new CorporateEvent("TechCorp Annual Meet", 60);
        Event birthday = new BirthdayEvent("Aarav's 5th Birthday", 40);
        check("Wedding category", wedding.getEventCategory().equals("WEDDING"));
        check("Corporate charge > 0", corporate.calculateCharges(30000, 1) > 0);
        check("Different subclasses -> different charge formulas",
                wedding.calculateCharges(50000, 2) != corporate.calculateCharges(50000, 2));

        // 4. Availability check + booking creation
        LocalDate start = LocalDate.now().plusDays(10);
        LocalDate end = LocalDate.now().plusDays(11);
        check("Grand Ballroom available initially", mgr.isHallAvailable(grand, start, end));

        Booking b1;
        try {
            b1 = mgr.createBooking(grand, rohan, wedding, start, end);
            check("Booking#1 created", b1 != null);
            check("Charge includes GOLD discount", b1.getTotalCharge() < (50000 * 2 * 1.30 + 15000));
        } catch (Exception ex) {
            check("booking1 should succeed: " + ex.getMessage(), false);
            return;
        }

        // 5. Overlap should now fail with HallNotAvailableException
        try {
            mgr.createBooking(grand, priya, birthday, start, end);
            check("overlapping booking should throw", false);
        } catch (HallNotAvailableException ex) {
            check("overlap correctly rejected: " + ex.getMessage(), true);
        } catch (InvalidBookingException ex) {
            check("wrong exception type for overlap", false);
        }

        // 6. Capacity violation should fail
        try {
            Event bigEvent = new CorporateEvent("Huge Conf", 500);
            mgr.createBooking(garden, priya, bigEvent, start.plusDays(20), end.plusDays(20));
            check("capacity violation should throw", false);
        } catch (InvalidBookingException ex) {
            check("capacity violation correctly rejected: " + ex.getMessage(), true);
        } catch (HallNotAvailableException ex) {
            check("wrong exception type for capacity", false);
        }

        // 7. Payments and status transitions
        try {
            Booking b1ref = mgr.findBooking(b1.getBookingId());
            check("Initial status PENDING", b1ref.getStatus() == BookingStatus.PENDING);
            mgr.recordPayment(b1.getBookingId(), b1ref.getTotalCharge() / 2, "UPI");
            check("Partial payment -> PARTIALLY_PAID", b1ref.getStatus() == BookingStatus.PARTIALLY_PAID);
            mgr.recordPayment(b1.getBookingId(), b1ref.getBalanceDue(), "CARD");
            check("Full payment -> CONFIRMED", b1ref.getStatus() == BookingStatus.CONFIRMED);
            check("Balance due is now 0", Math.abs(b1ref.getBalanceDue()) < 0.01);
        } catch (InvalidBookingException ex) {
            check("payments should succeed: " + ex.getMessage(), false);
        }

        // 8. Overpayment should be rejected
        try {
            mgr.recordPayment(b1.getBookingId(), 999999, "CASH");
            check("overpayment should throw", false);
        } catch (InvalidBookingException ex) {
            check("overpayment correctly rejected: " + ex.getMessage(), true);
        }

        // 9. Second booking + cancellation frees up the hall
        try {
            Booking b2 = mgr.createBooking(garden, priya, birthday, start.plusDays(5), start.plusDays(6));
            check("Booking#2 created", b2 != null);
            mgr.cancelBooking(b2.getBookingId());
            check("Booking#2 cancelled", b2.getStatus() == BookingStatus.CANCELLED);
            check("Hall free again after cancel",
                    mgr.isHallAvailable(garden, start.plusDays(5), start.plusDays(6)));
        } catch (Exception ex) {
            check("booking2/cancel flow should succeed: " + ex.getMessage(), false);
        }

        // 10. Report generation
        String report = mgr.buildSummaryReport();
        check("Report contains revenue line", report.contains("Total Revenue Collected"));

        System.out.println("\n" + report);
        System.out.println("\n===== RESULTS: " + pass + " passed, " + fail + " failed =====");
        if (fail > 0) System.exit(1);
    }

    static void check(String label, boolean condition) {
        if (condition) {
            pass++;
            System.out.println("[PASS] " + label);
        } else {
            fail++;
            System.out.println("[FAIL] " + label);
        }
    }
}
