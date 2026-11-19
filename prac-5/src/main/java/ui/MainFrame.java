package ui;

import dao.SocialNetworkDAO;
import db.Neo4jConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Question 4.2 - the investigator interface.
 *
 * One tab per investigation function, so each of the Question 4.3 functions can
 * be demonstrated on its own.
 */
public class MainFrame extends JFrame {

    public MainFrame() {
        super("COS326 Practical 5 - Social Network Investigation");

        SocialNetworkDAO dao = new SocialNetworkDAO();

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Network Overview", new NetworkPanel(dao));
        tabs.addTab("Connection Investigation", new ConnectionPanel(dao));
        tabs.addTab("Content Investigation", new ContentPanel(dao));

        add(tabs, BorderLayout.CENTER);

        JLabel status = new JLabel("  Connected to " + Neo4jConnection.getDatabaseName()
                + " at " + Neo4jConnection.getUri());
        add(status, BorderLayout.SOUTH);

        // Implementation requirement - the driver is closed when the window closes.
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                Neo4jConnection.close();
            }
        });

        setSize(900, 560);
    }
}
