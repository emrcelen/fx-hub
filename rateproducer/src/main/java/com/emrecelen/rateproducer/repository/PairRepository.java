package com.emrecelen.rateproducer.repository;

import com.emrecelen.rateproducer.model.Pair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PairRepository extends JpaRepository<Pair, String> {
}
