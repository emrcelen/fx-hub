package com.emrecelen.rateproducer.service;

import com.emrecelen.rateproducer.api.dto.RawRateRequest;
import com.emrecelen.rateproducer.common.Constants;
import com.emrecelen.rateproducer.domain.model.RateEvent;
import com.emrecelen.rateproducer.domain.model.RawRate;
import com.emrecelen.rateproducer.domain.service.RateEventFactory;
import com.emrecelen.rateproducer.exception.InvalidRateException;
import com.emrecelen.rateproducer.exception.PairNotActiveException;
import com.emrecelen.rateproducer.mapper.RateEventMapper;
import com.emrecelen.rateproducer.model.Pair;
import com.emrecelen.rateproducer.model.PairSequence;
import com.emrecelen.rateproducer.monitoring.metrics.RateProducerMetrics;
import com.emrecelen.rateproducer.repository.PairRepository;
import com.emrecelen.rateproducer.repository.PairSequenceRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


@Service
public class RateEventService {

    @Value("${spring.application.name}")
    private String appName;

    @Value("${server.port}")
    private String serverPort;

    private final OutboxWriter outboxWriter;
    private final PairRepository pairRepository;
    private final RateEventMapper rateEventMapper;
    private final RateProducerMetrics rateProducerMetrics;
    private final PairSequenceService pairSequenceService;
    private final PairSequenceRepository pairSequenceRepository;


    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

    public RateEventService(
            OutboxWriter outboxWriter,
            PairRepository pairRepository,
            RateEventMapper rateEventMapper,
            RateProducerMetrics rateProducerMetrics,
            PairSequenceService pairSequenceService,
            PairSequenceRepository pairSequenceRepository
    ) {
        this.outboxWriter = outboxWriter;
        this.pairRepository = pairRepository;
        this.rateEventMapper = rateEventMapper;
        this.rateProducerMetrics = rateProducerMetrics;
        this.pairSequenceService = pairSequenceService;
        this.pairSequenceRepository = pairSequenceRepository;
    }

    @Transactional
    public void createRateEvent(RawRateRequest request) {
        rateProducerMetrics.received();

        rateProducerMetrics.recordLatency(() -> {
            isValid(request);
            Pair pair = pairRepository.findById(request.pair())
                    .orElseGet(() -> createPair(request.pair()));

            if (!pair.isActive()) {
                log.warn(
                        "Pair is inactive. pair={}",
                        request.pair()
                );
                throw new PairNotActiveException(pair.getCurrencyPair());
            }
            ensureSequenceExists(pair.getCurrencyPair());

            long seq = pairSequenceService.nextSeq(pair.getCurrencyPair());
            RawRate raw = rateEventMapper.toRawRate(
                    toSource(),
                    seq,
                    request
            );
            outboxWriter.write(Constants.OutboxType.RATE_EVENT.name(), raw);
        });
    }

    private Pair createPair(String pair) {
        Pair entity = Pair.builder(pair)
                .active(true)
                .build();
        return pairRepository.save(entity);
    }

    private void ensureSequenceExists(String pair) {
        pairSequenceRepository.findById(pair)
                .orElseGet(() ->
                        pairSequenceRepository.save(
                                PairSequence.builder(pair)
                                        .seq(0)
                                        .build()
                        )
                );
    }

    private String toSource() {
        return appName.concat(":").concat(serverPort);
    }

    private void isValid(RawRateRequest request) {
        BigDecimal bid = new BigDecimal(request.bid());
        BigDecimal ask = new BigDecimal(request.ask());

        if (bid.compareTo(ask) >= 0) {
            throw new InvalidRateException(
                    "bid must be smaller than ask",
                    request.bid(),
                    request.ask()
            );
        }
    }
}
