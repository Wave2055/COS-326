package ui;

import dao.BookingDAO;
import model.Booking;
import model.BookingStatus;

import javax.swing.*;
import java.awt.*;

public class ManageBookingsPanel extends JPanel {

    private final BookingDAO bookingDao = new BookingDAO();
    private final BookingTableModel tableModel = new BookingTableModel();
    private final JTable table = new JTable(tableModel);
    private final JComboBox<BookingStatus> statusCombo = new JComboBox<>(BookingStatus.values());

    public ManageBookingsPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel header = new JLabel("Manage Bookings");
        header.setFont(UiUtils.HEADER_FONT);
        add(header, BorderLayout.NORTH);

        table.setFillsViewportHeight(true);
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actionRow.add(new JLabel("New Status:"));
        actionRow.add(statusCombo);
        JButton updateButton = new JButton("Update Status");
        updateButton.addActionListener(e -> updateStatus());
        JButton cancelButton = new JButton("Cancel Booking");
        cancelButton.addActionListener(e -> cancelBooking());
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refresh());
        actionRow.add(updateButton);
        actionRow.add(cancelButton);
        actionRow.add(refreshButton);

        JPanel center = new JPanel(new BorderLayout(4, 4));
        center.add(scrollPane, BorderLayout.CENTER);
        center.add(actionRow, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        refresh();
    }

    public void refresh() {
        tableModel.setBookings(bookingDao.findAll());
    }

    private Booking getSelectedBooking() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UiUtils.showError(this, "Select a booking from the table first");
            return null;
        }
        return tableModel.getBookingAt(row);
    }

    private void updateStatus() {
        Booking booking = getSelectedBooking();
        if (booking == null) return;
        try {
            BookingStatus newStatus = (BookingStatus) statusCombo.getSelectedItem();
            booking.setStatus(newStatus);
            bookingDao.update(booking);
            UiUtils.showInfo(this, "Booking " + booking.getBookingId() + " status updated to " + newStatus);
            refresh();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            UiUtils.showError(this, ex.getMessage());
        } catch (RuntimeException ex) {
            UiUtils.showError(this, "Could not update booking: " + ex.getMessage());
        }
    }

    private void cancelBooking() {
        Booking booking = getSelectedBooking();
        if (booking == null) return;
        if (!UiUtils.confirm(this, "Cancel booking " + booking.getBookingId()
                + "? This will remove it from the database (researcher and equipment records are kept).")) {
            return;
        }
        try {
            bookingDao.delete(booking.getBookingId());
            UiUtils.showInfo(this, "Booking " + booking.getBookingId() + " cancelled");
            refresh();
        } catch (RuntimeException ex) {
            UiUtils.showError(this, "Could not cancel booking: " + ex.getMessage());
        }
    }
}