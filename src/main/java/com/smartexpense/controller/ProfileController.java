package com.smartexpense.controller;

import com.smartexpense.model.Notification;
import com.smartexpense.model.User;
import com.smartexpense.service.NotificationService;
import com.smartexpense.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;
    private final NotificationService notificationService;

    public ProfileController(UserService userService, NotificationService notificationService) {
        this.userService = userService;
        this.notificationService = notificationService;
    }

    @GetMapping
    public String profilePage(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        Optional<User> user = userService.findById(userId);
        List<Notification> notifications = notificationService.getAll(userId);

        user.ifPresent(u -> model.addAttribute("user", u));
        model.addAttribute("notifications", notifications);
        return "profile";
    }

    @PostMapping("/update")
    public String updateProfile(@RequestParam String email, @RequestParam String fullName,
                               @RequestParam(required = false) BigDecimal monthlyIncome,
                               HttpSession session, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setEmail(email);
            user.setFullName(fullName);
            if (monthlyIncome != null) user.setMonthlyIncome(monthlyIncome);
            userService.updateProfile(user);
            session.setAttribute("fullName", fullName);
            redirectAttributes.addFlashAttribute("success", "Profile updated!");
        }

        return "redirect:/profile";
    }

    @PostMapping("/notifications/read-all")
    public String markAllRead(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        notificationService.markAllAsRead(userId);
        return "redirect:/profile";
    }
}
