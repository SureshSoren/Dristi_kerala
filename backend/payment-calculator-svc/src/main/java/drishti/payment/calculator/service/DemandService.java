package drishti.payment.calculator.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import drishti.payment.calculator.config.Configuration;
import drishti.payment.calculator.repository.ServiceRequestRepository;
import drishti.payment.calculator.web.models.*;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class DemandService {

    private final Configuration config;

    private final ObjectMapper mapper;

    private final ServiceRequestRepository repository;

    @Autowired
    public DemandService(Configuration config, ObjectMapper mapper, ServiceRequestRepository repository) {
        this.config = config;
        this.mapper = mapper;
        this.repository = repository;
    }

    public List<Demand> generateDemands(RequestInfo requestInfo, List<Calculation> calculations ,String moduleCode,String taxHeadMasterCode) {
        List<Demand> demands = new ArrayList<>();

        for (Calculation calculation : calculations) {
            DemandDetail demandDetail = DemandDetail.builder()
                    .tenantId(calculation.getTenantId())
                    .taxAmount(BigDecimal.valueOf(calculation.getTotalAmount()))
                    .taxHeadMasterCode(taxHeadMasterCode).build();

            Demand demand = Demand.builder()
                    .tenantId(calculation.getTenantId()).consumerCode(calculation.getApplicationId())
                    .consumerType("PAYMENT_BND_CONSUMER_CODE")
                    .businessService(moduleCode)
                    .taxPeriodFrom(System.currentTimeMillis()).taxPeriodTo(System.currentTimeMillis())
                    .demandDetails(Collections.singletonList(demandDetail))
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

    public BillResponse getBill(RequestInfoWrapper requestInfoWrapper, String tenantId, String applicationNumber,String businessService) {
        String uri = getFetchBillURI();
        uri = uri.replace("{1}", tenantId);
        uri = uri.replace("{2}", applicationNumber);
        uri = uri.replace("{3}", businessService);  //todo: this is important configure this

        Object response = repository.fetchResult(new StringBuilder(uri), requestInfoWrapper);
        BillResponse billResponse = mapper.convertValue(response, BillResponse.class);

        return billResponse;
    }

    public String getFetchBillURI() {
        StringBuilder url = new StringBuilder(config.getBillingServiceHost());
        url.append(config.getFetchBillEndpoint());
        url.append("?");
        url.append("tenantId=");
        url.append("{1}");
        url.append("&");
        url.append("consumerCode=");
        url.append("{2}");
        url.append("&");
        url.append("businessService=");
        url.append("{3}");

        return url.toString();
    }
}
