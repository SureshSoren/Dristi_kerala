package drishti.payment.calculator.service;

import drishti.payment.calculator.config.Configuration;
import drishti.payment.calculator.enrichment.PostalHubEnrichment;
import drishti.payment.calculator.kafka.Producer;
import drishti.payment.calculator.repository.PostalHubRepository;
import drishti.payment.calculator.validator.PostalHubValidator;
import drishti.payment.calculator.web.models.HubSearchRequest;
import drishti.payment.calculator.web.models.PostalHub;
import drishti.payment.calculator.web.models.PostalHubRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostalHubService {

    private final PostalHubRepository repository;
    private final PostalHubValidator validator;
    private final PostalHubEnrichment enrichment;
    private final Producer producer;
    private final Configuration config;

    public PostalHubService(PostalHubRepository repository, PostalHubValidator validator, PostalHubEnrichment enrichment, Producer producer, Configuration config) {
        this.repository = repository;
        this.validator = validator;
        this.enrichment = enrichment;
        this.producer = producer;
        this.config = config;
    }


    public List<PostalHub> create(PostalHubRequest request) {

        validator.validatePostalHubRequest(request);

        enrichment.enrichPostalHubRequest(request);

        producer.push(config.getPostalHubCreateTopic(), request.getPostalHubs());

        return request.getPostalHubs();
    }

    public List<PostalHub> search(HubSearchRequest searchRequest) {
       return repository.getPostalHub(searchRequest.getCriteria());
    }

    public List<PostalHub> update(PostalHubRequest request) {

        validator.validateExistingPostalHubRequest(request);

        enrichment.enrichExistingPostalHubRequest(request);

        producer.push(config.getPostalHubUpdateTopic(), request.getPostalHubs());

        return request.getPostalHubs();
    }

}
