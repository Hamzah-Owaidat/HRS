package hotelreservationsystem.ui;

import hotelreservationsystem.db.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

public class ManageRolesPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtRoleName;
    private JButton btnSubmit;

    private Integer editingId = null;

    public ManageRolesPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Add / Edit Role"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Role Name:"), gbc);
        gbc.gridx = 1;
        txtRoleName = new JTextField(15);
        formPanel.add(txtRoleName, gbc);

        gbc.gridx = 2; gbc.gridy = 0;
        btnSubmit = new JButton("➕ Add Role");
        btnSubmit.addActionListener(e -> submitForm());
        formPanel.add(btnSubmit, gbc);

        gbc.gridx = 3; gbc.gridy = 0;
        JButton btnClear = new JButton("✖ Clear / Cancel Edit");
        btnClear.addActionListener(e -> clearForm());
        formPanel.add(btnClear, gbc);

        add(formPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new Object[]{"ID", "Role Name"}, 0) {
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
        btnRefresh.addActionListener(e -> loadRoles());
        JButton btnDelete = new JButton("🗑️ Delete Selected");
        btnDelete.addActionListener(e -> deleteSelectedRole());
        bottomPanel.add(btnRefresh);
        bottomPanel.add(btnDelete);
        add(bottomPanel, BorderLayout.SOUTH);

        loadRoles();
    }

    private void loadSelectedRowIntoForm() {
        int row = table.getSelectedRow();
        if (row == -1) return;

        editingId = (int) tableModel.getValueAt(row, 0);
        txtRoleName.setText((String) tableModel.getValueAt(row, 1));
        btnSubmit.setText("💾 Save Changes");
    }

    private void clearForm() {
        editingId = null;
        txtRoleName.setText("");
        btnSubmit.setText("➕ Add Role");
        table.clearSelection();
    }

    private void submitForm() {
        String name = txtRoleName.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "❗ Role name is required.");
            return;
        }

        if (editingId == null) {
            insertRole(name);
        } else {
            updateRole(editingId, name);
        }
    }

    private void insertRole(String name) {
        String sql = "INSERT INTO roles (name) VALUES (?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, name);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "✅ Role added!");
            clearForm();
            loadRoles();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "❌ Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void updateRole(int id, String name) {
        String sql = "UPDATE roles SET name = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, name);
            pst.setInt(2, id);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "✅ Role updated!");
            clearForm();
            loadRoles();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "❌ Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void loadRoles() {
        tableModel.setRowCount(0);
        String sql = "SELECT id, name FROM roles ORDER BY id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                tableModel.addRow(new Object[]{rs.getInt("id"), rs.getString("name")});
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "❌ Error loading roles: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void deleteSelectedRole() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "❗ Select a role to delete.");
            return;
        }

        int id = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete role \"" + name + "\"?\n(This will fail if any user still has this role.)",
            "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        String sql = "DELETE FROM roles WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, id);
            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "✅ Deleted.");
            clearForm();
            loadRoles();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "❌ Cannot delete — one or more users still have this role.");
        }
    }
}