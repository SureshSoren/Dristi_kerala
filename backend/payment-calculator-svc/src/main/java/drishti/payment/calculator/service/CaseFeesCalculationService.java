package drishti.payment.calculator.service;


import drishti.payment.calculator.config.Configuration;
import drishti.payment.calculator.util.EFillingUtil;
import drishti.payment.calculator.web.models.*;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class CaseFeesCalculationService {

    private final EFillingUtil eFillingUtil;
//    private final DemandService demandService;
    private final Configuration config;

    @Autowired
    public CaseFeesCalculationService(EFillingUtil eFillingUtil, Configuration config) {
        this.eFillingUtil = eFillingUtil;
//        this.demandService = demandService;
        this.config = config;
    }


    public List<Calculation> calculateCaseFees(EFillingCalculationReq request) {


        RequestInfo requestInfo = request.getRequestInfo();

        List<EFillingCalculationCriteria> calculationCriteria = request.getCalculationCriteria();

        EFilingParam eFillingDefaultData = eFillingUtil.getEFillingDefaultData(requestInfo, calculationCriteria.get(0).getTenantId());

        Double courtFee = eFillingDefaultData.getCourtFee();
        Double applicationFee = eFillingDefaultData.getApplicationFee();
        Double vakalathnamaFee = eFillingDefaultData.getVakalathnamaFee();
        Double advocateWelfareFund = eFillingDefaultData.getAdvocateWelfareFund();
        Double advocateClerkWelfareFund = eFillingDefaultData.getAdvocateClerkWelfareFund();
        Double petitionFeePercentage = eFillingDefaultData.getPetitionFeePercentage();
        Double defaultPetitionFee = eFillingDefaultData.getDefaultPetitionFee();


        List<Calculation> result = new ArrayList<>();

        for (EFillingCalculationCriteria criteria : calculationCriteria) {


            Double totalApplicationFee = criteria.getNumberOfApplication() * applicationFee;

            Double petitionFee = Math.max(defaultPetitionFee, petitionFeePercentage * criteria.getCheckAmount());

            List<BreakDown> feeBreakdown = getFeeBreakdown(courtFee, vakalathnamaFee, advocateWelfareFund, advocateClerkWelfareFund, totalApplicationFee, petitionFee);
            Double totalCourtFee = courtFee + vakalathnamaFee + advocateWelfareFund + advocateClerkWelfareFund + totalApplicationFee + petitionFee;
            Calculation calculation = Calculation.builder()
                    .applicationId(criteria.getCaseId())
                    .totalAmount(totalCourtFee)
                    .tenantId(criteria.getTenantId())
                    .breakDown(feeBreakdown).build();

            result.add(calculation);
        }

//        demandService.generateDemands(requestInfo, result,config.getCaseModuleCode(),config.getCaseTaxHeadMasterCode());
        return result;

    }

    public List<BreakDown> getFeeBreakdown(double courtFee, double vakalathnamaFee, double advocateWelfareFund, double advocateClerkWelfareFund, double totalApplicationFee, double petitionFee) {
        List<BreakDown> feeBreakdowns = new ArrayList<>();

        feeBreakdowns.add(new BreakDown("Court Fee", courtFee, new HashMap<>()));
        feeBreakdowns.add(new BreakDown("Vakalathnama Fee", vakalathnamaFee, new HashMap<>()));
        feeBreakdowns.add(new BreakDown("Advocate Welfare Fund", advocateWelfareFund, new HashMap<>()));
        feeBreakdowns.add(new BreakDown("Advocate Clerk Welfare Fund", advocateClerkWelfareFund, new HashMap<>()));
        feeBreakdowns.add(new BreakDown("Total Application Fee", totalApplicationFee, new HashMap<>()));
        feeBreakdowns.add(new BreakDown("Petition Fee", petitionFee, new HashMap<>()));

        return feeBreakdowns;
    }

}
