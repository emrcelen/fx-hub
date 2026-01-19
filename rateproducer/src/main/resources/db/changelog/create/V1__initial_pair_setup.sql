-- liquibase formatted sql
-- changeset emrcelen:pair_001
CREATE TABLE IF NOT EXISTS allowed_pair(
    pair                        VARCHAR(31) PRIMARY KEY NOT NULL,
    is_active                   BOOLEAN NOT NULL DEFAULT FALSE,
    created_at                  TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS pair_sequence(
    pair                        VARCHAR(31) PRIMARY KEY NOT NULL,
    last_seq                    BIGINT NOT NULL DEFAULT 0
);

