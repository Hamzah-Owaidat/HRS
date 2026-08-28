/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hotelreservationsystem.ui;

import hotelreservationsystem.model.User;
import javax.swing.*;
import java.awt.*;

public class AdminDashboard extends JFrame {

    private User currentAdmin;

    public AdminDashboard(User user) {
        this.currentAdmin = user;

        setTitle("🏨 Admin Dashboard - " + user.getFullName());
        setSize(950, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top header bar
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(30, 60, 90));
        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel welcomeLabel = new JLabel("Welcome, " + user.getFullName() + " (Admin)");
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        header.add(welcomeLabel, BorderLayout.WEST);

        JButton btnLogout = new JButton("Logout");
        btnLogout.setFocusPainted(false);
        btnLogout.addActionListener(e -> {
            new LoginForm().setVisible(true);
            this.dispose();
        });
        header.add(btnLogout, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // Tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        tabs.addTab("👥 Employees", new ManageEmployeesPanel());
        tabs.addTab("🎭 Roles", new ManageRolesPanel());
        tabs.addTab("🚪 Rooms", new ManageRoomsPanel());
        tabs.addTab("🏷️ Room Types", new ManageRoomTypesPanel());
        tabs.addTab("📋 Reservations", new ManageReservationsPanel());
        tabs.addTab("💳 Payments", new ManagePaymentsPanel());

        add(tabs, BorderLayout.CENTER);
    }
}
