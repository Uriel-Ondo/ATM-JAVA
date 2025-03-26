package com.xai.atm;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ATMFrame extends JFrame {
    private User currentUser;
    private List<Transaction> transactions;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JPanel withdrawPanel;
    private JPanel receiptPanel;
    private JPanel ejectPanel;
    private JPanel balancePanel;
    private DatabaseManager dbManager;

    public ATMFrame() {
        super("xAI ATM");
        dbManager = new DatabaseManager();
        transactions = new ArrayList<>();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        add(mainPanel);

        mainPanel.add(createInsertCardPanel(), "InsertCard");
        mainPanel.add(createPinPanel(), "Pin");
        mainPanel.add(createMainMenuPanel(), "MainMenu");
        balancePanel = createBalancePanel();
        mainPanel.add(balancePanel, "Balance");
        withdrawPanel = createWithdrawPanel();
        mainPanel.add(withdrawPanel, "Withdraw");
        mainPanel.add(createCustomWithdrawPanel(), "CustomWithdraw");
        receiptPanel = createReceiptPanel();
        mainPanel.add(receiptPanel, "Receipt");
        ejectPanel = createEjectPanel();
        mainPanel.add(ejectPanel, "Eject");

        cardLayout.show(mainPanel, "InsertCard");
        setVisible(true);
    }

    private JPanel createInsertCardPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Bienvenue au Guichet Automatique", SwingConstants.CENTER);
        JTextField cardField = new JTextField("Numéro de carte (16 chiffres)");
        cardField.setForeground(Color.GRAY);
        cardField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (cardField.getText().equals("Numéro de carte (16 chiffres)")) {
                    cardField.setText("");
                    cardField.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (cardField.getText().isEmpty()) {
                    cardField.setForeground(Color.GRAY);
                    cardField.setText("Numéro de carte (16 chiffres)");
                }
            }
        });
        JButton insertButton = new JButton("Insérer");
        JLabel message = new JLabel("", SwingConstants.CENTER);

        insertButton.addActionListener(e -> {
            String cardNumber = cardField.getText();
            currentUser = dbManager.getUser(cardNumber);
            if (currentUser != null) {
                if (currentUser.isBlocked()) {
                    message.setText("Carte bloquée. Contactez votre banque.");
                } else {
                    cardLayout.show(mainPanel, "Pin");
                }
            } else {
                message.setText("Numéro de carte invalide.");
            }
        });

        panel.add(title);
        panel.add(cardField);
        panel.add(insertButton);
        panel.add(message);
        return panel;
    }

    private JPanel createPinPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Entrez votre PIN", SwingConstants.CENTER);
        JPasswordField pinField = new JPasswordField();
        JButton validateButton = new JButton("Valider");
        JLabel message = new JLabel("", SwingConstants.CENTER);

        validateButton.addActionListener(e -> {
            String pin = new String(pinField.getPassword());
            if (currentUser.getPinHash().equals(pin)) {
                currentUser.resetAttempts();
                dbManager.updateUser(currentUser);
                cardLayout.show(mainPanel, "MainMenu");
            } else {
                currentUser.incrementAttempts();
                if (currentUser.getAttempts() >= 3) {
                    currentUser.block();
                    dbManager.updateUser(currentUser);
                    message.setText("Carte bloquée après 3 tentatives.");
                    currentUser = null;
                    Timer timer = new Timer(2000, evt -> cardLayout.show(mainPanel, "InsertCard"));
                    timer.setRepeats(false);
                    timer.start();
                } else {
                    dbManager.updateUser(currentUser);
                    message.setText("PIN incorrect. Restant : " + (3 - currentUser.getAttempts()));
                }
            }
        });

        panel.add(title);
        panel.add(pinField);
        panel.add(validateButton);
        panel.add(message);
        return panel;
    }

    private JPanel createMainMenuPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Menu Principal", SwingConstants.CENTER);
        JButton balanceButton = new JButton("Vérifier le solde");
        JButton withdrawButton = new JButton("Retrait d'argent");
        JButton ejectButton = new JButton("Retirer la carte");

        balanceButton.addActionListener(e -> {
            updateBalancePanel();
            cardLayout.show(mainPanel, "Balance");
        });
        withdrawButton.addActionListener(e -> {
            updateWithdrawPanel();
            cardLayout.show(mainPanel, "Withdraw");
        });
        ejectButton.addActionListener(e -> {
            updateEjectPanel(null);
            cardLayout.show(mainPanel, "Eject");
        });

        panel.add(title);
        panel.add(balanceButton);
        panel.add(withdrawButton);
        panel.add(ejectButton);
        return panel;
    }

    private JPanel createBalancePanel() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Votre Solde", SwingConstants.CENTER);
        JLabel balanceLabel = new JLabel("", SwingConstants.CENTER);
        JButton backButton = new JButton("Retour");

        backButton.addActionListener(e -> cardLayout.show(mainPanel, "MainMenu"));

        panel.add(title);
        panel.add(balanceLabel);
        panel.add(backButton);
        return panel;
    }

    private void updateBalancePanel() {
        JLabel balanceLabel = (JLabel) balancePanel.getComponent(1);
        balanceLabel.setText("Solde actuel : " + (currentUser != null ? currentUser.getBalance() : 0.0) + " €");
    }

    private JPanel createWithdrawPanel() {
        JPanel panel = new JPanel(new GridLayout(8, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Retrait d'argent (Frais 5%)", SwingConstants.CENTER);
        JLabel balanceLabel = new JLabel("", SwingConstants.CENTER);
        ButtonGroup group = new ButtonGroup();
        JRadioButton rb20 = new JRadioButton("20 €"); group.add(rb20);
        JRadioButton rb50 = new JRadioButton("50 €"); group.add(rb50);
        JRadioButton rb100 = new JRadioButton("100 €"); group.add(rb100);
        JRadioButton rb200 = new JRadioButton("200 €"); group.add(rb200);
        JRadioButton rbCustom = new JRadioButton("Autre montant"); group.add(rbCustom);
        JButton confirmButton = new JButton("Confirmer");
        JButton backButton = new JButton("Retour");
        JLabel message = new JLabel("", SwingConstants.CENTER);

        confirmButton.addActionListener(e -> {
            double amount = 0;
            if (rb20.isSelected()) amount = 20;
            else if (rb50.isSelected()) amount = 50;
            else if (rb100.isSelected()) amount = 100;
            else if (rb200.isSelected()) amount = 200;
            else if (rbCustom.isSelected()) {
                cardLayout.show(mainPanel, "CustomWithdraw");
                return;
            } else {
                message.setText("Sélectionnez un montant.");
                return;
            }
            processWithdrawal(amount, message);
        });

        backButton.addActionListener(e -> cardLayout.show(mainPanel, "MainMenu"));

        panel.add(title);
        panel.add(balanceLabel);
        panel.add(rb20);
        panel.add(rb50);
        panel.add(rb100);
        panel.add(rb200);
        panel.add(rbCustom);
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.add(confirmButton);
        buttonPanel.add(backButton);
        panel.add(buttonPanel);
        panel.add(message);
        return panel;
    }

    private void updateWithdrawPanel() {
        JLabel balanceLabel = (JLabel) withdrawPanel.getComponent(1);
        balanceLabel.setText("Solde : " + (currentUser != null ? currentUser.getBalance() : 0.0) + " €");
    }

    private JPanel createCustomWithdrawPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Retrait personnalisé (Frais 5%)", SwingConstants.CENTER);
        JTextField amountField = new JTextField("Montant (multiple de 10)");
        amountField.setForeground(Color.GRAY);
        amountField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (amountField.getText().equals("Montant (multiple de 10)")) {
                    amountField.setText("");
                    amountField.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (amountField.getText().isEmpty()) {
                    amountField.setForeground(Color.GRAY);
                    amountField.setText("Montant (multiple de 10)");
                }
            }
        });
        JButton confirmButton = new JButton("Confirmer");
        JButton backButton = new JButton("Retour");
        JLabel message = new JLabel("", SwingConstants.CENTER);

        confirmButton.addActionListener(e -> {
            try {
                double amount = Double.parseDouble(amountField.getText());
                if (amount <= 0 || amount % 10 != 0) {
                    message.setText("Montant invalide (multiple de 10 requis).");
                } else {
                    processWithdrawal(amount, message);
                }
            } catch (NumberFormatException ex) {
                message.setText("Montant invalide.");
            }
        });

        backButton.addActionListener(e -> {
            updateWithdrawPanel();
            cardLayout.show(mainPanel, "Withdraw");
        });

        panel.add(title);
        panel.add(amountField);
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.add(confirmButton);
        buttonPanel.add(backButton);
        panel.add(buttonPanel);
        panel.add(message);
        return panel;
    }

    private void processWithdrawal(double amount, JLabel message) {
        if (currentUser == null) {
            message.setText("Utilisateur non authentifié.");
            return;
        }
        double fee = amount * 0.05;
        double total = amount + fee;
        if (total > currentUser.getBalance()) {
            message.setText("Solde insuffisant.");
        } else {
            currentUser.setBalance(currentUser.getBalance() - total);
            Transaction transaction = new Transaction(amount);
            transactions.add(transaction);
            dbManager.updateUser(currentUser); // Mettre à jour le solde dans la DB
            dbManager.addTransaction(transaction, currentUser.getCardNumber()); // Enregistrer la transaction
            updateReceiptPanel(transaction);
            cardLayout.show(mainPanel, "Receipt");
        }
    }

    private JPanel createReceiptPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Reçu de Transaction", SwingConstants.CENTER);
        JTextArea receipt = new JTextArea();
        receipt.setEditable(false);
        JButton ejectButton = new JButton("Retirer la carte");

        ejectButton.addActionListener(e -> {
            updateEjectPanel(transactions.get(transactions.size() - 1).getAmount());
            cardLayout.show(mainPanel, "Eject");
        });

        panel.add(title, BorderLayout.NORTH);
        panel.add(new JScrollPane(receipt), BorderLayout.CENTER);
        panel.add(ejectButton, BorderLayout.SOUTH);
        return panel;
    }

    private void updateReceiptPanel(Transaction transaction) {
        JTextArea receipt = (JTextArea) ((JScrollPane) receiptPanel.getComponent(1)).getViewport().getView();
        receipt.setText(
                "Guichet Automatique - xAI Bank\n" +
                        "Date: " + transaction.getTimestamp() + "\n" +
                        "Numéro de carte: ****-****-****-" + currentUser.getCardNumber().substring(12) + "\n" +
                        "--------------------------\n" +
                        "Type: Retrait\n" +
                        "Montant retiré: " + transaction.getAmount() + " €\n" +
                        "Frais (5%): " + transaction.getFee() + " €\n" +
                        "Total débité: " + transaction.getTotal() + " €\n" +
                        "Solde restant: " + currentUser.getBalance() + " €\n" +
                        "--------------------------\n" +
                        "Merci d'avoir utilisé nos services !"
        );
    }

    private JPanel createEjectPanel() {
        JPanel panel = new JPanel(new GridLayout(5, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Transaction Terminée", SwingConstants.CENTER);
        JLabel cardEjected = new JLabel("Votre carte est éjectée.", SwingConstants.CENTER);
        JLabel moneyLabel = new JLabel("", SwingConstants.CENTER);
        JLabel goodbye = new JLabel("Au revoir.", SwingConstants.CENTER);
        JButton restartButton = new JButton("Retour à l'accueil");

        restartButton.addActionListener(e -> {
            currentUser = null;
            cardLayout.show(mainPanel, "InsertCard");
        });

        panel.add(title);
        panel.add(cardEjected);
        panel.add(moneyLabel);
        panel.add(goodbye);
        panel.add(restartButton);
        return panel;
    }

    private void updateEjectPanel(Double amount) {
        JLabel moneyLabel = (JLabel) ejectPanel.getComponent(2);
        moneyLabel.setText(amount != null ? "Retirez votre argent : " + amount + " €" : "");
    }
}