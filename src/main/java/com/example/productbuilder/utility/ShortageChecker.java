package com.example.productbuilder.utility;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.productbuilder.model.BOM;

public class ShortageChecker {

    public static List<String> shortageListCreator(Map<String, Double> inventory, Map<String, Double> requirement,
            List<BOM> bomList, Map<String, Object[]> pipeline, Map<String, String> description, String outPutFolder) {
        // SETTING REQUIRED QUANTITY TO BOMS
        for (BOM bom : bomList) {

            bom.setMinQuantity(requirement.getOrDefault(bom.getProductId(), 0.0));

        } // ASSIGNING REQUIREMENT ENDS

        Map<String, Double> requiredInventory = HelperMethods.getRequirementInventory(bomList);

        Map<String, Double> shortage = getShortage(requiredInventory, inventory);

        HelperMethods.Shortage_Item_Wise(shortage, pipeline, description, "Shortage Item Wise.Csv", "ITEM NO.",
                "SHORTAGE QUANTITY",
                outPutFolder);

                return HelperMethods.getOutputFileNames();
    }

    public static Map<String, Double> getShortage(Map<String, Double> requiredInventory,
            Map<String, Double> avaialbeInventory) {
        Map<String, Double> shortage = new HashMap<>();

        for (Map.Entry<String, Double> req : requiredInventory.entrySet()) {
            String key = req.getKey();
            double reqQty = req.getValue();

            double availableQty = avaialbeInventory.getOrDefault(key, 0.0);

            if (reqQty > availableQty) {

                shortage.put(key, (reqQty - availableQty));
            }
        }

        return shortage;
    }
}
