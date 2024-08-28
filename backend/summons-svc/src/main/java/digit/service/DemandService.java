package digit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import digit.config.Configuration;
import digit.repository.ServiceRequestRepository;
import digit.util.MdmsUtil;
import digit.web.models.*;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static digit.config.ServiceConstants.*;

@Service
@Slf4j
public class DemandService {

    private final Configuration config;

    private final ObjectMapper mapper;

    private final ServiceRequestRepository repository;

    private final MdmsUtil mdmsUtil;

    @Autowired
    public DemandService(Configuration config, ObjectMapper mapper, ServiceRequestRepository repository, MdmsUtil mdmsUtil) {
        this.config = config;
        this.mapper = mapper;
        this.repository = repository;
        this.mdmsUtil = mdmsUtil;
    }

    public BillResponse fetchPaymentDetailsAndGenerateDemandAndBill(TaskRequest taskRequest) {
        Task task = taskRequest.getTask();
        List<Calculation> calculationList = generatePaymentDetails(taskRequest.getRequestInfo(), task);
        generateDemands(taskRequest.getRequestInfo(), calculationList, task);
        return getBill(taskRequest.getRequestInfo(), task);
    }

    public List<Calculation> generatePaymentDetails(RequestInfo requestInfo, Task task) {
        SummonCalculationCriteria criteria = SummonCalculationCriteria.builder()
                .channelId(ChannelName.fromString(task.getTaskDetails().getDeliveryChannel().getChannelName()).toString())
                .receiverPincode(task.getTaskDetails().getRespondentDetails().getAddress().getPinCode())
                .tenantId(task.getTenantId()).summonId(task.getTaskNumber()).build();

        StringBuilder url = new StringBuilder().append(config.getPaymentCalculatorHost())
                .append(config.getPaymentCalculatorCalculateEndpoint());

        log.info("Requesting Payment Calculator : {}", criteria.toString());

        SummonCalculationRequest calculationRequest = SummonCalculationRequest.builder()
                .requestInfo(requestInfo).calculationCriteria(Collections.singletonList(criteria)).build();

        Object response = repository.fetchResult(url, calculationRequest);

        CalculationResponse calculationResponse = mapper.convertValue(response, CalculationResponse.class);
        return calculationResponse.getCalculation();
    }

    public List<Demand> generateDemands(RequestInfo requestInfo, List<Calculation> calculations, Task task) {
        List<Demand> demands = new ArrayList<>();
        List<DemandDetail> demandDetailList = new ArrayList<>();
        Map<String, Map<String, JSONArray>> mdmsData = mdmsUtil.fetchMdmsData(requestInfo,config.getEgovStateTenantId(),config.getPaymentBusinessServiceNmae(),createMasterDetails());
        for (Calculation calculation : calculations) {
            if (config.isTest()) {
                DemandDetail demandDetail = DemandDetail.builder()
                        .tenantId(calculation.getTenantId())
//.taxAmount(BigDecimal.valueOf(calculation.getTotalAmount()))
                        .taxAmount(BigDecimal.valueOf(4))
                        .taxHeadMasterCode(config.getTaskTaxHeadMasterCode()).build();
            } else {
                Map<String,String> masterCodes = getTaxHeadMasterCodes(mdmsData,config.getTaskBusinessService());
                for (BreakDown breakDown : calculation.getBreakDown()) {
                    DemandDetail detail = DemandDetail.builder()
                            .tenantId(calculation.getTenantId())
                            .taxAmount(BigDecimal.valueOf(breakDown.getAmount()))
                            .taxHeadMasterCode(masterCodes.get(breakDown.getType())).build();
                    demandDetailList.add(detail);
                }
            }

            Demand demand = Demand.builder()
                    .tenantId(calculation.getTenantId())
                    .consumerCode(task.getTaskNumber())
                    .consumerType(config.getTaxConsumerType())
                    .businessService(config.getTaskModuleCode())
                    .taxPeriodFrom(config.getTaxPeriodFrom()).taxPeriodTo(config.getTaxPeriodTo())
                    .demandDetails(demandDetailList)
                    .build();
            demands.add(demand);
        }
        StringBuilder url = new StringBuilder().append(config.getBillingServiceHost())
                .append(config.getDemandCreateEndpoint());
        DemandRequest demandRequest = DemandRequest.builder().requestInfo(requestInfo).demands(demands).build();
        Object response = repository.fetchResult(url, demandRequest);
        DemandResponse demandResponse = mapper.convertValue(response, DemandResponse.class);
        return demandResponse.getDemands();
    }

    private Map<String, String> getTaxHeadMasterCodes(Map<String, Map<String, JSONArray>> mdmsData, String taskBusinessService) {
        if (mdmsData != null && mdmsData.containsKey("payment") && mdmsData.get(config.getPaymentBusinessServiceNmae()).containsKey(PAYMENTMASTERCODE)) {
            JSONArray masterCode = mdmsData.get(config.getPaymentBusinessServiceNmae()).get(PAYMENTMASTERCODE);
            Map<String, String> result = new HashMap<>();
            for (Object masterCodeObj : masterCode) {
                Map<String, String> subType = (Map<String, String>) masterCodeObj;
                if (taskBusinessService.equals(subType.get("businessService"))) {
                    result.put(subType.get("type"), subType.get("masterCode"));
                }
            }
            return result;
        }
        return Collections.emptyMap();
    }

    private List<String> createMasterDetails() {
        List<String> masterList = new ArrayList<>();
        masterList.add(PAYMENTMASTERCODE);
        return masterList;
    }

    public BillResponse getBill(RequestInfo requestInfo, Task task) {
        String uri = buildFetchBillURI(task.getTenantId(), task.getTaskNumber(), config.getTaskBusinessService());

        Object response = repository.fetchResult(new StringBuilder(uri), requestInfo);

        return mapper.convertValue(response, BillResponse.class);
    }

    private String buildFetchBillURI(String tenantId, String applicationNumber, String businessService) {
        try {
            String encodedTenantId = URLEncoder.encode(tenantId, StandardCharsets.UTF_8);
            String encodedApplicationNumber = URLEncoder.encode(applicationNumber, StandardCharsets.UTF_8);
            String encodedBusinessService = URLEncoder.encode(businessService, StandardCharsets.UTF_8);

            return URI.create(String.format("%s%s?tenantId=%s&consumerCode=%s&businessService=%s",
                    config.getBillingServiceHost(),
                    config.getFetchBillEndpoint(),
                    encodedTenantId,
                    encodedApplicationNumber,
                    encodedBusinessService)).toString();
        } catch (Exception e) {
            log.error("Error occurred when creating bill uri with search params", e);
            throw new CustomException("GENERATE_BILL_ERROR", "Error Occurred when  generating bill");
        }
    }
}