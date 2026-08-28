/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hotelreservationsystem.model;

public class RoomType {
    private int id;
    private String name;
    private String description;
    private int capacity;
    private int pricePerNight;

    public RoomType(int id, String name, String description, int capacity, int pricePerNight) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.capacity = capacity;
        this.pricePerNight = pricePerNight;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getCapacity() { return capacity; }
    public int getPricePerNight() { return pricePerNight; }

    @Override
    public String toString() {
        return name + " (up to " + capacity + " guests, $" + pricePerNight + "/night)";
    }
}
