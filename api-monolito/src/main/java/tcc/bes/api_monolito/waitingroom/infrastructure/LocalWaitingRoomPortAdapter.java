package tcc.bes.api_monolito.waitingroom.infrastructure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import tcc.bes.api_monolito.reservation.application.ReservationTerminalEventView;
import tcc.bes.api_monolito.reservation.application.ReservationView;
import tcc.bes.api_monolito.shared.idempotency.IdempotentResult;
import tcc.bes.api_monolito.waitingroom.application.EntryJoinResponse;
import tcc.bes.api_monolito.waitingroom.application.EntryView;
import tcc.bes.api_monolito.waitingroom.application.WaitingRoomApplicationService;
import tcc.bes.api_monolito.waitingroom.application.WaitingRoomPort;

import java.util.UUID;

@Component
@ConditionalOnExpression("'${app.module.waiting-room.enabled:true}' == 'true' && '${app.adapters.waiting-room.mode:local}' == 'local'")
public class LocalWaitingRoomPortAdapter implements WaitingRoomPort {

    private final WaitingRoomApplicationService waitingRoomApplicationService;

    public LocalWaitingRoomPortAdapter(WaitingRoomApplicationService waitingRoomApplicationService) {
        this.waitingRoomApplicationService = waitingRoomApplicationService;
    }

    @Override
    public IdempotentResult<EntryJoinResponse> join(
            UUID queueId,
            String participantKey,
            int quantity,
            String idempotencyKey
    ) {
        return waitingRoomApplicationService.join(queueId, participantKey, quantity, idempotencyKey);
    }

    @Override
    public EntryView getEntry(UUID entryId, String authorizationHeader) {
        return waitingRoomApplicationService.getEntry(entryId, authorizationHeader);
    }

    @Override
    public EntryView cancelEntry(UUID entryId, String authorizationHeader) {
        return waitingRoomApplicationService.cancelEntry(entryId, authorizationHeader);
    }

    @Override
    public IdempotentResult<ReservationView> confirmReservation(
            UUID reservationId,
            String authorizationHeader,
            String idempotencyKey
    ) {
        return waitingRoomApplicationService.confirmReservation(reservationId, authorizationHeader, idempotencyKey);
    }

    @Override
    public ReservationView cancelReservation(UUID reservationId, String authorizationHeader) {
        return waitingRoomApplicationService.cancelReservation(reservationId, authorizationHeader);
    }

    @Override
    public ReservationView getReservation(UUID reservationId, String authorizationHeader) {
        return waitingRoomApplicationService.getReservation(reservationId, authorizationHeader);
    }

    @Override
    public void processQueue(UUID queueId) {
        waitingRoomApplicationService.processQueue(queueId);
    }

    @Override
    public void applyReservationTerminalEvent(ReservationTerminalEventView event) {
        waitingRoomApplicationService.applyReservationTerminalEvent(event);
    }

    @Override
    public void cancelWaitingEntriesForClosedQueue(UUID queueId) {
        waitingRoomApplicationService.cancelWaitingEntriesForClosedQueue(queueId);
    }
}
