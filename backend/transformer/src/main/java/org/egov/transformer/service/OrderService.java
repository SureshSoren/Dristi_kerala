package org.egov.transformer.service;

import lombok.extern.slf4j.Slf4j;
import org.egov.transformer.config.ServiceConstants;
import org.egov.transformer.config.TransformerProperties;
import org.egov.transformer.models.Order;

import org.egov.transformer.models.OrderRequest;

import org.egov.transformer.producer.OrderProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final CaseService caseService;
    private  final TransformerProperties properties;
    private  final OrderProducer producer;
    private final ApplicationService applicationService;



    @Autowired
    public OrderService(CaseService caseService, TransformerProperties properties, OrderProducer producer, ApplicationService applicationService) {
        this.caseService = caseService;
        this.properties = properties;
        this.producer = producer;
        this.applicationService = applicationService;
    }
   public void addOrderDetailsToApplication(Order order)
   {int index;
       for(index=0;index<order.getApplicationNumber().size();index++)
        {

           applicationService.updateApplication(order,index);

        }

   }
    public void addOrderDetails(Order order){


        if (order.getFilingNumber() != null
                && (order.getOrderType().equalsIgnoreCase(ServiceConstants.BAIL_ORDER_TYPE)
                    || order.getOrderType().equalsIgnoreCase(ServiceConstants.JUDGEMENT_ORDER_TYPE))) {
                caseService.updateCase(order);

            }
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setOrder(order);
        producer.push(properties.getOrderCreateTopic(), orderRequest);
    }
}
