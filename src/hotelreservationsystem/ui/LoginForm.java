/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hotelreservationsystem.ui;

import hotelreservationsystem.db.DBConnection;
import hotelreservationsystem.model.User;
import org.mindrot.jbcrypt.BCrypt;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoginForm extends JFrame{
    JTextField txtEmail;
    JPasswordField txtPassword;
    JButton btnLogin;

    public LoginForm() {
        setTitle("🏨 Hotel Reservation System - Login");
        setSize(450, 350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(240, 244, 248));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Hotel Staff Login", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(30, 60, 90));
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(title, gbc);

        gbc.gridwidth = 1;

        gbc.gridy++;
        mainPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        txtEmail = new JTextField(15);
        mainPanel.add(txtEmail, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        mainPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        txtPassword = new JPasswordField(15);
        mainPanel.add(txtPassword, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        btnLogin = new JButton("Login");
        styleButton(btnLogin, new Color(30, 90, 150));
        mainPanel.add(btnLogin, gbc);

        add(mainPanel, BorderLayout.CENTER);

        btnLogin.addActionListener(e -> handleLogin());
        // Allow pressing Enter in the password field to trigger login
        txtPassword.addActionListener(e -> handleLogin());
    }

    private void handleLogin() {
        String email = txtEmail.getText().trim();
        String password = String.valueOf(txtPassword.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "❗ Please enter both email and password.");
            return;
        }

        String sql = "SELECT u.id, u.full_name, u.email, u.password, u.role_id, r.name AS role_name " +
                     "FROM users u JOIN roles r ON u.role_id = r.id WHERE u.email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, email);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password");

                if (BCrypt.checkpw(password, storedHash)) {
                    User loggedInUser = new User(
                        rs.getInt("id"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getInt("role_id"),
                        rs.getString("role_name")
                    );

                    if (loggedInUser.isAdmin()) {
                        new AdminDashboard(loggedInUser).setVisible(true);
                    } else {
                        new EmployeeDashboard(loggedInUser).setVisible(true);
                    }
                    this.dispose();

                } else {
                    JOptionPane.showMessageDialog(this, "❌ Incorrect password.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "❌ No account found with that email.");
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "❌ Database error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
    }
}
