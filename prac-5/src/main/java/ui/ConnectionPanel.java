package ui;

import dao.SocialNetworkDAO;
import org.neo4j.driver.exceptions.Neo4jException;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/** Question 4.3c - the shortest FOLLOWS path between two accounts. */
public class ConnectionPanel extends JPanel {

    private final SocialNetworkDAO dao;
    private final JTextField fromField = new JTextField(18);
    private final JTextField toField = new JTextField(18);
    private final JTextArea output = new JTextArea(10, 50);

    public ConnectionPanel(SocialNetworkDAO dao) {
        this.dao = dao;

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JButton search = new JButton("Find Shortest Path");
        search.addActionListener(e -> findPath());

        JPanel inputs = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        inputs.add(new JLabel("From:"));
        inputs.add(fromField);
        inputs.add(new JLabel("To:"));
        inputs.add(toField);
        inputs.add(search);

        output.setEditable(false);
        output.setLineWrap(true);
        output.setWrapStyleWord(true);

        add(inputs, BorderLayout.NORTH);
        add(new JScrollPane(output), BorderLayout.CENTER);
    }

    private void findPath() {
        String from = fromField.getText().trim();
        String to = toField.getText().trim();

        // Question 4.4b - the input is checked before the query is sent.
        if (from.isEmpty() || to.isEmpty()) {
            UiUtils.showValidationMessage(this, "Please enter both account names.");
            return;
        }
        if (from.equals(to)) {
            UiUtils.showValidationMessage(this, "Please enter two different account names.");
            return;
        }

        try {
            if (!dao.accountExists(from)) {
                UiUtils.showValidationMessage(this, "No account named " + from + " exists.");
                return;
            }
            if (!dao.accountExists(to)) {
                UiUtils.showValidationMessage(this, "No account named " + to + " exists.");
                return;
            }

            List<String> path = dao.findShortestPath(from, to);
            if (path.isEmpty()) {
                output.setText("No FOLLOWS path exists between " + from + " and " + to + ".");
                return;
            }

            // The path length is the number of relationships, not the number of nodes.
            output.setText("Shortest path (" + (path.size() - 1) + " relationships):\n\n"
                    + String.join("\n  -> ", path));
        } catch (Neo4jException e) {
            UiUtils.showError(this, "Could not search for a path.", e);
        }
    }
}
