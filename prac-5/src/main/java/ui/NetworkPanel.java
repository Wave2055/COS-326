package ui;

import dao.SocialNetworkDAO;
import model.InfluentialAccount;
import model.NetworkOverview;
import org.neo4j.driver.exceptions.Neo4jException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/** Questions 4.3a and 4.3b - network overview and the most influential accounts. */
public class NetworkPanel extends JPanel {

    private static final int TOP_ACCOUNTS = 5;

    private final SocialNetworkDAO dao;
    private final JLabel overviewLabel = new JLabel("Press Refresh to load the network overview.");
    private final DefaultTableModel tableModel =
            new DefaultTableModel(new Object[]{"Account", "Followers"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

    public NetworkPanel(SocialNetworkDAO dao) {
        this.dao = dao;

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> refresh());

        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.add(overviewLabel, BorderLayout.CENTER);
        top.add(refresh, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(new JTable(tableModel)), BorderLayout.CENTER);
        add(new JLabel("Top " + TOP_ACCOUNTS + " accounts by follower count"), BorderLayout.SOUTH);
    }

    /** Runs both queries and replaces whatever is currently on screen. */
    private void refresh() {
        try {
            NetworkOverview overview = dao.getNetworkOverview();
            overviewLabel.setText("People: " + overview.personCount()
                    + "    FOLLOWS relationships: " + overview.followsCount());

            List<InfluentialAccount> accounts = dao.getInfluentialAccounts(TOP_ACCOUNTS);
            tableModel.setRowCount(0);
            for (InfluentialAccount account : accounts) {
                tableModel.addRow(new Object[]{account.name(), account.followerCount()});
            }
        } catch (Neo4jException e) {
            UiUtils.showError(this, "Could not load the network overview.", e);
        }
    }
}
