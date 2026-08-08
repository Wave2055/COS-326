package ui;

import dao.BookingDAO;
import dao.EquipmentDAO;
import dao.ResearcherDAO;
import model.EquipmentStatus;
import model.Researcher;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

/** Covers spec (c)(viii) Equipment Summary and (d) Additional Functionality. */
public class ReportsPanel extends JPanel {

    private final EquipmentDAO equipmentDao = new EquipmentDAO();
    private final BookingDAO bookingDao = new BookingDAO();
    private final ResearcherDAO researcherDao = new ResearcherDAO();

    private final JLabel totalCostLabel = new JLabel("Total replacement cost: —");
    private final EquipmentTableModel availableModel = new EquipmentTableModel();
    private final EquipmentTableModel neverBookedModel = new EquipmentTableModel();
    private final JTextArea topResearchersArea = new JTextArea(4, 30);
    private final JTextField researcherIdField = new JTextField(10);
    private final BookingTableModel researcherBookingsModel = new BookingTableModel();

    public ReportsPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel header = new JLabel("Reports");
        header.setFont(UiUtils.HEADER_FONT);
        add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Equipment Summary", buildSummaryTab());
        tabs.addTab("Available Equipment", buildTableTab(availableModel));
        tabs.addTab("Never Booked", buildTableTab(neverBookedModel));
        tabs.addTab("Top Researchers", buildTopResearchersTab());
        tabs.addTab("Researcher's Bookings", buildResearcherBookingsTab());
        add(tabs, BorderLayout.CENTER);

        refresh();
    }

    private JPanel buildSummaryTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        totalCostLabel.setFont(UiUtils.HEADER_FONT);
        panel.add(totalCostLabel, BorderLayout.NORTH);
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refresh());
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row.add(refreshButton);
        panel.add(row, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildTableTab(EquipmentTableModel model) {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refresh());
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row.add(refreshButton);
        panel.add(row, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildTopResearchersTab() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        topResearchersArea.setEditable(false);
        panel.add(new JScrollPane(topResearchersArea), BorderLayout.CENTER);
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refresh());
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row.add(refreshButton);
        panel.add(row, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildResearcherBookingsTab() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchRow.add(new JLabel("Researcher ID:"));
        searchRow.add(researcherIdField);
        JButton searchButton = new JButton("Show Bookings");
        searchButton.addActionListener(e -> showResearcherBookings());
        searchRow.add(searchButton);
        panel.add(searchRow, BorderLayout.NORTH);

        JTable table = new JTable(researcherBookingsModel);
        table.setFillsViewportHeight(true);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void showResearcherBookings() {
        try {
            long id = UiUtils.parseId(researcherIdField.getText(), "Researcher ID");
            Researcher researcher = researcherDao.findById(id);
            if (researcher == null) {
                UiUtils.showError(this, "No researcher found with ID " + id);
                researcherBookingsModel.setBookings(java.util.Collections.emptyList());
                return;
            }
            researcherBookingsModel.setBookings(bookingDao.findByResearcher(researcher));
        } catch (IllegalArgumentException ex) {
            UiUtils.showError(this, ex.getMessage());
        }
    }

    /** Reloads every report — call before showing this panel. */
    public void refresh() {
        totalCostLabel.setText(String.format("Total replacement cost of all equipment: %.2f",
                equipmentDao.getTotalReplacementCost()));
        availableModel.setEquipment(equipmentDao.findByStatus(EquipmentStatus.AVAILABLE));
        neverBookedModel.setEquipment(equipmentDao.findNeverBooked());

        List<Researcher> topResearchers = bookingDao.findResearchersWithMostBookings();
        if (topResearchers.isEmpty()) {
            topResearchersArea.setText("No bookings recorded yet.");
        } else {
            String text = topResearchers.stream()
                    .map(r -> r.getFullName() + " (ID " + r.getResearcherId() + ", " + r.getDepartment() + ")")
                    .collect(Collectors.joining("\n"));
            topResearchersArea.setText(text);
        }
    }
}