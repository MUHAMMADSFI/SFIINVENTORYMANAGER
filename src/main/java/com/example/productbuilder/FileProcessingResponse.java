// File: FileProcessingResponse.java
package com.example.productbuilder;

import com.example.productbuilder.model.BOM;
import java.util.List;
import java.util.Map;

public class FileProcessingResponse {
    private List<BOM> bom;
    private Map<String, String> descriptionMap;
    private Map<String, Double> inventoryMap;
    private Map<String, Double> requirementsMap;
    private Map<String, Object[]> pipelineMap;

    // Getters and Setters

    public List<BOM> getBom() {
        return bom;
    }

    public void setBom(List<BOM> bom) {
        this.bom = bom;
    }

    public Map<String, String> getDescriptionMap() {
        return descriptionMap;
    }

    public void setDescriptionMap(Map<String, String> descriptionMap) {
        this.descriptionMap = descriptionMap;
    }

    public Map<String, Double> getInventoryMap() {
        return inventoryMap;
    }

    public void setInventoryMap(Map<String, Double> inventoryMap) {
        this.inventoryMap = inventoryMap;
    }

    public Map<String, Double> getRequirementsMap() {
        return requirementsMap;
    }

    public void setRequirementsMap(Map<String, Double> requirementsMap) {
        this.requirementsMap = requirementsMap;
    }

    public Map<String, Object[]> getPipelineMap() {
        return pipelineMap;
    }

    public void setPipelineMap(Map<String, Object[]> pipelineMap) {
        this.pipelineMap = pipelineMap;
    }
}
