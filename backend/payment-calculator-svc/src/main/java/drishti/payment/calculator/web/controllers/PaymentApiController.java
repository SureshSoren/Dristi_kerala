package drishti.payment.calculator.web.controllers;


import drishti.payment.calculator.service.CaseFeesCalculationService;
import drishti.payment.calculator.service.DemandService;
import drishti.payment.calculator.service.SummonCalculationService;
import drishti.payment.calculator.util.ResponseInfoFactory;
import drishti.payment.calculator.web.models.*;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.constraints.NotNull;
import java.util.List;

@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-06-10T14:05:42.847785340+05:30[Asia/Kolkata]")
@Controller
@RequestMapping("")
@Slf4j
public class PaymentApiController {

    private final CaseFeesCalculationService caseFeesService;
    private final SummonCalculationService summonCalculationService;
//    private final DemandService demandService;

    @Autowired
    public PaymentApiController(CaseFeesCalculationService caseFeesService, SummonCalculationService summonCalculationService) {
        this.caseFeesService = caseFeesService;
        this.summonCalculationService = summonCalculationService;

//        this.demandService = demandService;
    }

    @RequestMapping(value = "/v1/summons/_calculate", method = RequestMethod.POST)
    public ResponseEntity<CalculationRes> v1CalculatePost(@Parameter(in = ParameterIn.DEFAULT, description = "", required = true, schema = @Schema()) @Valid @RequestBody SummonCalculationReq request) {
        log.info("api = /v1/summons/_calculate, result=IN_PROGRESS ");
        List<Calculation> calculations = summonCalculationService.calculateSummonFees(request);
        CalculationRes response = CalculationRes.builder().responseInfo(ResponseInfoFactory.createResponseInfoFromRequestInfo(request.getRequestInfo(), true)).calculation(calculations).build();
        log.info("api = /v1/summons/_calculate, result=SUCCESS");
        return new ResponseEntity<>(response, HttpStatus.OK);

    }


    @RequestMapping(value = "/v1/case/fees/_calculate", method = RequestMethod.POST)
    public ResponseEntity<CalculationRes> caseFeesCalculation(@Parameter(in = ParameterIn.DEFAULT, description = "", required = true, schema = @Schema()) @Valid @RequestBody EFillingCalculationReq body) {
        log.info("api = /v1/case/fees/_calculate, result=IN_PROGRESS ");
        List<Calculation>calculations=caseFeesService.calculateCaseFees(body);
        CalculationRes response = CalculationRes.builder().responseInfo(ResponseInfoFactory.createResponseInfoFromRequestInfo(body.getRequestInfo(), true)).calculation(calculations).build();
        log.info("api = /v1/case/fees/_calculate, result=SUCCESS");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

//    @RequestMapping(value = "/v1/_getbill", method = RequestMethod.POST)
//    public ResponseEntity<BillResponse> v1GetbillPost(@NotNull @ApiParam(value = "Unique id for a tenant.", required = true) @javax.validation.Valid @RequestParam(value = "tenantId", required = true) String tenantId, @NotNull @ApiParam(value = "Unique birth registration application number.", required = true) @javax.validation.Valid @RequestParam(value = "applicationNumber", required = true) String applicationNumber, @NotNull @ApiParam(value = "Unique id for a business.", required = true) @javax.validation.Valid @RequestParam(value = "business", required = true) String businessService,@ApiParam(value = "Parameter to carry Request metadata in the request body") @javax.validation.Valid @RequestBody RequestInfoWrapper requestInfoWrapper) {
//        BillResponse billResponse = demandService.getBill(requestInfoWrapper, tenantId, applicationNumber,businessService);
//        return new ResponseEntity<BillResponse>(billResponse, HttpStatus.OK);
//    }
}
