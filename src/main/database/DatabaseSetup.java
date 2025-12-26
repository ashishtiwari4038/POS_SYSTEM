package main.database;

import java.sql.*;

public class DatabaseSetup {
    public static void initializeDatabase() {
        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                id INT PRIMARY KEY AUTO_INCREMENT,
                username VARCHAR(50) UNIQUE NOT NULL,
                password VARCHAR(255) NOT NULL,
                role ENUM('CASHIER', 'OWNER') NOT NULL
            )
        """;
        
        String createSalesTable = """
            CREATE TABLE IF NOT EXISTS sales (
                id INT PRIMARY KEY AUTO_INCREMENT,
                customer_name VARCHAR(100) NOT NULL,
                customer_number VARCHAR(15),
                product_company VARCHAR(100) NOT NULL,
                product_model VARCHAR(100) NOT NULL,
                sale_date DATETIME DEFAULT CURRENT_TIMESTAMP,
                mrp DECIMAL(10,2) NOT NULL,
                discount DECIMAL(10,2) DEFAULT 0,
                gst DECIMAL(5,2) DEFAULT 0,
                total_price DECIMAL(10,2) NOT NULL,
                cashier_id INT,
                FOREIGN KEY (cashier_id) REFERENCES users(id)
            )
        """;
        
        String insertDefaultUsers = """
            INSERT IGNORE INTO users (username, password, role) VALUES
            ('owner', 'owner123', 'OWNER'),
            ('cashier1', 'cashier123', 'CASHIER')
        """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute(createUsersTable);
            stmt.execute(createSalesTable);
            stmt.execute(insertDefaultUsers);
            
            System.out.println("Database initialized successfully!");
            
        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
        }
    }
}