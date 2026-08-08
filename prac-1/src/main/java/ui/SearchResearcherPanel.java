package ui;

import dao.BookingDAO;
import dao.ResearcherDAO;
import model.Researcher;

import javax.swing.*;
import java.awt.*;

/** Implements spec (c)(iv) Search Researcher: find by ID, show researcher + their bookings. */
public class SearchResearcherPanel extends JPanel {

    private final ResearcherDAO researcherDao = new ResearcherDAO();
    private final BookingDAO bookingDao = new BookingDAO();

    private final JTextField idField = new JTextField(10);
    private final JLabel resultLabel = new JLabel(" ");
    private final BookingTableModel tableModel = new BookingTableModel();

    public SearchResearcherPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel header = new JLabel("Search Researcher");
        header.setFont(UiUtils.HEADER_FONT);
        add(header, BorderLayout.NORTH);

        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchRow.add(new JLabel("Researcher ID:"));
        searchRow.add(idField);
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> search());
        searchRow.add(searchButton);

        JPanel top = new JPanel(new BorderLayout(4, 4));
        top.add(searchRow, BorderLayout.NORTH);
        resultLabel.setFont(UiUtils.LABEL_FONT);
        top.add(resultLabel, BorderLayout.SOUTH);

        JTable table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Associated Bookings"));

        JPanel center = new JPanel(new BorderLayout(4, 4));
        center.add(top, BorderLayout.NORTH);
        center.add(scrollPane, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
    }

    private void search() {
        try {
            long id = UiUtils.parseId(idField.getText(), "Researcher ID");
            Researcher researcher = researcherDao.findById(id);
            if (researcher == null) {
                resultLabel.setText("No researcher found with ID " + id);
                tableModel.setBookings(java.util.Collections.emptyList());
                return;
            }
            resultLabel.setText(researcher.getFullName() + " — " + researcher.getDepartment()
                    + " — " + researcher.getEmail());
            tableModel.setBookings(bookingDao.findByResearcher(researcher));
        } catch (IllegalArgumentException ex) {
            UiUtils.showError(this, ex.getMessage());
        }
    }
}