package ui;

import dao.BookingDAO;
import dao.EquipmentDAO;
import model.Equipment;

import javax.swing.*;
import java.awt.*;

public class SearchEquipmentPanel extends JPanel {

    private final EquipmentDAO equipmentDao = new EquipmentDAO();
    private final BookingDAO bookingDao = new BookingDAO();

    private final JTextField idField = new JTextField(10);
    private final JLabel resultLabel = new JLabel(" ");
    private final BookingTableModel tableModel = new BookingTableModel();

    public SearchEquipmentPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel header = new JLabel("Search Equipment");
        header.setFont(UiUtils.HEADER_FONT);
        add(header, BorderLayout.NORTH);

        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchRow.add(new JLabel("Equipment ID:"));
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
        scrollPane.setBorder(BorderFactory.createTitledBorder("Booking History"));

        JPanel center = new JPanel(new BorderLayout(4, 4));
        center.add(top, BorderLayout.NORTH);
        center.add(scrollPane, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
    }

    private void search() {
        try {
            long id = UiUtils.parseId(idField.getText(), "Equipment ID");
            Equipment equipment = equipmentDao.findById(id);
            if (equipment == null) {
                resultLabel.setText("No equipment found with ID " + id);
                tableModel.setBookings(java.util.Collections.emptyList());
                return;
            }
            resultLabel.setText(equipment.getEquipmentName() + " — " + equipment.getEquipmentCategory()
                    + " — " + equipment.getStatus() + " — Replacement cost: "
                    + String.format("%.2f", equipment.getReplacementCost()));
            tableModel.setBookings(bookingDao.findByEquipment(equipment));
        } catch (IllegalArgumentException ex) {
            UiUtils.showError(this, ex.getMessage());
        }
    }
}