package tcc.bes.api_monolito.reservation.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import tcc.bes.api_monolito.filter.CorrelationIdFilter;
import tcc.bes.api_monolito.reservation.application.ResourceView;
import tcc.bes.api_monolito.shared.internal.InternalHeaders;
import tcc.bes.api_monolito.shared.internal.InternalRequestAuth;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class HttpReservationPortAdapterTest {

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void shouldPropagateInternalTokenAndCorrelationId() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        HttpReservationPortAdapter adapter = new HttpReservationPortAdapter(
                builder,
                new InternalRequestAuth("internal-secret"),
                new ObjectMapper().findAndRegisterModules(),
                "http://reservation-service:8080"
        );
        UUID resourceId = UUID.randomUUID();
        UUID managerId = UUID.randomUUID();

        MDC.put(CorrelationIdFilter.CORRELATION_ID_MDC_KEY, "corr-123");

        server.expect(requestTo("http://reservation-service:8080/internal/v1/resources/" + resourceId + "/internal"))
                .andExpect(header(InternalHeaders.INTERNAL_TOKEN, "internal-secret"))
                .andExpect(header(CorrelationIdFilter.CORRELATION_ID_HEADER, "corr-123"))
                .andRespond(withSuccess("""
                        {
                          "id": "%s",
                          "managerId": "%s",
                          "name": "Resource",
                          "totalQuantity": 10,
                          "availableQuantity": 10,
                          "heldQuantity": 0,
                          "confirmedQuantity": 0,
                          "createdAt": "2026-06-29T12:00:00Z"
                        }
                        """.formatted(resourceId, managerId), MediaType.APPLICATION_JSON));

        ResourceView resource = adapter.getResourceForInternalUse(resourceId);

        assertThat(resource.id()).isEqualTo(resourceId);
        assertThat(resource.managerId()).isEqualTo(managerId);
        assertThat(resource.availableQuantity()).isEqualTo(10);
        server.verify();
    }
}
