package ui;

import javax.swing.*;
import java.awt.*;

public final class UiUtils {

    public static final Font LABEL_FONT = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font HEADER_FONT = new Font("SansSerif", Font.BOLD, 16);
    public static final Color ACCENT = new Color(45, 52, 64);

    private UiUtils() {
    }

    /** Builds the bold heading shown at the top of each panel. */
    public static JLabel heading(String text) {
        JLabel label = new JLabel(text);
        label.setFont(HEADER_FONT);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        return label;
    }

    public static JPanel titledPanel(String title, LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(title),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        return panel;
    }

    /** Status line used by every panel to report the outcome of an operation. */
    public static JLabel statusLabel() {
        JLabel label = new JLabel(" ");
        label.setFont(LABEL_FONT);
        label.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        return label;
    }

    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Information",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    /** Widens the table columns so the longer description text stays readable. */
    public static JScrollPane inScrollPane(JTable table) {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setRowHeight(22);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        return new JScrollPane(table);
    }
}
