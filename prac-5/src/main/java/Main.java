import db.Neo4jConnection;
import org.neo4j.driver.exceptions.Neo4jException;
import ui.MainFrame;

import javax.swing.*;

/**
 * COS326 - Database Systems - Practical 5 2026
 * u23621372 - Moodally J
 *
 * Task 4 - social network investigation application. Connects to the
 * COS326Prac5 Neo4j database and opens the investigator interface.
 */
public class Main {

    public static void main(String[] args) {
        // Question 4.1b - the connection is verified before the GUI is built, so
        // a database that is not running is reported instead of a broken window.
        try {
            Neo4jConnection.connect();
        } catch (Neo4jException e) {
            JOptionPane.showMessageDialog(null,
                    "Could not connect to Neo4j at " + Neo4jConnection.getUri() + ".\n\n"
                            + e.getMessage() + "\n\n"
                            + "Please make sure the Neo4j server is running and try again.",
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
