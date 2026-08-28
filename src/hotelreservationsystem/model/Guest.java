/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hotelreservationsystem.model;


public class Guest {
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private int phone;
    private String nationality;

    public Guest(int id, String firstName, String lastName, String email, int phone, String nationality) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.nationality = nationality;
    }

    public int getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public int getPhone() { return phone; }
    public String getNationality() { return nationality; }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public String toString() {
        // used so Guest objects display nicely in JComboBox / JList
        return getFullName() + " (" + email + ")";
    }
}
