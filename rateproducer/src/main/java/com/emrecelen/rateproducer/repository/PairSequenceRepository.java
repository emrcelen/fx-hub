package com.emrecelen.rateproducer.repository;

import com.emrecelen.rateproducer.model.PairSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PairSequenceRepository extends JpaRepository<PairSequence, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
                SELECT ps
                FROM PairSequence ps
                WHERE ps.pair = :pair
            """)
    Optional<PairSequence> findByPairForUpdate(
            @Param("pair") String pair
    );
}
