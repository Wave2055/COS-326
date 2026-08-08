package ui;

import dao.ResearcherDAO;
import model.Researcher;

import javax.swing.*;
import java.awt.*;

public class RegisterResearcherPanel extends JPanel {

    private final ResearcherDAO researcherDao = new ResearcherDAO();

    private final JTextField fullNameField = new JTextField(20);
    private final JTextField departmentField = new JTextField(20);
    private final JTextField emailField = new JTextField(20);

    public RegisterResearcherPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel header = new JLabel("Register Researcher");
        header.setFont(UiUtils.HEADER_FONT);
        add(header, BorderLayout.NORTH);

        JPanel form = UiUtils.titledPanel("Researcher Details", new GridBagLayout());
        GridBagConstraints gbc = UiUtils.defaultConstraints();
        UiUtils.addFormRow(form, gbc, 0, "Full Name:", fullNameField);
        UiUtils.addFormRow(form, gbc, 1, "Department:", departmentField);
        UiUtils.addFormRow(form, gbc, 2, "Email Address:", emailField);

        JButton saveButton = new JButton("Register Researcher");
        saveButton.addActionListener(e -> save());
        getRootPane();
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonRow.add(saveButton);

        JPanel center = new JPanel(new BorderLayout());
        center.add(form, BorderLayout.NORTH);
        center.add(buttonRow, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);
    }

    private void save() {
        try {
            String fullName = fullNameField.getText();
            String department = departmentField.getText();
            String email = emailField.getText();
            UiUtils.requireNonEmpty(fullName, "Full Name");
            UiUtils.requireNonEmpty(department, "Department");
            UiUtils.requireNonEmpty(email, "Email Address");

            Researcher researcher = new Researcher(fullName.trim(), department.trim(), email.trim());
            Researcher saved = researcherDao.create(researcher);

            UiUtils.showInfo(this, "Researcher registered with ID " + saved.getResearcherId());
            clearForm();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            UiUtils.showError(this, ex.getMessage());
        } catch (RuntimeException ex) {
            UiUtils.showError(this, "Could not register researcher: " + ex.getMessage());
        }
    }

    private void clearForm() {
        fullNameField.setText("");
        departmentField.setText("");
        emailField.setText("");
    }
}