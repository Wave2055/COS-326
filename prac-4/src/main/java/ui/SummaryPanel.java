package ui;

import com.mongodb.MongoException;
import dao.SecurityEventDAO;
import model.SeveritySummaryRow;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Question 3.5 - runs the aggregation pipeline and displays the number of
 * security events at each severity level.
 */
public class SummaryPanel extends JPanel {

    private final SecurityEventDAO dao = new SecurityEventDAO();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Severity", "Number of events"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable summaryTable = new JTable(tableModel);
    private final JLabel statusLabel = UiUtils.statusLabel();

    public SummaryPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel top = new JPanel(new BorderLayout());
        top.add(UiUtils.heading("Security Event Summary"), BorderLayout.NORTH);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JButton summaryButton = new JButton("Security Summary");
        summaryButton.addActionListener(e -> loadSummary());
        controls.add(summaryButton);
        top.add(controls, BorderLayout.CENTER);

        summaryTable.setRowHeight(22);
        summaryTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));

        JScrollPane scrollPane = new JScrollPane(summaryTable);
        scrollPane.setPreferredSize(new Dimension(420, 160));

        JPanel centre = new JPanel(new FlowLayout(FlowLayout.LEFT));
        centre.add(scrollPane);

        add(top, BorderLayout.NORTH);
        add(centre, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
    }

    /** Reads the summary from MongoDB - no values are hard-coded. */
    private void loadSummary() {
        try {
            List<SeveritySummaryRow> summary = dao.getSeveritySummary();
            tableModel.setRowCount(0);

            int total = 0;
            for (SeveritySummaryRow row : summary) {
                tableModel.addRow(new Object[]{row.getSeverity(), row.getNumberOfEvents()});
                total += row.getNumberOfEvents();
            }

            statusLabel.setText(total + " security events across " + summary.size() + " severity levels.");
        } catch (MongoException e) {
            UiUtils.showError(this, "The summary could not be generated:\n" + e.getMessage());
        }
    }
}
