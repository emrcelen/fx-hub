package com.emrecelen.rateproducer.service;

import com.emrecelen.rateproducer.model.PairSequence;
import com.emrecelen.rateproducer.repository.PairSequenceRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class PairSequenceService {

    private final PairSequenceRepository repository;

    public PairSequenceService(PairSequenceRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public long nextSeq(String pair) {
        PairSequence seq = repository.findByPairForUpdate(pair)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "PairSequence not found for pair=" + pair
                        )
                );

        seq.increment();
        long next = seq.getLastSeq();
        repository.saveAndFlush(seq);
        return next;
    }
}
