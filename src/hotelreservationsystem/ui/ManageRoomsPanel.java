package hotelreservationsystem.ui;

import hotelreservationsystem.db.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class ManageRoomsPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtRoomNumber, txtFloor, txtPrice;
    private JComboBox<String> comboRoomType;
    private JComboBox<String> comboStatus;
    private JButton btnSubmit;

    private Integer editingId = null;

    private Map<String, Integer> roomTypeMap = new LinkedHashMap<>();
    private Map<String, Integer> roomTypeDefaultPrice = new LinkedHashMap<>();

    public ManageRoomsPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Add / Edit Room"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Room Number:"), gbc);
        gbc.gridx = 1;
        txtRoomNumber = new JTextField(10);
        formPanel.add(txtRoomNumber, gbc);

        gbc.gridx = 2; gbc.gridy = 0;
        formPanel.add(new JLabel("Floor:"), gbc);
        gbc.gridx = 3;
        txtFloor = new JTextField(10);
        formPanel.add(txtFloor, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Room Type:"), gbc);
        gbc.gridx = 1;
        comboRoomType = new JComboBox<>();
        formPanel.add(comboRoomType, gbc);

        comboRoomType.addActionListener(e -> {
            String selected = (String) comboRoomType.getSelectedItem();
            if (selected != null && roomTypeDefaultPrice.containsKey(selected)) {
                txtPrice.setText(String.valueOf(roomTypeDefaultPrice.get(selected)));
            }
        });

        gbc.gridx = 2; gbc.gridy = 1;
        formPanel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 3;
        comboStatus = new JComboBox<>(new String[]{"available", "occupied", "maintenance"});
        formPanel.add(comboStatus, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Price/Night ($):"), gbc);
        gbc.gridx = 1;
        txtPrice = new JTextField(10);
        formPanel.add(txtPrice, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        btnSubmit = new JButton("➕ Add Room");
        btnSubmit.addActionListener(e -> submitForm());
        formPanel.add(btnSubmit, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 2; gbc.gridy = 3;
        JButton btnClear = new JButton("✖ Clear / Cancel Edit");
        btnClear.addActionListener(e -> clearForm());
        formPanel.add(btnClear, gbc);

        add(formPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
            new Object[]{"ID", "Room #", "Type", "Floor", "Status", "Price/Night ($)"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    loadSelectedRowIntoForm();
                }
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnRefresh = new JButton("🔄 Refresh");
        btnRefresh.addActionListener(e -> {
            loadRoomTypesIntoCombo();
            loadRooms();
        });
        JButton btnDelete = new JButton("🗑️ Delete Selected");
        btnDelete.addActionListener(e -> deleteSelectedRoom());
        bottomPanel.add(btnRefresh);
        bottomPanel.add(btnDelete);
        add(bottomPanel, BorderLayout.SOUTH);

        loadRoomTypesIntoCombo();
        loadRooms();
    }

    private void loadSelectedRowIntoForm() {
        int row = table.getSelectedRow();
        if (row == -1) return;

        editingId = (int) tableModel.getValueAt(row, 0);
        txtRoomNumber.setText(String.valueOf(tableModel.getValueAt(row, 1)));
        comboRoomType.setSelectedItem(tableModel.getValueAt(row, 2));
        txtFloor.setText(String.valueOf(tableModel.getValueAt(row, 3)));
        comboStatus.setSelectedItem(tableModel.getValueAt(row, 4));

        String priceStr = tableModel.getValueAt(row, 5).toString().replace("$", "");
        txtPrice.setText(priceStr);

        btnSubmit.setText("💾 Save Changes");
    }

    private void clearForm() {
        editingId = null;
        txtRoomNumber.setText("");
        txtFloor.setText("");
        txtPrice.setText("");
        btnSubmit.setText("➕ Add Room");
        table.clearSelection();
    }

    private void loadRoomTypesIntoCombo() {
        comboRoomType.removeAllItems();
        roomTypeMap.clear();
        roomTypeDefaultPrice.clear();

        String sql = "SELECT id, name, price_per_night FROM room_types ORDER BY name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                String name = rs.getString("name");
                roomTypeMap.put(name, rs.getInt("id"));
                roomTypeDefaultPrice.put(name, rs.getInt("price_per_night"));
                comboRoomType.addItem(name);
            }

            if (roomTypeMap.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "⚠️ No room types found. Please add a room type first (Room Types tab).");
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "❌ Error loading room types: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void submitForm() {
        String roomNumberStr = txtRoomNumber.getText().trim();
        String floorStr = txtFloor.getText().trim();
        String priceStr = txtPrice.getText().trim();
        String selectedTypeName = (String) comboRoomType.getSelectedItem();
        String status = (String) comboStatus.getSelectedItem();

        if (roomNumberStr.isEmpty() || floorStr.isEmpty() || priceStr.isEmpty() || selectedTypeName == null) {
            JOptionPane.showMessageDialog(this, "❗ All fields are required.");
            return;
        }

        int roomNumber, floor, price;
        try {
            roomNumber = Integer.parseInt(roomNumberStr);
            floor = Integer.parseInt(floorStr);
            price = Integer.parseInt(priceStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "❗ Room Number, Floor, and Price must be numbers.");
            return;
        }

        int roomTypeId = roomTypeMap.get(selectedTypeName);

        if (editingId == null) {
            insertRoom(roomNumber, roomTypeId, floor, status, price);
        } else {
            updateRoom(editingId, roomNumber, roomTypeId, floor, status, price);
        }
    }

    private void insertRoom(int roomNumber, int roomTypeId, int floor, String status, int price) {
        String sql = "INSERT INTO rooms (room_number, room_type_id, floor, status, price_per_night) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, roomNumber);
            pst.setInt(2, roomTypeId);
            pst.setInt(3, floor);
            pst.setString(4, status);
            pst.setInt(5, price);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "✅ Room added!");
            clearForm();
            loadRooms();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "❌ Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void updateRoom(int id, int roomNumber, int roomTypeId, int floor, String status, int price) {
        String sql = "UPDATE rooms SET room_number = ?, room_type_id = ?, floor = ?, status = ?, price_per_night = ? " +
                     "WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, roomNumber);
            pst.setInt(2, roomTypeId);
            pst.setInt(3, floor);
            pst.setString(4, status);
            pst.setInt(5, price);
            pst.setInt(6, id);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "✅ Room updated!");
            clearForm();
            loadRooms();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "❌ Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void loadRooms() {
        tableModel.setRowCount(0);
        String sql = "SELECT r.id, r.room_number, rt.name AS type_name, r.floor, r.status, r.price_per_night " +
                     "FROM rooms r JOIN room_types rt ON r.room_type_id = rt.id " +
                     "ORDER BY r.room_number";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getInt("room_number"),
                    rs.getString("type_name"),
                    rs.getInt("floor"),
                    rs.getString("status"),
                    "$" + rs.getBigDecimal("price_per_night")
                });
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "❌ Error loading rooms: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void deleteSelectedRoom() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "❗ Select a room to delete.");
            return;
        }

        int id = (int) tableModel.getValueAt(row, 0);
        int roomNumber = (int) tableModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete room #" + roomNumber + "?\n(This will fail if it has existing reservations.)",
            "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        String sql = "DELETE FROM rooms WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, id);
            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "✅ Deleted.");
            clearForm();
            loadRooms();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "❌ Cannot delete — this room has existing reservations.");
        }
    }
}