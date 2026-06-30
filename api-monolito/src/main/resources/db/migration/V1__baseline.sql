CREATE TABLE manager_accounts (
    id UUID PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(160) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE resources (
    id UUID PRIMARY KEY,
    manager_id UUID NOT NULL REFERENCES manager_accounts(id),
    name VARCHAR(160) NOT NULL,
    total_quantity INTEGER NOT NULL CHECK (total_quantity > 0),
    available_quantity INTEGER NOT NULL CHECK (available_quantity >= 0),
    held_quantity INTEGER NOT NULL CHECK (held_quantity >= 0),
    confirmed_quantity INTEGER NOT NULL CHECK (confirmed_quantity >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (total_quantity = available_quantity + held_quantity + confirmed_quantity)
);

CREATE TABLE queues (
    id UUID PRIMARY KEY,
    manager_id UUID NOT NULL REFERENCES manager_accounts(id),
    resource_id UUID NOT NULL REFERENCES resources(id),
    name VARCHAR(160) NOT NULL,
    max_quantity_per_participant INTEGER NOT NULL CHECK (max_quantity_per_participant > 0),
    hold_duration_seconds INTEGER NOT NULL CHECK (hold_duration_seconds > 0),
    worker_interval_ms INTEGER NOT NULL CHECK (worker_interval_ms > 0),
    max_batch_size INTEGER NOT NULL CHECK (max_batch_size > 0),
    state VARCHAR(32) NOT NULL,
    next_sequence BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_queues_resource_not_closed
    ON queues(resource_id)
    WHERE state <> 'CLOSED';

CREATE TABLE entries (
    id UUID PRIMARY KEY,
    queue_id UUID NOT NULL REFERENCES queues(id),
    resource_id UUID NOT NULL REFERENCES resources(id),
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

CREATE TABLE reservations (
    id UUID PRIMARY KEY,
    resource_id UUID NOT NULL REFERENCES resources(id),
    entry_id UUID NOT NULL UNIQUE REFERENCES entries(id),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    expires_at TIMESTAMPTZ NOT NULL,
    state VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    confirmed_at TIMESTAMPTZ NULL,
    cancelled_at TIMESTAMPTZ NULL,
    expired_at TIMESTAMPTZ NULL
);

ALTER TABLE entries
    ADD CONSTRAINT fk_entries_reservation
    FOREIGN KEY (reservation_id) REFERENCES reservations(id);

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

CREATE INDEX ix_reservations_state_expires
    ON reservations(state, expires_at);

CREATE INDEX ix_resources_manager
    ON resources(manager_id);

CREATE INDEX ix_queues_manager
    ON queues(manager_id);
