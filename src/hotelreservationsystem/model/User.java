/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hotelreservationsystem.model;

public class User {
    private int id;
    private String fullName;
    private String email;
    private int roleId;
    private String roleName; // "admin" or "employee" - filled via JOIN

    public User(int id, String fullName, String email, int roleId, String roleName) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.roleId = roleId;
        this.roleName = roleName;
    }

    public int getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public int getRoleId() { return roleId; }
    public String getRoleName() { return roleName; }

    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(roleName);
    }
}
