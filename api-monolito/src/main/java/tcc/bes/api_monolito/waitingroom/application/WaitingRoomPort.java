package tcc.bes.api_monolito.waitingroom.application;

import tcc.bes.api_monolito.reservation.application.ReservationTerminalEventView;
import tcc.bes.api_monolito.reservation.application.ReservationView;
import tcc.bes.api_monolito.shared.idempotency.IdempotentResult;

import java.util.UUID;

public interface WaitingRoomPort {

    IdempotentResult<EntryJoinResponse> join(
            UUID queueId,
            String participantKey,
            int quantity,
            String idempotencyKey
    );

    EntryView getEntry(UUID entryId, String authorizationHeader);

    EntryView cancelEntry(UUID entryId, String authorizationHeader);

    IdempotentResult<ReservationView> confirmReservation(
            UUID reservationId,
            String authorizationHeader,
            String idempotencyKey
    );

    ReservationView cancelReservation(UUID reservationId, String authorizationHeader);

    ReservationView getReservation(UUID reservationId, String authorizationHeader);

    void processQueue(UUID queueId);

    void applyReservationTerminalEvent(ReservationTerminalEventView event);

    void cancelWaitingEntriesForClosedQueue(UUID queueId);
}
