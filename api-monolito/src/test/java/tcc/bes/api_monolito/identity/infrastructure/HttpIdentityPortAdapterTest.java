package tcc.bes.api_monolito.identity.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import tcc.bes.api_monolito.filter.CorrelationIdFilter;
import tcc.bes.api_monolito.identity.application.LoginResult;
import tcc.bes.api_monolito.identity.application.ManagerIdentity;
import tcc.bes.api_monolito.shared.internal.InternalHeaders;
import tcc.bes.api_monolito.shared.internal.InternalRequestAuth;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class HttpIdentityPortAdapterTest {

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void shouldPropagateInternalTokenAndCorrelationIdOnLogin() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        HttpIdentityPortAdapter adapter = new HttpIdentityPortAdapter(
                builder,
                new InternalRequestAuth("internal-secret"),
                new ObjectMapper().findAndRegisterModules(),
                "http://identity-service:8080"
        );
        UUID managerId = UUID.randomUUID();

        MDC.put(CorrelationIdFilter.CORRELATION_ID_MDC_KEY, "corr-identity");

        server.expect(requestTo("http://identity-service:8080/internal/v1/auth/login"))
                .andExpect(header(InternalHeaders.INTERNAL_TOKEN, "internal-secret"))
                .andExpect(header(CorrelationIdFilter.CORRELATION_ID_HEADER, "corr-identity"))
                .andExpect(content().json("""
                        {
                          "username": "admin",
                          "password": "admin123"
                        }
                        """))
                .andRespond(withSuccess("""
                        {
                          "accessToken": "jwt-token",
                          "tokenType": "Bearer",
                          "expiresAt": "2026-06-29T12:00:00Z",
                          "manager": {
                            "id": "%s",
                            "username": "admin",
                            "displayName": "Admin"
                          }
                        }
                        """.formatted(managerId), MediaType.APPLICATION_JSON));

        LoginResult result = adapter.login("admin", "admin123");

        assertThat(result.accessToken()).isEqualTo("jwt-token");
        assertThat(result.manager().id()).isEqualTo(managerId);
        server.verify();
    }

    @Test
    void shouldResolveManagerThroughInternalApi() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        HttpIdentityPortAdapter adapter = new HttpIdentityPortAdapter(
                builder,
                new InternalRequestAuth("internal-secret"),
                new ObjectMapper().findAndRegisterModules(),
                "http://identity-service:8080"
        );
        UUID managerId = UUID.randomUUID();

        server.expect(requestTo("http://identity-service:8080/internal/v1/auth/manager-resolution"))
                .andExpect(header(InternalHeaders.INTERNAL_TOKEN, "internal-secret"))
                .andExpect(content().json("""
                        {
                          "authorizationHeader": "Bearer jwt-token"
                        }
                        """))
                .andRespond(withSuccess("""
                        {
                          "id": "%s",
                          "username": "admin",
                          "displayName": "Admin"
                        }
                        """.formatted(managerId), MediaType.APPLICATION_JSON));

        ManagerIdentity manager = adapter.requireManager("Bearer jwt-token");

        assertThat(manager.id()).isEqualTo(managerId);
        assertThat(manager.username()).isEqualTo("admin");
        server.verify();
    }
}
