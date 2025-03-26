package com.xai.atm;

import java.time.LocalDateTime;

public class Transaction {
    private double amount;
    private double fee;
    private double total;
    private LocalDateTime timestamp;

    public Transaction(double amount) {
        this.amount = amount;
        this.fee = amount * 0.05;
        this.total = amount + fee;
        this.timestamp = LocalDateTime.now();
    }

    public double getAmount() { return amount; }
    public double getFee() { return fee; }
    public double getTotal() { return total; }
    public LocalDateTime getTimestamp() { return timestamp; }
}