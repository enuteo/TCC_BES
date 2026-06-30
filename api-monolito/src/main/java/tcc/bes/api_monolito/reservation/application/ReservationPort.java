package tcc.bes.api_monolito.reservation.application;

import tcc.bes.api_monolito.identity.application.ManagerIdentity;
import tcc.bes.api_monolito.shared.idempotency.IdempotentResult;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ReservationPort {

    IdempotentResult<ResourceView> createResource(
            ManagerIdentity manager,
            String name,
            int totalQuantity,
            String idempotencyKey
    );

    List<ResourceView> listResources(ManagerIdentity manager);

    ResourceView getResource(ManagerIdentity manager, UUID resourceId);

    ResourceView getResourceForInternalUse(UUID resourceId);

    HoldDecision tryCreateHold(UUID resourceId, UUID entryId, int quantity, Instant expiresAt);

    ReservationView getReservation(UUID reservationId);

    ReservationView confirm(UUID reservationId);

    ReservationView cancel(UUID reservationId);

    List<ReservationView> expireDue(int limit);

    List<ReservationTerminalEventView> listPendingTerminalEvents(int limit);

    void acknowledgeTerminalEvents(List<UUID> eventIds);
}
