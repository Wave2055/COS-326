package ui;

import dao.JPAUtil;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private static final String CARD_REGISTER_RESEARCHER = "registerResearcher";
    private static final String CARD_REGISTER_EQUIPMENT = "registerEquipment";
    private static final String CARD_CREATE_BOOKING = "createBooking";
    private static final String CARD_SEARCH_RESEARCHER = "searchResearcher";
    private static final String CARD_SEARCH_EQUIPMENT = "searchEquipment";
    private static final String CARD_MANAGE_BOOKINGS = "manageBookings";
    private static final String CARD_REPORTS = "reports";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    private final CreateBookingPanel createBookingPanel = new CreateBookingPanel();
    private final ManageBookingsPanel manageBookingsPanel = new ManageBookingsPanel();
    private final ReportsPanel reportsPanel = new ReportsPanel();

    public MainFrame() {
        super("Research Equipment Booking System");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(900, 600));

        contentPanel.add(new RegisterResearcherPanel(), CARD_REGISTER_RESEARCHER);
        contentPanel.add(new RegisterEquipmentPanel(), CARD_REGISTER_EQUIPMENT);
        contentPanel.add(createBookingPanel, CARD_CREATE_BOOKING);
        contentPanel.add(new SearchResearcherPanel(), CARD_SEARCH_RESEARCHER);
        contentPanel.add(new SearchEquipmentPanel(), CARD_SEARCH_EQUIPMENT);
        contentPanel.add(manageBookingsPanel, CARD_MANAGE_BOOKINGS);
        contentPanel.add(reportsPanel, CARD_REPORTS);

        setLayout(new BorderLayout());
        add(buildNavPanel(), BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                JPAUtil.close();
            }
        });
    }

    private JPanel buildNavPanel() {
        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(BorderFactory.createEmptyBorder(16, 12, 16, 12));
        nav.setPreferredSize(new Dimension(210, 0));
        nav.setBackground(new Color(45, 52, 64));

        JLabel title = new JLabel("Equipment Booking");
        title.setFont(new Font("SansSerif", Font.BOLD, 15));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        title.setBorder(BorderFactory.createEmptyBorder(0, 4, 16, 0));
        nav.add(title);

        nav.add(navButton("Register Researcher", CARD_REGISTER_RESEARCHER, null));
        nav.add(navButton("Register Equipment", CARD_REGISTER_EQUIPMENT, null));
        nav.add(navButton("Create Booking", CARD_CREATE_BOOKING, createBookingPanel::refresh));
        nav.add(navButton("Search Researcher", CARD_SEARCH_RESEARCHER, null));
        nav.add(navButton("Search Equipment", CARD_SEARCH_EQUIPMENT, null));
        nav.add(navButton("Manage Bookings", CARD_MANAGE_BOOKINGS, manageBookingsPanel::refresh));
        nav.add(navButton("Reports", CARD_REPORTS, reportsPanel::refresh));

        nav.add(Box.createVerticalGlue());
        return nav;
    }

    private JButton navButton(String label, String cardName, Runnable onShow) {
        JButton button = new JButton(label);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 0, 6, 0),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        button.addActionListener(e -> {
            if (onShow != null) onShow.run();
            cardLayout.show(contentPanel, cardName);
        });
        return button;
    }
}