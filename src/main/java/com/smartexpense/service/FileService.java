package com.smartexpense.service;

import com.smartexpense.model.Expense;
import com.smartexpense.model.Category;
import com.smartexpense.model.base.Exportable;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * File service for CSV/JSON import/export and object serialization.
 * Demonstrates: File I/O (BufferedReader, BufferedWriter, ObjectStreams).
 */
@Service
public class FileService {

    /**
     * Export expenses to CSV using BufferedWriter.
     * Demonstrates: File writing with BufferedWriter.
     */
    public String exportToCsv(List<Expense> expenses) {
        StringBuilder sb = new StringBuilder();
        sb.append(Exportable.getCsvHeader()).append("\n");

        for (Expense expense : expenses) {
            sb.append(expense.toCsv()).append("\n");
        }

        return sb.toString();
    }

    /**
     * Write CSV to a file using BufferedWriter.
     */
    public void writeCsvToFile(List<Expense> expenses, Path filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile()))) {
            writer.write(Exportable.getCsvHeader());
            writer.newLine();

            for (Expense expense : expenses) {
                writer.write(expense.toCsv());
                writer.newLine();
            }
        }
    }

    /**
     * Export expenses to JSON string.
     */
    public String exportToJson(List<Expense> expenses) {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");

        for (int i = 0; i < expenses.size(); i++) {
            sb.append("  ").append(expenses.get(i).toJson());
            if (i < expenses.size() - 1) sb.append(",");
            sb.append("\n");
        }

        sb.append("]");
        return sb.toString();
    }

    /**
     * Import expenses from CSV string using BufferedReader.
     * Demonstrates: File reading with BufferedReader.
     */
    public List<Expense> importFromCsv(String csvContent, Long userId) {
        List<Expense> expenses = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new StringReader(csvContent))) {
            String line = reader.readLine(); // Skip header

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                try {
                    String[] parts = parseCsvLine(line);
                    if (parts.length >= 5) {
                        Expense expense = new Expense();
                        expense.setUserId(userId);
                        // parts[0] = id (skip for import), parts[1] = type
                        expense.setAmount(new BigDecimal(parts[2].trim()));
                        expense.setCategory(Category.valueOf(parts[3].trim()));
                        expense.setDescription(parts[4].trim().replace("\"", ""));
                        if (parts.length > 5) {
                            expense.setDate(LocalDate.parse(parts[5].trim()));
                        } else {
                            expense.setDate(LocalDate.now());
                        }
                        expenses.add(expense);
                    }
                } catch (Exception e) {
                    // Skip malformed lines
                    System.err.println("Skipping malformed CSV line: " + line);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading CSV", e);
        }

        return expenses;
    }

    /**
     * Serialize object to bytes — demonstrates Object Serialization.
     */
    public byte[] serialize(Serializable object) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(object);
            return bos.toByteArray();
        }
    }

    /**
     * Deserialize object from bytes — demonstrates Object Deserialization.
     */
    @SuppressWarnings("unchecked")
    public <T> T deserialize(byte[] data) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            return (T) ois.readObject();
        }
    }

    /**
     * Save serialized data to file.
     */
    public void saveSerializedToFile(Serializable object, Path filePath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filePath.toFile());
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(object);
        }
    }

    /**
     * Load serialized data from file.
     */
    @SuppressWarnings("unchecked")
    public <T> T loadSerializedFromFile(Path filePath) throws IOException, ClassNotFoundException {
        try (FileInputStream fis = new FileInputStream(filePath.toFile());
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            return (T) ois.readObject();
        }
    }

    private String[] parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder field = new StringBuilder();

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(field.toString());
                field = new StringBuilder();
            } else {
                field.append(c);
            }
        }
        result.add(field.toString());

        return result.toArray(new String[0]);
    }
}
