package tcc.bes.api_monolito.reservation.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import tcc.bes.api_monolito.filter.CorrelationIdFilter;
import tcc.bes.api_monolito.identity.application.ManagerIdentity;
import tcc.bes.api_monolito.reservation.application.HoldDecision;
import tcc.bes.api_monolito.reservation.application.ReservationPort;
import tcc.bes.api_monolito.reservation.application.ReservationTerminalEventView;
import tcc.bes.api_monolito.reservation.application.ReservationView;
import tcc.bes.api_monolito.reservation.application.ResourceView;
import tcc.bes.api_monolito.shared.error.ApiException;
import tcc.bes.api_monolito.shared.idempotency.IdempotentResult;
import tcc.bes.api_monolito.shared.internal.InternalHeaders;
import tcc.bes.api_monolito.shared.internal.InternalRequestAuth;
import tcc.bes.api_monolito.shared.internal.RemoteApiErrors;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "app.adapters.reservation.mode", havingValue = "http")
public class HttpReservationPortAdapter implements ReservationPort {

    private final RestClient restClient;
    private final InternalRequestAuth internalRequestAuth;
    private final ObjectMapper objectMapper;

    public HttpReservationPortAdapter(
            RestClient.Builder restClientBuilder,
            InternalRequestAuth internalRequestAuth,
            ObjectMapper objectMapper,
            @Value("${app.internal.reservation.base-url}") String baseUrl
    ) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.internalRequestAuth = internalRequestAuth;
        this.objectMapper = objectMapper;
    }

    @Override
    public IdempotentResult<ResourceView> createResource(
            ManagerIdentity manager,
            String name,
            int totalQuantity,
            String idempotencyKey
    ) {
        return execute(() -> {
            ResponseEntity<ResourceView> response = restClient.post()
                    .uri("/internal/v1/resources")
                    .headers(headers -> addHeaders(headers, idempotencyKey))
                    .body(new CreateResourceRequest(manager.id(), name, totalQuantity))
                    .retrieve()
                    .toEntity(ResourceView.class);
            return new IdempotentResult<>(response.getStatusCode().value(), response.getBody());
        });
    }

    @Override
    public List<ResourceView> listResources(ManagerIdentity manager) {
        return execute(() -> restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/internal/v1/resources")
                        .queryParam("managerId", manager.id())
                        .build())
                .headers(this::addHeaders)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                }));
    }

    @Override
    public ResourceView getResource(ManagerIdentity manager, UUID resourceId) {
        return execute(() -> restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/internal/v1/resources/{resourceId}")
                        .queryParam("managerId", manager.id())
                        .build(resourceId))
                .headers(this::addHeaders)
                .retrieve()
                .body(ResourceView.class));
    }

    @Override
    public ResourceView getResourceForInternalUse(UUID resourceId) {
        return execute(() -> restClient.get()
                .uri("/internal/v1/resources/{resourceId}/internal", resourceId)
                .headers(this::addHeaders)
                .retrieve()
                .body(ResourceView.class));
    }

    @Override
    public HoldDecision tryCreateHold(UUID resourceId, UUID entryId, int quantity, Instant expiresAt) {
        return execute(() -> restClient.post()
                .uri("/internal/v1/reservations/holds")
                .headers(this::addHeaders)
                .body(new CreateHoldRequest(resourceId, entryId, quantity, expiresAt))
                .retrieve()
                .body(HoldDecision.class));
    }

    @Override
    public ReservationView getReservation(UUID reservationId) {
        return execute(() -> restClient.get()
                .uri("/internal/v1/reservations/{reservationId}", reservationId)
                .headers(this::addHeaders)
                .retrieve()
                .body(ReservationView.class));
    }

    @Override
    public ReservationView confirm(UUID reservationId) {
        return execute(() -> restClient.post()
                .uri("/internal/v1/reservations/{reservationId}/confirm", reservationId)
                .headers(this::addHeaders)
                .retrieve()
                .body(ReservationView.class));
    }

    @Override
    public ReservationView cancel(UUID reservationId) {
        return execute(() -> restClient.delete()
                .uri("/internal/v1/reservations/{reservationId}", reservationId)
                .headers(this::addHeaders)
                .retrieve()
                .body(ReservationView.class));
    }

    @Override
    public List<ReservationView> expireDue(int limit) {
        return execute(() -> restClient.post()
                .uri(uriBuilder -> uriBuilder.path("/internal/v1/reservations/expire-due")
                        .queryParam("limit", limit)
                        .build())
                .headers(this::addHeaders)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                }));
    }

    @Override
    public List<ReservationTerminalEventView> listPendingTerminalEvents(int limit) {
        return execute(() -> restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/internal/v1/reservation-terminal-events")
                        .queryParam("limit", limit)
                        .build())
                .headers(this::addHeaders)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                }));
    }

    @Override
    public void acknowledgeTerminalEvents(List<UUID> eventIds) {
        execute(() -> {
            restClient.post()
                    .uri("/internal/v1/reservation-terminal-events/ack")
                    .headers(this::addHeaders)
                    .body(new AcknowledgeEventsRequest(eventIds))
                    .retrieve()
                    .toBodilessEntity();
            return null;
        });
    }

    private void addHeaders(HttpHeaders headers) {
        addHeaders(headers, null);
    }

    private void addHeaders(HttpHeaders headers, String idempotencyKey) {
        headers.set(InternalHeaders.INTERNAL_TOKEN, internalRequestAuth.token());
        if (idempotencyKey != null) {
            headers.set("Idempotency-Key", idempotencyKey);
        }
        String correlationId = MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY);
        if (correlationId != null) {
            headers.set(CorrelationIdFilter.CORRELATION_ID_HEADER, correlationId);
        }
    }

    private <T> T execute(RemoteCall<T> call) {
        try {
            return call.execute();
        } catch (RestClientResponseException ex) {
            throw RemoteApiErrors.from(ex, objectMapper);
        } catch (ApiException ex) {
            throw ex;
        }
    }

    private interface RemoteCall<T> {
        T execute();
    }

    private record CreateResourceRequest(UUID managerId, String name, int totalQuantity) {
    }

    private record CreateHoldRequest(UUID resourceId, UUID entryId, int quantity, Instant expiresAt) {
    }

    private record AcknowledgeEventsRequest(List<UUID> eventIds) {
    }
}
