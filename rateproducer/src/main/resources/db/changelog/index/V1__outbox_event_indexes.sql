-- liquibase formatted sql
-- changeset emrcelen:outbox_event_indexes_001
CREATE INDEX ix_outbox_status_available ON outbox_event(status, available_at);
