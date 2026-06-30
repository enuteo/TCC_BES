package tcc.bes.api_monolito.waitingroom.interfaces;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tcc.bes.api_monolito.waitingroom.application.EntryJoinResponse;
import tcc.bes.api_monolito.waitingroom.application.EntryView;
import tcc.bes.api_monolito.waitingroom.application.WaitingRoomPort;

import java.util.UUID;

@RestController
@ConditionalOnProperty(name = "app.public-api.enabled", havingValue = "true", matchIfMissing = true)
public class EntryController {

    private final WaitingRoomPort waitingRoomPort;

    public EntryController(WaitingRoomPort waitingRoomPort) {
        this.waitingRoomPort = waitingRoomPort;
    }

    @PostMapping("/api/v1/queues/{queueId}/entries")
    public ResponseEntity<EntryJoinResponse> join(
            @PathVariable UUID queueId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody JoinEntryRequest request
    ) {
        var result = waitingRoomPort.join(
                queueId,
                request.participantKey(),
                request.quantity(),
                idempotencyKey
        );
        return ResponseEntity.status(result.statusCode()).body(result.body());
    }

    @GetMapping("/api/v1/entries/{entryId}")
    public EntryView getEntry(
            @PathVariable UUID entryId,
            @RequestHeader("Authorization") String authorization
    ) {
        return waitingRoomPort.getEntry(entryId, authorization);
    }

    @DeleteMapping("/api/v1/entries/{entryId}")
    public EntryView cancelEntry(
            @PathVariable UUID entryId,
            @RequestHeader("Authorization") String authorization
    ) {
        return waitingRoomPort.cancelEntry(entryId, authorization);
    }

    public record JoinEntryRequest(
            @NotBlank(message = "Participant key cannot be empty")
            @Size(max = 200, message = "Participant key must have at most 200 characters")
            String participantKey,
            @Min(value = 1, message = "Quantity must be positive")
            int quantity
    ) {
    }
}
