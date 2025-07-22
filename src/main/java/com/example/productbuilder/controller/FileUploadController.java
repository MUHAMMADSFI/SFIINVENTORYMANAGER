package com.example.productbuilder.controller;

import com.example.productbuilder.FileProcessingResponse;
import com.example.productbuilder.model.BOM;
import com.example.productbuilder.services.FileProcessingService;

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
            @RequestParam("pipelineFile") MultipartFile pipelineFile) {

        try {
            List<BOM> bom = fileProcessingService.processBomFile(bomFile);
            Map<String, String> descriptionMap = fileProcessingService.processDescriptionFile(descriptionFile);
            Map<String, Double> inventoryMap = fileProcessingService.processCsvfile(inventoryFile);
            Map<String, Double> requirementsMap = fileProcessingService.processCsvfile(requirementsFile);
            Map<String, Object[]> pipelineMap = fileProcessingService.processPipelineFile(pipelineFile);

            FileProcessingResponse response = new FileProcessingResponse();
            response.setBom(bom);
            response.setDescriptionMap(descriptionMap);
            response.setInventoryMap(inventoryMap);
            response.setRequirementsMap(requirementsMap);
            response.setPipelineMap(pipelineMap);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}
