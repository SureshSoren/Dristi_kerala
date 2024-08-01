package org.pucar.dristi.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.response.ResponseInfo;
import org.pucar.dristi.model.*;
import org.pucar.dristi.service.EPostService;
import org.pucar.dristi.util.ResponseInfoFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Collections;

@Controller
@Slf4j
public class EPostController {

    private final EPostService ePostService;

    private final ResponseInfoFactory responseInfoFactory;

    @Autowired
    public EPostController(EPostService ePostService, ResponseInfoFactory responseInfoFactory) {
        this.ePostService = ePostService;
        this.responseInfoFactory = responseInfoFactory;
    }

    @RequestMapping(value = "epost/v1/_sendEPost", method = RequestMethod.POST)
    public ResponseEntity<ChannelResponse> sendEPost(@RequestBody TaskRequest body){
        ChannelMessage channelMessage = ePostService.sendEPost(body);
        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(body.getRequestInfo(), true);
        ChannelResponse channelResponse = ChannelResponse.builder().channelMessage(channelMessage).responseInfo(responseInfo).build();
        return new ResponseEntity<>(channelResponse, HttpStatus.OK);
    }
    @RequestMapping(value = "epost/v1/_getEPost", method = RequestMethod.POST)
    public ResponseEntity<EPostResponse> getEPost(@Parameter(in = ParameterIn.DEFAULT, description = "Hearing Details and Request Info", required = true, schema = @Schema()) @Valid @RequestBody EPostTrackerSearchRequest request) {
        EPostResponse ePostResponse = ePostService.getEPost(request);
        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(request.getRequestInfo(), true);
        ePostResponse.setResponseInfo(responseInfo);
        return new ResponseEntity<>(ePostResponse, HttpStatus.OK);
    }

    @RequestMapping(value = "epost/v1/_updateEPost", method = RequestMethod.POST)
    public ResponseEntity<EPostResponse> updateEPost(@RequestBody EPostRequest body){
        EPostTracker ePostTracker = ePostService.updateEPost(body);
        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(body.getRequestInfo(), true);
        EPostResponse ePostResponse = EPostResponse.builder().ePostTrackers(Collections.singletonList(ePostTracker)).responseInfo(responseInfo).build();
        return new ResponseEntity<>(ePostResponse, HttpStatus.OK);
    }
}
