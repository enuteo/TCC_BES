CREATE TABLE resources (
    id UUID PRIMARY KEY,
    manager_id UUID NOT NULL,
    name VARCHAR(160) NOT NULL,
    total_quantity INTEGER NOT NULL CHECK (total_quantity > 0),
    available_quantity INTEGER NOT NULL CHECK (available_quantity >= 0),
    held_quantity INTEGER NOT NULL CHECK (held_quantity >= 0),
    confirmed_quantity INTEGER NOT NULL CHECK (confirmed_quantity >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (total_quantity = available_quantity + held_quantity + confirmed_quantity)
);

CREATE TABLE reservations (
    id UUID PRIMARY KEY,
    resource_id UUID NOT NULL REFERENCES resources(id),
    entry_id UUID NOT NULL UNIQUE,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    expires_at TIMESTAMPTZ NOT NULL,
    state VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    confirmed_at TIMESTAMPTZ NULL,
    cancelled_at TIMESTAMPTZ NULL,
    expired_at TIMESTAMPTZ NULL
);

CREATE TABLE reservation_terminal_events (
    id UUID PRIMARY KEY,
    reservation_id UUID NOT NULL REFERENCES reservations(id),
    entry_id UUID NOT NULL,
    state VARCHAR(32) NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    acknowledged_at TIMESTAMPTZ NULL
);

CREATE UNIQUE INDEX uq_reservation_terminal_events_reservation
    ON reservation_terminal_events(reservation_id);

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

CREATE INDEX ix_reservations_state_expires
    ON reservations(state, expires_at);

CREATE INDEX ix_resources_manager
    ON resources(manager_id);

CREATE INDEX ix_reservation_terminal_events_pending
    ON reservation_terminal_events(acknowledged_at, occurred_at, id);
