-- liquibase formatted sql
-- changeset emrcelen:outbox_event_constraint_001

CREATE UNIQUE INDEX ux_outbox_event_key ON  outbox_event(event_key);