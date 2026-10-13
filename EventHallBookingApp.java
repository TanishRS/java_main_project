import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Main GUI application (Swing) for the Event Hall Booking System.
 * Tabs correspond directly to the case-study objectives:
 *   1. Halls        - maintain hall information
 *   2. Customers     - register customers
 *   3. Book Event    - register events, check availability, calculate charges
 *   4. Payments      - track payments and cancellations
 *   5. Reports       - application summaries
 */
public class EventHallBookingApp extends JFrame {

    private final BookingManager manager = new BookingManager();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    // Halls tab widgets
    private DefaultTableModel hallTableModel;
    private JTextField hallNameField, hallCapacityField, hallRateField;

    // Customers tab widgets
    private DefaultTableModel customerTableModel;
    private JTextField custNameField, custPhoneField, custEmailField;
    private JComboBox<String> custTierBox;

    // Booking tab widgets
    private DefaultTableModel bookingTableModel;
    private JComboBox<Hall> hallBox;
    private JComboBox<Customer> customerBox;
    private JComboBox<String> eventTypeBox;
    private JTextField eventNameField, guestsField, startDateField, endDateField;
    private JLabel chargePreviewLabel;

    // Payments tab widgets
    private JTextField bookingIdField, paymentAmountField;
    private JComboBox<String> paymentModeBox;
    private JTextArea paymentLogArea;

    // Reports tab
    private JTextArea reportArea;

    public EventHallBookingApp() {
        super("Event Hall Booking System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Halls", buildHallsTab());
        tabs.addTab("Customers", buildCustomersTab());
        tabs.addTab("Book Event", buildBookingTab());
        tabs.addTab("Payments & Cancellations", buildPaymentsTab());
        tabs.addTab("Reports", buildReportsTab());

        add(tabs);
        seedSampleData();
        refreshAllTables();
    }

    // ==================== HALLS TAB ====================

    private JPanel buildHallsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel form = new JPanel(new GridLayout(2, 4, 8, 8));
        form.setBorder(BorderFactory.createTitledBorder("Add / Maintain Hall"));
        hallNameField = new JTextField();
        hallCapacityField = new JTextField();
        hallRateField = new JTextField();
        JButton addHallBtn = new JButton("Add Hall");
        JButton removeHallBtn = new JButton("Remove Selected");

        form.add(new JLabel("Hall Name:"));
        form.add(new JLabel("Capacity:"));
        form.add(new JLabel("Rate/Day (Rs.):"));
        form.add(new JLabel(""));
        form.add(hallNameField);
        form.add(hallCapacityField);
        form.add(hallRateField);
        form.add(addHallBtn);

        hallTableModel = new DefaultTableModel(
                new Object[]{"ID", "Name", "Capacity", "Rate/Day"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable hallTable = new JTable(hallTableModel);

        addHallBtn.addActionListener(e -> onAddHall());
        removeHallBtn.addActionListener(e -> onRemoveHall(hallTable));

        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        south.add(removeHallBtn);

        panel.add(form, BorderLayout.NORTH);
        panel.add(new JScrollPane(hallTable), BorderLayout.CENTER);
        panel.add(south, BorderLayout.SOUTH);
        return panel;
    }

    private void onAddHall() {
        try {
            String name = hallNameField.getText().trim();
            int capacity = Integer.parseInt(hallCapacityField.getText().trim());
            double rate = Double.parseDouble(hallRateField.getText().trim());
            manager.addHall(name, capacity, rate);
            hallNameField.setText("");
            hallCapacityField.setText("");
            hallRateField.setText("");
            refreshAllTables();
            info("Hall added successfully.");
        } catch (NumberFormatException ex) {
            error("Capacity and Rate must be valid numbers.");
        } catch (IllegalArgumentException ex) {
            error(ex.getMessage());
        }
    }

    private void onRemoveHall(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) { error("Select a hall to remove."); return; }
        int hallId = (int) hallTableModel.getValueAt(row, 0);
        try {
            manager.removeHall(hallId);
            refreshAllTables();
            info("Hall removed.");
        } catch (InvalidBookingException ex) {
            error(ex.getMessage());
        }
    }

    // ==================== CUSTOMERS TAB ====================

    private JPanel buildCustomersTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel form = new JPanel(new GridLayout(2, 5, 8, 8));
        form.setBorder(BorderFactory.createTitledBorder("Register Customer"));
        custNameField = new JTextField();
        custPhoneField = new JTextField();
        custEmailField = new JTextField();
        custTierBox = new JComboBox<>(new String[]{"STANDARD", "SILVER", "GOLD"});
        JButton addCustBtn = new JButton("Register");

        form.add(new JLabel("Name:"));
        form.add(new JLabel("Phone (10 digits):"));
        form.add(new JLabel("Email:"));
        form.add(new JLabel("Loyalty Tier:"));
        form.add(new JLabel(""));
        form.add(custNameField);
        form.add(custPhoneField);
        form.add(custEmailField);
        form.add(custTierBox);
        form.add(addCustBtn);

