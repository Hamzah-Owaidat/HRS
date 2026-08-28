/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hotelreservationsystem.model;

import java.math.BigDecimal;

/**
 *
 * @author hamza
 */
public class Room {
    private int id;
    private int roomNumber;
    private int roomTypeId;
    private String roomTypeName; // filled via JOIN, optional
    private int floor;
    private String status; // "available", "occupied", "maintenance", etc.
    private BigDecimal pricePerNight;

    public Room(int id, int roomNumber, int roomTypeId, String roomTypeName,
                int floor, String status, BigDecimal pricePerNight) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.roomTypeId = roomTypeId;
        this.roomTypeName = roomTypeName;
        this.floor = floor;
        this.status = status;
        this.pricePerNight = pricePerNight;
    }

    public int getId() { return id; }
    public int getRoomNumber() { return roomNumber; }
    public int getRoomTypeId() { return roomTypeId; }
    public String getRoomTypeName() { return roomTypeName; }
    public int getFloor() { return floor; }
    public String getStatus() { return status; }
    public BigDecimal getPricePerNight() { return pricePerNight; }

    @Override
    public String toString() {
        return "Room " + roomNumber + " - " + roomTypeName + " (Floor " + floor + ") - $" + pricePerNight + "/night";
    }
}
