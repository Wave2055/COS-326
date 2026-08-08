package ui;

import dao.EquipmentDAO;
import model.Equipment;
import model.EquipmentStatus;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class RegisterEquipmentPanel extends JPanel {

    private final EquipmentDAO equipmentDao = new EquipmentDAO();

    private final JTextField nameField = new JTextField(20);
    private final JTextField categoryField = new JTextField(20);
    private final JTextField purchaseDateField = new JTextField(LocalDate.now().toString(), 20);
    private final JTextField replacementCostField = new JTextField(20);
    private final JComboBox<EquipmentStatus> statusCombo = new JComboBox<>(EquipmentStatus.values());

    public RegisterEquipmentPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel header = new JLabel("Register Equipment");
        header.setFont(UiUtils.HEADER_FONT);
        add(header, BorderLayout.NORTH);

        JPanel form = UiUtils.titledPanel("Equipment Details", new GridBagLayout());
        GridBagConstraints gbc = UiUtils.defaultConstraints();
        UiUtils.addFormRow(form, gbc, 0, "Equipment Name:", nameField);
        UiUtils.addFormRow(form, gbc, 1, "Category:", categoryField);
        UiUtils.addFormRow(form, gbc, 2, "Purchase Date (yyyy-MM-dd):", purchaseDateField);
        UiUtils.addFormRow(form, gbc, 3, "Replacement Cost:", replacementCostField);
        UiUtils.addFormRow(form, gbc, 4, "Status:", statusCombo);

        JButton saveButton = new JButton("Register Equipment");
        saveButton.addActionListener(e -> save());
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonRow.add(saveButton);

        JPanel center = new JPanel(new BorderLayout());
        center.add(form, BorderLayout.NORTH);
        center.add(buttonRow, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);
    }

    private void save() {
        try {
            String name = nameField.getText();
            String category = categoryField.getText();
            UiUtils.requireNonEmpty(name, "Equipment Name");
            UiUtils.requireNonEmpty(category, "Category");
            LocalDate purchaseDate = UiUtils.parseDate(purchaseDateField.getText());
            double cost = UiUtils.parseCost(replacementCostField.getText());
            EquipmentStatus status = (EquipmentStatus) statusCombo.getSelectedItem();

            Equipment equipment = new Equipment(name.trim(), category.trim(), purchaseDate, cost, status);
            Equipment saved = equipmentDao.create(equipment);

            UiUtils.showInfo(this, "Equipment registered with ID " + saved.getEquipmentId());
            clearForm();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            UiUtils.showError(this, ex.getMessage());
        } catch (RuntimeException ex) {
            UiUtils.showError(this, "Could not register equipment: " + ex.getMessage());
        }
    }

    private void clearForm() {
        nameField.setText("");
        categoryField.setText("");
        purchaseDateField.setText(LocalDate.now().toString());
        replacementCostField.setText("");
        statusCombo.setSelectedIndex(0);
    }
}