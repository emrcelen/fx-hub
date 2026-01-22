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

    /**
     * Creates a new rate event from incoming request and persists it to outbox.
     * <p>
     * Flow:
     * <ul>
     *   <li>Records receive metric</li>
     *   <li>Validates request payload</li>
     *   <li>Finds or creates currency pair</li>
     *   <li>Checks pair active status</li>
     *   <li>Generates next sequence number</li>
     *   <li>Maps request to domain model</li>
     *   <li>Writes event to outbox</li>
     * </ul>
     * <p>
     * All operations are executed within a single transaction to guarantee
     * consistency between sequence generation and outbox persistence.
     *
     * @param request incoming raw rate request
     * @throws PairNotActiveException if requested pair is not active
     */
    @Transactional
    public void createRateEvent(RawRateRequest request) {
        log.info(
                "Rate event request received. pair={}",
                request.pair()
        );
        rateProducerMetrics.received();

        rateProducerMetrics.recordLatency(() -> {
            isValid(request);
            Pair pair = pairRepository.findById(request.pair())
                    .orElseGet(() -> {
                        log.info(
                                "Pair not found, creating new pair. pair={}",
                                request.pair()
                        );
                        return createPair(request.pair());
                    });

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
            log.info(
                    "Rate event successfully written to outbox. pair={} seq={}",
                    pair.getCurrencyPair(),
                    seq
            );
        });
    }

    /**
     * Creates and persists a new currency pair if it does not exist.
     * <p>
     * This method is called when a rate is received for a pair that is not yet
     * present in the system. Newly created pairs are marked as active by default.
     *
     * @param pair currency pair code (e.g. EUR/USD)
     * @return persisted Pair entity
     */
    private Pair createPair(String pair) {
        log.info(
                "Creating new currency pair. pair={}",
                pair
        );
        Pair entity = Pair.builder(pair)
                .active(true)
                .build();
        Pair saved = pairRepository.save(entity);

        log.info(
                "Currency pair successfully created. pair={} active={}",
                saved.getCurrencyPair(),
                saved.isActive()
        );
        return saved;
    }

    /**
     * Ensures that a sequence record exists for the given currency pair.
     * <p>
     * This method is responsible for initializing the sequence row if it does not
     * already exist. It is used to guarantee that sequence generation can safely
     * start from zero for new pairs.
     * <p>
     * If the sequence already exists, no action is taken.
     *
     * @param pair currency pair code (e.g. EUR/USD)
     */
    private void ensureSequenceExists(String pair) {
        pairSequenceRepository.findById(pair)
                .orElseGet(() -> {
                            log.info(
                                    "Initializing sequence for new pair. pair={}",
                                    pair
                            );
                            return pairSequenceRepository.save(
                                    PairSequence.builder(pair)
                                            .seq(0)
                                            .build()
                            );
                        }
                );
    }

    private String toSource() {
        return appName.concat(":").concat(serverPort);
    }

    /**
     * Validates bid/ask values of incoming rate request.
     *
     * Business rules:
     * - bid must be smaller than ask
     * - values must be valid decimal numbers
     *
     * If validation fails, a domain-specific {@link InvalidRateException} is thrown.
     *
     * @param request incoming raw rate request
     * @throws InvalidRateException if bid >= ask or values are invalid
     */
    private void isValid(RawRateRequest request) {
        try {
            BigDecimal bid = new BigDecimal(request.bid());
            BigDecimal ask = new BigDecimal(request.ask());

            if (bid.compareTo(ask) >= 0) {
                log.warn(
                        "Invalid rate detected. bid >= ask. bid={} ask={}",
                        request.bid(),
                        request.ask()
                );
                throw new InvalidRateException(
                        "bid must be smaller than ask",
                        request.bid(),
                        request.ask()
                );
            }
            log.debug(
                    "Rate validation passed. bid={} ask={}",
                    request.bid(),
                    request.ask()
            );
        } catch (NumberFormatException ex) {
            log.warn(
                    "Invalid numeric format for rate values. bid={} ask={}",
                    request.bid(),
                    request.ask(),
                    ex
            );
            throw new InvalidRateException(
                    "bid/ask must be valid decimal numbers",
                    request.bid(),
                    request.ask()
            );
        }
    }
}
