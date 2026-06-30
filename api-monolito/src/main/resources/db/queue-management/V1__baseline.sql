CREATE TABLE queues (
    id UUID PRIMARY KEY,
    manager_id UUID NOT NULL,
    resource_id UUID NOT NULL,
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

CREATE INDEX ix_queues_manager
    ON queues(manager_id);
