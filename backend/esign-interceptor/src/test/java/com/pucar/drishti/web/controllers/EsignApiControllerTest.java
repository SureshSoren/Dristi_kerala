package com.pucar.drishti.web.controllers;

import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import com.pucar.drishti.TestConfiguration;

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
        .APPLICATION_JSON))
        .andExpect(status().isOk());
    }

    @Test
    public void esignV1InterceptPostFailure() throws Exception {
        mockMvc.perform(post("/esign/v1/_intercept").contentType(MediaType
        .APPLICATION_JSON))
        .andExpect(status().isBadRequest());
    }

}
