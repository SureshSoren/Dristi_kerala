package drishti.payment.calculator.validator;

import drishti.payment.calculator.repository.PostalHubRepository;
import drishti.payment.calculator.web.models.HubSearchCriteria;
import drishti.payment.calculator.web.models.PostalHub;
import drishti.payment.calculator.web.models.PostalHubRequest;
import org.apache.commons.lang3.ObjectUtils;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PostalHubValidator {

    private final PostalHubRepository repository;

    @Autowired
    public PostalHubValidator(PostalHubRepository repository) {
        this.repository = repository;
    }

    public void validatePostalHubRequest(PostalHubRequest request) {
        request.getPostalHubs().forEach(hub -> {
            if (ObjectUtils.isEmpty(hub.getTenantId()))
                throw new CustomException("DK_PC_TENANT_ERR", "tenantId is mandatory for creating postal hub");

            if (ObjectUtils.isEmpty(hub.getName()))
                throw new CustomException("DK_PC_NAME_ERR", "name is mandatory for creating postal hub");

            if (ObjectUtils.isEmpty(hub.getPincode()))
                throw new CustomException("DK_PC_PINCODE_ERR", "pincode is mandatory for creating postal hub");

        });

    }

    public void validateExistingPostalHubRequest(PostalHubRequest request) {
        request.getPostalHubs().forEach(hub -> {
            PostalHub postalHub = repository.getPostalHub(HubSearchCriteria.builder().hubId(hub.getHubId()).build()).get(0);
            if(hub.getAddress().getId().equals(postalHub.getAddress().getId()))
                throw new CustomException("Duplicate Address", "Address already exists");
        });
    }
}
