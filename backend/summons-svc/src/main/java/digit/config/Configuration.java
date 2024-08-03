package digit.config;

import lombok.*;
import org.egov.tracer.config.TracerConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

@Component
@Import({TracerConfiguration.class})
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class Configuration {

    //Tenant Id
    @Value("${egov-state-level-tenant-id}")
    private String egovStateTenantId;

    //Idgen Config
    @Value("${egov.idgen.host}")
    private String idGenHost;

    @Value("${egov.idgen.path}")
    private String idGenPath;

    @Value("${summons.idgen.format}")
    private String summonsIdFormat;

    //Pdf Service Config
    @Value("${summons.pdf.template.key}")
    private String summonsPdfTemplateKey;

    @Value("${warrant.pdf.template.key}")
    private String warrantPdfTemplateKey;

    @Value("${bail.pdf.template.key}")
    private String bailPdfTemplateKey;

    //MDMS
    @Value("${egov.mdms.host}")
    private String mdmsHost;

    @Value("${egov.mdms.search.endpoint}")
    private String mdmsEndPoint;

    //SMSNotification
    @Value("${egov.sms.notification.topic}")
    private String smsNotificationTopic;

    @Value("${egov.pdf.service.host}")
    private String pdfServiceHost;

    @Value("${egov.pdf.service.create.endpoint}")
    private String pdfServiceEndpoint;

    // File Store Service
    @Value("${egov.file.store.summons.module}")
    private String summonsFileStoreModule;

    @Value("${egov.file.store.host}")
    private String fileStoreHost;

    @Value("${egov.file.store.save.endpoint}")
    private String fileStoreEndPoint;

    // task service

    @Value("${egov.task.service.host}")
    private String taskServiceHost;

    @Value("${egov.task.service.update.endpoint}")
    private String taskServiceUpdateEndpoint;

    // ICops

    @Value("${egov.icops.host}")
    private String iCopsHost;

    @Value("${egov.icops.request.endpoint}")
    private String iCopsRequestEndPoint;

    // ESummons

    @Value("${egov.esummons.host}")
    private String eSummonsHost;

    @Value("${egov.esummons.request.endpoint}")
    private String ESummonsRequestEndPoint;

    //Billing Service

    @Value("${egov.billingservice.host}")
    private String billingServiceHost;

    @Value("${egov.demand.create.endpoint}")
    private String demandCreateEndpoint;

    @Value("${egov.billingservice.fetch.bill}")
    private String fetchBillEndpoint;

    @Value("${task.taxhead.master.code}")
    private String taskTaxHeadMasterCode;

    @Value("${egov.tax.period.to}")
    private Long taxPeriodTo;

    @Value("${egov.tax.period.from}")
    private Long taxPeriodFrom;

    @Value("${egov.tax.consumer.type}")
    private String taxConsumerType;

    @Value("${task.module.code}")
    private String taskModuleCode;

    @Value(("${task.business.service}"))
    private String taskBusinessService;

    //Payment Calculator Service

    @Value("${payment.calculator.host}")
    private String paymentCalculatorHost;

    @Value("${payment.calculator.calculate.endpoint}")
    private String paymentCalculatorCalculateEndpoint;
}
