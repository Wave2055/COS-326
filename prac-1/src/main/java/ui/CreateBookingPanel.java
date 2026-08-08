package ui;

import dao.BookingDAO;
import dao.EquipmentDAO;
import dao.ResearcherDAO;
import model.Booking;
import model.BookingStatus;
import model.Equipment;
import model.Researcher;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

/** Implements spec (c)(iii) Create Booking. Bookings may only reference existing records,
 *  so researcher/equipment are picked from dropdowns rather than typed IDs. */
public class CreateBookingPanel extends JPanel {

    private final BookingDAO bookingDao = new BookingDAO();
    private final ResearcherDAO researcherDao = new ResearcherDAO();
    private final EquipmentDAO equipmentDao = new EquipmentDAO();

    private final JComboBox<Researcher> researcherCombo = new JComboBox<>();
    private final JComboBox<Equipment> equipmentCombo = new JComboBox<>();
    private final JTextField bookingDateField = new JTextField(LocalDate.now().toString(), 20);
    private final JTextField startTimeField = new JTextField(20);
    private final JTextField endTimeField = new JTextField(20);
    private final JTextArea purposeArea = new JTextArea(3, 20);

    public CreateBookingPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel header = new JLabel("Create Booking");
        header.setFont(UiUtils.HEADER_FONT);
        add(header, BorderLayout.NORTH);

        JPanel form = UiUtils.titledPanel("Booking Details", new GridBagLayout());
        GridBagConstraints gbc = UiUtils.defaultConstraints();
        UiUtils.addFormRow(form, gbc, 0, "Researcher:", researcherCombo);
        UiUtils.addFormRow(form, gbc, 1, "Equipment:", equipmentCombo);
        UiUtils.addFormRow(form, gbc, 2, "Booking Date (yyyy-MM-dd):", bookingDateField);
        UiUtils.addFormRow(form, gbc, 3, "Start Time (HH:mm):", startTimeField);
        UiUtils.addFormRow(form, gbc, 4, "End Time (HH:mm):", endTimeField);
        purposeArea.setLineWrap(true);
        purposeArea.setWrapStyleWord(true);
        UiUtils.addFormRow(form, gbc, 5, "Research Purpose:", new JScrollPane(purposeArea));

        JButton saveButton = new JButton("Create Booking");
        saveButton.addActionListener(e -> save());
        JButton refreshButton = new JButton("Refresh Lists");
        refreshButton.addActionListener(e -> refresh());
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonRow.add(saveButton);
        buttonRow.add(refreshButton);

        JPanel center = new JPanel(new BorderLayout());
        center.add(form, BorderLayout.NORTH);
        center.add(buttonRow, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        refresh();
    }

    /** Reloads researcher/equipment dropdowns — call before showing this panel. */
    public void refresh() {
        Researcher selectedResearcher = (Researcher) researcherCombo.getSelectedItem();
        Equipment selectedEquipment = (Equipment) equipmentCombo.getSelectedItem();

        researcherCombo.removeAllItems();
        for (Researcher r : researcherDao.findAll()) {
            researcherCombo.addItem(r);
        }
        equipmentCombo.removeAllItems();
        for (Equipment e : equipmentDao.findAll()) {
            equipmentCombo.addItem(e);
        }

        if (selectedResearcher != null) researcherCombo.setSelectedItem(selectedResearcher);
        if (selectedEquipment != null) equipmentCombo.setSelectedItem(selectedEquipment);
    }

    private void save() {
        try {
            Researcher researcher = (Researcher) researcherCombo.getSelectedItem();
            Equipment equipment = (Equipment) equipmentCombo.getSelectedItem();
            if (researcher == null || equipment == null) {
                throw new IllegalArgumentException("Register a researcher and equipment before creating a booking");
            }
            LocalDate bookingDate = UiUtils.parseDate(bookingDateField.getText());
            java.time.LocalTime start = UiUtils.parseTime(startTimeField.getText());
            java.time.LocalTime end = UiUtils.parseTime(endTimeField.getText());
            String purpose = purposeArea.getText();
            UiUtils.requireNonEmpty(purpose, "Research Purpose");

            Booking booking = new Booking(bookingDate, start, end, purpose.trim(),
                    BookingStatus.ACTIVE, researcher, equipment);
            Booking saved = bookingDao.create(booking);

            UiUtils.showInfo(this, "Booking created with ID " + saved.getBookingId());
            clearForm();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            UiUtils.showError(this, ex.getMessage());
        } catch (RuntimeException ex) {
            UiUtils.showError(this, "Could not create booking: " + ex.getMessage());
        }
    }

    private void clearForm() {
        bookingDateField.setText(LocalDate.now().toString());
        startTimeField.setText("");
        endTimeField.setText("");
        purposeArea.setText("");
    }
}