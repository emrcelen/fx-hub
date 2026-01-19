-- liquibase formatted sql
-- changeset emrcelen:outbox_event_001
CREATE TABLE IF NOT EXISTS outbox_event  (
  id                        UUID PRIMARY KEY NOT NULL,
  event_key                 VARCHAR(127) NOT NULL,
  event_type                VARCHAR(31) NOT NULL,
  schema_version            INT NOT NULL,
  payload                   JSONB NOT NULL,
  status                    VARCHAR(31) NOT NULL,
  attempts                  INT NOT NULL DEFAULT 0,
  available_at              TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  created_at                TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  processing_started_at     TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  last_error                TEXT NULL
);
