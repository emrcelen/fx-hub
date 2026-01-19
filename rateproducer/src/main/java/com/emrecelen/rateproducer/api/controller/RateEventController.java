package com.emrecelen.rateproducer.api.controller;

import com.emrecelen.rateproducer.api.dto.RawRateRequest;
import com.emrecelen.rateproducer.service.RateEventService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rates")
public class RateEventController {

    private final RateEventService rateEventService;

    public RateEventController(RateEventService rateEventService) {
        this.rateEventService = rateEventService;
    }

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody @Valid RawRateRequest req) {
        rateEventService.createRateEvent(req);
        return ResponseEntity.accepted().build();
    }
}
