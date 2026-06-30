package tcc.bes.api_monolito.reservation.infrastructure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import tcc.bes.api_monolito.identity.application.ManagerIdentity;
import tcc.bes.api_monolito.reservation.application.HoldDecision;
import tcc.bes.api_monolito.reservation.application.ReservationApplicationService;
import tcc.bes.api_monolito.reservation.application.ReservationPort;
import tcc.bes.api_monolito.reservation.application.ReservationTerminalEventView;
import tcc.bes.api_monolito.reservation.application.ReservationView;
import tcc.bes.api_monolito.reservation.application.ResourceView;
import tcc.bes.api_monolito.shared.idempotency.IdempotentResult;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
@ConditionalOnExpression("'${app.module.reservation.enabled:true}' == 'true' && '${app.adapters.reservation.mode:local}' == 'local'")
public class LocalReservationPortAdapter implements ReservationPort {

    private final ReservationApplicationService reservationApplicationService;

    public LocalReservationPortAdapter(ReservationApplicationService reservationApplicationService) {
        this.reservationApplicationService = reservationApplicationService;
    }

    @Override
    public IdempotentResult<ResourceView> createResource(
            ManagerIdentity manager,
            String name,
            int totalQuantity,
            String idempotencyKey
    ) {
        return reservationApplicationService.createResource(manager, name, totalQuantity, idempotencyKey);
    }

    @Override
    public List<ResourceView> listResources(ManagerIdentity manager) {
        return reservationApplicationService.listResources(manager);
    }

    @Override
    public ResourceView getResource(ManagerIdentity manager, UUID resourceId) {
        return reservationApplicationService.getResource(manager, resourceId);
    }

    @Override
    public ResourceView getResourceForInternalUse(UUID resourceId) {
        return reservationApplicationService.getResourceForInternalUse(resourceId);
    }

    @Override
    public HoldDecision tryCreateHold(UUID resourceId, UUID entryId, int quantity, Instant expiresAt) {
        return reservationApplicationService.tryCreateHold(resourceId, entryId, quantity, expiresAt);
    }

    @Override
    public ReservationView getReservation(UUID reservationId) {
        return reservationApplicationService.getReservation(reservationId);
    }

    @Override
    public ReservationView confirm(UUID reservationId) {
        return reservationApplicationService.confirm(reservationId);
    }

    @Override
    public ReservationView cancel(UUID reservationId) {
        return reservationApplicationService.cancel(reservationId);
    }

    @Override
    public List<ReservationView> expireDue(int limit) {
        return reservationApplicationService.expireDue(limit);
    }

    @Override
    public List<ReservationTerminalEventView> listPendingTerminalEvents(int limit) {
        return reservationApplicationService.listPendingTerminalEvents(limit);
    }

    @Override
    public void acknowledgeTerminalEvents(List<UUID> eventIds) {
        reservationApplicationService.acknowledgeTerminalEvents(eventIds);
    }
}
