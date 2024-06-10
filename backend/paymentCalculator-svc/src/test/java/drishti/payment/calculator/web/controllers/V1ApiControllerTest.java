package drishti.payment.calculator.web.controllers;

import drishti.payment.calculator.web.models.BillResponse;
import drishti.payment.calculator.web.models.CalculationReq;
import drishti.payment.calculator.web.models.CalculationRes;
import drishti.payment.calculator.web.models.ErrorResponse;
import drishti.payment.calculator.web.models.RequestInfoWrapper;
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
import drishti.payment.calculator.TestConfiguration;

    import java.util.ArrayList;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
* API tests for V1ApiController
*/
@Ignore
@RunWith(SpringRunner.class)
@WebMvcTest(V1ApiController.class)
@Import(TestConfiguration.class)
public class V1ApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void v1CalculatePostSuccess() throws Exception {
        mockMvc.perform(post("/v1/_calculate").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void v1CalculatePostFailure() throws Exception {
        mockMvc.perform(post("/v1/_calculate").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

    @Test
    public void v1GetbillPostSuccess() throws Exception {
        mockMvc.perform(post("/v1/_getbill").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void v1GetbillPostFailure() throws Exception {
        mockMvc.perform(post("/v1/_getbill").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

}
