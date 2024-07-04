package com.pucar.drishti.web.controllers;

import com.pucar.drishti.web.models.ErrorResponse;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import com.pucar.drishti.TestConfiguration;

    import java.util.ArrayList;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
* API tests for EsignApiController
*/
@Ignore
@RunWith(SpringRunner.class)
@WebMvcTest(InterceptorApiController.class)
@Import(TestConfiguration.class)
public class EsignApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void esignV1InterceptPostSuccess() throws Exception {
        mockMvc.perform(post("/esign/v1/_intercept").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void esignV1InterceptPostFailure() throws Exception {
        mockMvc.perform(post("/esign/v1/_intercept").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

}
