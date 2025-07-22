package com.example.productbuilder.controller;

import com.example.productbuilder.FileProcessingResponse;
import com.example.productbuilder.model.BOM;
import com.example.productbuilder.services.FileProcessingService;
import com.example.productbuilder.util.HelperMethods;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Allow frontend access from any origin
public class FileUploadController {

    private final FileProcessingService fileProcessingService;

    @Autowired
    public FileUploadController(FileProcessingService fileProcessingService) {
        this.fileProcessingService = fileProcessingService;
    }

    @PostMapping("/upload")
    public ResponseEntity<FileProcessingResponse> uploadFiles(
            @RequestParam("bomFile") MultipartFile bomFile,
            @RequestParam("descriptionFile") MultipartFile descriptionFile,
            @RequestParam("inventoryFile") MultipartFile inventoryFile,
            @RequestParam("requirementsFile") MultipartFile requirementsFile,
            @RequestParam("pipelineFile") MultipartFile pipelineFile,
            @RequestParam("purchaseFile") MultipartFile purchaseFile) {

        try {
            List<BOM> bom = fileProcessingService.processBomFile(bomFile);
            Map<String, String> description = fileProcessingService.processDescriptionFile(descriptionFile);
            Map<String, Double> inventory = HelperMethods.inventoryPlusPurchase(fileProcessingService.processCsvfile(inventoryFile),fileProcessingService.processCsvfile(purchaseFile));
            Map<String, Double> requirements = fileProcessingService.processCsvfile(requirementsFile);
            Map<String, Object[]> pipeline = fileProcessingService.processPipelineFile(pipelineFile);

            FileProcessingResponse response = new FileProcessingResponse();
            response.setBom(bom);
            response.setDescriptionMap(description);
            response.setInventoryMap(inventory);
            response.setRequirementsMap(requirements);
            response.setPipelineMap(pipeline);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}
