package tcc.bes.api_monolito.reservation.interfaces;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tcc.bes.api_monolito.reservation.application.ReservationView;
import tcc.bes.api_monolito.waitingroom.application.WaitingRoomPort;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reservations")
@ConditionalOnProperty(name = "app.public-api.enabled", havingValue = "true", matchIfMissing = true)
public class ReservationController {

    private final WaitingRoomPort waitingRoomPort;

    public ReservationController(WaitingRoomPort waitingRoomPort) {
        this.waitingRoomPort = waitingRoomPort;
    }

    @GetMapping("/{reservationId}")
    public ReservationView getReservation(
            @PathVariable UUID reservationId,
            @RequestHeader("Authorization") String authorization
    ) {
        return waitingRoomPort.getReservation(reservationId, authorization);
    }

    @PostMapping("/{reservationId}/confirm")
    public ResponseEntity<ReservationView> confirm(
            @PathVariable UUID reservationId,
            @RequestHeader("Authorization") String authorization,
            @RequestHeader("Idempotency-Key") String idempotencyKey
    ) {
        var result = waitingRoomPort.confirmReservation(reservationId, authorization, idempotencyKey);
        return ResponseEntity.status(result.statusCode()).body(result.body());
    }

    @DeleteMapping("/{reservationId}")
    public ReservationView cancel(
            @PathVariable UUID reservationId,
            @RequestHeader("Authorization") String authorization
    ) {
        return waitingRoomPort.cancelReservation(reservationId, authorization);
    }
}
