package com.xai.atm;

public class User {
    private String cardNumber;
    private String pinHash;
    private double balance;
    private int attempts;
    private boolean blocked;

    public User(String cardNumber, String pin, double balance) {
        this(cardNumber, pin, balance, 0, false);
    }

    public User(String cardNumber, String pin, double balance, int attempts, boolean blocked) {
        this.cardNumber = cardNumber;
        this.pinHash = pin;
        this.balance = balance;
        this.attempts = attempts;
        this.blocked = blocked;
    }

    public String getCardNumber() { return cardNumber; }
    public String getPinHash() { return pinHash; }
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
    public int getAttempts() { return attempts; }
    public void incrementAttempts() { this.attempts++; }
    public void resetAttempts() { this.attempts = 0; }
    public boolean isBlocked() { return blocked; }
    public void block() { this.blocked = true; }
}