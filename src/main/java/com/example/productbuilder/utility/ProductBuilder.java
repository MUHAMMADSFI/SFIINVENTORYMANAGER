package com.example.productbuilder.utility;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.example.productbuilder.model.BOM;

public class ProductBuilder {
    private static Set<BOM> unbuiltItems = new HashSet<>();

    public static List<String> startProductBuilder(Map<String, Double> inventory, Map<String, Double> requirement,
            List<BOM> bomList, Map<String, Object[]> pipeline, Map<String, String> description, String outputFolderName)
            throws IOException {
        // ASSIGNING REQUIREMENT
        for (BOM bom : bomList) {
            bom.setMinQuantity(requirement.getOrDefault(bom.getProductId(), 0.0));
        } // ASSIGNING REQUIREMENT ENDS

        // MAIN CODE FOR PRODUCTION

        Map<String, Double> builtProducts = buildProducts(bomList, inventory);

        HelperMethods.createBuiltProductsCsv(builtProducts, bomList, "BUILTPRODUCTS.csv", description,
                outputFolderName);
        HelperMethods.writeMapToCSV(inventory, description, "FINALINVENTORY.csv", "ITEM NO.", "QUANTITY",
                outputFolderName);
        consumeInventoryForUnbuiltItems(unbuiltItems, builtProducts, inventory);
        HelperMethods.createCsvOfUnbuiltItems(unbuiltItems, inventory, builtProducts, pipeline, description,
                outputFolderName);
        createSummary(requirement, builtProducts, description, outputFolderName);
        return HelperMethods.getOutputFileNames();
    }

    

    /**
     * @param unbuiltItems
     * @param builtProducts
     * @param inventory
     */
    public static void consumeInventoryForUnbuiltItems(Set<BOM> unbuiltItems, Map<String, Double> builtProducts,
            Map<String, Double> inventory) {
        for (BOM bom : unbuiltItems) {
            for (Map.Entry<String, Double> map : bom.getrequiredItems().entrySet()) {
                if (map.getValue()
                        * (bom.getMinQuantity() - builtProducts.getOrDefault(bom.getProductId(), 0.0)) < inventory
                                .get(map.getKey())) {
                    String key = map.getKey();
                    Double value = map.getValue();
                    // consumeUnbuiltItemsInventory(map.getKey(),map.getValue(),inventory);
                    inventory.put(key, inventory.get(key)
                            - value * (bom.getMinQuantity() - builtProducts.getOrDefault(bom.getProductId(), 0.0)));
                    bom.getrequiredItems().put(key, 0.0);
                }
            }
        }
    }

    public static void createSummary(Map<String, Double> requirement, Map<String, Double> build,
            Map<String, String> desc, String folderName) {
        String fileName = "Summary.csv";

        File folder = new File(folderName);
        if (!folder.exists()) {
            folder.mkdir();
        }

        // Full file path
        File file = new File(folder, fileName);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("FG CODE" + "," + "DESCRIPTION" + "," + "REQUIRED QTY (CTN)" + "," + "CAN BE MADE (CTN)" + ","
                    + "SHORT QTY (CTN)" + "\n"); // CSV header
            for (Map.Entry<String, Double> entry : requirement.entrySet()) {

                writer.write(entry.getKey() + ","
                        + desc.getOrDefault(entry.getKey(), "DESCRIPTION N.A") + ","
                        + entry.getValue() + ","
                        + build.getOrDefault(entry.getKey(), 0.0) + ","
                        + (entry.getValue() - build.getOrDefault(entry.getKey(), 0.0)) + "\n");
            }
            System.out.println("CSV written: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, Double> buildProducts(List<BOM> boms, Map<String, Double> inventory) {
        Map<String, Double> builtProducts = new HashMap<>();
        boolean builtSomething;

        do {
            builtSomething = false;
            for (BOM bom : boms) {
                // THIS CONDITION CHECK WHETHER THE BOM HAS TO BE BUILT OR NOT
                if (bom.getMinQuantity() > 0.0)
                    while (canBuild(bom, inventory)
                            && builtProducts.getOrDefault(bom.getProductId(), 0.0) < bom.getMinQuantity()) {
                        consumeInventory(bom, inventory);
                        builtProducts.put(bom.getProductId(), builtProducts.getOrDefault(bom.getProductId(), 0.0) + 1);
                        builtSomething = true;
                    }

            }
        } while (builtSomething);

        return builtProducts;
    }

    private static boolean canBuild(BOM bom, Map<String, Double> inventory) {
        for (Map.Entry<String, Double> entry : bom.getrequiredItems().entrySet()) {
            String itemId = entry.getKey();
            Double requiredQty = entry.getValue();
            if (inventory.getOrDefault(itemId, 0.0) < requiredQty) {
                // ADDING UNBUILT ITEMS TO CHEKC SHORTAGE ITEM LIST
                if (!unbuiltItems.contains(bom)) {
                    unbuiltItems.add(bom);
                }

                return false;
            }
        }
        return true;
    }

    private static void consumeInventory(BOM bom, Map<String, Double> inventory) {
        for (Map.Entry<String, Double> entry : bom.getrequiredItems().entrySet()) {
            String itemId = entry.getKey();
            Double requiredQty = entry.getValue();
            inventory.put(itemId, (double) Math.round((inventory.get(itemId) - requiredQty) * 1000) / 1000);
        }
    }
}
