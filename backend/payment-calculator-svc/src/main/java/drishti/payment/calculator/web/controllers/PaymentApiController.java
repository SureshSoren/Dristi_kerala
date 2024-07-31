package drishti.payment.calculator.web.controllers;


import drishti.payment.calculator.service.CaseFeesCalculationService;
import drishti.payment.calculator.service.SummonCalculationService;
import drishti.payment.calculator.util.ResponseInfoFactory;
import drishti.payment.calculator.web.models.*;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-06-10T14:05:42.847785340+05:30[Asia/Kolkata]")
@Controller
@RequestMapping("")
public class PaymentApiController {

    private final CaseFeesCalculationService caseFeesService;
    private final SummonCalculationService summonCalculationService;
    @Autowired
    public PaymentApiController(CaseFeesCalculationService caseFeesService, SummonCalculationService summonCalculationService) {
        this.caseFeesService = caseFeesService;
        this.summonCalculationService = summonCalculationService;
    }

    @PostMapping(value = "/v1/summons/_calculate")
    public ResponseEntity<CalculationRes> v1CalculatePost(@Parameter(in = ParameterIn.DEFAULT, description = "", required = true, schema = @Schema()) @Valid @RequestBody SummonCalculationReq request) {
        List<Calculation> calculations = summonCalculationService.calculateSummonFees(request);
        CalculationRes response = CalculationRes.builder().responseInfo(ResponseInfoFactory.createResponseInfoFromRequestInfo(request.getRequestInfo(), true)).calculation(calculations).build();
        return new ResponseEntity<>(response, HttpStatus.OK);

    }


    @PostMapping(value = "/v1/case/fees/_calculate")
    public ResponseEntity<CalculationRes> caseFeesCalculation(@Parameter(in = ParameterIn.DEFAULT, description = "", required = true, schema = @Schema()) @Valid @RequestBody EFillingCalculationReq body) {

        List<Calculation>calculations=caseFeesService.calculateCaseFees(body);
        CalculationRes response = CalculationRes.builder().responseInfo(ResponseInfoFactory.createResponseInfoFromRequestInfo(body.getRequestInfo(), true)).calculation(calculations).build();
        return new ResponseEntity<>(response, HttpStatus.OK);

    }
}
