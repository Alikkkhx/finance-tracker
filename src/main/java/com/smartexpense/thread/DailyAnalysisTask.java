package com.smartexpense.thread;

import com.smartexpense.dto.Recommendation;
import com.smartexpense.model.User;
import com.smartexpense.repository.UserRepository;
import com.smartexpense.service.NotificationService;
import com.smartexpense.service.RecommendationService;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Background task that runs periodic analysis and generates recommendations.
 * Demonstrates: Threads — Runnable for automated analysis.
 */
@Component
public class DailyAnalysisTask implements Runnable {

    private final RecommendationService recommendationService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public DailyAnalysisTask(RecommendationService recommendationService,
                            NotificationService notificationService,
                            UserRepository userRepository) {
        this.recommendationService = recommendationService;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    @Override
    public void run() {
        try {
            System.out.println("[Thread: DailyAnalysis] Running analysis...");

            List<User> users = userRepository.findAll();

            for (User user : users) {
                List<Recommendation> recommendations =
                        recommendationService.generateRecommendations(user.getId());

                // Send top-priority recommendation as notification
                if (!recommendations.isEmpty()) {
                    Recommendation top = recommendations.get(0);
                    notificationService.create(
                            user.getId(),
                            top.getTitle() + " — " + top.getMessage(),
                            top.getType()
                    );
                    System.out.println("[Thread: DailyAnalysis] Recommendation for " +
                            user.getUsername() + ": " + top.getTitle());
                }
            }

            System.out.println("[Thread: DailyAnalysis] Analysis completed.");
        } catch (Exception e) {
            System.err.println("[Thread: DailyAnalysis] Error: " + e.getMessage());
        }
    }
}
