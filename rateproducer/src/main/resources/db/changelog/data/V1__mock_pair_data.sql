-- liquibase formatted sql
-- changeset emrcelen:mock_pair_data_001
WITH pairs(pair) AS (
    SELECT unnest(ARRAY[
        'EUR/USD',
        'USD/EUR',
        'GBP/USD',
        'USD/GBP',
        'USD/TRY',
        'TRY/USD',
        'EUR/TRY',
        'TRY/EUR',
        'EUR/GBP',
        'GBP/EUR'
    ])
)
INSERT INTO allowed_pair (pair, is_active, created_at)
SELECT p.pair, true, now()
FROM pairs p
WHERE NOT EXISTS (
    SELECT 1 FROM allowed_pair ap WHERE ap.pair = p.pair
);

WITH pairs(pair) AS (
    SELECT unnest(ARRAY[
        'EUR/USD',
        'USD/EUR',
        'GBP/USD',
        'USD/GBP',
        'USD/TRY',
        'TRY/USD',
        'EUR/TRY',
        'TRY/EUR',
        'EUR/GBP',
        'GBP/EUR'
    ])
)
INSERT INTO pair_sequence (pair, last_seq)
SELECT p.pair, 0
FROM pairs p
WHERE NOT EXISTS (
    SELECT 1 FROM pair_sequence ps WHERE ps.pair = p.pair
);