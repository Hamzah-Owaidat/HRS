package hotelreservationsystem.ui;

import hotelreservationsystem.db.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class ManagePaymentsPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;

    private JComboBox<String> comboReservation;
    private JTextField txtAmount;
    private JComboBox<String> comboMethod;
    private JLabel lblReservationInfo;

    private Map<String, Integer> reservationIdMap = new LinkedHashMap<>();
    private Map<String, BigDecimal> reservationTotalMap = new LinkedHashMap<>();

    public ManagePaymentsPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(buildFormPanel(), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
            new Object[]{"ID", "Reservation", "Guest", "Total ($)", "Paid ($)", "Remaining ($)", "Method", "Paid At"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnRefresh = new JButton("🔄 Refresh");
        btnRefresh.addActionListener(e -> {
            loadReservationsIntoCombo();
            loadPayments();
        });
        bottomPanel.add(btnRefresh);
        add(bottomPanel, BorderLayout.SOUTH);

        loadReservationsIntoCombo();
        loadPayments();
    }

    private JPanel buildFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Record New Payment"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx = 0; g.gridy = 0;
        formPanel.add(new JLabel("Reservation:"), g);
        g.gridx = 1;
        comboReservation = new JComboBox<>();
        formPanel.add(comboReservation, g);

        g.gridx = 2; g.gridy = 0;
        lblReservationInfo = new JLabel(" ");
        lblReservationInfo.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        formPanel.add(lblReservationInfo, g);

        comboReservation.addItemListener(e -> {
            if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
                String selected = (String) e.getItem();
                if (selected != null) {
                    updateReservationInfoAndSuggestedAmount(selected);
                }
            }
        });

        g.gridx = 0; g.gridy = 1;
        formPanel.add(new JLabel("Amount ($):"), g);
        g.gridx = 1;
        txtAmount = new JTextField(10);
        formPanel.add(txtAmount, g);

        g.gridx = 2; g.gridy = 1;
        formPanel.add(new JLabel("Method:"), g);
        g.gridx = 3;
        comboMethod = new JComboBox<>(new String[]{"cash", "credit_card", "debit_card", "bank_transfer"});
        formPanel.add(comboMethod, g);

        g.gridx = 0; g.gridy = 2; g.gridwidth = 4;
        JButton btnRecord = new JButton("💳 Record Payment");
        btnRecord.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRecord.addActionListener(e -> recordPayment());
        formPanel.add(btnRecord, g);

        return formPanel;
    }

    private void loadReservationsIntoCombo() {
        comboReservation.removeAllItems();
        reservationIdMap.clear();
        reservationTotalMap.clear();

        String sql =
            "SELECT res.id, g.first_name, g.last_name, r.room_number, res.total_amount " +
            "FROM reservations res " +
            "JOIN guests g ON res.guest_id = g.id " +
            "JOIN rooms r ON res.room_id = r.id " +
            "WHERE res.status != 'cancelled' " +
            "ORDER BY res.id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                String label = "#" + rs.getInt("id") + " - " + rs.getString("first_name") + " " +
                               rs.getString("last_name") + " (Room " + rs.getInt("room_number") + ")";
                reservationIdMap.put(label, rs.getInt("id"));
                reservationTotalMap.put(label, rs.getBigDecimal("total_amount"));
                comboReservation.addItem(label);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "❌ Error loading reservations: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void updateReservationInfoAndSuggestedAmount(String selectedLabel) {
        int reservationId = reservationIdMap.get(selectedLabel);
        BigDecimal total = reservationTotalMap.get(selectedLabel);
        BigDecimal alreadyPaid = getTotalPaidForReservation(reservationId);
        BigDecimal remaining = total.subtract(alreadyPaid);

        if (remaining.compareTo(BigDecimal.ZERO) < 0) {
            remaining = BigDecimal.ZERO;
        }

        lblReservationInfo.setText("Total: $" + total + " | Paid: $" + alreadyPaid + " | Remaining: $" + remaining);
        txtAmount.setText(remaining.toString());
    }

    private BigDecimal getTotalPaidForReservation(int reservationId) {
        String sql = "SELECT COALESCE(SUM(amount), 0) AS total_paid FROM payments WHERE reservation_id = ? AND status = 'paid'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, reservationId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getBigDecimal("total_paid");
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return BigDecimal.ZERO;
    }

    private void recordPayment() {
        String selectedReservation = (String) comboReservation.getSelectedItem();
        String amountStr = txtAmount.getText().trim();
        String method = (String) comboMethod.getSelectedItem();

        if (selectedReservation == null || amountStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "❗ Select a reservation and enter an amount.");
            return;
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(amountStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "❗ Amount must be a valid number.");
            return;
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            JOptionPane.showMessageDialog(this, "❗ Amount must be greater than zero.");
            return;
        }

        int reservationId = reservationIdMap.get(selectedReservation);
        BigDecimal total = reservationTotalMap.get(selectedReservation);
        BigDecimal alreadyPaid = getTotalPaidForReservation(reservationId);
        BigDecimal remaining = total.subtract(alreadyPaid);

        if (amount.compareTo(remaining) > 0) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "⚠️ This amount ($" + amount + ") exceeds the remaining balance ($" + remaining + ").\n" +
                "Record it anyway (overpayment)?",
                "Overpayment Warning", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
        }

        String sql = "INSERT INTO payments (reservation_id, amount, payment_method, status, paid_at) " +
                     "VALUES (?, ?, ?, 'paid', GETDATE())";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, reservationId);
            pst.setBigDecimal(2, amount);
            pst.setString(3, method);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "✅ Payment recorded!");
            txtAmount.setText("");
            loadPayments();
            updateReservationInfoAndSuggestedAmount(selectedReservation);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "❌ Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void loadPayments() {
        tableModel.setRowCount(0);

        // Get every reservation with its total, then sum payments per reservation as we go
        String sql =
            "SELECT p.id, p.reservation_id, g.first_name, g.last_name, res.total_amount, " +
            "p.amount, p.payment_method, p.paid_at " +
            "FROM payments p " +
            "JOIN reservations res ON p.reservation_id = res.id " +
            "JOIN guests g ON res.guest_id = g.id " +
            "ORDER BY p.id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                int reservationId = rs.getInt("reservation_id");
                BigDecimal total = rs.getBigDecimal("total_amount");
                BigDecimal paidSoFar = getTotalPaidForReservation(reservationId);
                BigDecimal remaining = total.subtract(paidSoFar);
                if (remaining.compareTo(BigDecimal.ZERO) < 0) remaining = BigDecimal.ZERO;

                tableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    "#" + reservationId,
                    rs.getString("first_name") + " " + rs.getString("last_name"),
                    "$" + total,
                    "$" + paidSoFar,
                    "$" + remaining,
                    rs.getString("payment_method"),
                    rs.getTimestamp("paid_at")
                });
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "❌ Error loading payments: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}