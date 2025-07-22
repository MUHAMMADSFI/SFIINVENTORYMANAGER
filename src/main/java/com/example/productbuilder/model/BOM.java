package com.example.productbuilder.model;

import java.util.*;

public class BOM {
    String productId;
    Map<String, Double> requiredItems;
    double minQuantity;

    public BOM(String productId, Map<String, Double> requiredItems, double minQuantity) {
        this.productId = productId;
        this.requiredItems = requiredItems;
        this.minQuantity = minQuantity;
    }

    public void setMinQuantity(double minQuantity) {
        this.minQuantity = minQuantity;
    }
    public double getMinQuantity(){
        return minQuantity;
    }

    public void setrequiredItems(Map<String ,Double> requiredItems){
        this.requiredItems = requiredItems;
    }
    public Map<String,Double> getrequiredItems(){
        return requiredItems;
    }
    public void setProductId(String productId){
        this.productId = productId;
    }
    public String getProductId(){
        return productId;
    }
    @Override
    public String toString() {
        return "BOM{" +
                "productId='" + productId + '\'' +
                ", requiredItems=" + requiredItems +
                ", minQuantity=" + minQuantity +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        BOM bom = (BOM) o;
        return productId.equals(bom.productId); // or use Objects.equals()
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId);
    }

}
