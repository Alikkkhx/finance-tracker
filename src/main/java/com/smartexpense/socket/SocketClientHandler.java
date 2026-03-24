package com.smartexpense.socket;

import com.smartexpense.model.Expense;
import com.smartexpense.model.Category;
import com.smartexpense.service.ExpenseService;
import com.smartexpense.service.ScoringService;
import org.springframework.stereotype.Component;

import java.io.*;
import java.math.BigDecimal;
import java.net.Socket;
import java.time.LocalDate;

/**
 * Handles individual client connections on the raw TCP socket.
 * Demonstrates: Socket programming — reading/writing from Socket streams, Thread per client.
 */
@Component
public class SocketClientHandler implements Runnable {

    private Socket clientSocket;
    private ExpenseService expenseService;
    private ScoringService scoringService;

    // No-arg constructor for Spring
    public SocketClientHandler() {}

    public SocketClientHandler(Socket clientSocket, ExpenseService expenseService,
                               ScoringService scoringService) {
        this.clientSocket = clientSocket;
        this.expenseService = expenseService;
        this.scoringService = scoringService;
    }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            out.println("=== Smart Expense Analyzer Socket Server ===");
            out.println("Commands: ADD_EXPENSE|userId|amount|category|description");
            out.println("          GET_SUMMARY|userId");
            out.println("          GET_SCORE|userId");
            out.println("          QUIT");
            out.println("Ready>");

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                String response = processCommand(inputLine.trim());
                out.println(response);

                if ("BYE".equals(response)) break;
                out.println("Ready>");
            }
        } catch (IOException e) {
            System.err.println("[Socket] Client disconnected: " + e.getMessage());
        } finally {
            try { clientSocket.close(); } catch (IOException ignored) {}
        }
    }

    private String processCommand(String command) {
        if (command.isEmpty()) return "ERROR: Empty command";

        String[] parts = command.split("\\|");
        String action = parts[0].toUpperCase();

        try {
            switch (action) {
                case "ADD_EXPENSE":
                    return handleAddExpense(parts);
                case "GET_SUMMARY":
                    return handleGetSummary(parts);
                case "GET_SCORE":
                    return handleGetScore(parts);
                case "QUIT":
                    return "BYE";
                default:
                    return "ERROR: Unknown command: " + action;
            }
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    private String handleAddExpense(String[] parts) {
        if (parts.length < 5) return "ERROR: Usage: ADD_EXPENSE|userId|amount|category|description";
        Long userId = Long.parseLong(parts[1]);
        BigDecimal amount = new BigDecimal(parts[2]);
        Category category = Category.valueOf(parts[3].toUpperCase());
        String description = parts[4];
        Expense expense = expenseService.addExpense(userId, amount, category, description, LocalDate.now());
        return "OK: Expense #" + expense.getId() + " added — " + expense.getFormattedAmount() + " for " + category.getDisplayName();
    }

    private String handleGetSummary(String[] parts) {
        if (parts.length < 2) return "ERROR: Usage: GET_SUMMARY|userId";
        Long userId = Long.parseLong(parts[1]);
        LocalDate now = LocalDate.now();
        BigDecimal total = expenseService.getMonthlyTotal(userId, now.getMonthValue(), now.getYear());
        int count = expenseService.getExpensesByMonth(userId, now.getMonthValue(), now.getYear()).size();
        return String.format("SUMMARY: Month=%s | Total=$%.2f | Transactions=%d",
                now.getMonth().name(), total.doubleValue(), count);
    }

    private String handleGetScore(String[] parts) {
        if (parts.length < 2) return "ERROR: Usage: GET_SCORE|userId";
        Long userId = Long.parseLong(parts[1]);
        int score = scoringService.calculateScore(userId);
        String label = scoringService.getScoreLabel(score);
        return String.format("SCORE: %d/10 (%s)", score, label);
    }
}
