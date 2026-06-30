package tcc.bes.api_monolito.waitingroom;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tcc.bes.api_monolito.support.PostgresIntegrationTestSupport;
import tcc.bes.api_monolito.waitingroom.application.WaitingRoomApplicationService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class WaitingRoomFlowIntegrationTest extends PostgresIntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WaitingRoomApplicationService waitingRoomApplicationService;

    @Test
    void shouldGrantAndConfirmHoldWithoutBreakingStockInvariant() throws Exception {
        String managerToken = login();
        UUID resourceId = createResource(managerToken, 2);
        UUID queueId = createQueue(managerToken, resourceId, 1, 60, 10);
        openQueue(managerToken, queueId);

        JsonNode entry = join(queueId, "participant-a", 1);
        UUID entryId = UUID.fromString(entry.get("id").asText());
        String entryToken = entry.get("entryToken").asText();

        waitingRoomApplicationService.processQueue(queueId);

        JsonNode grantedEntry = readJson(mockMvc.perform(get("/api/v1/entries/{entryId}", entryId)
                        .header("Authorization", bearer(entryToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("HOLD_GRANTED"))
                .andExpect(jsonPath("$.reservationId").exists())
                .andReturn().getResponse().getContentAsString());

        UUID reservationId = UUID.fromString(grantedEntry.get("reservationId").asText());
        mockMvc.perform(post("/api/v1/reservations/{reservationId}/confirm", reservationId)
                        .header("Authorization", bearer(entryToken))
                        .header("Idempotency-Key", "confirm-" + UUID.randomUUID()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("CONFIRMED"));

        mockMvc.perform(get("/api/v1/resources/{resourceId}", resourceId)
                        .header("Authorization", bearer(managerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalQuantity").value(2))
                .andExpect(jsonPath("$.availableQuantity").value(1))
                .andExpect(jsonPath("$.heldQuantity").value(0))
                .andExpect(jsonPath("$.confirmedQuantity").value(1));
    }

    @Test
    void shouldPreserveFifoWhenEarlierHoldMayReleaseCapacity() throws Exception {
        String managerToken = login();
        UUID resourceId = createResource(managerToken, 1);
        UUID queueId = createQueue(managerToken, resourceId, 1, 60, 10);
        openQueue(managerToken, queueId);

        JsonNode first = join(queueId, "participant-fifo-1", 1);
        JsonNode second = join(queueId, "participant-fifo-2", 1);
        String firstToken = first.get("entryToken").asText();
        String secondToken = second.get("entryToken").asText();
        UUID secondEntryId = UUID.fromString(second.get("id").asText());

        waitingRoomApplicationService.processQueue(queueId);

        JsonNode firstAfterWorker = readJson(mockMvc.perform(get("/api/v1/entries/{entryId}", first.get("id").asText())
                        .header("Authorization", bearer(firstToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("HOLD_GRANTED"))
                .andReturn().getResponse().getContentAsString());

        mockMvc.perform(get("/api/v1/entries/{entryId}", secondEntryId)
                        .header("Authorization", bearer(secondToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("WAITING"))
                .andExpect(jsonPath("$.reservationId").doesNotExist());

        UUID firstReservationId = UUID.fromString(firstAfterWorker.get("reservationId").asText());
        mockMvc.perform(delete("/api/v1/reservations/{reservationId}", firstReservationId)
                        .header("Authorization", bearer(firstToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("CANCELLED"));

        waitingRoomApplicationService.processQueue(queueId);

        mockMvc.perform(get("/api/v1/entries/{entryId}", secondEntryId)
                        .header("Authorization", bearer(secondToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("HOLD_GRANTED"))
                .andExpect(jsonPath("$.reservationId").exists());
    }

    private String login() throws Exception {
        JsonNode response = readJson(mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "admin",
                                  "password": "admin123"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
        assertThat(response.get("accessToken").asText()).isNotBlank();
        return response.get("accessToken").asText();
    }

    private UUID createResource(String managerToken, int totalQuantity) throws Exception {
        JsonNode response = readJson(mockMvc.perform(post("/api/v1/resources")
                        .header("Authorization", bearer(managerToken))
                        .header("Idempotency-Key", "resource-" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Resource %s",
                                  "totalQuantity": %d
                                }
                                """.formatted(UUID.randomUUID(), totalQuantity)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());
        return UUID.fromString(response.get("id").asText());
    }

    private UUID createQueue(String managerToken, UUID resourceId, int maxQuantity, int holdSeconds, int batchSize) throws Exception {
        JsonNode response = readJson(mockMvc.perform(post("/api/v1/queues")
                        .header("Authorization", bearer(managerToken))
                        .header("Idempotency-Key", "queue-" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Queue %s",
                                  "resourceId": "%s",
                                  "maxQuantityPerParticipant": %d,
                                  "holdDurationSeconds": %d,
                                  "workerIntervalMs": 1000,
                                  "maxBatchSize": %d
                                }
                                """.formatted(UUID.randomUUID(), resourceId, maxQuantity, holdSeconds, batchSize)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());
        return UUID.fromString(response.get("id").asText());
    }

    private void openQueue(String managerToken, UUID queueId) throws Exception {
        mockMvc.perform(post("/api/v1/queues/{queueId}/open", queueId)
                        .header("Authorization", bearer(managerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("OPEN"));
    }

    private JsonNode join(UUID queueId, String participantKey, int quantity) throws Exception {
        return readJson(mockMvc.perform(post("/api/v1/queues/{queueId}/entries", queueId)
                        .header("Idempotency-Key", "entry-" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "participantKey": "%s",
                                  "quantity": %d
                                }
                                """.formatted(participantKey, quantity)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());
    }

    private JsonNode readJson(String json) throws Exception {
        return objectMapper.readTree(json);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
