package com.example.placement.config;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/placement_tracker",
                    "root",
                    "Root@2005"
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to get DB connection", e);
        }
    }
}

