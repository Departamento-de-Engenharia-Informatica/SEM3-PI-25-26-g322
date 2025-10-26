package isep.ipp.pt.g322.service;

import isep.ipp.pt.g322.model.Return;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class ReturnLoaderService {

    public List<Return> loadReturns(String filePath) {
        List<Return> returns = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine(); // Skip header line
            int lineNumber = 1;

            while ((line = br.readLine()) != null) {
                lineNumber++;
                try {
                    Return ret = parseReturnLine(line);
                    returns.add(ret);
                } catch (Exception e) {
                    errors.add("Line " + lineNumber + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("ERROR: Failed to read returns file: " + e.getMessage());
        }

        // error reporter
        if (!errors.isEmpty()) {
            System.err.println("Errors loading returns:");
            errors.forEach(System.err::println);
        }

        return returns;
    }

    private Return parseReturnLine(String line) throws IllegalArgumentException {
        String[] data = line.split(",");

        if (data.length < 4) {
            throw new IllegalArgumentException("Invalid format - missing fields");
        }

        String returnId = data[0].trim();
        String sku = data[1].trim();

        int qty;
        try {
            qty = Integer.parseInt(data[2].trim());
            if (qty <= 0) {
                throw new IllegalArgumentException("Invalid quantity: " + qty);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid quantity format: " + data[2]);
        }

        String reason = data[3].trim();

        LocalDateTime timestamp;
        try {
            timestamp = LocalDateTime.parse(data[4].trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid timestamp: " + data[4]);
        }

        LocalDate expiryDate = null;
        if (data.length > 5 && !data[5].trim().isEmpty()) {
            try {
                expiryDate = LocalDate.parse(data[5].trim());
            } catch (DateTimeParseException e) {
                // leaving this as null in case it's invalid. possibly to refactor later on
            }
        }

        return new Return(returnId, sku, qty, reason, timestamp, expiryDate);
    }
}