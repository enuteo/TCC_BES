package tcc.bes.api_monolito.waitingroom.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import tcc.bes.api_monolito.filter.CorrelationIdFilter;
import tcc.bes.api_monolito.reservation.application.ReservationView;
import tcc.bes.api_monolito.shared.idempotency.IdempotentResult;
import tcc.bes.api_monolito.shared.internal.InternalHeaders;
import tcc.bes.api_monolito.shared.internal.InternalRequestAuth;
import tcc.bes.api_monolito.waitingroom.application.EntryJoinResponse;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class HttpWaitingRoomPortAdapterTest {

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void shouldPropagateInternalTokenCorrelationAndIdempotencyOnJoin() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        HttpWaitingRoomPortAdapter adapter = new HttpWaitingRoomPortAdapter(
                builder,
                new InternalRequestAuth("internal-secret"),
                new ObjectMapper().findAndRegisterModules(),
                "http://waiting-room-service:8080"
        );
        UUID queueId = UUID.randomUUID();
        UUID entryId = UUID.randomUUID();

        MDC.put(CorrelationIdFilter.CORRELATION_ID_MDC_KEY, "corr-waiting-room");

        server.expect(requestTo("http://waiting-room-service:8080/internal/v1/waiting-room/queues/" + queueId + "/entries"))
                .andExpect(header(InternalHeaders.INTERNAL_TOKEN, "internal-secret"))
                .andExpect(header(CorrelationIdFilter.CORRELATION_ID_HEADER, "corr-waiting-room"))
                .andExpect(header("Idempotency-Key", "entry-key"))
                .andExpect(content().json("""
                        {
                          "participantKey": "participant-1",
                          "quantity": 1
                        }
                        """))
                .andRespond(withSuccess("""
                        {
                          "id": "%s",
                          "queueId": "%s",
                          "state": "WAITING",
                          "quantity": 1,
                          "position": 0,
                          "sequence": 1,
                          "createdAt": "2026-06-29T12:00:00Z",
                          "entryToken": "entry-token"
                        }
                        """.formatted(entryId, queueId), MediaType.APPLICATION_JSON));

        IdempotentResult<EntryJoinResponse> result = adapter.join(queueId, "participant-1", 1, "entry-key");

        assertThat(result.statusCode()).isEqualTo(200);
        assertThat(result.body().id()).isEqualTo(entryId);
        assertThat(result.body().entryToken()).isEqualTo("entry-token");
        server.verify();
    }

    @Test
    void shouldForwardAuthorizationAndIdempotencyOnConfirm() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        HttpWaitingRoomPortAdapter adapter = new HttpWaitingRoomPortAdapter(
                builder,
                new InternalRequestAuth("internal-secret"),
                new ObjectMapper().findAndRegisterModules(),
                "http://waiting-room-service:8080"
        );
        UUID reservationId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();
        UUID entryId = UUID.randomUUID();

        server.expect(requestTo("http://waiting-room-service:8080/internal/v1/waiting-room/reservations/" + reservationId + "/confirm"))
                .andExpect(header(InternalHeaders.INTERNAL_TOKEN, "internal-secret"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer entry-token"))
                .andExpect(header("Idempotency-Key", "confirm-key"))
                .andRespond(withSuccess("""
                        {
                          "id": "%s",
                          "resourceId": "%s",
                          "entryId": "%s",
                          "quantity": 1,
                          "expiresAt": "2026-06-29T12:01:00Z",
                          "state": "CONFIRMED",
                          "createdAt": "2026-06-29T12:00:00Z",
                          "confirmedAt": "2026-06-29T12:00:10Z",
                          "cancelledAt": null,
                          "expiredAt": null
                        }
                        """.formatted(reservationId, resourceId, entryId), MediaType.APPLICATION_JSON));

        IdempotentResult<ReservationView> result = adapter.confirmReservation(
                reservationId,
                "Bearer entry-token",
                "confirm-key"
        );

        assertThat(result.statusCode()).isEqualTo(200);
        assertThat(result.body().state()).isEqualTo("CONFIRMED");
        assertThat(result.body().entryId()).isEqualTo(entryId);
        server.verify();
    }
}
