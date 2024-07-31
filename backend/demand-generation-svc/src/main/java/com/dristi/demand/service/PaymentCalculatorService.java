package com.dristi.demand.service;


import com.dristi.demand.config.Configuration;
import com.dristi.demand.repository.ServiceRequestRepository;
import com.dristi.demand.web.models.Calculation;
import com.dristi.demand.web.models.CalculationRes;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class PaymentCalculatorService {

    private final Configuration config;

    private final ObjectMapper mapper;

    private final ServiceRequestRepository repository;

    @Autowired
    public PaymentCalculatorService(Configuration config, ObjectMapper mapper, ServiceRequestRepository repository) {
        this.config = config;
        this.mapper = mapper;
        this.repository = repository;
    }


    public List<Calculation> calculatePayment(JSONObject paymentCalculatorPayload, String endpoint) {

        StringBuilder url = new StringBuilder().append(config.getPaymentCalculatorHost())
                .append(endpoint);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(paymentCalculatorPayload.toString(), httpHeaders);
        Object response = repository.fetchResult(url, entity);

        CalculationRes calculationRes = mapper.convertValue(response, CalculationRes.class);

        return calculationRes.getCalculation();


    }
}
