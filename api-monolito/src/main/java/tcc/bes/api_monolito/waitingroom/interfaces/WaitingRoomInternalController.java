package tcc.bes.api_monolito.waitingroom.interfaces;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tcc.bes.api_monolito.reservation.application.ReservationTerminalEventView;
import tcc.bes.api_monolito.reservation.application.ReservationView;
import tcc.bes.api_monolito.shared.internal.InternalHeaders;
import tcc.bes.api_monolito.shared.internal.InternalRequestAuth;
import tcc.bes.api_monolito.waitingroom.application.EntryJoinResponse;
import tcc.bes.api_monolito.waitingroom.application.EntryView;
import tcc.bes.api_monolito.waitingroom.application.WaitingRoomApplicationService;

import java.util.UUID;

@RestController
@RequestMapping("/internal/v1/waiting-room")
@ConditionalOnProperty(name = {
        "app.internal-api.enabled",
        "app.module.waiting-room.enabled"
}, havingValue = "true")
public class WaitingRoomInternalController {

    private final WaitingRoomApplicationService waitingRoomApplicationService;
    private final InternalRequestAuth internalRequestAuth;

    public WaitingRoomInternalController(
            WaitingRoomApplicationService waitingRoomApplicationService,
            InternalRequestAuth internalRequestAuth
    ) {
        this.waitingRoomApplicationService = waitingRoomApplicationService;
        this.internalRequestAuth = internalRequestAuth;
    }

    @PostMapping("/queues/{queueId}/entries")
    public ResponseEntity<EntryJoinResponse> join(
            @RequestHeader(InternalHeaders.INTERNAL_TOKEN) String internalToken,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @PathVariable UUID queueId,
            @RequestBody JoinEntryRequest request
    ) {
        internalRequestAuth.require(internalToken);
        var result = waitingRoomApplicationService.join(
                queueId,
                request.participantKey(),
                request.quantity(),
                idempotencyKey
        );
        return ResponseEntity.status(result.statusCode()).body(result.body());
    }

    @GetMapping("/entries/{entryId}")
    public EntryView getEntry(
            @RequestHeader(InternalHeaders.INTERNAL_TOKEN) String internalToken,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @PathVariable UUID entryId
    ) {
        internalRequestAuth.require(internalToken);
        return waitingRoomApplicationService.getEntry(entryId, authorization);
    }

    @DeleteMapping("/entries/{entryId}")
    public EntryView cancelEntry(
            @RequestHeader(InternalHeaders.INTERNAL_TOKEN) String internalToken,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @PathVariable UUID entryId
    ) {
        internalRequestAuth.require(internalToken);
        return waitingRoomApplicationService.cancelEntry(entryId, authorization);
    }

    @GetMapping("/reservations/{reservationId}")
    public ReservationView getReservation(
            @RequestHeader(InternalHeaders.INTERNAL_TOKEN) String internalToken,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @PathVariable UUID reservationId
    ) {
        internalRequestAuth.require(internalToken);
        return waitingRoomApplicationService.getReservation(reservationId, authorization);
    }

    @PostMapping("/reservations/{reservationId}/confirm")
    public ResponseEntity<ReservationView> confirmReservation(
            @RequestHeader(InternalHeaders.INTERNAL_TOKEN) String internalToken,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @PathVariable UUID reservationId
    ) {
        internalRequestAuth.require(internalToken);
        var result = waitingRoomApplicationService.confirmReservation(reservationId, authorization, idempotencyKey);
        return ResponseEntity.status(result.statusCode()).body(result.body());
    }

    @DeleteMapping("/reservations/{reservationId}")
    public ReservationView cancelReservation(
            @RequestHeader(InternalHeaders.INTERNAL_TOKEN) String internalToken,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @PathVariable UUID reservationId
    ) {
        internalRequestAuth.require(internalToken);
        return waitingRoomApplicationService.cancelReservation(reservationId, authorization);
    }

    @PostMapping("/queues/{queueId}/process")
    public ResponseEntity<Void> processQueue(
            @RequestHeader(InternalHeaders.INTERNAL_TOKEN) String internalToken,
            @PathVariable UUID queueId
    ) {
        internalRequestAuth.require(internalToken);
        waitingRoomApplicationService.processQueue(queueId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reservation-terminal-events/apply")
    public ResponseEntity<Void> applyReservationTerminalEvent(
            @RequestHeader(InternalHeaders.INTERNAL_TOKEN) String internalToken,
            @RequestBody ReservationTerminalEventView event
    ) {
        internalRequestAuth.require(internalToken);
        waitingRoomApplicationService.applyReservationTerminalEvent(event);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/queues/{queueId}/cancel-waiting")
    public ResponseEntity<Void> cancelWaitingEntriesForClosedQueue(
            @RequestHeader(InternalHeaders.INTERNAL_TOKEN) String internalToken,
            @PathVariable UUID queueId
    ) {
        internalRequestAuth.require(internalToken);
        waitingRoomApplicationService.cancelWaitingEntriesForClosedQueue(queueId);
        return ResponseEntity.noContent().build();
    }

    private record JoinEntryRequest(String participantKey, int quantity) {
    }
}
