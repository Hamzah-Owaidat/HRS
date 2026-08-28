/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hotelreservationsystem.model;

import java.math.BigDecimal;
import java.security.Timestamp;

public class Payment {
    private int id;
    private int reservationId;
    private BigDecimal amount;
    private String paymentMethod;
    private String status; // "paid", "pending", "refunded"
    private Timestamp paidAt;

    public Payment(int id, int reservationId, BigDecimal amount, String paymentMethod, String status, Timestamp paidAt) {
        this.id = id;
        this.reservationId = reservationId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.paidAt = paidAt;
    }

    public int getId() { return id; }
    public int getReservationId() { return reservationId; }
    public BigDecimal getAmount() { return amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getStatus() { return status; }
    public Timestamp getPaidAt() { return paidAt; }
}
