/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hotelreservationsystem.model;

import java.math.BigDecimal;
import java.util.Date;

public class Reservation {
    private int id;
    private int guestId;
    private String guestName; // filled via JOIN, optional
    private int roomId;
    private int roomNumber;   // filled via JOIN, optional
    private Date checkIn;
    private Date checkOut;
    private int guestsCount;
    private String status; // "confirmed", "checked_in", "checked_out", "cancelled"
    private BigDecimal totalAmount;

    public Reservation(int id, int guestId, String guestName, int roomId, int roomNumber,
                        Date checkIn, Date checkOut, int guestsCount, String status, BigDecimal totalAmount) {
        this.id = id;
        this.guestId = guestId;
        this.guestName = guestName;
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.guestsCount = guestsCount;
        this.status = status;
        this.totalAmount = totalAmount;
    }

    public int getId() { return id; }
    public int getGuestId() { return guestId; }
    public String getGuestName() { return guestName; }
    public int getRoomId() { return roomId; }
    public int getRoomNumber() { return roomNumber; }
    public Date getCheckIn() { return checkIn; }
    public Date getCheckOut() { return checkOut; }
    public int getGuestsCount() { return guestsCount; }
    public String getStatus() { return status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
}
