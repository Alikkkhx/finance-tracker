package com.smartexpense.socket;

import com.smartexpense.service.ExpenseService;
import com.smartexpense.service.ScoringService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Raw TCP Socket Server for expense data sync.
 * Demonstrates: Socket Programming — ServerSocket, accept(), multi-threaded client handling.
 * Listens on configurable port (default 5555).
 */
@Component
public class ExpenseSocketServer {

    @Value("${app.socket.port:5555}")
    private int port;

    private ServerSocket serverSocket;
    private ExecutorService clientPool;
    private Thread serverThread;
    private volatile boolean running = false;

    private final ExpenseService expenseService;
    private final ScoringService scoringService;

    public ExpenseSocketServer(ExpenseService expenseService, ScoringService scoringService) {
        this.expenseService = expenseService;
        this.scoringService = scoringService;
    }

    /**
     * Start the socket server in a separate thread on application startup.
     */
    @PostConstruct
    public void start() {
        clientPool = Executors.newFixedThreadPool(5);
        running = true;

        serverThread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                System.out.println("=== Expense Socket Server started on port " + port + " ===");

                while (running) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        System.out.println("[Socket] New client connected: " +
                                clientSocket.getInetAddress().getHostAddress());

                        // Handle each client in a separate thread
                        SocketClientHandler handler = new SocketClientHandler(
                                clientSocket, expenseService, scoringService
                        );
                        clientPool.submit(handler);
                    } catch (IOException e) {
                        if (running) {
                            System.err.println("[Socket] Accept error: " + e.getMessage());
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("[Socket] Server error: " + e.getMessage());
            }
        }, "ExpenseSocketServer");

        serverThread.setDaemon(true);
        serverThread.start();
    }

    /**
     * Stop the socket server gracefully.
     */
    @PreDestroy
    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException ignored) {}
        if (clientPool != null) {
            clientPool.shutdownNow();
        }
        System.out.println("=== Expense Socket Server stopped ===");
    }
}
