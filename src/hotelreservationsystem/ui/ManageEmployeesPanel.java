package hotelreservationsystem.ui;

import hotelreservationsystem.db.DBConnection;
import org.mindrot.jbcrypt.BCrypt;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

public class ManageEmployeesPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtFullName, txtEmail;
    private JPasswordField txtPassword;
    private JComboBox<String> comboRole;
    private JButton btnSubmit;
    private JLabel lblPasswordHint;

    private Integer editingId = null;

    public ManageEmployeesPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Add / Edit Employee / Admin"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1;
        txtFullName = new JTextField(15);
        formPanel.add(txtFullName, gbc);

        gbc.gridx = 2; gbc.gridy = 0;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 3;
        txtEmail = new JTextField(15);
        formPanel.add(txtEmail, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        txtPassword = new JPasswordField(15);
        formPanel.add(txtPassword, gbc);

        gbc.gridx = 2; gbc.gridy = 1;
        lblPasswordHint = new JLabel(" ");
        lblPasswordHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        formPanel.add(lblPasswordHint, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1;
        comboRole = new JComboBox<>();
        formPanel.add(comboRole, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        btnSubmit = new JButton("➕ Add Employee");
        btnSubmit.addActionListener(e -> submitForm());
        formPanel.add(btnSubmit, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 2; gbc.gridy = 3;
        JButton btnClear = new JButton("✖ Clear / Cancel Edit");
        btnClear.addActionListener(e -> clearForm());
        formPanel.add(btnClear, gbc);

        add(formPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new Object[]{"ID", "Full Name", "Email", "Role"}, 0) {
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
            loadRolesIntoCombo();
            loadEmployees();
        });
        JButton btnDelete = new JButton("🗑️ Delete Selected");
        btnDelete.addActionListener(e -> deleteSelectedEmployee());
        bottomPanel.add(btnRefresh);
        bottomPanel.add(btnDelete);
        add(bottomPanel, BorderLayout.SOUTH);

        loadRolesIntoCombo();
        loadEmployees();
    }

    private void loadSelectedRowIntoForm() {
        int row = table.getSelectedRow();
        if (row == -1) return;

        editingId = (int) tableModel.getValueAt(row, 0);
        txtFullName.setText((String) tableModel.getValueAt(row, 1));
        txtEmail.setText((String) tableModel.getValueAt(row, 2));
        comboRole.setSelectedItem(tableModel.getValueAt(row, 3));

        txtPassword.setText("");
        lblPasswordHint.setText("(leave blank to keep current password)");
        btnSubmit.setText("💾 Save Changes");
    }

    private void clearForm() {
        editingId = null;
        txtFullName.setText("");
        txtEmail.setText("");
        txtPassword.setText("");
        lblPasswordHint.setText(" ");
        btnSubmit.setText("➕ Add Employee");
        table.clearSelection();
    }

    private void loadRolesIntoCombo() {
        comboRole.removeAllItems();
        String sql = "SELECT name FROM roles ORDER BY name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                comboRole.addItem(rs.getString("name"));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "❌ Error loading roles: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void submitForm() {
        String fullName = txtFullName.getText().trim();
        String email = txtEmail.getText().trim();
        String password = String.valueOf(txtPassword.getPassword());
        String role = (String) comboRole.getSelectedItem();

        if (fullName.isEmpty() || email.isEmpty() || role == null) {
            JOptionPane.showMessageDialog(this, "❗ Full name, email, and role are required.");
            return;
        }

        // Password required only when adding new; optional when editing
        if (editingId == null && password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "❗ Password is required for a new employee.");
            return;
        }

        if (editingId == null) {
            insertEmployee(fullName, email, password, role);
        } else {
            updateEmployee(editingId, fullName, email, password, role);
        }
    }

    private void insertEmployee(String fullName, String email, String password, String role) {
        String roleIdSql = "SELECT id FROM roles WHERE name = ?";
        String insertSql = "INSERT INTO users (full_name, email, password, role_id) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {

            int roleId = getRoleId(conn, role, roleIdSql);
            if (roleId == -1) return;

            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

            try (PreparedStatement pst = conn.prepareStatement(insertSql)) {
                pst.setString(1, fullName);
                pst.setString(2, email);
                pst.setString(3, hashedPassword);
                pst.setInt(4, roleId);
                pst.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "✅ Employee added successfully!");
            clearForm();
            loadEmployees();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "❌ Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void updateEmployee(int id, String fullName, String email, String password, String role) {
        String roleIdSql = "SELECT id FROM roles WHERE name = ?";

        try (Connection conn = DBConnection.getConnection()) {

            int roleId = getRoleId(conn, role, roleIdSql);
            if (roleId == -1) return;

            String sql;
            if (password.isEmpty()) {
                // Keep existing password — don't touch that column
                sql = "UPDATE users SET full_name = ?, email = ?, role_id = ? WHERE id = ?";
                try (PreparedStatement pst = conn.prepareStatement(sql)) {
                    pst.setString(1, fullName);
                    pst.setString(2, email);
                    pst.setInt(3, roleId);
                    pst.setInt(4, id);
                    pst.executeUpdate();
                }
            } else {
                String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
                sql = "UPDATE users SET full_name = ?, email = ?, password = ?, role_id = ? WHERE id = ?";
                try (PreparedStatement pst = conn.prepareStatement(sql)) {
                    pst.setString(1, fullName);
                    pst.setString(2, email);
                    pst.setString(3, hashedPassword);
                    pst.setInt(4, roleId);
                    pst.setInt(5, id);
                    pst.executeUpdate();
                }
            }

            JOptionPane.showMessageDialog(this, "✅ Employee updated!");
            clearForm();
            loadEmployees();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "❌ Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private int getRoleId(Connection conn, String roleName, String sql) throws SQLException {
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, roleName);
            ResultSet rs = pst.executeQuery();
            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "❌ Role not found in database.");
                return -1;
            }
            return rs.getInt("id");
        }
    }

    private void loadEmployees() {
        tableModel.setRowCount(0);
        String sql = "SELECT u.id, u.full_name, u.email, r.name AS role_name " +
                     "FROM users u JOIN roles r ON u.role_id = r.id ORDER BY u.id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("full_name"),
                    rs.getString("email"),
                    rs.getString("role_name")
                });
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "❌ Error loading employees: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void deleteSelectedEmployee() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "❗ Select an employee to delete.");
            return;
        }

        int id = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete employee \"" + name + "\"?", "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, id);
            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "✅ Deleted.");
            clearForm();
            loadEmployees();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "❌ Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}