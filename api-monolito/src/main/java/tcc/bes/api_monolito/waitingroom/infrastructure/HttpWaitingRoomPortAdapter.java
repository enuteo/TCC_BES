package tcc.bes.api_monolito.waitingroom.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import tcc.bes.api_monolito.filter.CorrelationIdFilter;
import tcc.bes.api_monolito.reservation.application.ReservationTerminalEventView;
import tcc.bes.api_monolito.reservation.application.ReservationView;
import tcc.bes.api_monolito.shared.error.ApiException;
import tcc.bes.api_monolito.shared.idempotency.IdempotentResult;
import tcc.bes.api_monolito.shared.internal.InternalHeaders;
import tcc.bes.api_monolito.shared.internal.InternalRequestAuth;
import tcc.bes.api_monolito.shared.internal.RemoteApiErrors;
import tcc.bes.api_monolito.waitingroom.application.EntryJoinResponse;
import tcc.bes.api_monolito.waitingroom.application.EntryView;
import tcc.bes.api_monolito.waitingroom.application.WaitingRoomPort;

import java.util.UUID;

@Component
@ConditionalOnProperty(name = "app.adapters.waiting-room.mode", havingValue = "http")
public class HttpWaitingRoomPortAdapter implements WaitingRoomPort {

    private final RestClient restClient;
    private final InternalRequestAuth internalRequestAuth;
    private final ObjectMapper objectMapper;

    public HttpWaitingRoomPortAdapter(
            RestClient.Builder restClientBuilder,
            InternalRequestAuth internalRequestAuth,
            ObjectMapper objectMapper,
            @Value("${app.internal.waiting-room.base-url}") String baseUrl
    ) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.internalRequestAuth = internalRequestAuth;
        this.objectMapper = objectMapper;
    }

    @Override
    public IdempotentResult<EntryJoinResponse> join(
            UUID queueId,
            String participantKey,
            int quantity,
            String idempotencyKey
    ) {
        return execute(() -> {
            ResponseEntity<EntryJoinResponse> response = restClient.post()
                    .uri("/internal/v1/waiting-room/queues/{queueId}/entries", queueId)
                    .headers(headers -> addHeaders(headers, null, idempotencyKey))
                    .body(new JoinEntryRequest(participantKey, quantity))
                    .retrieve()
                    .toEntity(EntryJoinResponse.class);
            return new IdempotentResult<>(response.getStatusCode().value(), response.getBody());
        });
    }

    @Override
    public EntryView getEntry(UUID entryId, String authorizationHeader) {
        return execute(() -> restClient.get()
                .uri("/internal/v1/waiting-room/entries/{entryId}", entryId)
                .headers(headers -> addHeaders(headers, authorizationHeader, null))
                .retrieve()
                .body(EntryView.class));
    }

    @Override
    public EntryView cancelEntry(UUID entryId, String authorizationHeader) {
        return execute(() -> restClient.delete()
                .uri("/internal/v1/waiting-room/entries/{entryId}", entryId)
                .headers(headers -> addHeaders(headers, authorizationHeader, null))
                .retrieve()
                .body(EntryView.class));
    }

    @Override
    public IdempotentResult<ReservationView> confirmReservation(
            UUID reservationId,
            String authorizationHeader,
            String idempotencyKey
    ) {
        return execute(() -> {
            ResponseEntity<ReservationView> response = restClient.post()
                    .uri("/internal/v1/waiting-room/reservations/{reservationId}/confirm", reservationId)
                    .headers(headers -> addHeaders(headers, authorizationHeader, idempotencyKey))
                    .retrieve()
                    .toEntity(ReservationView.class);
            return new IdempotentResult<>(response.getStatusCode().value(), response.getBody());
        });
    }

    @Override
    public ReservationView cancelReservation(UUID reservationId, String authorizationHeader) {
        return execute(() -> restClient.delete()
                .uri("/internal/v1/waiting-room/reservations/{reservationId}", reservationId)
                .headers(headers -> addHeaders(headers, authorizationHeader, null))
                .retrieve()
                .body(ReservationView.class));
    }

    @Override
    public ReservationView getReservation(UUID reservationId, String authorizationHeader) {
        return execute(() -> restClient.get()
                .uri("/internal/v1/waiting-room/reservations/{reservationId}", reservationId)
                .headers(headers -> addHeaders(headers, authorizationHeader, null))
                .retrieve()
                .body(ReservationView.class));
    }

    @Override
    public void processQueue(UUID queueId) {
        execute(() -> {
            restClient.post()
                    .uri("/internal/v1/waiting-room/queues/{queueId}/process", queueId)
                    .headers(this::addHeaders)
                    .retrieve()
                    .toBodilessEntity();
            return null;
        });
    }

    @Override
    public void applyReservationTerminalEvent(ReservationTerminalEventView event) {
        execute(() -> {
            restClient.post()
                    .uri("/internal/v1/waiting-room/reservation-terminal-events/apply")
                    .headers(this::addHeaders)
                    .body(event)
                    .retrieve()
                    .toBodilessEntity();
            return null;
        });
    }

    @Override
    public void cancelWaitingEntriesForClosedQueue(UUID queueId) {
        execute(() -> {
            restClient.post()
                    .uri("/internal/v1/waiting-room/queues/{queueId}/cancel-waiting", queueId)
                    .headers(this::addHeaders)
                    .retrieve()
                    .toBodilessEntity();
            return null;
        });
    }

    private void addHeaders(HttpHeaders headers) {
        addHeaders(headers, null, null);
    }

    private void addHeaders(HttpHeaders headers, String authorizationHeader, String idempotencyKey) {
        headers.set(InternalHeaders.INTERNAL_TOKEN, internalRequestAuth.token());
        if (authorizationHeader != null) {
            headers.set(HttpHeaders.AUTHORIZATION, authorizationHeader);
        }
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

    private record JoinEntryRequest(String participantKey, int quantity) {
    }
}
