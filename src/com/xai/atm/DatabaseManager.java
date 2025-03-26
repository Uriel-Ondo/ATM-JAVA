package com.xai.atm;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:atm.db";

    public DatabaseManager() {
        createTables();
    }

    private void createTables() {
        String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                "card_number TEXT PRIMARY KEY, " +
                "pin TEXT NOT NULL, " +
                "balance REAL NOT NULL, " +
                "attempts INTEGER DEFAULT 0, " +
                "blocked INTEGER DEFAULT 0)";
        String createTransactionsTable = "CREATE TABLE IF NOT EXISTS transactions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "card_number TEXT, " +
                "amount REAL, " +
                "fee REAL, " +
                "total REAL, " +
                "timestamp TEXT, " +
                "FOREIGN KEY (card_number) REFERENCES users(card_number))";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createTransactionsTable);

            // Ajouter un utilisateur de test s'il n'existe pas
            String checkUser = "SELECT COUNT(*) FROM users WHERE card_number = '1234567890123456'";
            ResultSet rs = stmt.executeQuery(checkUser);
            if (rs.getInt(1) == 0) {
                String insertUser = "INSERT INTO users (card_number, pin, balance) VALUES ('1234567890123456', '1234', 1000.0)";
                stmt.execute(insertUser);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public User getUser(String cardNumber) {
        String query = "SELECT * FROM users WHERE card_number = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, cardNumber);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getString("card_number"),
                        rs.getString("pin"),
                        rs.getDouble("balance"),
                        rs.getInt("attempts"),
                        rs.getInt("blocked") == 1
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateUser(User user) {
        String query = "UPDATE users SET pin = ?, balance = ?, attempts = ?, blocked = ? WHERE card_number = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, user.getPinHash());
            pstmt.setDouble(2, user.getBalance());
            pstmt.setInt(3, user.getAttempts());
            pstmt.setInt(4, user.isBlocked() ? 1 : 0);
            pstmt.setString(5, user.getCardNumber());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addTransaction(Transaction transaction, String cardNumber) {
        String query = "INSERT INTO transactions (card_number, amount, fee, total, timestamp) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, cardNumber);
            pstmt.setDouble(2, transaction.getAmount());
            pstmt.setDouble(3, transaction.getFee());
            pstmt.setDouble(4, transaction.getTotal());
            pstmt.setString(5, transaction.getTimestamp().toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Transaction> getTransactions(String cardNumber) {
        List<Transaction> transactions = new ArrayList<>();
        String query = "SELECT * FROM transactions WHERE card_number = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, cardNumber);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                transactions.add(new Transaction(rs.getDouble("amount")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }
}