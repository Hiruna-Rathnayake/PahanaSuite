package com.pahanaedu.pahanasuite.dao;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnectionFactory {

    private static final String URL = "jdbc:mysql://localhost:3306/pahanaedu";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws Exception {
        // Load driver if needed
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
