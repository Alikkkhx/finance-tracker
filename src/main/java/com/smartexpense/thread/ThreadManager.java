package com.smartexpense.thread;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Thread Manager — manages background scheduled tasks.
 * Demonstrates: Threads — ScheduledExecutorService, thread pool management.
 */
@Component
public class ThreadManager {

    @Value("${app.thread.pool-size:3}")
    private int poolSize;

    @Value("${app.thread.analysis-interval:60}")
    private int analysisInterval;

    @Value("${app.thread.budget-monitor-interval:30}")
    private int budgetMonitorInterval;

    private ScheduledExecutorService executorService;

    private final DailyAnalysisTask dailyAnalysisTask;
    private final BudgetMonitorTask budgetMonitorTask;

    public ThreadManager(DailyAnalysisTask dailyAnalysisTask,
                        BudgetMonitorTask budgetMonitorTask) {
        this.dailyAnalysisTask = dailyAnalysisTask;
        this.budgetMonitorTask = budgetMonitorTask;
    }

    /**
     * Start all background threads on application startup.
     */
    @PostConstruct
    public void startThreads() {
        executorService = Executors.newScheduledThreadPool(poolSize);

        // Schedule daily analysis task
        executorService.scheduleAtFixedRate(
                dailyAnalysisTask,
                30, // initial delay (seconds)
                analysisInterval,
                TimeUnit.SECONDS
        );

        // Schedule budget monitor task
        executorService.scheduleAtFixedRate(
                budgetMonitorTask,
                15, // initial delay
                budgetMonitorInterval,
                TimeUnit.SECONDS
        );

        System.out.println("=== Thread Manager Started ===");
        System.out.println("  Pool size: " + poolSize);
        System.out.println("  Analysis interval: " + analysisInterval + "s");
        System.out.println("  Budget monitor interval: " + budgetMonitorInterval + "s");
    }

    /**
     * Gracefully shutdown threads on application stop.
     */
    @PreDestroy
    public void stopThreads() {
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
            System.out.println("=== Thread Manager Stopped ===");
        }
    }
}
