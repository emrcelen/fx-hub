-- liquibase formatted sql
-- changeset emrcelen:pair_constraint_001

ALTER TABLE pair_sequence
ADD CONSTRAINT fk_pair_sequence_pair
FOREIGN KEY (pair)
REFERENCES allowed_pair (pair)
ON UPDATE CASCADE
ON DELETE RESTRICT;