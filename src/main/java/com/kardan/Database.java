package com.kardan;

import java.sql.*;

public class Database {

    private static final String URL = "jdbc:sqlite:users.db";


    public static Connection connect() throws SQLException {
        Connection c = DriverManager.getConnection(URL);
        initDB(c);
        return c;
    }

    private static void initDB(Connection c) {
         String sql =
                "CREATE TABLE IF NOT EXISTS users (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "username TEXT UNIQUE, " +
                        "password TEXT" +
                        ");";

        try (var stmt = c.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
