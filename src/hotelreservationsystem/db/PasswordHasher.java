/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hotelreservationsystem.db;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHasher {
    public static void main(String[] args) {
        String plainPassword = "admin123"; // change this to whatever you want your admin password to be
        String hashed = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
        System.out.println("Hashed password: " + hashed);
    }
}
