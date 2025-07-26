package com.example.productbuilder.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
   @GetMapping("/product-builder")
    public String productBuilderPage(Model model) {
        model.addAttribute("title", "Product Builder");
        model.addAttribute("description", "This page allows you to upload BOM, Inventory, and Pipeline data and generate reports.");
        return "product-builder";
    }

    @GetMapping("/inventory-report")
    public String inventoryReportPage(Model model) {
        model.addAttribute("title", "Inventory Report");
        model.addAttribute("description", "This page displays your inventory status and shortage items.");
        return "inventory-report";
    }
}
