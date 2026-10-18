package ui;

import com.mongodb.MongoException;
import dao.SecurityEventDAO;

import javax.swing.*;
import java.awt.*;

/**
 * Question 3.4 - lets the analyst change the severity of a single security
 * event, identified by its event ID.
 */
public class UpdateEventPanel extends JPanel {

    private final SecurityEventDAO dao = new SecurityEventDAO();

    private final JTextField eventIdField = new JTextField(18);
    private final JComboBox<String> severityBox =
            new JComboBox<>(SecurityEventDAO.SEVERITY_LEVELS.toArray(new String[0]));
    private final JLabel statusLabel = UiUtils.statusLabel();

    public UpdateEventPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel content = new JPanel(new BorderLayout());
        content.add(UiUtils.heading("Update Security Event"), BorderLayout.NORTH);
        content.add(buildForm(), BorderLayout.CENTER);
        content.add(statusLabel, BorderLayout.SOUTH);

        add(content, BorderLayout.NORTH);
    }

    private JPanel buildForm() {
        JPanel form = UiUtils.titledPanel("Change event severity", new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel idLabel = new JLabel("Event ID:");
        idLabel.setFont(UiUtils.LABEL_FONT);
        form.add(idLabel, gbc);

        gbc.gridx = 1;
        form.add(eventIdField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel severityLabel = new JLabel("New severity:");
        severityLabel.setFont(UiUtils.LABEL_FONT);
        form.add(severityLabel, gbc);

        gbc.gridx = 1;
        form.add(severityBox, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        JButton updateButton = new JButton("Update Severity");
        updateButton.addActionListener(e -> updateSeverity());
        form.add(updateButton, gbc);

        return form;
    }

    /** Applies the update and reports whether the document was changed. */
    private void updateSeverity() {
        String eventId = eventIdField.getText().trim();
        String newSeverity = (String) severityBox.getSelectedItem();

        if (eventId.isEmpty()) {
            UiUtils.showError(this, "Please enter the event ID of the event to update.");
            return;
        }

        try {
            if (!dao.eventExists(eventId)) {
                statusLabel.setText("No security event with ID " + eventId + " exists.");
                UiUtils.showError(this, "No security event with ID " + eventId + " exists.");
                return;
            }

            long modified = dao.updateSeverity(eventId, newSeverity);

            if (modified > 0) {
                String message = "Security event " + eventId + " was updated to severity " + newSeverity + ".";
                statusLabel.setText(message);
                UiUtils.showInfo(this, message);
            } else {
                // The event exists but already carries the selected severity.
                String message = "Security event " + eventId + " already has severity " + newSeverity + ".";
                statusLabel.setText(message);
                UiUtils.showInfo(this, message);
            }
        } catch (MongoException e) {
            UiUtils.showError(this, "The update could not be completed:\n" + e.getMessage());
        }
    }
}
