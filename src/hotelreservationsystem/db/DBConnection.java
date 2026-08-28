package hotelreservationsystem.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL =
        "jdbc:sqlserver://localhost:1433;databaseName=hotel;encrypt=true;trustServerCertificate=true";
    private static final String USER = "hotelapp";
    private static final String PASSWORD = "Training@2026"; 

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQL Server JDBC Driver not found.", e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}