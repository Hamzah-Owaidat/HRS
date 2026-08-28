package hotelreservationsystem.ui;

import hotelreservationsystem.db.DBConnection;
import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;

public class ManageReservationsPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;

    private JTextField txtGuestEmail, txtFirstName, txtLastName, txtPhone, txtNationality;

    private JComboBox<String> comboRoom;
    private DatePicker datePickerCheckIn, datePickerCheckOut;
    private JSpinner spinnerGuestsCount;
    private JLabel lblCalculatedTotal;

    private Map<String, Integer> roomIdMap = new LinkedHashMap<>();
    private Map<String, BigDecimal> roomPriceMap = new LinkedHashMap<>();
    
    private Map<String, String> roomTypeInfoMap = new LinkedHashMap<>();
    private JLabel lblRoomInfo;

    public ManageReservationsPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(buildFormPanel(), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
            new Object[]{"ID", "Guest", "Room #", "Check-In", "Check-Out", "Guests", "Status", "Total ($)"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        add(buildBottomPanel(), BorderLayout.SOUTH);

        loadRooms();
        loadReservations();
    }

    private JPanel buildFormPanel() {
        JPanel outer = new JPanel();
        outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));

        // ---- Guest section ----
        JPanel guestPanel = new JPanel(new GridBagLayout());
        guestPanel.setBorder(BorderFactory.createTitledBorder("Guest (existing email = reuse, new email = create)"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5);
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx = 0; g.gridy = 0;
        guestPanel.add(new JLabel("Email:"), g);
        g.gridx = 1;
        txtGuestEmail = new JTextField(15);
        guestPanel.add(txtGuestEmail, g);
        JButton btnLookup = new JButton("🔍 Lookup");
        btnLookup.addActionListener(e -> lookupGuest());
        g.gridx = 2;
        guestPanel.add(btnLookup, g);

        g.gridx = 0; g.gridy = 1;
        guestPanel.add(new JLabel("First Name:"), g);
        g.gridx = 1;
        txtFirstName = new JTextField(15);
        guestPanel.add(txtFirstName, g);

        g.gridx = 2; g.gridy = 1;
        guestPanel.add(new JLabel("Last Name:"), g);
        g.gridx = 3;
        txtLastName = new JTextField(15);
        guestPanel.add(txtLastName, g);

        g.gridx = 0; g.gridy = 2;
        guestPanel.add(new JLabel("Phone:"), g);
        g.gridx = 1;
        txtPhone = new JTextField(15);
        guestPanel.add(txtPhone, g);

        g.gridx = 2; g.gridy = 2;
        guestPanel.add(new JLabel("Nationality:"), g);
        g.gridx = 3;
        txtNationality = new JTextField(15);
        guestPanel.add(txtNationality, g);

        outer.add(guestPanel);

        // ---- Reservation section ----
        JPanel resPanel = new JPanel(new GridBagLayout());
        resPanel.setBorder(BorderFactory.createTitledBorder("Reservation Details"));
        GridBagConstraints r = new GridBagConstraints();
        r.insets = new Insets(5, 5, 5, 5);
        r.fill = GridBagConstraints.HORIZONTAL;

        DatePickerSettings settingsIn = new DatePickerSettings();
        settingsIn.setFormatForDatesCommonEra("yyyy-MM-dd");
        settingsIn.setAllowEmptyDates(false);

        r.gridx = 0; r.gridy = 0;
        resPanel.add(new JLabel("Check-In:"), r);
        r.gridx = 1;
        datePickerCheckIn = new DatePicker(settingsIn);
        datePickerCheckIn.setDateToToday();
        resPanel.add(datePickerCheckIn, r);

        DatePickerSettings settingsOut = new DatePickerSettings();
        settingsOut.setFormatForDatesCommonEra("yyyy-MM-dd");
        settingsOut.setAllowEmptyDates(false);

        r.gridx = 2; r.gridy = 0;
        resPanel.add(new JLabel("Check-Out:"), r);
        r.gridx = 3;
        datePickerCheckOut = new DatePicker(settingsOut);
        datePickerCheckOut.setDate(LocalDate.now().plusDays(1));
        resPanel.add(datePickerCheckOut, r);

        r.gridx = 0; r.gridy = 1;
        resPanel.add(new JLabel("Guests Count:"), r);
        r.gridx = 1;
        spinnerGuestsCount = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
        resPanel.add(spinnerGuestsCount, r);

        r.gridx = 2; r.gridy = 1;
        resPanel.add(new JLabel("Room:"), r);
        r.gridx = 3;
        comboRoom = new JComboBox<>();
        resPanel.add(comboRoom, r);
        
        r.gridx = 4; r.gridy = 1;
        lblRoomInfo = new JLabel(" ");
        lblRoomInfo.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        resPanel.add(lblRoomInfo, r);

        comboRoom.addItemListener(e -> {
            if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
                String selected = (String) e.getItem();
                System.out.println("DEBUG: selected = " + selected + " | info = " + roomTypeInfoMap.get(selected));
                lblRoomInfo.setText(roomTypeInfoMap.containsKey(selected)
                    ? roomTypeInfoMap.get(selected) : " ");
            }
        });

        r.gridx = 0; r.gridy = 2;
        JButton btnCheckAvailability = new JButton("🔄 Check Available Rooms for These Dates");
        btnCheckAvailability.addActionListener(e -> loadRooms());
        r.gridwidth = 2;
        resPanel.add(btnCheckAvailability, r);
        r.gridwidth = 1;

        r.gridx = 2; r.gridy = 2;
        lblCalculatedTotal = new JLabel("Total: $0");
        lblCalculatedTotal.setFont(new Font("Segoe UI", Font.BOLD, 14));
        resPanel.add(lblCalculatedTotal, r);

        JButton btnCalculate = new JButton("🧮 Calculate");
        btnCalculate.addActionListener(e -> calculateTotal());
        r.gridx = 3; r.gridy = 2;
        resPanel.add(btnCalculate, r);

        r.gridx = 0; r.gridy = 3; r.gridwidth = 4;
        JButton btnCreate = new JButton("➕ Create Reservation");
        btnCreate.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCreate.addActionListener(e -> createReservation());
        resPanel.add(btnCreate, r);

        outer.add(resPanel);
        return outer;
    }

    private JPanel buildBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnRefresh = new JButton("🔄 Refresh List");
        btnRefresh.addActionListener(e -> {
            loadRooms();
            loadReservations();
        });

        JButton btnCheckIn = new JButton("✅ Check-In");
        btnCheckIn.addActionListener(e -> updateStatus("checked_in"));

        JButton btnCheckOut = new JButton("🚪 Check-Out");
        btnCheckOut.addActionListener(e -> updateStatus("checked_out"));

        JButton btnCancel = new JButton("❌ Cancel Reservation");
        btnCancel.addActionListener(e -> updateStatus("cancelled"));

        panel.add(btnRefresh);
        panel.add(btnCheckIn);
        panel.add(btnCheckOut);
        panel.add(btnCancel);
        return panel;
    }

    private void lookupGuest() {
        String email = txtGuestEmail.getText().trim();
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "❗ Enter an email to look up.");
            return;
        }

        String sql = "SELECT first_name, last_name, phone, nationality FROM guests WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, email);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                txtFirstName.setText(rs.getString("first_name"));
                txtLastName.setText(rs.getString("last_name"));
                txtPhone.setText(rs.getString("phone"));
                txtNationality.setText(rs.getString("nationality"));
                JOptionPane.showMessageDialog(this, "✅ Existing guest found and loaded.");
            } else {
                txtFirstName.setText("");
                txtLastName.setText("");
                txtPhone.setText("");
                txtNationality.setText("");
                JOptionPane.showMessageDialog(this, "ℹ️ No guest with that email — fill in details to create a new one.");
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "❌ Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void loadRooms() {
        comboRoom.removeAllItems();
        roomIdMap.clear();
        roomPriceMap.clear();

        LocalDate checkIn = datePickerCheckIn.getDate();
        LocalDate checkOut = datePickerCheckOut.getDate();

        if (checkIn == null || checkOut == null) {
            JOptionPane.showMessageDialog(this, "❗ Please select both check-in and check-out dates.");
            return;
        }

        // Rooms NOT already booked (confirmed or checked_in) for an overlapping date range
        String sql =
            "SELECT r.id, r.room_number, r.price_per_night, rt.name AS type_name, rt.capacity " +
            "FROM rooms r JOIN room_types rt ON r.room_type_id = rt.id " +
            "WHERE r.status != 'maintenance' AND r.id NOT IN ( " +
            "   SELECT res.room_id FROM reservations res " +
            "   WHERE res.status IN ('confirmed','checked_in') " +
            "   AND res.check_in < ? AND res.check_out > ? " +
            ") ORDER BY r.room_number";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setDate(1, Date2Sql(checkOut));
            pst.setDate(2, Date2Sql(checkIn));

            ResultSet rs = pst.executeQuery();
            roomTypeInfoMap.clear(); // add this alongside the other .clear() calls at the top of loadRooms()

            while (rs.next()) {
                String label = "Room " + rs.getInt("room_number");
                roomIdMap.put(label, rs.getInt("id"));
                roomPriceMap.put(label, rs.getBigDecimal("price_per_night"));
                roomTypeInfoMap.put(label, rs.getString("type_name") + " · Capacity: " + rs.getInt("capacity"));
                comboRoom.addItem(label);
            }

            if (roomIdMap.isEmpty()) {
                JOptionPane.showMessageDialog(this, "⚠️ No rooms available for these dates.");
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "❌ Error loading rooms: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void calculateTotal() {
        String selectedRoom = (String) comboRoom.getSelectedItem();
        if (selectedRoom == null) {
            JOptionPane.showMessageDialog(this, "❗ Select a room first.");
            return;
        }

        LocalDate checkIn = datePickerCheckIn.getDate();
        LocalDate checkOut = datePickerCheckOut.getDate();

        if (checkIn == null || checkOut == null) {
            JOptionPane.showMessageDialog(this, "❗ Please select both check-in and check-out dates.");
            return;
        }

        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (nights <= 0) {
            JOptionPane.showMessageDialog(this, "❗ Check-out must be after check-in.");
            return;
        }

        BigDecimal pricePerNight = roomPriceMap.get(selectedRoom);
        BigDecimal total = pricePerNight.multiply(BigDecimal.valueOf(nights));
        lblCalculatedTotal.setText("Total: $" + total + " (" + nights + " night" + (nights > 1 ? "s" : "") + ")");
    }

    private void createReservation() {
        String email = txtGuestEmail.getText().trim();
        String firstName = txtFirstName.getText().trim();
        String lastName = txtLastName.getText().trim();
        String phoneStr = txtPhone.getText().trim();
        String nationality = txtNationality.getText().trim();
        String selectedRoom = (String) comboRoom.getSelectedItem();

        if (email.isEmpty() || firstName.isEmpty() || lastName.isEmpty() ||
            phoneStr.isEmpty() || nationality.isEmpty() || selectedRoom == null) {
            JOptionPane.showMessageDialog(this, "❗ Please fill in all guest and reservation fields.");
            return;
        }

//        int phone;
//        try {
//            phone = Integer.parseInt(phoneStr);
//        } catch (NumberFormatException ex) {
//            JOptionPane.showMessageDialog(this, "❗ Phone must be numeric.");
//            return;
//        }

        LocalDate checkIn = datePickerCheckIn.getDate();
        LocalDate checkOut = datePickerCheckOut.getDate();

        if (checkIn == null || checkOut == null) {
            JOptionPane.showMessageDialog(this, "❗ Please select both check-in and check-out dates.");
            return;
        }

        int guestsCount = (int) spinnerGuestsCount.getValue();
        int roomId = roomIdMap.get(selectedRoom);
        BigDecimal pricePerNight = roomPriceMap.get(selectedRoom);
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        BigDecimal total = pricePerNight.multiply(BigDecimal.valueOf(nights));

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                int guestId = findOrCreateGuest(conn, firstName, lastName, email, phoneStr, nationality);

                String insertRes = "INSERT INTO reservations " +
                    "(guest_id, room_id, check_in, check_out, guests_count, status, total_amount) " +
                    "VALUES (?, ?, ?, ?, ?, 'confirmed', ?)";
                try (PreparedStatement pst = conn.prepareStatement(insertRes)) {
                    pst.setInt(1, guestId);
                    pst.setInt(2, roomId);
                    pst.setDate(3, Date2Sql(checkIn));
                    pst.setDate(4, Date2Sql(checkOut));
                    pst.setInt(5, guestsCount);
                    pst.setBigDecimal(6, total);
                    pst.executeUpdate();
                }

                String updateRoom = "UPDATE rooms SET status = 'occupied' WHERE id = ?";
                try (PreparedStatement pst = conn.prepareStatement(updateRoom)) {
                    pst.setInt(1, roomId);
                    pst.executeUpdate();
                }

                conn.commit();
                JOptionPane.showMessageDialog(this, "✅ Reservation created! Total: $" + total);

                clearForm();
                loadRooms();
                loadReservations();

            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "❌ Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private int findOrCreateGuest(Connection conn, String firstName, String lastName,
                                   String email, String phone, String nationality) throws SQLException {

        String findSql = "SELECT id FROM guests WHERE email = ?";
        try (PreparedStatement pst = conn.prepareStatement(findSql)) {
            pst.setString(1, email);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }

        String insertSql = "INSERT INTO guests (first_name, last_name, email, phone, nationality) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pst = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, firstName);
            pst.setString(2, lastName);
            pst.setString(3, email);
            pst.setString(4, phone);
            pst.setString(5, nationality);
            pst.executeUpdate();

            ResultSet keys = pst.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }
        }
        throw new SQLException("Failed to create guest.");
    }

    private void loadReservations() {
        tableModel.setRowCount(0);
        String sql =
            "SELECT res.id, g.first_name, g.last_name, r.room_number, res.check_in, res.check_out, " +
            "res.guests_count, res.status, res.total_amount " +
            "FROM reservations res " +
            "JOIN guests g ON res.guest_id = g.id " +
            "JOIN rooms r ON res.room_id = r.id " +
            "ORDER BY res.id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("first_name") + " " + rs.getString("last_name"),
                    rs.getInt("room_number"),
                    rs.getDate("check_in"),
                    rs.getDate("check_out"),
                    rs.getInt("guests_count"),
                    rs.getString("status"),
                    "$" + rs.getBigDecimal("total_amount")
                });
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "❌ Error loading reservations: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void updateStatus(String newStatus) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "❗ Select a reservation first.");
            return;
        }

        int reservationId = (int) tableModel.getValueAt(row, 0);
        String currentStatus = (String) tableModel.getValueAt(row, 6);

        if (currentStatus.equals("cancelled") || currentStatus.equals("checked_out")) {
            JOptionPane.showMessageDialog(this, "❗ This reservation is already " + currentStatus + ".");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String updateRes = "UPDATE reservations SET status = ? WHERE id = ?";
                int roomId;

                String getRoomSql = "SELECT room_id FROM reservations WHERE id = ?";
                try (PreparedStatement pst = conn.prepareStatement(getRoomSql)) {
                    pst.setInt(1, reservationId);
                    ResultSet rs = pst.executeQuery();
                    rs.next();
                    roomId = rs.getInt("room_id");
                }

                try (PreparedStatement pst = conn.prepareStatement(updateRes)) {
                    pst.setString(1, newStatus);
                    pst.setInt(2, reservationId);
                    pst.executeUpdate();
                }

                // Free up the room if checked out or cancelled
                if (newStatus.equals("checked_out") || newStatus.equals("cancelled")) {
                    String updateRoom = "UPDATE rooms SET status = 'available' WHERE id = ?";
                    try (PreparedStatement pst = conn.prepareStatement(updateRoom)) {
                        pst.setInt(1, roomId);
                        pst.executeUpdate();
                    }
                }

                conn.commit();
                JOptionPane.showMessageDialog(this, "✅ Status updated to \"" + newStatus + "\".");
                loadReservations();
                loadRooms();

            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "❌ Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void clearForm() {
        txtGuestEmail.setText("");
        txtFirstName.setText("");
        txtLastName.setText("");
        txtPhone.setText("");
        txtNationality.setText("");
        lblCalculatedTotal.setText("Total: $0");
    }

    private java.sql.Date Date2Sql(LocalDate localDate) {
        return java.sql.Date.valueOf(localDate);
    }
}