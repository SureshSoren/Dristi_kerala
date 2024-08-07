package digit.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import digit.config.Configuration;
import digit.repository.ServiceRequestRepository;
import digit.web.models.hearing.HearingRequest;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.ServiceCallException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import static digit.config.ServiceConstants.*;

@Component
@Slf4j
public class HearingUtil {

    private final ObjectMapper objectMapper;

    private final Configuration configuration;

    private final ServiceRequestRepository serviceRequestRepository;

    public HearingUtil(ObjectMapper objectMapper, Configuration configuration, ServiceRequestRepository serviceRequestRepository) {
        this.objectMapper = objectMapper;
        this.configuration = configuration;
        this.serviceRequestRepository = serviceRequestRepository;
    }

    public void callHearing(HearingRequest hearingRequest) {
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS,false);
        StringBuilder uri = new StringBuilder(configuration.getHearingHost().concat(configuration.getHearingUpdateEndPoint()));
        try {
            serviceRequestRepository.fetchResult(uri,hearingRequest);
        } catch (HttpClientErrorException e) {
            log.error(EXTERNAL_SERVICE_EXCEPTION, e);
            throw new ServiceCallException(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error(SEARCHER_SERVICE_EXCEPTION, e);
        }

    }
}
