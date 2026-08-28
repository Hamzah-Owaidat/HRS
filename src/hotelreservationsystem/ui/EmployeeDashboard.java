package hotelreservationsystem.ui;

import hotelreservationsystem.model.User;
import javax.swing.*;
import java.awt.*;

public class EmployeeDashboard extends JFrame {

    public EmployeeDashboard(User user) {
        setTitle("🏨 Employee Dashboard - " + user.getFullName());
        setSize(950, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top header bar
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(30, 90, 60));
        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel welcomeLabel = new JLabel("Welcome, " + user.getFullName() + " (Employee)");
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

        // Employees can handle reservations and payments — not staff/room/role management
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabs.addTab("📋 Reservations", new ManageReservationsPanel());
        tabs.addTab("💳 Payments", new ManagePaymentsPanel());

        add(tabs, BorderLayout.CENTER);
    }
}