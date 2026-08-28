package hotelreservationsystem.ui;

import hotelreservationsystem.db.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

public class ManageRoomTypesPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtName, txtCapacity, txtPrice;
    private JTextArea txtDescription;
    private JButton btnSubmit;

    private Integer editingId = null; // null = adding new, non-null = editing existing

    public ManageRoomTypesPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Add / Edit Room Type"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        txtName = new JTextField(15);
        formPanel.add(txtName, gbc);

        gbc.gridx = 2; gbc.gridy = 0;
        formPanel.add(new JLabel("Capacity:"), gbc);
        gbc.gridx = 3;
        txtCapacity = new JTextField(6);
        formPanel.add(txtCapacity, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Price/Night ($):"), gbc);
        gbc.gridx = 1;
        txtPrice = new JTextField(15);
        formPanel.add(txtPrice, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        txtDescription = new JTextArea(2, 20);
        txtDescription.setLineWrap(true);
        formPanel.add(new JScrollPane(txtDescription), gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 3;
        btnSubmit = new JButton("➕ Add Room Type");
        btnSubmit.addActionListener(e -> submitForm());
        formPanel.add(btnSubmit, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 3; gbc.gridy = 3;
        JButton btnClear = new JButton("✖ Clear / Cancel Edit");
        btnClear.addActionListener(e -> clearForm());
        formPanel.add(btnClear, gbc);

        add(formPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
            new Object[]{"ID", "Name", "Description", "Capacity", "Price/Night ($)"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);

        // Double-click a row to load it into the form for editing
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
        btnRefresh.addActionListener(e -> loadRoomTypes());
        JButton btnDelete = new JButton("🗑️ Delete Selected");
        btnDelete.addActionListener(e -> deleteSelectedRoomType());
        bottomPanel.add(btnRefresh);
        bottomPanel.add(btnDelete);
        add(bottomPanel, BorderLayout.SOUTH);

        loadRoomTypes();
    }

    private void loadSelectedRowIntoForm() {
        int row = table.getSelectedRow();
        if (row == -1) return;

        editingId = (int) tableModel.getValueAt(row, 0);
        txtName.setText((String) tableModel.getValueAt(row, 1));
        txtDescription.setText((String) tableModel.getValueAt(row, 2));
        txtCapacity.setText(String.valueOf(tableModel.getValueAt(row, 3)));

        // Strip "$" before putting back into the editable field
        String priceStr = tableModel.getValueAt(row, 4).toString().replace("$", "");
        txtPrice.setText(priceStr);

        btnSubmit.setText("💾 Save Changes");
    }

    private void clearForm() {
        editingId = null;
        txtName.setText("");
        txtDescription.setText("");
        txtCapacity.setText("");
        txtPrice.setText("");
        btnSubmit.setText("➕ Add Room Type");
        table.clearSelection();
    }

    private void submitForm() {
        String name = txtName.getText().trim();
        String description = txtDescription.getText().trim();
        String capacityStr = txtCapacity.getText().trim();
        String priceStr = txtPrice.getText().trim();

        if (name.isEmpty() || description.isEmpty() || capacityStr.isEmpty() || priceStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "❗ All fields are required.");
            return;
        }

        int capacity, price;
        try {
            capacity = Integer.parseInt(capacityStr);
            price = Integer.parseInt(priceStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "❗ Capacity and Price must be numbers.");
            return;
        }

        if (editingId == null) {
            insertRoomType(name, description, capacity, price);
        } else {
            updateRoomType(editingId, name, description, capacity, price);
        }
    }

    private void insertRoomType(String name, String description, int capacity, int price) {
        String sql = "INSERT INTO room_types (name, description, capacity, price_per_night) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, name);
            pst.setString(2, description);
            pst.setInt(3, capacity);
            pst.setInt(4, price);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "✅ Room type added!");
            clearForm();
            loadRoomTypes();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "❌ Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void updateRoomType(int id, String name, String description, int capacity, int price) {
        String sql = "UPDATE room_types SET name = ?, description = ?, capacity = ?, price_per_night = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, name);
            pst.setString(2, description);
            pst.setInt(3, capacity);
            pst.setInt(4, price);
            pst.setInt(5, id);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "✅ Room type updated!");
            clearForm();
            loadRoomTypes();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "❌ Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void loadRoomTypes() {
        tableModel.setRowCount(0);
        String sql = "SELECT id, name, description, capacity, price_per_night FROM room_types ORDER BY id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getInt("capacity"),
                    "$" + rs.getInt("price_per_night")
                });
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "❌ Error loading room types: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void deleteSelectedRoomType() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "❗ Select a room type to delete.");
            return;
        }

        int id = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete room type \"" + name + "\"?\n(This will fail if rooms are still using it.)",
            "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        String sql = "DELETE FROM room_types WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, id);
            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "✅ Deleted.");
            clearForm();
            loadRoomTypes();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "❌ Cannot delete — this room type is still assigned to one or more rooms.");
        }
    }
}