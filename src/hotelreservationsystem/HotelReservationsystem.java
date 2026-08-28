/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package hotelreservationsystem;

import hotelreservationsystem.ui.LoginForm;
import javax.swing.SwingUtilities;

public class HotelReservationsystem {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
    }
}
