package com.emrecelen.rateproducer.service;

import com.emrecelen.rateproducer.model.PairSequence;
import com.emrecelen.rateproducer.repository.PairSequenceRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PairSequenceService {

    private final PairSequenceRepository repository;

    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

    public PairSequenceService(PairSequenceRepository repository) {
        this.repository = repository;
    }

    /**
     * Generates and persists the next sequence number for the given currency pair.
     *
     * <p>
     * This method uses a database-level lock (FOR UPDATE) to ensure
     * sequence consistency under concurrent access.
     * </p>
     * <p>
     * Workflow:
     * <ul>
     *   <li>Locks {@link PairSequence} row for the given pair</li>
     *   <li>Increments sequence atomically</li>
     *   <li>Persists updated value immediately</li>
     * </ul>
     *
     * @param pair currency pair (e.g. EUR/USD)
     * @return next sequence number
     * @throws IllegalStateException if sequence record does not exist
     */
    @Transactional
    public long nextSeq(String pair) {
        log.debug("Generating next sequence for pair={}", pair);

        PairSequence seq = repository.findByPairForUpdate(pair)
                .orElseThrow(() -> {
                            log.error(
                                    "PairSequence not found while generating next sequence. pair={}",
                                    pair
                            );
                            return new IllegalStateException(
                                    "PairSequence not found for pair=" + pair
                            );
                        }
                );
        long previous = seq.getLastSeq();
        seq.increment();
        long next = seq.getLastSeq();
        repository.saveAndFlush(seq);
        log.info(
                "Sequence incremented successfully. pair={} previousSeq={} nextSeq={}",
                pair,
                previous,
                next
        );

        return next;
    }
}
