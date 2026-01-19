package com.emrecelen.rateproducer.controller;

import com.emrecelen.rateproducer.api.controller.RateEventController;
import com.emrecelen.rateproducer.api.dto.RawRateRequest;
import com.emrecelen.rateproducer.api.error.GlobalExceptionHandler;
import com.emrecelen.rateproducer.config.TestConfig;
import com.emrecelen.rateproducer.exception.PairNotActiveException;
import com.emrecelen.rateproducer.service.RateEventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import({
        GlobalExceptionHandler.class,
        TestConfig.class
})
@WebMvcTest(RateEventController.class)
class RateEventControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private RateEventService rateEventService;

    @Test
    void should_create_rate_event_and_return_202() throws Exception {
        RawRateRequest request = new RawRateRequest(
                "EUR/TRY",
                "1.00001",
                "1.00001"
        );
        mockMvc.perform(post("/api/rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());

        verify(rateEventService).createRateEvent(any(RawRateRequest.class));
    }

    @Test
    void should_return_400_when_request_invalid() throws Exception {
        RawRateRequest invalidRequest = new RawRateRequest(
                null,
                null,
                null
        );

        mockMvc.perform(post("/api/rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0].field").exists())
                .andExpect(jsonPath("$.errors[0].message").exists());
    }

    @Test
    void should_return_409_when_pair_not_active() throws Exception {
        // given
        RawRateRequest request = new RawRateRequest(
                "EUR/USD",
                "1.0001",
                "1.0001"
        );

        doThrow(new PairNotActiveException("EUR/USD"))
                .when(rateEventService)
                .createRateEvent(any());

        mockMvc.perform(post("/api/rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PAIR_NOT_AVAILABLE"))
                .andExpect(jsonPath("$.message")
                        .value("Requested pair is currently not available"))
                .andExpect(jsonPath("$.items").doesNotExist());
    }
}
