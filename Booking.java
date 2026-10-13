import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * A booking ties together a Hall, a Customer, and an Event
 * (polymorphic reference - could be Wedding/Corporate/Birthday)
 * across a date range, and tracks payments/status against it.
 */
public class Booking implements Serializable {
    private static int counter = 1000;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final int bookingId;
    private final Hall hall;
    private final Customer customer;
    private final Event event; // polymorphic: Event reference, concrete Wedding/Corporate/Birthday object
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final double totalCharge;
    private BookingStatus status;
    private final List<Payment> payments;

    public Booking(Hall hall, Customer customer, Event event, LocalDate startDate, LocalDate endDate) {
        this.bookingId = counter++;
        this.hall = hall;
        this.customer = customer;
        this.event = event;
        this.startDate = startDate;
        this.endDate = endDate;
        int days = (int) (endDate.toEpochDay() - startDate.toEpochDay()) + 1;
        // POLYMORPHIC CALL: actual subclass's calculateCharges() runs at runtime
        double rawCharge = event.calculateCharges(hall.getBaseRatePerDay(), days);
        double discount = rawCharge * customer.getDiscountRate();
        this.totalCharge = round2(rawCharge - discount);
        this.status = BookingStatus.PENDING;
        this.payments = new ArrayList<>();
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    public int getBookingId() { return bookingId; }
    public Hall getHall() { return hall; }
    public Customer getCustomer() { return customer; }
    public Event getEvent() { return event; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public double getTotalCharge() { return totalCharge; }
    public BookingStatus getStatus() { return status; }
    public List<Payment> getPayments() { return payments; }

    public int getDays() {
        return (int) (endDate.toEpochDay() - startDate.toEpochDay()) + 1;
    }

    public double getAmountPaid() {
        double sum = 0;
        for (Payment p : payments) sum += p.getAmount();
        return round2(sum);
    }

    public double getBalanceDue() {
        return round2(totalCharge - getAmountPaid());
    }

    public void addPayment(Payment p) {
        payments.add(p);
        refreshStatus();
    }

    public void cancel() {
        this.status = BookingStatus.CANCELLED;
    }

    private void refreshStatus() {
        if (status == BookingStatus.CANCELLED) return;
        double paid = getAmountPaid();
        if (paid <= 0) {
            status = BookingStatus.PENDING;
        } else if (paid < totalCharge) {
            status = BookingStatus.PARTIALLY_PAID;
        } else {
            status = BookingStatus.CONFIRMED;
        }
    }

    /** Two bookings overlap if their date ranges intersect (for availability checks). */
    public boolean overlaps(LocalDate otherStart, LocalDate otherEnd) {
        return !startDate.isAfter(otherEnd) && !otherStart.isAfter(endDate);
    }

    @Override
    public String toString() {
        return String.format(
                "Booking#%-5d | %-15s | %-12s | %-9s | %s to %s (%dd) | Charge: Rs.%.2f | Paid: Rs.%.2f | Due: Rs.%.2f | Status: %s",
                bookingId, customer.getName(), event.getEventCategory(), hall.getName(),
                startDate.format(FMT), endDate.format(FMT), getDays(),
                totalCharge, getAmountPaid(), getBalanceDue(), status);
    }
}
