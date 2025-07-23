package com.example.productbuilder.utility;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.example.productbuilder.model.BOM;

public class CustomerBalanceMaterial {
    public static List<String> startCustomerBalanceMaterial(List<BOM> bomList,
            Map<String, Double> inventory,
            Map<String, Double> requirement, Map<String, Object[]> pipeline, Map<String, String> description,
            String outPutFolderName) {

        Map<String, Double> remainingInventory = createCustomerBalanceMaterialAndGetRemainingInventory(bomList,
                inventory, requirement, pipeline, description,
                outPutFolderName);

        HelperMethods.writeMapToCSV(remainingInventory, description, "RemainingInventory.csv",
                "ITEM NO.", "QUANTITY", outPutFolderName);
        HelperMethods.createCsvOfMaterialCanBeMadeFromRemainingInventory(bomList, requirement, remainingInventory,
                description, outPutFolderName, "Can Be Made.csv");
                return HelperMethods.getOutputFileNames();
    }

    public static Map<String, Double> createCustomerBalanceMaterialAndGetRemainingInventory(List<BOM> bomList,
            Map<String, Double> inventory,
            Map<String, Double> requirement, Map<String, Object[]> pipeline, Map<String, String> description,
            String outPutFolderName) {
        for (BOM bom : bomList) {

            bom.setMinQuantity(requirement.getOrDefault(bom.getProductId(), 0.0));

        } // ASSIGNING REQUIREMENT ENDS

        // GETTING THE LIST OF BOMS WHICH ARE ONLY OF THE REQUIRED ITEMS
        List<BOM> listOfBomsOfRequirement = HelperMethods.getBOMsOfRequiredItemsOnly(bomList, requirement);
        Map<String, Double> inventoryOfRequiredItemsOnly = HelperMethods.getInventoryOfRequiredItemsOnly(
                listOfBomsOfRequirement,
                inventory);
        Map<String, Double> requiredInventory = HelperMethods.getRequirementInventory(listOfBomsOfRequirement);
        createResultCsv(inventoryOfRequiredItemsOnly, pipeline, requiredInventory, description, outPutFolderName);

        Map<String, Double> remainingInventory = HelperMethods
                .getRemainingInventoryOfRequiredItemsOnlyIncludingPipeling(
                        inventoryOfRequiredItemsOnly, pipeline, requiredInventory, description);

        return remainingInventory;
    }

    public static void createResultCsv(Map<String, Double> inventory, Map<String, Object[]> pipeline,
            Map<String, Double> requiredInventory, Map<String, String> desc, String folderName) {

        String fileName = "CustomerBalanceMaterial.csv";

        File folder = new File(folderName);
        if (!folder.exists()) {
            folder.mkdir();
        }

        // Full file path
        File file = new File(folder, fileName);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("MATERIAL CODE" + "," + "DESCRIPTION" + "," + "INVENTORY" + "," + "PIPELINE" + ","
                    + "INVENTORY + PIPELINE" + "," + "CONSUMPTION THIS MONTH" + "," + "BALANCE MATERIAL" + "\n"); // CSV
                                                                                                                  // header
            for (Map.Entry<String, Double> entry : inventory.entrySet()) {

                double pipelineValue = 0.0;
                LocalDate pipelineDate = LocalDate.parse("0000-01-01");

                Object[] pipelineobj = pipeline.getOrDefault(entry.getKey(), null);
                if (pipelineobj != null) {
                    pipelineValue = (double) pipelineobj[0];
                    pipelineDate = (LocalDate) pipelineobj[1];
                }
                if (!(entry.getKey().contains("SF-MIC") || entry.getKey().contains("SF-OTH"))) {
                }

                writer.write(entry.getKey() + ","
                        + desc.getOrDefault(entry.getKey(), "DECRIPTION N.A") + ","
                        + entry.getValue() + ","
                        + pipelineValue + ","
                        + (entry.getValue() + pipelineValue) + ","
                        + requiredInventory.getOrDefault(entry.getKey(), -0.0) + ","
                        + (entry.getValue() + pipelineValue
                                - requiredInventory.getOrDefault(entry.getKey(), 0.0))
                        + "\n");
            }
            System.out.println("CSV written: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
