package tcc.bes.api_monolito.shared.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientResponseException;
import tcc.bes.api_monolito.dto.ApiErrorDTO;
import tcc.bes.api_monolito.shared.error.ApiException;

public final class RemoteApiErrors {

    private RemoteApiErrors() {
    }

    public static ApiException from(RestClientResponseException ex, ObjectMapper objectMapper) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.BAD_GATEWAY;
        }

        try {
            ApiErrorDTO error = objectMapper.readValue(ex.getResponseBodyAsByteArray(), ApiErrorDTO.class);
            return new ApiException(status, error.getCode(), error.getMessage());
        } catch (Exception ignored) {
            return new ApiException(status, "INTERNAL_SERVICE_ERROR",
                    "Internal service returned an error.");
        }
    }
}
