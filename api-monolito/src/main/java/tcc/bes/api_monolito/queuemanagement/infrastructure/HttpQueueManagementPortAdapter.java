package tcc.bes.api_monolito.queuemanagement.infrastructure;

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
import tcc.bes.api_monolito.queuemanagement.application.QueueJoinSlot;
import tcc.bes.api_monolito.queuemanagement.application.QueueManagementPort;
import tcc.bes.api_monolito.queuemanagement.application.QueueView;
import tcc.bes.api_monolito.shared.error.ApiException;
import tcc.bes.api_monolito.shared.idempotency.IdempotentResult;
import tcc.bes.api_monolito.shared.internal.InternalHeaders;
import tcc.bes.api_monolito.shared.internal.InternalRequestAuth;
import tcc.bes.api_monolito.shared.internal.RemoteApiErrors;

import java.util.List;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "app.adapters.queue-management.mode", havingValue = "http")
public class HttpQueueManagementPortAdapter implements QueueManagementPort {

    private final RestClient restClient;
    private final InternalRequestAuth internalRequestAuth;
    private final ObjectMapper objectMapper;

    public HttpQueueManagementPortAdapter(
            RestClient.Builder restClientBuilder,
            InternalRequestAuth internalRequestAuth,
            ObjectMapper objectMapper,
            @Value("${app.internal.queue-management.base-url}") String baseUrl
    ) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.internalRequestAuth = internalRequestAuth;
        this.objectMapper = objectMapper;
    }

    @Override
    public IdempotentResult<QueueView> createQueue(
            ManagerIdentity manager,
            String name,
            UUID resourceId,
            int maxQuantityPerParticipant,
            int holdDurationSeconds,
            int workerIntervalMs,
            int maxBatchSize,
            String idempotencyKey
    ) {
        return execute(() -> {
            ResponseEntity<QueueView> response = restClient.post()
                    .uri("/internal/v1/queues")
                    .headers(headers -> addHeaders(headers, idempotencyKey))
                    .body(new CreateQueueRequest(
                            manager.id(),
                            name,
                            resourceId,
                            maxQuantityPerParticipant,
                            holdDurationSeconds,
                            workerIntervalMs,
                            maxBatchSize
                    ))
                    .retrieve()
                    .toEntity(QueueView.class);
            return new IdempotentResult<>(response.getStatusCode().value(), response.getBody());
        });
    }

    @Override
    public List<QueueView> listQueues(ManagerIdentity manager) {
        return execute(() -> restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/internal/v1/queues")
                        .queryParam("managerId", manager.id())
                        .build())
                .headers(this::addHeaders)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                }));
    }

    @Override
    public QueueView getQueue(ManagerIdentity manager, UUID queueId) {
        return execute(() -> restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/internal/v1/queues/{queueId}")
                        .queryParam("managerId", manager.id())
                        .build(queueId))
                .headers(this::addHeaders)
                .retrieve()
                .body(QueueView.class));
    }

    @Override
    public QueueView getQueueInternal(UUID queueId) {
        return execute(() -> restClient.get()
                .uri("/internal/v1/queues/{queueId}/internal", queueId)
                .headers(this::addHeaders)
                .retrieve()
                .body(QueueView.class));
    }

    @Override
    public QueueView open(ManagerIdentity manager, UUID queueId) {
        return transition(manager, queueId, "open");
    }

    @Override
    public QueueView pause(ManagerIdentity manager, UUID queueId) {
        return transition(manager, queueId, "pause");
    }

    @Override
    public QueueView resume(ManagerIdentity manager, UUID queueId) {
        return transition(manager, queueId, "resume");
    }

    @Override
    public QueueView close(ManagerIdentity manager, UUID queueId) {
        return transition(manager, queueId, "close");
    }

    @Override
    public QueueJoinSlot allocateSequenceForJoin(UUID queueId, int quantity) {
        return execute(() -> restClient.post()
                .uri("/internal/v1/queues/{queueId}/join-slots", queueId)
                .headers(this::addHeaders)
                .body(new AllocateJoinSlotRequest(quantity))
                .retrieve()
                .body(QueueJoinSlot.class));
    }

    @Override
    public List<QueueView> listOpenQueues() {
        return execute(() -> restClient.get()
                .uri("/internal/v1/queues/open")
                .headers(this::addHeaders)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                }));
    }

    private QueueView transition(ManagerIdentity manager, UUID queueId, String action) {
        return execute(() -> restClient.post()
                .uri(uriBuilder -> uriBuilder.path("/internal/v1/queues/{queueId}/" + action)
                        .queryParam("managerId", manager.id())
                        .build(queueId))
                .headers(this::addHeaders)
                .retrieve()
                .body(QueueView.class));
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

    private record CreateQueueRequest(
            UUID managerId,
            String name,
            UUID resourceId,
            int maxQuantityPerParticipant,
            int holdDurationSeconds,
            int workerIntervalMs,
            int maxBatchSize
    ) {
    }

    private record AllocateJoinSlotRequest(int quantity) {
    }
}
