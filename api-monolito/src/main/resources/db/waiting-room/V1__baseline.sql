CREATE TABLE entries (
    id UUID PRIMARY KEY,
    queue_id UUID NOT NULL,
    resource_id UUID NOT NULL,
    participant_hash VARCHAR(88) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    sequence BIGINT NOT NULL,
    state VARCHAR(32) NOT NULL,
    reservation_id UUID NULL,
    token_hash VARCHAR(88) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    hold_granted_at TIMESTAMPTZ NULL,
    terminal_at TIMESTAMPTZ NULL,
    UNIQUE(queue_id, sequence),
    UNIQUE(reservation_id)
);

CREATE UNIQUE INDEX uq_entries_active_participant
    ON entries(queue_id, participant_hash)
    WHERE state IN ('WAITING', 'HOLD_GRANTED');

CREATE TABLE idempotency_records (
    namespace VARCHAR(80) NOT NULL,
    actor_hash VARCHAR(88) NOT NULL,
    key_hash VARCHAR(88) NOT NULL,
    request_hash VARCHAR(88) NOT NULL,
    response_payload TEXT NOT NULL,
    status_code INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY(namespace, actor_hash, key_hash)
);

CREATE INDEX ix_entries_queue_state_sequence
    ON entries(queue_id, state, sequence);

CREATE INDEX ix_entries_reservation
    ON entries(reservation_id);
