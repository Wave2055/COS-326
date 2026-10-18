package ui;

import db.MongoConnection;

import javax.swing.*;
import java.awt.*;

/**
 * Question 3.2 - the SOC analyst interface. A navigation bar switches between
 * searching events, updating an event and generating the security summary.
 */
public class MainFrame extends JFrame {

    private static final String CARD_SEARCH = "search";
    private static final String CARD_UPDATE = "update";
    private static final String CARD_SUMMARY = "summary";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    public MainFrame() {
        super("CyberShield SOC - Security Event Management");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 620));

        contentPanel.add(new SearchEventsPanel(), CARD_SEARCH);
        contentPanel.add(new UpdateEventPanel(), CARD_UPDATE);
        contentPanel.add(new SummaryPanel(), CARD_SUMMARY);

        setLayout(new BorderLayout());
        add(buildNavPanel(), BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                MongoConnection.close();
            }
        });
    }

    private JPanel buildNavPanel() {
        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(BorderFactory.createEmptyBorder(16, 12, 16, 12));
        nav.setPreferredSize(new Dimension(210, 0));
        nav.setBackground(UiUtils.ACCENT);

        JLabel title = new JLabel("CyberShield SOC");
        title.setFont(new Font("SansSerif", Font.BOLD, 15));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        nav.add(title);
        nav.add(Box.createVerticalStrut(18));

        nav.add(navButton("Search Events", CARD_SEARCH));
        nav.add(Box.createVerticalStrut(8));
        nav.add(navButton("Update Event", CARD_UPDATE));
        nav.add(Box.createVerticalStrut(8));
        nav.add(navButton("Security Summary", CARD_SUMMARY));
        nav.add(Box.createVerticalGlue());

        return nav;
    }

    private JButton navButton(String text, String card) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        button.setFocusPainted(false);
        button.addActionListener(e -> cardLayout.show(contentPanel, card));
        return button;
    }

    /** Shows which database the application is working against. */
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));

        JLabel label = new JLabel("Connected to " + MongoConnection.getConnectionString()
                + " - database: " + MongoConnection.getDatabaseName());
        label.setFont(new Font("SansSerif", Font.PLAIN, 11));
        bar.add(label);

        return bar;
    }
}
