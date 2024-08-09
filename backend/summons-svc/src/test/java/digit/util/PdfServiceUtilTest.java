package digit.util;

import digit.config.Configuration;
import digit.web.models.TaskRequest;
import org.egov.tracer.model.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PdfServiceUtilTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private Configuration config;

    @InjectMocks
    private PdfServiceUtil pdfServiceUtil;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void generatePdfFromPdfService_Success() {
        TaskRequest taskRequest = new TaskRequest();
        String tenantId = "tenant1";
        String pdfTemplateKey = "templateKey";
        ByteArrayResource byteArrayResource = new ByteArrayResource(new byte[]{1, 2, 3});

        when(config.getPdfServiceHost()).thenReturn("http://localhost");
        when(config.getPdfServiceEndpoint()).thenReturn("/pdf-service");
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(ByteArrayResource.class)))
                .thenReturn(ResponseEntity.ok(byteArrayResource));

        ByteArrayResource result = pdfServiceUtil.generatePdfFromPdfService(taskRequest, tenantId, pdfTemplateKey);

        assertNotNull(result);
        assertArrayEquals(new byte[]{1, 2, 3}, result.getByteArray());
    }

    @Test
    void generatePdfFromPdfService_Failure() {
        TaskRequest taskRequest = new TaskRequest();
        String tenantId = "tenant1";
        String pdfTemplateKey = "templateKey";

        when(config.getPdfServiceHost()).thenReturn("http://localhost");
        when(config.getPdfServiceEndpoint()).thenReturn("/pdf-service");
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(ByteArrayResource.class)))
                .thenThrow(new RuntimeException("Service error"));

        CustomException exception = assertThrows(CustomException.class, () ->
                pdfServiceUtil.generatePdfFromPdfService(taskRequest, tenantId, pdfTemplateKey));

        assertEquals("SU_PDF_APP_ERROR", exception.getCode());
        assertEquals("Error getting response from Pdf Service", exception.getMessage());
    }
}