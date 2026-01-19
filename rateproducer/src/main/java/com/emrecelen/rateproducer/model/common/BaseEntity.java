package com.emrecelen.rateproducer.model.common;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@MappedSuperclass
public abstract class BaseEntity {
    @Id
    @UuidGenerator
    private UUID id;

    public UUID getId() {
        return id;
    }
}
