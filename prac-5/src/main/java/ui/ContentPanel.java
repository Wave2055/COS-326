package ui;

import dao.SocialNetworkDAO;
import model.PostSummary;
import org.neo4j.driver.exceptions.Neo4jException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/** Question 4.3d - the posts belonging to one account. */
public class ContentPanel extends JPanel {

    private final SocialNetworkDAO dao;
    private final JTextField accountField = new JTextField(20);
    private final DefaultTableModel tableModel =
            new DefaultTableModel(new Object[]{"Post ID", "Hashtag", "Message", "Reposted"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

    public ContentPanel(SocialNetworkDAO dao) {
        this.dao = dao;

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JButton search = new JButton("Show Posts");
        search.addActionListener(e -> showPosts());

        JPanel inputs = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        inputs.add(new JLabel("Account:"));
        inputs.add(accountField);
        inputs.add(search);

        add(inputs, BorderLayout.NORTH);
        add(new JScrollPane(new JTable(tableModel)), BorderLayout.CENTER);
    }

    private void showPosts() {
        String account = accountField.getText().trim();
        if (account.isEmpty()) {
            UiUtils.showValidationMessage(this, "Please enter an account name.");
            return;
        }

        try {
            if (!dao.accountExists(account)) {
                UiUtils.showValidationMessage(this, "No account named " + account + " exists.");
                return;
            }

            List<PostSummary> posts = dao.getPostsForAccount(account);
            tableModel.setRowCount(0);
            if (posts.isEmpty()) {
                UiUtils.showInfo(this, account + " has not posted anything.");
                return;
            }
            for (PostSummary post : posts) {
                tableModel.addRow(new Object[]{
                        post.postId(), post.hashtag(), post.message(),
                        post.reposted() ? "Yes" : "No"});
            }
        } catch (Neo4jException e) {
            UiUtils.showError(this, "Could not load the posts for " + account + ".", e);
        }
    }
}
