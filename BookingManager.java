import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Central business-logic layer. Uses ArrayList (Java Collections)
 * to store dynamic records of Halls, Customers, and Bookings.
 * Encapsulates all the objectives: maintain hall info, register
 * customers/events, check availability, calculate charges, and
 * track payments/cancellations.
 */
public class BookingManager {

    private final List<Hall> halls = new ArrayList<>();
    private final List<Customer> customers = new ArrayList<>();
    private final List<Booking> bookings = new ArrayList<>();

    // ---------- Hall management ----------

    public Hall addHall(String name, int capacity, double rate) {
        Hall h = new Hall(name, capacity, rate);
        halls.add(h);
        return h;
    }

    public List<Hall> getHalls() {
        return halls;
    }

    public void removeHall(int hallId) throws InvalidBookingException {
        for (Booking b : bookings) {
            if (b.getHall().getHallId() == hallId && b.getStatus() != BookingStatus.CANCELLED) {
                throw new InvalidBookingException("Cannot remove hall: it has active bookings.");
            }
        }
        halls.removeIf(h -> h.getHallId() == hallId);
    }

    // ---------- Customer management ----------

    public Customer registerCustomer(String name, String phone, String email, String tier) {
        Customer c = new Customer(name, phone, email, tier);
        customers.add(c);
        return c;
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    // ---------- Availability ----------

    /**
     * Checks whether a hall is free for the given date range.
     * Ignores cancelled bookings when checking overlaps.
     */
    public boolean isHallAvailable(Hall hall, LocalDate start, LocalDate end) {
        for (Booking b : bookings) {
            if (b.getHall().getHallId() == hall.getHallId()
                    && b.getStatus() != BookingStatus.CANCELLED
                    && b.overlaps(start, end)) {
                return false;
            }
        }
        return true;
    }

    public List<Hall> getAvailableHalls(LocalDate start, LocalDate end, int guestCount) {
        List<Hall> available = new ArrayList<>();
        for (Hall h : halls) {
            if (h.getCapacity() >= guestCount && isHallAvailable(h, start, end)) {
                available.add(h);
            }
        }
        return available;
    }

    // ---------- Booking creation (validation + exception handling) ----------

    public Booking createBooking(Hall hall, Customer customer, Event event,
                                  LocalDate start, LocalDate end)
            throws HallNotAvailableException, InvalidBookingException {

        if (hall == null || customer == null || event == null) {
            throw new InvalidBookingException("Hall, customer, and event must all be selected.");
        }
        if (start == null || end == null || end.isBefore(start)) {
            throw new InvalidBookingException("Invalid date range: end date must not be before start date.");
        }
        if (start.isBefore(LocalDate.now())) {
            throw new InvalidBookingException("Booking start date cannot be in the past.");
        }
        if (event.getExpectedGuests() > hall.getCapacity()) {
            throw new InvalidBookingException(String.format(
                    "Expected guests (%d) exceed hall capacity (%d).",
                    event.getExpectedGuests(), hall.getCapacity()));
        }
        if (!isHallAvailable(hall, start, end)) {
            throw new HallNotAvailableException(String.format(
                    "%s is already booked for an overlapping date range.", hall.getName()));
        }

        Booking booking = new Booking(hall, customer, event, start, end);
        bookings.add(booking);
        return booking;
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public Booking findBooking(int bookingId) throws InvalidBookingException {
        for (Booking b : bookings) {
            if (b.getBookingId() == bookingId) return b;
        }
        throw new InvalidBookingException("No booking found with ID " + bookingId);
    }

    // ---------- Payments ----------

    public Payment recordPayment(int bookingId, double amount, String mode)
            throws InvalidBookingException {
        Booking b = findBooking(bookingId);
        if (b.getStatus() == BookingStatus.CANCELLED) {
            throw new InvalidBookingException("Cannot pay towards a cancelled booking.");
        }
        if (amount > b.getBalanceDue() + 0.001) {
            throw new InvalidBookingException(String.format(
                    "Payment (Rs.%.2f) exceeds balance due (Rs.%.2f).", amount, b.getBalanceDue()));
        }
        Payment p = new Payment(bookingId, amount, mode);
        b.addPayment(p);
        return p;
    }

    // ---------- Cancellation ----------

    public void cancelBooking(int bookingId) throws InvalidBookingException {
        Booking b = findBooking(bookingId);
        if (b.getStatus() == BookingStatus.CANCELLED) {
            throw new InvalidBookingException("Booking is already cancelled.");
        }
        b.cancel();
    }

    // ---------- Reports / summaries ----------

    public double getTotalRevenueCollected() {
        double sum = 0;
        for (Booking b : bookings) {
            if (b.getStatus() != BookingStatus.CANCELLED) sum += b.getAmountPaid();
        }
        return Math.round(sum * 100.0) / 100.0;
    }

    public double getTotalOutstandingBalance() {
        double sum = 0;
        for (Booking b : bookings) {
            if (b.getStatus() != BookingStatus.CANCELLED) sum += b.getBalanceDue();
        }
        return Math.round(sum * 100.0) / 100.0;
    }

    public int countByStatus(BookingStatus status) {
        int count = 0;
        for (Booking b : bookings) {
            if (b.getStatus() == status) count++;
        }
        return count;
    }

    public String buildSummaryReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("===== EVENT HALL BOOKING SYSTEM - SUMMARY REPORT =====\n\n");
        sb.append(String.format("Total Halls Registered     : %d%n", halls.size()));
        sb.append(String.format("Total Customers Registered : %d%n", customers.size()));
        sb.append(String.format("Total Bookings Created     : %d%n", bookings.size()));
        sb.append(String.format("  - Pending                : %d%n", countByStatus(BookingStatus.PENDING)));
        sb.append(String.format("  - Partially Paid         : %d%n", countByStatus(BookingStatus.PARTIALLY_PAID)));
        sb.append(String.format("  - Confirmed              : %d%n", countByStatus(BookingStatus.CONFIRMED)));
        sb.append(String.format("  - Cancelled              : %d%n", countByStatus(BookingStatus.CANCELLED)));
        sb.append(String.format("%nTotal Revenue Collected    : Rs.%.2f%n", getTotalRevenueCollected()));
        sb.append(String.format("Total Outstanding Balance  : Rs.%.2f%n", getTotalOutstandingBalance()));
        return sb.toString();
    }
}
