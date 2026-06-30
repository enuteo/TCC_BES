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

CREATE INDEX ix_reservation_terminal_events_pending
    ON reservation_terminal_events(acknowledged_at, occurred_at, id);
