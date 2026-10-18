package ui;

import com.mongodb.MongoException;
import dao.SecurityEventDAO;
import model.SecurityEvent;

import javax.swing.*;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.List;

/**
 * Question 3.3 - lets the analyst enter a source IP address and retrieve the
 * matching security events from MongoDB.
 */
public class SearchEventsPanel extends JPanel {

    private final SecurityEventDAO dao = new SecurityEventDAO();

    private final JTextField sourceIpField = new JTextField(18);
    private final SecurityEventTableModel tableModel = new SecurityEventTableModel();
    private final JTable resultsTable = new JTable(tableModel);
    private final JLabel statusLabel = UiUtils.statusLabel();

    public SearchEventsPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(buildTopPanel(), BorderLayout.NORTH);
        add(UiUtils.inScrollPane(resultsTable), BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        applyColumnWidths();
        loadAllEvents();
    }

    private JPanel buildTopPanel() {
        JPanel top = new JPanel(new BorderLayout());
        top.add(UiUtils.heading("Search Security Events"), BorderLayout.NORTH);

        JPanel controls = UiUtils.titledPanel("Search by source IP", new FlowLayout(FlowLayout.LEFT, 8, 4));

        JLabel label = new JLabel("Source IP:");
        label.setFont(UiUtils.LABEL_FONT);
        controls.add(label);
        controls.add(sourceIpField);

        JButton searchButton = new JButton("Search Events");
        searchButton.addActionListener(e -> searchEvents());
        controls.add(searchButton);

        JButton showAllButton = new JButton("Show All Events");
        showAllButton.addActionListener(e -> loadAllEvents());
        controls.add(showAllButton);

        // Searching on Enter is quicker than reaching for the button.
        sourceIpField.addActionListener(e -> searchEvents());

        top.add(controls, BorderLayout.CENTER);
        return top;
    }

    private void applyColumnWidths() {
        TableColumnModel columns = resultsTable.getColumnModel();
        int[] widths = SecurityEventTableModel.columnWidths();

        for (int i = 0; i < widths.length && i < columns.getColumnCount(); i++) {
            columns.getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    /** Runs the query and shows the results, or a message when nothing matches. */
    private void searchEvents() {
        String sourceIP = sourceIpField.getText().trim();

        if (sourceIP.isEmpty()) {
            UiUtils.showError(this, "Please enter a source IP address to search for.");
            return;
        }

        try {
            List<SecurityEvent> events = dao.findBySourceIP(sourceIP);
            tableModel.setEvents(events);

            if (events.isEmpty()) {
                statusLabel.setText("No security events were found for source IP " + sourceIP + ".");
                UiUtils.showInfo(this, "No security events were found for source IP " + sourceIP + ".");
            } else {
                statusLabel.setText(events.size() + " security event(s) found for source IP " + sourceIP + ".");
            }
        } catch (MongoException e) {
            UiUtils.showError(this, "The search could not be completed:\n" + e.getMessage());
        }
    }

    private void loadAllEvents() {
        try {
            List<SecurityEvent> events = dao.findAll();
            tableModel.setEvents(events);
            statusLabel.setText("Showing all " + events.size() + " security events.");
        } catch (MongoException e) {
            UiUtils.showError(this, "The security events could not be loaded:\n" + e.getMessage());
        }
    }
}
