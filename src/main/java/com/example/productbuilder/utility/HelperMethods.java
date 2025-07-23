package com.example.productbuilder.utility;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.example.productbuilder.model.BOM;

public class HelperMethods {
    public static LocalDate dumydate = LocalDate.parse("0000-01-01");

    public static Map<String, Double> getRequirementInventory(List<BOM> boms) {
        Map<String, Double> inventoryRequired = new HashMap<>();
        for (BOM bom : boms) {
            // IF BOM PRODUCTION REQUIREMENT QUANTITY IS GREATER THAN ZERO THEN CREATE ITEM
            // REQUIREMENT LIST
            if (bom.getMinQuantity() > 0.0) {
                for (Map.Entry<String, Double> entry : bom.getrequiredItems().entrySet()) {
                    String key = entry.getKey();
                    Double value = entry.getValue() * bom.getMinQuantity();
                    inventoryRequired.put(key, inventoryRequired.getOrDefault(key, 0.0) + value);
                }
            }
        }
        return inventoryRequired;
    }

    // WRITE MAPS TO CSV
    public static void writeMapToCSV(Map<String, Double> map, Map<String, String> desc, String fileName,
            String columnName1, String columnName2, String folderName) {
        File folder = new File(folderName);
        if (!folder.exists()) {
            folder.mkdir();
        }

        // FULL FILE PATH
        File file = new File(folder, fileName);

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(columnName1 + "," + "DESCRIPTION" + "," + columnName2 + "\n"); // CSV header
            for (Map.Entry<String, Double> entry : map.entrySet()) {
                writer.write(entry.getKey() + "," + desc.getOrDefault(entry.getKey(), "DESCRIPTION N.A") + ","
                        + entry.getValue() + "\n");
            }
            System.out.println("CSV written: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void Shortage_Item_Wise(Map<String, Double> map, Map<String, Object[]> pipeline,
            Map<String, String> desc, String fileName,
            String columnName1, String columnName2, String folderName) {
        File folder = new File(folderName);
        if (!folder.exists()) {
            folder.mkdir();
        }

        // FULL FILE PATH
        File file = new File(folder, fileName);

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(columnName1 + "," + "DESCRIPTION" + "," + columnName2 + "," + "PIPELINE" + "," + "ETA" + "\n"); // CSV
                                                                                                                         // header
            for (Map.Entry<String, Double> entry : map.entrySet()) {
                double pipelineValue = 0.0;
                LocalDate pipelineDate = LocalDate.parse("0000-01-01");
                String key = entry.getKey();
                Object[] pipelineobj = pipeline.getOrDefault(key, null);
                if (pipelineobj != null) {
                    pipelineValue = (double) pipelineobj[0];
                    pipelineDate = (LocalDate) pipelineobj[1];
                }

                writer.write(entry.getKey() + "," + desc.getOrDefault(key, "DESCRIPTION N.A") + ","
                        + entry.getValue() + ","
                        + ((pipelineValue <= 0.0) ? "NOT IN PIPELINE" : pipelineValue) + ","
                        + ((pipelineDate.equals(dumydate)) ? "NOT IN PIPELINE" : pipelineDate) + "\n");
            }
            System.out.println("CSV written: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static BOM getBomFromProductId(String productId, List<BOM> boms) {
        for (BOM bom : boms) {
            if (bom.getProductId().equals(productId)) {

                return bom;
            }
        }

        return null;

    }

    public static Map<String, Double> getRemainingInventoryOfRequiredItemsOnlyIncludingPipeling(
            Map<String, Double> inventory, Map<String, Object[]> pipeline, Map<String, Double> required,
            Map<String, String> desc) {
        Map<String, Double> remaining = new HashMap<>();
        for (Map.Entry<String, Double> entry : inventory.entrySet()) {
            String key = entry.getKey();
            double actual = entry.getValue();
            double pipelineValue = 0.0;
            Object[] obj = pipeline.getOrDefault(key, null);
            if (obj != null) {
                pipelineValue = (double) obj[0];
            }
            double req = required.getOrDefault(key, 0.0);
            double remainingInventory = ((actual + pipelineValue) - req);
            remaining.put(key, remainingInventory);
        }

        return remaining;
    }

    public static Map<String, Double> getInventoryOfRequiredItemsOnly(List<BOM> boms/*
                                                                                     * HERE TO INSERT THE BOMS OF ONLY
                                                                                     * REQUIRED ITEMS NOT COMPLETE BOMS
                                                                                     */,
            Map<String, Double> inventory/* COMPLETE INVENTORY TO BE INSERTED */) {
        Map<String, Double> finalInventory = new HashMap<>();

        for (BOM bom : boms) {
            for (Map.Entry<String, Double> reqItem : bom.getrequiredItems().entrySet()) {
                finalInventory.put(reqItem.getKey(), inventory.getOrDefault(reqItem.getKey(), 0.0));
            }
        }
        return finalInventory;
    }

    public static List<BOM> getBOMsOfRequiredItemsOnly(List<BOM> boms, Map<String, Double> requirement) {
        List<BOM> result = new ArrayList<>();

        for (BOM bom : boms) {

            boolean isBomInRequirementList = requirement.keySet().stream()
                    .anyMatch(key -> key.contains(bom.getProductId()));
            // CHECK IF THE BOM IS FROM THE LIST OF REQUIRED ITEMS
            if (isBomInRequirementList) {
                result.add(bom);
            }
        } // ASSIGNING REQUIREMENT ENDS
        return result;
    }

    public static <T> void printMap(Map<String, T> map) {
        for (Map.Entry<String, T> entry : map.entrySet()) {
            System.out.println(entry.getKey() + " | " + entry.getValue());
        }
    }

    /**
     * @param map
     * @param boms
     * @param fileName
     */
    public static void createBuiltProductsCsv(Map<String, Double> map/* UNBUILT ITEMS MAP */, List<BOM> boms,
            String fileName, Map<String, String> desc, String folderName) {
        File folder = new File(folderName);
        if (!folder.exists()) {
            folder.mkdir();
        }

        // FULL FILE PATH
        File file = new File(folder, fileName);

        try (FileWriter writer = new FileWriter(file)) {
            writer.write("FG ITEM CODE" + "," + "DESCRIPTION" + "," + "BUILT QTY" + "," + "UNBUILT QTY" + "\n"); // CSV
                                                                                                                 // header
            for (Map.Entry<String, Double> entry : map.entrySet()) {
                BOM bom = HelperMethods.getBomFromProductId(entry.getKey(), boms);
                double difference = bom.getMinQuantity() - entry.getValue();
                writer.write(entry.getKey() + ","
                        + desc.getOrDefault(entry.getKey(), "description not available") + ","
                        + entry.getValue() + ","
                        + difference + "\n");
            }
            System.out.println("CSV written: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createCsvOfUnbuiltItems(Set<BOM> unbuilt, Map<String, Double> inventory,
            Map<String, Double> builtProducts, Map<String, Object[]> pipeline, Map<String, String> description,
            String folderName) {
        String fileName = "ItemsShortageList.csv";

        File folder = new File(folderName);
        if (!folder.exists()) {
            folder.mkdir();
        }

        // Full file path
        File file = new File(folder, fileName);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("FG CODE" + "," + "DESCRIPTION" + "," + "SHORTAGE ITEM" + "," + "DESCRIPTOIN" + ","
                    + "REQUIRED QUANTITY IN BOM FOR 1 CTN" + "," + "ABAILABLE INVENTORY"
                    + "," + "UNABLE TO BUILT IN CARTONS" + ","
                    + "MATERIAL REQUIRED TO FULFILL THE ORDER REQUIREMENT" + ","
                    + "MATERIAL SHORTAGE" + ","
                    + "PIPELINE" + ","
                    + "ETA"
                    + "\n"); // CSV header
            for (BOM bom : unbuilt) {
                for (Map.Entry<String, Double> map : bom.getrequiredItems().entrySet()) {
                    String key = map.getKey();
                    double pipelineValue = 0.0;
                    LocalDate pipelineDate = LocalDate.parse("0000-01-01");

                    Object[] pipelineobj = pipeline.getOrDefault(key, null);
                    if (pipelineobj != null) {
                        pipelineValue = (double) pipelineobj[0];
                        pipelineDate = (LocalDate) pipelineobj[1];
                    }
                    // THIS IF METHOD CHECKS WHETHER THE REQUIRED ITEMS VALUE MULTIPLY BY BOM
                    // UNBUILT QUANTITY IS GREATER THAN INVENTORY (MEANS WE HAVE SHORTEAGE INVENTORY
                    // IS LESS THAN REQUIREMENT)
                    if (map.getValue()
                            * (bom.getMinQuantity() - builtProducts.getOrDefault(bom.getProductId(), 0.0)) > inventory
                                    .get(key)) {
                        writer.write(bom.getProductId() + ","
                                + description.getOrDefault(bom.getProductId(), "description not available").replaceAll(
                                        ",",
                                        "")
                                + ","
                                + key + ","
                                + description.getOrDefault(key, "DESCRIPTION N.A").replaceAll(",",
                                        "")
                                + ","
                                + map.getValue() + ","
                                + inventory.getOrDefault(map.getKey(), 999999999999999.99999999999) + ","
                                + (bom.getMinQuantity() - builtProducts.getOrDefault(bom.getProductId(), 0.0)) + ","
                                + (map.getValue()
                                        * (bom.getMinQuantity() - builtProducts.getOrDefault(bom.getProductId(), 0.0)))
                                + ","
                                + ((map.getValue()
                                        * (bom.getMinQuantity() - builtProducts.getOrDefault(bom.getProductId(), 0.0)))
                                        - inventory.getOrDefault(map.getKey(), 0.0))
                                + ","
                                + ((pipelineValue <= 0.0) ? "NOT IN PIPELINE" : pipelineValue) + ","
                                + ((pipelineDate.equals(dumydate)) ? "NOT IN PIPELINE" : pipelineDate) +
                                "\n");
                        inventory.put(key, 0.0);
                    }

                }

            }
            System.out.println("CSV written: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // for(BOM bom: unbuilt){
        // String key = bom.requiredItems.entrySet().iterator().next().getKey();
        // double value = bom.requiredItems.entrySet().iterator().next().getValue();
        // System.out.println("ProductID: "+bom.productId+" | ShortageItem: "+key+" |
        // required quantity: "+value+" | available inventory: "
        // + inventory.getOrDefault(key, 0.0));
        // }

    }

    public static Map<String, Map<String, Double>> cartonsCanBeMadeComputeForIndividualItemFromBOM(List<BOM> boms,
            Map<String, Double> inventory) {
        Map<String, Map<String, Double>> listOfEachItemsCanBeMade = new HashMap<>();
        for (BOM bom : boms) {
            Map<String, Double> temp = new HashMap<>();
            for (Map.Entry<String, Double> entry : bom.getrequiredItems().entrySet()) {
                String key = entry.getKey();
                Double value = entry.getValue();
                temp.put(key, (value / inventory.getOrDefault(key, 0.0)));
            }
            listOfEachItemsCanBeMade.put(bom.getProductId(), temp);
        }
        return listOfEachItemsCanBeMade;
    }

    public static Map<String, Double> inventoryPlusPurchase(Map<String, Double> inventory,
            Map<String, Double> purchase) {

        for (Map.Entry<String, Double> pur : purchase.entrySet()) {
            String key = pur.getKey();
            double value = pur.getValue();
            double inventoryValue = inventory.getOrDefault(key, 0.0);
            inventory.put(key, (value + inventoryValue));
        }

        return inventory;
    }

    public static void createCsvOfMaterialCanBeMadeFromRemainingInventory(List<BOM> boms,
            Map<String, Double> requirement, Map<String, Double> remainingInventory, Map<String, String> desc,
            String folderName, String fileName) {

        File folder = new File(folderName);
        if (!folder.exists()) {
            folder.mkdir();
        }
        // FULL FILE PATH
        File file = new File(folder, fileName);

        try (FileWriter writer = new FileWriter(file)) {
            writer.write("FG ITEM CODE" + "," + "DESCRIPTION" + "," + "ITEM CODE" + "," + "DESCRIPTION" + ","
                    + "BOM REQUIREMENT" + "," + "AVAILABLE MATERIAL" + "," + "CAN BE MADE IN CARTONS" + "\n"); // CSV
            // header
            for (Map.Entry<String, Double> entry : requirement.entrySet()) {
                String fg_code = entry.getKey();
                BOM bom = getBomFromProductId(fg_code, boms);
                for (Map.Entry<String, Double> inner : bom.getrequiredItems().entrySet()) {
                    String item_code = inner.getKey();
                    Double item_value_required = inner.getValue();
                    Double rem = remainingInventory.getOrDefault(item_code, 0.0);
                    if (item_code.contains("SF-MIC"))
                        continue;
                    writer.write(fg_code + "," + desc.getOrDefault(fg_code, "N.A") + ","
                            + item_code + "," + desc.getOrDefault(item_code, "N.A") + ","
                            + item_value_required + "," + rem + ","
                            + (rem / item_value_required) + "\n");
                }
            }
            System.out.println("CSV written: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // delete the output folder before createing new files
    public static void cleanOutputFolder() throws IOException {
        Path outputPath = Paths.get("output");

        if (Files.exists(outputPath)) {
            // Delete all files and subdirectories recursively
            Files.walk(outputPath)
                    .sorted(Comparator.reverseOrder()) // delete files before folders
                    .map(Path::toFile)
                    .forEach(File::delete);
        }

        // Recreate the folder after deleting
        Files.createDirectories(outputPath);
    }

    public static List<String> getOutputFileNames() {
        File folder = new File("output"); // Relative path to output folder
        List<String> fileNames = new ArrayList<>();

        if (folder.exists() && folder.isDirectory()) {
            for (File file : folder.listFiles()) {
                if (file.isFile()) {
                    fileNames.add(file.getName());
                }
            }
        }
        return fileNames;
    }
}
