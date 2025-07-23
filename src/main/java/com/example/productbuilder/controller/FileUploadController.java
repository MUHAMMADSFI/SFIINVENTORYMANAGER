package com.example.productbuilder.controller;

import com.example.productbuilder.FileProcessingResponse;
import com.example.productbuilder.model.BOM;
import com.example.productbuilder.services.FileProcessingService;
import com.example.productbuilder.utility.CustomerBalanceMaterial;
import com.example.productbuilder.utility.HelperMethods;
import com.example.productbuilder.utility.ProductBuilder;

import com.example.productbuilder.utility.ShortageChecker;




import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
    public ResponseEntity<String> uploadFiles(
            @RequestParam("bomFile") MultipartFile bomFile,
            @RequestParam("descriptionFile") MultipartFile descriptionFile,
            @RequestParam("inventoryFile") MultipartFile inventoryFile,
            @RequestParam("requirementsFile") MultipartFile requirementsFile,
            @RequestParam("pipelineFile") MultipartFile pipelineFile,
            @RequestParam("purchaseFile") MultipartFile purchaseFile) {

        try {
            List<BOM> bomList = fileProcessingService.processBomFile(bomFile);
            

            Map<String, String> description = fileProcessingService.processDescriptionFile(descriptionFile);
            Map<String, Double> inventory = HelperMethods.inventoryPlusPurchase(
                    fileProcessingService.processCsvfile(inventoryFile),
                    fileProcessingService.processCsvfile(purchaseFile));
            Map<String, Double> inventoryForShortage = inventory;
            Map<String, Double> inventoryForCustomerBalance = inventory;
            Map<String, Double> requirements = fileProcessingService.processCsvfile(requirementsFile);
            Map<String, Object[]> pipeline = fileProcessingService.processPipelineFile(pipelineFile);


            HelperMethods.cleanOutputFolder();

            List<String> response = ProductBuilder.startProductBuilder(inventory, requirements, bomList, pipeline, description, "output");

            
            
            response = ShortageChecker.shortageListCreator(inventoryForShortage, requirements, bomList, pipeline, description, "output");

            response = CustomerBalanceMaterial.startCustomerBalanceMaterial(bomList, inventoryForCustomerBalance, requirements, pipeline, description, "output");
            //this is the code commented it returns all the files csvs into json format
            // FileProcessingResponse response = new FileProcessingResponse();
            // response.setBom(bomList);
            // response.setDescriptionMap(description);
            // response.setInventoryMap(inventory);
            // response.setRequirementsMap(requirements);
            // response.setPipelineMap(pipeline);
            // return ResponseEntity.ok(response);

            return ResponseEntity.ok("Your results are ready click download to get your results");


        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/download-all")
    public ResponseEntity<Resource> downloadAllCSVs() throws IOException {
        // Define CSV file paths (adjust according to your file creation logic)
        String[] csvFiles = {
                "output/BUILTPRODUCTS.csv",
                "output/FINALINVENTORY.csv",
                "output/ItemsShortageList.csv",
                "output/summary.csv",
                "output/CustomerBalanceMaterial.csv",
                "output/Shortage Item Wise.Csv",
                "output/RemainingInventory.csv",
                "output/Can Be Made.csv"
        };

        // Create a temporary ZIP file
        Path zipPath = Files.createTempFile("productbuilder-", ".zip");

        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
            for (String filePath : csvFiles) {
                Path path = Paths.get(filePath);
                if (Files.exists(path)) {
                    ZipEntry entry = new ZipEntry(path.getFileName().toString());
                    zos.putNextEntry(entry);
                    Files.copy(path, zos);
                    zos.closeEntry();
                }
            }
        }

        // Return the ZIP file as response
        FileSystemResource resource = new FileSystemResource(zipPath.toFile());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=all-csv-files.zip")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(resource.contentLength())
                .body(resource);
    }

}
