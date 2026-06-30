package tcc.bes.api_monolito.reservation.interfaces;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tcc.bes.api_monolito.identity.application.ManagerIdentity;
import tcc.bes.api_monolito.reservation.application.HoldDecision;
import tcc.bes.api_monolito.reservation.application.ReservationApplicationService;
import tcc.bes.api_monolito.reservation.application.ReservationTerminalEventView;
import tcc.bes.api_monolito.reservation.application.ReservationView;
import tcc.bes.api_monolito.reservation.application.ResourceView;
import tcc.bes.api_monolito.shared.internal.InternalHeaders;
import tcc.bes.api_monolito.shared.internal.InternalRequestAuth;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/internal/v1")
@ConditionalOnProperty(name = {
        "app.internal-api.enabled",
        "app.module.reservation.enabled"
}, havingValue = "true")
public class ReservationInternalController {

    private final ReservationApplicationService reservationApplicationService;
    private final InternalRequestAuth internalRequestAuth;

    public ReservationInternalController(
            ReservationApplicationService reservationApplicationService,
            InternalRequestAuth internalRequestAuth
    ) {
        this.reservationApplicationService = reservationApplicationService;
        this.internalRequestAuth = internalRequestAuth;
    }

    @PostMapping("/resources")
    public ResponseEntity<ResourceView> createResource(
            @RequestHeader(InternalHeaders.INTERNAL_TOKEN) String internalToken,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody CreateResourceRequest request
    ) {
        internalRequestAuth.require(internalToken);
        var result = reservationApplicationService.createResource(
                manager(request.managerId()),
                request.name(),
                request.totalQuantity(),
                idempotencyKey
        );
        return ResponseEntity.status(result.statusCode()).body(result.body());
    }

    @GetMapping("/resources")
    public List<ResourceView> listResources(
            @RequestHeader(InternalHeaders.INTERNAL_TOKEN) String internalToken,
            @RequestParam UUID managerId
    ) {
        internalRequestAuth.require(internalToken);
        return reservationApplicationService.listResources(manager(managerId));
    }

    @GetMapping("/resources/{resourceId}")
    public ResourceView getResource(
            @RequestHeader(InternalHeaders.INTERNAL_TOKEN) String internalToken,
            @RequestParam UUID managerId,
            @PathVariable UUID resourceId
    ) {
        internalRequestAuth.require(internalToken);
        return reservationApplicationService.getResource(manager(managerId), resourceId);
    }

    @GetMapping("/resources/{resourceId}/internal")
    public ResourceView getResourceForInternalUse(
            @RequestHeader(InternalHeaders.INTERNAL_TOKEN) String internalToken,
            @PathVariable UUID resourceId
    ) {
        internalRequestAuth.require(internalToken);
        return reservationApplicationService.getResourceForInternalUse(resourceId);
    }

    @PostMapping("/reservations/holds")
    public HoldDecision tryCreateHold(
            @RequestHeader(InternalHeaders.INTERNAL_TOKEN) String internalToken,
            @RequestBody CreateHoldRequest request
    ) {
        internalRequestAuth.require(internalToken);
        return reservationApplicationService.tryCreateHold(
                request.resourceId(),
                request.entryId(),
                request.quantity(),
                request.expiresAt()
        );
    }

    @GetMapping("/reservations/{reservationId}")
    public ReservationView getReservation(
            @RequestHeader(InternalHeaders.INTERNAL_TOKEN) String internalToken,
            @PathVariable UUID reservationId
    ) {
        internalRequestAuth.require(internalToken);
        return reservationApplicationService.getReservation(reservationId);
    }

    @PostMapping("/reservations/{reservationId}/confirm")
    public ReservationView confirm(
            @RequestHeader(InternalHeaders.INTERNAL_TOKEN) String internalToken,
            @PathVariable UUID reservationId
    ) {
        internalRequestAuth.require(internalToken);
        return reservationApplicationService.confirm(reservationId);
    }

    @DeleteMapping("/reservations/{reservationId}")
    public ReservationView cancel(
            @RequestHeader(InternalHeaders.INTERNAL_TOKEN) String internalToken,
            @PathVariable UUID reservationId
    ) {
        internalRequestAuth.require(internalToken);
        return reservationApplicationService.cancel(reservationId);
    }

    @PostMapping("/reservations/expire-due")
    public List<ReservationView> expireDue(
            @RequestHeader(InternalHeaders.INTERNAL_TOKEN) String internalToken,
            @RequestParam(defaultValue = "100") int limit
    ) {
        internalRequestAuth.require(internalToken);
        return reservationApplicationService.expireDue(limit);
    }

    @GetMapping("/reservation-terminal-events")
    public List<ReservationTerminalEventView> listPendingTerminalEvents(
            @RequestHeader(InternalHeaders.INTERNAL_TOKEN) String internalToken,
            @RequestParam(defaultValue = "100") int limit
    ) {
        internalRequestAuth.require(internalToken);
        return reservationApplicationService.listPendingTerminalEvents(limit);
    }

    @PostMapping("/reservation-terminal-events/ack")
    public ResponseEntity<Void> acknowledgeTerminalEvents(
            @RequestHeader(InternalHeaders.INTERNAL_TOKEN) String internalToken,
            @RequestBody AcknowledgeEventsRequest request
    ) {
        internalRequestAuth.require(internalToken);
        reservationApplicationService.acknowledgeTerminalEvents(request.eventIds());
        return ResponseEntity.noContent().build();
    }

    private ManagerIdentity manager(UUID managerId) {
        return new ManagerIdentity(managerId, "internal", "internal");
    }

    private record CreateResourceRequest(UUID managerId, String name, int totalQuantity) {
    }

    private record CreateHoldRequest(UUID resourceId, UUID entryId, int quantity, Instant expiresAt) {
    }

    private record AcknowledgeEventsRequest(List<UUID> eventIds) {
    }
}
