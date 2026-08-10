package ui;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public final class UiUtils {

    public static final Font LABEL_FONT = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font HEADER_FONT = new Font("SansSerif", Font.BOLD, 16);

    private UiUtils() {
    }

    /** Adds a label+field row to a panel using GridBagLayout, at the given row index. */
    public static void addFormRow(JPanel panel, GridBagConstraints gbc, int row,
                                   String labelText, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        JLabel label = new JLabel(labelText);
        label.setFont(LABEL_FONT);
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, gbc);
    }

    public static GridBagConstraints defaultConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        return gbc;
    }

    public static JPanel titledPanel(String title, LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(title),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        return panel;
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirm(Component parent, String message) {
        int result = JOptionPane.showConfirmDialog(parent, message, "Confirm",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }

    /** Parses "yyyy-MM-dd", throws IllegalArgumentException with a friendly message on failure. */
    public static LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(text.trim());
        } catch (DateTimeParseException | NullPointerException e) {
            throw new IllegalArgumentException("Date must be in yyyy-MM-dd format (e.g. 2026-08-15)");
        }
    }

    /** Parses "HH:mm", throws IllegalArgumentException with a friendly message on failure. */
    public static LocalTime parseTime(String text) {
        try {
            return LocalTime.parse(text.trim());
        } catch (DateTimeParseException | NullPointerException e) {
            throw new IllegalArgumentException("Time must be in HH:mm 24-hour format (e.g. 14:30)");
        }
    }

    public static double parseCost(String text) {
        try {
            double value = Double.parseDouble(text.trim());
            if (value < 0) {
                throw new IllegalArgumentException("Replacement cost cannot be negative");
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Replacement cost must be a valid number");
        }
    }

    public static void requireNonEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty");
        }
    }

    public static long parseId(String text, String fieldName) {
        try {
            return Long.parseLong(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " must be a valid number");
        }
    }
}