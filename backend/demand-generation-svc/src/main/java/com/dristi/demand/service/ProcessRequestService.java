package com.dristi.demand.service;

import com.dristi.demand.util.MdmsUtil;
import com.dristi.demand.web.models.Calculation;
import com.dristi.demand.web.models.DemandConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ProcessRequestService {

    @Autowired
    private PaymentCalculatorService calculatorService;

    @Autowired
    private DemandService demandService;


    @Autowired
    private MdmsUtil mdmsUtil;

    @Autowired
    private ObjectMapper mapper;

    public void process(HashMap<String, Object> record, String topic) {

        String tenantId = " ";//fixme : read me from record
        Object requestInfo = record.get("RequestInfo");

        Gson gsonForReq = new Gson();
        String reqJson = gsonForReq.toJson(requestInfo);
        JSONObject JsonRequestInfo = new JSONObject(reqJson);

        RequestInfo info = mapper.convertValue(requestInfo, RequestInfo.class);

        Gson gson = new Gson();
        String jsonString = gson.toJson(record);
        JSONObject jsonObject = new JSONObject(jsonString);

        DemandConfiguration configFromMDMS = mdmsUtil.getConfigFromMDMS(tenantId, topic);

        // get consumer id
        String path = configFromMDMS.getConsumerId();
        String[] pathElements = path.split("\\.");

        String consumerId = getValueFromJson(jsonObject, pathElements);

        Object payment = configFromMDMS.getPayment();

        LinkedHashMap<String, Object> paymentConfig = (LinkedHashMap<String, Object>) payment;

        String endpoint = paymentConfig.get("endPoint").toString();


        JSONObject paymentCalculatorPayload = getPaymentCalculatorPayload(JsonRequestInfo, paymentConfig, jsonObject);

        // todo :  call payment calculator

        List<Calculation> calculations = calculatorService.calculatePayment( paymentCalculatorPayload, endpoint);


        calculations.get(0).setApplicationId(consumerId);

        //todo : call demand
        demandService.generateDemands(info, calculations, configFromMDMS.getModuleCode(), configFromMDMS.getTaxHeadMasterCode());

    }


    public JSONObject getPaymentCalculatorPayload(JSONObject requestInfo, LinkedHashMap<String, Object> payment, JSONObject data) {


        String heading = payment.get("heading").toString();

        JSONArray criteriaArray = new JSONArray();
        JSONObject criteriaObject = new JSONObject();
        JSONObject paymentRequest = new JSONObject();


        for (Map.Entry<String, Object> entry : payment.entrySet()) {
            String key = entry.getKey();
            if (!key.equals("heading") && !key.equals("endPoint")) {
                String path = payment.get(key).toString();
                String[] pathElements = path.split("\\.");
                String valueFromJson = null;
                if (pathElements.length > 1) {
                    valueFromJson = getValueFromJson(data, pathElements);
                } else {
                    valueFromJson = path;
                }
                criteriaObject.put(key, valueFromJson);


            }

        }

        criteriaArray.put(criteriaObject);
        paymentRequest.put(heading, criteriaArray);
        paymentRequest.put("RequestInfo",requestInfo);

        return paymentRequest;
    }


    private String getValueFromJson(JSONObject jsonObject, String[] pathElements) {
        Object currentObject = jsonObject;

        String value;
        for (String element : pathElements) {
            if (currentObject instanceof JSONObject) {
                currentObject = ((JSONObject) currentObject).opt(element);
            } else if (currentObject instanceof JSONArray) {
                try {
                    int index = Integer.parseInt(element);
                    currentObject = ((JSONArray) currentObject).opt(index);
                } catch (NumberFormatException e) {
                    return null; // Path element is not a valid array index
                }
            } else {
                return null;
            }
        }
        return currentObject.toString();
    }

}
