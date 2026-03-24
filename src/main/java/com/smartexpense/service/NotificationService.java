package com.smartexpense.service;

import com.smartexpense.model.Notification;
import com.smartexpense.repository.NotificationRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for managing and broadcasting notifications.
 * Uses WebSocket (STOMP) for real-time browser notifications.
 */
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(NotificationRepository notificationRepository,
                               SimpMessagingTemplate messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Create and broadcast a notification.
     */
    public Notification createAndBroadcast(Long userId, String message, String type) {
        Notification notification = new Notification(userId, message, type);
        notification = notificationRepository.save(notification);

        // Broadcast via WebSocket to the user's topic
        messagingTemplate.convertAndSend(
            "/topic/notifications/" + userId,
            notification
        );

        return notification;
    }

    /**
     * Create notification without broadcasting.
     */
    public Notification create(Long userId, String message, String type) {
        Notification notification = new Notification(userId, message, type);
        return notificationRepository.save(notification);
    }

    public List<Notification> getUnread(Long userId) {
        return notificationRepository.findUnreadByUserId(userId);
    }

    public List<Notification> getAll(Long userId) {
        return notificationRepository.findByUserId(userId);
    }

    public void markAsRead(Long id, Long userId) {
        notificationRepository.markAsRead(id, userId);
    }

    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }

    public int getUnreadCount(Long userId) {
        return notificationRepository.countUnread(userId);
    }
}
