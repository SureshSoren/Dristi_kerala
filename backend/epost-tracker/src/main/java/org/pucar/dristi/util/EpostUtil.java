package org.pucar.dristi.util;

import org.egov.common.contract.models.AuditDetails;
import org.egov.common.contract.request.RequestInfo;
import org.pucar.dristi.config.EPostConfiguration;
import org.pucar.dristi.model.*;
import org.pucar.dristi.repository.EPostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class EpostUtil {

    private final IdgenUtil idgenUtil;

    private final EPostConfiguration config;

    private final EPostRepository ePostRepository;

    @Autowired
    public EpostUtil(IdgenUtil idgenUtil, EPostConfiguration config, EPostRepository ePostRepository) {
        this.idgenUtil = idgenUtil;
        this.config = config;
        this.ePostRepository = ePostRepository;
    }

    public EPostTracker createPostTrackerBody(TaskRequest request) {
        String processNumber = idgenUtil.getIdList(request.getRequestInfo(), config.getEgovStateTenantId(),
                config.getIdName(),null,1).get(0);
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        return EPostTracker.builder()
                .processNumber(processNumber)
                .tenantId(config.getEgovStateTenantId())
                .taskNumber(request.getTask().getTaskNumber())
                .fileStoreId(request.getTask().getDocuments().get(0).getFileStore())
                .address(request.getTask().getTaskDetails().getRespondentDetails().getAddress())
                .pinCode(request.getTask().getTaskDetails().getRespondentDetails().getPinCode())
                .deliveryStatus(DeliveryStatus.NOT_UPDATED)
                .additionalDetails(request.getTask().getAdditionalDetails())
                .rowVersion(0)
                .bookingDate(currentDate)
                .auditDetails(createAuditDetails(request.getRequestInfo()))
                .build();
    }

    public EPostTracker updateEPostTracker(EPostRequest ePostRequest) {
        Pagination pagination = Pagination.builder().limit(5).offSet(0).build();
        EPostTrackerSearchCriteria searchCriteria = EPostTrackerSearchCriteria.builder()
                .processNumber(ePostRequest.getEPostTracker().getProcessNumber()).pagination(pagination).build();
        List<EPostTracker> ePostTrackers = ePostRepository.getEPostTrackerList(searchCriteria);
        if (ePostTrackers.size() != 1) {
            throw new RuntimeException("Invalid EPost Tracker field with processNumber : " + ePostRequest.getEPostTracker().getProcessNumber());
        }
        EPostTracker ePostTracker = ePostTrackers.get(0);

        Long currentTime = System.currentTimeMillis();
        ePostTracker.getAuditDetails().setLastModifiedTime(currentTime);
        ePostTracker.getAuditDetails().setLastModifiedBy(ePostRequest.getRequestInfo().getUserInfo().getUuid());
        ePostTracker.setRowVersion(ePostTracker.getRowVersion() + 1);

        ePostTracker.setTrackingNumber(ePostRequest.getEPostTracker().getTrackingNumber());
        ePostTracker.setDeliveryStatus(ePostRequest.getEPostTracker().getDeliveryStatus());
        ePostTracker.setRemarks(ePostRequest.getEPostTracker().getRemarks());
        ePostTracker.setTaskNumber(ePostRequest.getEPostTracker().getTaskNumber());
        ePostTracker.setReceivedDate(ePostRequest.getEPostTracker().getReceivedDate());

        return ePostTracker;

    }

    private AuditDetails createAuditDetails(RequestInfo requestInfo) {
        long currentTime = System.currentTimeMillis();
        String userId = requestInfo.getUserInfo().getUuid();
        return AuditDetails.builder()
                .createdBy(userId)
                .createdTime(currentTime)
                .lastModifiedBy(userId)
                .lastModifiedTime(currentTime)
                .build();
    }

}
