package ui;

import model.Booking;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class BookingTableModel extends AbstractTableModel {

    private static final String[] COLUMNS = {
            "Booking ID", "Date", "Start", "End", "Status", "Researcher", "Equipment", "Purpose"
    };

    private List<Booking> bookings = new ArrayList<>();

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings != null ? bookings : new ArrayList<>();
        fireTableDataChanged();
    }

    public Booking getBookingAt(int row) {
        return bookings.get(row);
    }

    @Override
    public int getRowCount() {
        return bookings.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Booking b = bookings.get(rowIndex);
        switch (columnIndex) {
            case 0: return b.getBookingId();
            case 1: return b.getBookingDate();
            case 2: return b.getStartTime();
            case 3: return b.getEndTime();
            case 4: return b.getStatus();
            case 5: return b.getResearcher() != null ? b.getResearcher().getFullName() : "";
            case 6: return b.getEquipment() != null ? b.getEquipment().getEquipmentName() : "";
            case 7: return b.getResearchPurpose();
            default: return "";
        }
    }
}