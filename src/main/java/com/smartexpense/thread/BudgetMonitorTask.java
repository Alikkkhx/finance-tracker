package com.smartexpense.thread;

import com.smartexpense.model.Budget;
import com.smartexpense.model.User;
import com.smartexpense.repository.UserRepository;
import com.smartexpense.service.BudgetService;
import com.smartexpense.service.NotificationService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Background task that periodically checks budget limits.
 * Demonstrates: Threads — Runnable implementation for scheduled execution.
 */
@Component
public class BudgetMonitorTask implements Runnable {

    private final BudgetService budgetService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public BudgetMonitorTask(BudgetService budgetService,
                            NotificationService notificationService,
                            UserRepository userRepository) {
        this.budgetService = budgetService;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    @Override
    public void run() {
        try {
            System.out.println("[Thread: BudgetMonitor] Running budget check...");
            LocalDate now = LocalDate.now();
            int month = now.getMonthValue();
            int year = now.getYear();

            List<User> users = userRepository.findAll();

            for (User user : users) {
                List<Budget> budgets = budgetService.getBudgetsWithSpending(
                        user.getId(), month, year
                );

                for (Budget budget : budgets) {
                    if (budget.shouldNotify()) {
                        notificationService.createAndBroadcast(
                                user.getId(),
                                budget.getNotificationMessage(),
                                budget.getNotificationType()
                        );
                        System.out.println("[Thread: BudgetMonitor] Alert for user " +
                                user.getUsername() + ": " + budget.getNotificationMessage());
                    }
                }
            }

            System.out.println("[Thread: BudgetMonitor] Check completed.");
        } catch (Exception e) {
            System.err.println("[Thread: BudgetMonitor] Error: " + e.getMessage());
        }
    }
}