        customerTableModel = new DefaultTableModel(
                new Object[]{"ID", "Name", "Phone", "Email", "Tier", "Discount"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable custTable = new JTable(customerTableModel);

        addCustBtn.addActionListener(e -> onAddCustomer());

        panel.add(form, BorderLayout.NORTH);
        panel.add(new JScrollPane(custTable), BorderLayout.CENTER);
        return panel;
    }

    private void onAddCustomer() {
        try {
            String name = custNameField.getText().trim();
            String phone = custPhoneField.getText().trim();
            String email = custEmailField.getText().trim();
            String tier = (String) custTierBox.getSelectedItem();
            manager.registerCustomer(name, phone, email, tier);
            custNameField.setText("");
            custPhoneField.setText("");
            custEmailField.setText("");
            refreshAllTables();
            info("Customer registered successfully.");
        } catch (IllegalArgumentException ex) {
            error(ex.getMessage());
        }
    }

    // ==================== BOOK EVENT TAB ====================

    private JPanel buildBookingTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel form = new JPanel(new GridLayout(4, 4, 8, 8));
        form.setBorder(BorderFactory.createTitledBorder("Register Event & Book Hall"));

        hallBox = new JComboBox<>();
        customerBox = new JComboBox<>();
        eventTypeBox = new JComboBox<>(new String[]{"WEDDING", "CORPORATE", "BIRTHDAY"});
        eventNameField = new JTextField();
        guestsField = new JTextField();
        startDateField = new JTextField("dd-MM-yyyy");
        endDateField = new JTextField("dd-MM-yyyy");
        JButton checkAvailBtn = new JButton("Check Availability");
        JButton bookBtn = new JButton("Confirm Booking");
        chargePreviewLabel = new JLabel(" ");

        form.add(new JLabel("Customer:"));
        form.add(customerBox);
        form.add(new JLabel("Event Type:"));
        form.add(eventTypeBox);

        form.add(new JLabel("Event Name:"));
        form.add(eventNameField);
        form.add(new JLabel("Expected Guests:"));
        form.add(guestsField);

        form.add(new JLabel("Start Date:"));
        form.add(startDateField);
        form.add(new JLabel("End Date:"));
        form.add(endDateField);

        form.add(new JLabel("Hall:"));
        form.add(hallBox);
        form.add(checkAvailBtn);
        form.add(bookBtn);

        bookingTableModel = new DefaultTableModel(
                new Object[]{"ID", "Customer", "Event", "Hall", "Start", "End", "Charge", "Paid", "Due", "Status"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable bookingTable = new JTable(bookingTableModel);

        checkAvailBtn.addActionListener(e -> onCheckAvailability());
        bookBtn.addActionListener(e -> onCreateBooking());

        JPanel top = new JPanel(new BorderLayout());
        top.add(form, BorderLayout.CENTER);
        top.add(chargePreviewLabel, BorderLayout.SOUTH);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(bookingTable), BorderLayout.CENTER);
        return panel;
    }

    private LocalDate parseDate(String s) throws InvalidBookingException {
        try {
            return LocalDate.parse(s.trim(), DATE_FMT);
        } catch (DateTimeParseException ex) {
            throw new InvalidBookingException("Dates must be in dd-MM-yyyy format.");
        }
    }

    private void onCheckAvailability() {
        try {
            Hall hall = (Hall) hallBox.getSelectedItem();
            if (hall == null) { error("No hall selected."); return; }
            LocalDate start = parseDate(startDateField.getText());
            LocalDate end = parseDate(endDateField.getText());
            if (end.isBefore(start)) { error("End date cannot be before start date."); return; }
            boolean available = manager.isHallAvailable(hall, start, end);
            if (available) {
                chargePreviewLabel.setText("Available: " + hall.getName() + " is FREE for the selected dates.");
            } else {
                chargePreviewLabel.setText("Not available: " + hall.getName() + " is already booked in that range.");
            }
        } catch (InvalidBookingException ex) {
            error(ex.getMessage());
        }
    }

    private void onCreateBooking() {
        try {
            Hall hall = (Hall) hallBox.getSelectedItem();
            Customer customer = (Customer) customerBox.getSelectedItem();
            if (hall == null || customer == null) {
                error("Please add at least one hall and one customer first.");
                return;
            }
            String eventName = eventNameField.getText().trim();
            int guests = Integer.parseInt(guestsField.getText().trim());
            String type = (String) eventTypeBox.getSelectedItem();

            Event event;
            switch (type) {
                case "WEDDING": event = new WeddingEvent(eventName, guests); break;
                case "CORPORATE": event = new CorporateEvent(eventName, guests); break;
                default: event = new BirthdayEvent(eventName, guests); break;
            }

            LocalDate start = parseDate(startDateField.getText());
            LocalDate end = parseDate(endDateField.getText());

            Booking booking = manager.createBooking(hall, customer, event, start, end);
            refreshAllTables();
            chargePreviewLabel.setText(String.format(
                    "Booked! Booking#%d created. Total charge: Rs.%.2f",
                    booking.getBookingId(), booking.getTotalCharge()));
            eventNameField.setText("");
            guestsField.setText("");
        } catch (NumberFormatException ex) {
            error("Expected guests must be a valid number.");
        } catch (IllegalArgumentException | HallNotAvailableException | InvalidBookingException ex) {
            error(ex.getMessage());
        }
    }

    // ==================== PAYMENTS TAB ====================

    private JPanel buildPaymentsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel form = new JPanel(new GridLayout(2, 4, 8, 8));
        form.setBorder(BorderFactory.createTitledBorder("Record Payment / Cancel Booking"));
        bookingIdField = new JTextField();
        paymentAmountField = new JTextField();
        paymentModeBox = new JComboBox<>(new String[]{"CASH", "CARD", "UPI"});
        JButton payBtn = new JButton("Record Payment");
        JButton cancelBtn = new JButton("Cancel Booking");

        form.add(new JLabel("Booking ID:"));
        form.add(new JLabel("Amount (Rs.):"));
        form.add(new JLabel("Mode:"));
        form.add(new JLabel(""));
        form.add(bookingIdField);
        form.add(paymentAmountField);
        form.add(paymentModeBox);
        form.add(payBtn);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        south.add(cancelBtn);

        paymentLogArea = new JTextArea();
        paymentLogArea.setEditable(false);
        paymentLogArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        payBtn.addActionListener(e -> onRecordPayment());
        cancelBtn.addActionListener(e -> onCancelBooking());

        JPanel top = new JPanel(new BorderLayout());
        top.add(form, BorderLayout.CENTER);
        top.add(south, BorderLayout.SOUTH);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(paymentLogArea), BorderLayout.CENTER);
        return panel;
    }

    private void onRecordPayment() {
        try {
            int bookingId = Integer.parseInt(bookingIdField.getText().trim());
            double amount = Double.parseDouble(paymentAmountField.getText().trim());
            String mode = (String) paymentModeBox.getSelectedItem();
            Payment p = manager.recordPayment(bookingId, amount, mode);
            paymentLogArea.append(p.toString() + "\n");
            paymentAmountField.setText("");
            refreshAllTables();
            info("Payment recorded.");
        } catch (NumberFormatException ex) {
            error("Booking ID and Amount must be valid numbers.");
        } catch (IllegalArgumentException | InvalidBookingException ex) {
            error(ex.getMessage());
        }
    }

    private void onCancelBooking() {
        try {
            int bookingId = Integer.parseInt(bookingIdField.getText().trim());
            manager.cancelBooking(bookingId);
            paymentLogArea.append("Booking#" + bookingId + " CANCELLED.\n");
            refreshAllTables();
            info("Booking cancelled.");
        } catch (NumberFormatException ex) {
            error("Enter a valid Booking ID.");
        } catch (InvalidBookingException ex) {
            error(ex.getMessage());
        }
    }

    // ==================== REPORTS TAB ====================

    private JPanel buildReportsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        reportArea = new JTextArea();
        reportArea.setEditable(false);
        reportArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        JButton refreshBtn = new JButton("Refresh Report");
        refreshBtn.addActionListener(e -> reportArea.setText(manager.buildSummaryReport()));
        panel.add(new JScrollPane(reportArea), BorderLayout.CENTER);
        panel.add(refreshBtn, BorderLayout.NORTH);
        return panel;
    }

    // ==================== SHARED HELPERS ====================

    private void refreshAllTables() {
        // Halls
        hallTableModel.setRowCount(0);
        hallBox.removeAllItems();
        for (Hall h : manager.getHalls()) {
            hallTableModel.addRow(new Object[]{h.getHallId(), h.getName(), h.getCapacity(), h.getBaseRatePerDay()});
            hallBox.addItem(h);
        }
        // Customers
        customerTableModel.setRowCount(0);
        customerBox.removeAllItems();
        for (Customer c : manager.getCustomers()) {
            customerTableModel.addRow(new Object[]{
                    c.getCustomerId(), c.getName(), c.getPhone(), c.getEmail(),
                    c.getLoyaltyTier(), String.format("%.0f%%", c.getDiscountRate() * 100)});
            customerBox.addItem(c);
        }
        // Bookings
        bookingTableModel.setRowCount(0);
        for (Booking b : manager.getBookings()) {
            bookingTableModel.addRow(new Object[]{
                    b.getBookingId(), b.getCustomer().getName(), b.getEvent().getEventCategory(),
                    b.getHall().getName(), b.getStartDate().format(DATE_FMT), b.getEndDate().format(DATE_FMT),
                    String.format("%.2f", b.getTotalCharge()), String.format("%.2f", b.getAmountPaid()),
                    String.format("%.2f", b.getBalanceDue()), b.getStatus()});
        }
        reportArea.setText(manager.buildSummaryReport());
    }

    private void seedSampleData() {
        manager.addHall("Grand Ballroom", 300, 50000);
        manager.addHall("Garden Pavilion", 150, 30000);
        manager.addHall("Conference Suite A", 80, 18000);
        manager.registerCustomer("Rohan Mehta", "9876543210", "rohan@example.com", "GOLD");
        manager.registerCustomer("Priya Shah", "9123456780", "priya@example.com", "STANDARD");
    }

    private void info(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) { }
            new EventHallBookingApp().setVisible(true);
        });
    }
}
