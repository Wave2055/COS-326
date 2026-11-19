package ui;

import javax.swing.JOptionPane;
import java.awt.Component;

/** Question 4.4b - shared helpers for reporting problems without closing the application. */
public final class UiUtils {

    private UiUtils() {
    }

    /** Reports a failed query. The application stays open. */
    public static void showError(Component parent, String message, Exception e) {
        JOptionPane.showMessageDialog(parent,
                message + "\n\n" + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
    }

    /** Reports a problem with what the investigator typed. */
    public static void showValidationMessage(Component parent, String message) {
        JOptionPane.showMessageDialog(parent,
                message, "Invalid Input", JOptionPane.WARNING_MESSAGE);
    }

    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent,
                message, "Result", JOptionPane.INFORMATION_MESSAGE);
    }
}
