package tcc.bes.api_monolito.reservation.infrastructure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tcc.bes.api_monolito.reservation.application.ReservationPort;
import tcc.bes.api_monolito.waitingroom.application.WaitingRoomPort;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = {
        "app.jobs.expiration.enabled",
        "app.module.waiting-room.enabled"
}, havingValue = "true", matchIfMissing = true)
public class ReservationExpirationJob {

    private static final int BATCH_SIZE = 100;

    private final ReservationPort reservationPort;
    private final WaitingRoomPort waitingRoomPort;

    public ReservationExpirationJob(
            ReservationPort reservationPort,
            WaitingRoomPort waitingRoomPort
    ) {
        this.reservationPort = reservationPort;
        this.waitingRoomPort = waitingRoomPort;
    }

    @Scheduled(fixedDelayString = "${app.jobs.expiration.fixed-delay-ms}")
    public void run() {
        reservationPort.expireDue(BATCH_SIZE);

        List<UUID> processed = new ArrayList<>();
        reservationPort.listPendingTerminalEvents(BATCH_SIZE).forEach(event -> {
            waitingRoomPort.applyReservationTerminalEvent(event);
            processed.add(event.id());
        });
        reservationPort.acknowledgeTerminalEvents(processed);
    }
}
