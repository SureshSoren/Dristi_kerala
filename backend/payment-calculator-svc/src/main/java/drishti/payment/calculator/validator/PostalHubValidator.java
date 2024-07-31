package drishti.payment.calculator.validator;

import drishti.payment.calculator.web.models.PostalHubRequest;
import org.apache.commons.lang3.ObjectUtils;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;

@Component
public class PostalHubValidator {

    public void validatePostalHubRequest(PostalHubRequest request) {
        request.getPostalHubs().forEach((hub) -> {
            if (ObjectUtils.isEmpty(hub.getTenantId()))
                throw new CustomException("DK_PC_TENANT_ERR", "tenantId is mandatory for creating postal hub");

            if (ObjectUtils.isEmpty(hub.getName()))
                throw new CustomException("DK_PC_NAME_ERR", "name is mandatory for creating postal hub");

            if (ObjectUtils.isEmpty(hub.getPincode()))
                throw new CustomException("DK_PC_PINCODE_ERR", "pincode is mandatory for creating postal hub");

        });

    }

    public void validateExistingPostalHubRequest(PostalHubRequest request) {

//        request.getPostalHubs().forEach((element) -> {
//
//        });



    }
}
