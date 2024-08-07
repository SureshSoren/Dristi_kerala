package org.pucar.dristi.repository.rowMapper;

import org.pucar.dristi.web.model.Ocr;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class OcrRowMapper implements RowMapper<Ocr> {
    @Override
    public Ocr mapRow(ResultSet rs, int rowNum) throws SQLException {
        Ocr ocr = new Ocr();
        ocr.setId(UUID.fromString(rs.getString("id")));
        ocr.setTenantId(rs.getString("tenantId"));
        ocr.setFilingNumber(rs.getString("filingNumber"));
        ocr.setFileStoreId(rs.getString("fileStoreId"));
        ocr.setDocumentType(rs.getString("documentType"));
        ocr.setMessage(rs.getString("message"));
        ocr.setExtractedData(rs.getObject("extractedData"));
        return ocr;
    }
}
