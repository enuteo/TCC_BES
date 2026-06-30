package tcc.bes.api_monolito.identity.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import tcc.bes.api_monolito.filter.CorrelationIdFilter;
import tcc.bes.api_monolito.identity.application.IdentityPort;
import tcc.bes.api_monolito.identity.application.LoginResult;
import tcc.bes.api_monolito.identity.application.ManagerIdentity;
import tcc.bes.api_monolito.shared.error.ApiException;
import tcc.bes.api_monolito.shared.internal.InternalHeaders;
import tcc.bes.api_monolito.shared.internal.InternalRequestAuth;
import tcc.bes.api_monolito.shared.internal.RemoteApiErrors;

import java.util.Optional;

@Component
@ConditionalOnProperty(name = "app.adapters.identity.mode", havingValue = "http")
public class HttpIdentityPortAdapter implements IdentityPort {

    private final RestClient restClient;
    private final InternalRequestAuth internalRequestAuth;
    private final ObjectMapper objectMapper;

    public HttpIdentityPortAdapter(
            RestClient.Builder restClientBuilder,
            InternalRequestAuth internalRequestAuth,
            ObjectMapper objectMapper,
            @Value("${app.internal.identity.base-url}") String baseUrl
    ) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.internalRequestAuth = internalRequestAuth;
        this.objectMapper = objectMapper;
    }

    @Override
    public LoginResult login(String username, String password) {
        return execute(() -> restClient.post()
                .uri("/internal/v1/auth/login")
                .headers(this::addHeaders)
                .body(new LoginRequest(username, password))
                .retrieve()
                .body(LoginResult.class));
    }

    @Override
    public ManagerIdentity requireManager(String authorizationHeader) {
        return execute(() -> restClient.post()
                .uri("/internal/v1/auth/manager-resolution")
                .headers(this::addHeaders)
                .body(new ResolveManagerRequest(authorizationHeader))
                .retrieve()
                .body(ManagerIdentity.class));
    }

    @Override
    public Optional<ManagerIdentity> tryManager(String authorizationHeader) {
        try {
            return Optional.of(requireManager(authorizationHeader));
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
    }

    private void addHeaders(HttpHeaders headers) {
        headers.set(InternalHeaders.INTERNAL_TOKEN, internalRequestAuth.token());
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

    private record LoginRequest(String username, String password) {
    }

    private record ResolveManagerRequest(String authorizationHeader) {
    }
}
