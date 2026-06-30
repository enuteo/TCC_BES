package tcc.bes.api_monolito.reservation.interfaces;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tcc.bes.api_monolito.identity.application.IdentityPort;
import tcc.bes.api_monolito.identity.application.ManagerIdentity;
import tcc.bes.api_monolito.reservation.application.ReservationPort;
import tcc.bes.api_monolito.reservation.application.ResourceView;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/resources")
@ConditionalOnProperty(name = "app.public-api.enabled", havingValue = "true", matchIfMissing = true)
public class ResourceController {

    private final IdentityPort identityPort;
    private final ReservationPort reservationPort;

    public ResourceController(
            IdentityPort identityPort,
            ReservationPort reservationPort
    ) {
        this.identityPort = identityPort;
        this.reservationPort = reservationPort;
    }

    @PostMapping
    public ResponseEntity<ResourceView> createResource(
            @RequestHeader("Authorization") String authorization,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CreateResourceRequest request
    ) {
        ManagerIdentity manager = identityPort.requireManager(authorization);
        var result = reservationPort.createResource(
                manager,
                request.name(),
                request.totalQuantity(),
                idempotencyKey
        );
        return ResponseEntity.status(result.statusCode()).body(result.body());
    }

    @GetMapping
    public List<ResourceView> listResources(@RequestHeader("Authorization") String authorization) {
        return reservationPort.listResources(identityPort.requireManager(authorization));
    }

    @GetMapping("/{resourceId}")
    public ResourceView getResource(
            @RequestHeader("Authorization") String authorization,
            @PathVariable UUID resourceId
    ) {
        return reservationPort.getResource(identityPort.requireManager(authorization), resourceId);
    }

    public record CreateResourceRequest(
            @NotBlank(message = "Name cannot be empty")
            @Size(max = 160, message = "Name must have at most 160 characters")
            String name,
            @Min(value = 1, message = "Total quantity must be positive")
            int totalQuantity
    ) {
    }
}
