package ui;

import model.SecurityEvent;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/** Displays a list of security events in a JTable. */
public class SecurityEventTableModel extends AbstractTableModel {

    private static final String[] COLUMNS = {
            "Event ID", "Timestamp", "Source IP", "Destination IP",
            "Event Type", "Severity", "Description", "Tags", "Response Actions"
    };

    private static final int[] COLUMN_WIDTHS = {
            90, 165, 120, 120, 165, 80, 380, 200, 260
    };

    private List<SecurityEvent> events = new ArrayList<>();

    public void setEvents(List<SecurityEvent> events) {
        this.events = events;
        fireTableDataChanged();
    }

    public static int[] columnWidths() {
        return COLUMN_WIDTHS;
    }

    @Override
    public int getRowCount() {
        return events.size();
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
        SecurityEvent event = events.get(rowIndex);

        return switch (columnIndex) {
            case 0 -> event.getEventId();
            case 1 -> event.getTimestamp();
            case 2 -> event.getSourceIP();
            case 3 -> event.getDestinationIP();
            case 4 -> event.getEventType();
            case 5 -> event.getSeverity();
            case 6 -> event.getDescription();
            case 7 -> event.getTags();
            case 8 -> event.getResponseActions();
            default -> "";
        };
    }
}
