package com.example.productbuilder.services;

import com.example.productbuilder.model.BOM;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

@Service
public class FileProcessingService {

    public List<BOM> processBomFile(MultipartFile bomFile) throws Exception {
        Map<String, Map<String, Double>> productToItemsMap = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(bomFile.getInputStream()))) {
            String line = br.readLine();

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 3)
                    continue;

                String productId = parts[0].trim();
                String materialId = parts[1].trim();
                double quantity = Double.parseDouble(parts[2].trim());

                // Get or create the map of materials for the product
                Map<String, Double> itemMap = productToItemsMap.getOrDefault(productId, new HashMap<>());

                // Sum quantities if material repeats
                itemMap.put(materialId, itemMap.getOrDefault(materialId, 0.0) + quantity);
                productToItemsMap.put(productId, itemMap);
            }
        }

        // Convert to BOM list
        List<BOM> bomList = new ArrayList<>();
        for (Map.Entry<String, Map<String, Double>> entry : productToItemsMap.entrySet()) {
            String productId = entry.getKey();
            Map<String, Double> requiredItems = entry.getValue();
            bomList.add(new BOM(productId, requiredItems, 0.0));
        }

        return bomList;

    }

    public Map<String, Object[]> processPipelineFile(MultipartFile pipeline) throws IOException {
        Map<String, Object[]> pipelineMap = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(pipeline.getInputStream()))) {
            String line;
            // skip header

            while ((line = br.readLine()) != null) {
                line = removeUTF8BOM(line);
                String[] parts = line.split(",");
                if (parts.length != 3)
                    continue;
                DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                        .appendPattern("d/M/yyyy") // Accepts 1- or 2-digit month and day
                        .toFormatter();

                String productId = parts[0].trim();
                double quantity = Double.parseDouble(parts[1].trim());
                LocalDate date = LocalDate.parse(parts[2].trim(), formatter);

                pipelineMap.put(productId, new Object[] { quantity, date });
            }
        }

        return pipelineMap;
    }

    public Map<String, Double> processCsvfile(MultipartFile file) throws IOException {
        String line;
        String delimiter = ",";
        Map<String, Double> inventory = new HashMap<>();
        List<String[]> inventoryList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            while ((line = br.readLine()) != null) {
                boolean firstLine = true;

                if (firstLine) {
                    line = removeUTF8BOM(line);
                    firstLine = false;
                }
                String[] values = line.split(delimiter);
                inventoryList.add(values);
                // System.out.println(values);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String[] row : inventoryList) {
            if (row[0].equals("SF-MIC0033") || row[0].equals("SF-MIC0060"))
                inventory.put(row[0], 9999999.99);

            else
                inventory.put(row[0], Double.parseDouble(row[1]));
            // System.out.println(row[0] + " | " + row[1]);
        }

        return inventory;
    }

    public Map<String, String> processDescriptionFile(MultipartFile description) throws IOException {
        Map<String, String> desc = new HashMap<>();
        String line;
        String delimiter = ",";
        List<String[]> descList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(description.getInputStream()))) {
            while ((line = br.readLine()) != null) {
                boolean firstLine = true;

                if (firstLine) {
                    line = removeUTF8BOM(line);
                    firstLine = false;
                }

                String[] values = line.split(delimiter);
                descList.add(values);
                // System.out.println(values);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (String[] row : descList) {

            desc.put(row[0].replaceAll("\\?", ""), row[1]);
            // System.out.println(row[0].replaceAll("\\?", "") + " | " +row[1]);
        }
        return desc;
    }

    private String removeUTF8BOM(String line) {
        final String UTF8_BOM = "\uFEFF";
        if (line.startsWith(UTF8_BOM)) {
            return line.substring(1);
        }
        return line;
    }
}
