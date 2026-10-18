import com.mongodb.MongoException;
import db.MongoConnection;
import ui.MainFrame;

import javax.swing.*;

/**
 * COS326 - Database Systems - Practical 4 2026
 * u23621372 - Moodally J
 *
 * Task 3 - CyberShield SOC application. Connects to the cybershield MongoDB
 * database and opens the analyst interface.
 */
public class Main {

    public static void main(String[] args) {
        // Question 3.1 - the connection is verified before the GUI is built, so
        // a database that is not running is reported instead of a broken window.
        try {
            MongoConnection.connect();
        } catch (MongoException e) {
            JOptionPane.showMessageDialog(null,
                    "Could not connect to MongoDB at " + MongoConnection.getConnectionString() + ".\n\n"
                            + e.getMessage() + "\n\n"
                            + "Please make sure the MongoDB server is running and try again.",
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
