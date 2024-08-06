package org.pucar.dristi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import jakarta.annotation.PostConstruct;
import net.minidev.json.JSONArray;
import org.egov.tracer.model.CustomException;
import org.pucar.dristi.config.Properties;
import org.pucar.dristi.util.FileStoreUtil;
import org.pucar.dristi.util.MdmsFetcher;
import org.pucar.dristi.util.Util;
import org.pucar.dristi.web.model.DocumentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class Service {

    private static final Logger log = LoggerFactory.getLogger(Service.class);

    private final Util utils;
    private final Properties properties;
    private MdmsFetcher mdmsFetcher;
    private ObjectMapper objectMapper;
    private FileStoreUtil fileStoreUtil;

    private Map<String, List<String>> keyWordsByDocument;

    @Autowired
    public Service(Util utils, Properties properties, MdmsFetcher mdmsFetcher,
                   ObjectMapper objectMapper, FileStoreUtil fileStoreUtil) {
        this.utils = utils;
        this.properties = properties;
        this.mdmsFetcher = mdmsFetcher;
        this.objectMapper = objectMapper;
        this.fileStoreUtil = fileStoreUtil;
    }

    @PostConstruct
    void getMdmsForOCR() throws JsonProcessingException {
        try {
            JSONArray jsonArray = this.mdmsFetcher.getMdmsForOCR((String) null);
            ObjectReader reader = this.objectMapper.readerFor(this.objectMapper.getTypeFactory().constructCollectionType(List.class, DocumentType.class));
            List<DocumentType> documentTypes = reader.readValue(jsonArray.toString());
            log.info("DocumentTypes: {}", documentTypes.toString());
            this.keyWordsByDocument = documentTypes.stream().collect(Collectors.toMap(DocumentType::getType, DocumentType::getKeyWords));
        } catch (IOException e) {
            log.error("Error occurred while reading OCR Document Types from MDMS Config", e);
            throw new CustomException("OCR_DOCUMENT_TYPES_READING_ERROR", e.getMessage());
        }
    }

    public Set<String> getDocumentTypes() {
        return this.keyWordsByDocument.keySet();
    }

    public Map<String, Object> callOCR(Resource resource, List<String> wordCheckList, Integer distanceCutoff, String docType, Boolean extractData) {
        String url = properties.getOcrHost() + properties.getOcrEndPoint();
        return utils.callOCR(url, resource, wordCheckList, distanceCutoff, docType, extractData).getBody();
    }

    public Map<String, Object> verifyDocument(String fileStoreId, String documentType) {

        Resource resource = fileStoreUtil.getFileFromStore(fileStoreId);
        List<String> keywords = this.keyWordsByDocument.get(documentType);
        log.info(keywords.toString());
        return callOCR(resource, keywords, null, documentType, null);
    }
}
