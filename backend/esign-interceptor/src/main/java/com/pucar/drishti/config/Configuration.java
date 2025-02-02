package com.pucar.drishti.config;

import lombok.*;
import org.egov.tracer.config.TracerConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

@Component
@Data
@Import({TracerConfiguration.class})
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class Configuration {

    //filestore
    @Value("${egov.filestore.host}")
    private String filestoreHost;

    @Value("${egov.filestore.search.endpoint}")
    private String filestoreEndPoint;

    //e-sign
    @Value("${drishti.esign.host}")
    private String eSignHost;

    @Value("${drishti.esign.endpoint}")
    private String eSignEndPoint;

    @Value("${drishti.esign.redirect.url}")
    private String redirectUrl;


}
