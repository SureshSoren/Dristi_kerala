package org.pucar.dristi.web.controller;

import org.pucar.dristi.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Set;


@RestController
public class Controller {

    private static final Logger logger = LoggerFactory.getLogger(Controller.class);

    private final Service service;

    @Autowired
    public Controller(Service service) {
        this.service = service;
    }


    @PostMapping("/ocr")
    public ResponseEntity<Map<String, Object>> callOCR(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "wordCheckList", required = false) List<String> wordCheckList,
            @RequestParam(value = "distanceCutoff", required = false) Integer distanceCutoff,
            @RequestParam(value = "documentType", required = false) String documentType,
            @RequestParam(value = "extractData", required = false) Boolean extractData) {

        logger.info("Received request to process image with file: ");

        Map<String, Object> response = service.callOCR(file, wordCheckList, distanceCutoff, documentType, extractData);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ocr/documentTypes")
    public Set<String> getDocumentTypes() {
        return service.getDocumentTypes();
    }

    @PostMapping("/ocr/verify")
    public ResponseEntity<Map<String, Object>> verifyDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") String documentType) {

        logger.info("Received request to verify image for documentType: {}", documentType);

        Map<String, Object> response = service.verifyDocument(file, documentType);
        return ResponseEntity.ok(response);
    }

}
