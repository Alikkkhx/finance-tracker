package com.smartexpense.controller;

import com.smartexpense.model.Expense;
import com.smartexpense.service.ExpenseService;
import com.smartexpense.service.FileService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequestMapping("/files")
public class FileController {

    private final FileService fileService;
    private final ExpenseService expenseService;

    public FileController(FileService fileService, ExpenseService expenseService) {
        this.fileService = fileService;
        this.expenseService = expenseService;
    }

    @GetMapping("/export/csv")
    public void exportCsv(HttpSession session, HttpServletResponse response) throws IOException {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return;

        List<Expense> expenses = expenseService.getExpensesByUser(userId);
        String csv = fileService.exportToCsv(expenses);

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=expenses.csv");
        response.getWriter().write(csv);
    }

    @GetMapping("/export/json")
    public void exportJson(HttpSession session, HttpServletResponse response) throws IOException {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return;

        List<Expense> expenses = expenseService.getExpensesByUser(userId);
        String json = fileService.exportToJson(expenses);

        response.setContentType("application/json");
        response.setHeader("Content-Disposition", "attachment; filename=expenses.json");
        response.getWriter().write(json);
    }

    @PostMapping("/import/csv")
    public String importCsv(@RequestParam("file") MultipartFile file,
                           HttpSession session, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            List<Expense> imported = fileService.importFromCsv(content, userId);

            for (Expense expense : imported) {
                expenseService.addExpense(userId, expense.getAmount(), expense.getCategory(),
                        expense.getDescription(), expense.getDate());
            }

            redirectAttributes.addFlashAttribute("success",
                    "Imported " + imported.size() + " expenses successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Import error: " + e.getMessage());
        }

        return "redirect:/profile";
    }
}
