package tcc.bes.api_monolito.shared.idempotency;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import tcc.bes.api_monolito.shared.error.ApiException;
import tcc.bes.api_monolito.shared.security.HashingService;
import tcc.bes.api_monolito.shared.security.PayloadCipher;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static tcc.bes.api_monolito.shared.persistence.JdbcTimestamps.timestamp;

@Service
public class IdempotencyService {

    private static final int RETENTION_HOURS = 24;

    private final JdbcTemplate jdbcTemplate;
    private final HashingService hashingService;
    private final PayloadCipher payloadCipher;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public IdempotencyService(
            JdbcTemplate jdbcTemplate,
            HashingService hashingService,
            PayloadCipher payloadCipher,
            ObjectMapper objectMapper,
            Clock clock
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.hashingService = hashingService;
        this.payloadCipher = payloadCipher;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    public String requireRequestHash(Object request) {
        try {
            return hashingService.sha256(objectMapper.writeValueAsString(request));
        } catch (Exception ex) {
            throw new IllegalStateException("Could not hash idempotent request", ex);
        }
    }

    public <T> Optional<Replay<T>> findReplay(
            String namespace,
            String actor,
            String idempotencyKey,
            String requestHash,
            Class<T> responseType
    ) {
        String keyHash = requireKeyHash(idempotencyKey);
        String actorHash = hashingService.sha256(actor);

        try {
            StoredRecord record = jdbcTemplate.queryForObject("""
                            SELECT request_hash, response_payload, status_code
                            FROM idempotency_records
                            WHERE namespace = ? AND actor_hash = ? AND key_hash = ?
                            """,
                    (rs, rowNum) -> new StoredRecord(
                            rs.getString("request_hash"),
                            rs.getString("response_payload"),
                            rs.getInt("status_code")
                    ),
                    namespace,
                    actorHash,
                    keyHash
            );

            if (!record.requestHash().equals(requestHash)) {
                throw new ApiException(HttpStatus.CONFLICT, "IDEMPOTENCY_CONFLICT",
                        "The same idempotency key was used with a different request.");
            }

            String json = payloadCipher.decrypt(record.responsePayload());
            return Optional.of(new Replay<>(record.statusCode(), objectMapper.readValue(json, responseType)));
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Could not replay idempotent response", ex);
        }
    }

    public void save(
            String namespace,
            String actor,
            String idempotencyKey,
            String requestHash,
            int statusCode,
            Object response
    ) {
        String keyHash = requireKeyHash(idempotencyKey);
        String actorHash = hashingService.sha256(actor);
        try {
            String encryptedPayload = payloadCipher.encrypt(objectMapper.writeValueAsString(response));
            Instant now = clock.instant();
            jdbcTemplate.update("""
                            INSERT INTO idempotency_records
                                (namespace, actor_hash, key_hash, request_hash, response_payload, status_code, created_at, expires_at)
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                            """,
                    namespace,
                    actorHash,
                    keyHash,
                    requestHash,
                    encryptedPayload,
                    statusCode,
                    timestamp(now),
                    timestamp(now.plus(RETENTION_HOURS, ChronoUnit.HOURS))
            );
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.CONFLICT, "IDEMPOTENCY_RACE",
                    "The idempotent operation is already being processed.");
        }
    }

    private String requireKeyHash(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "IDEMPOTENCY_KEY_REQUIRED",
                    "Idempotency-Key header is required.");
        }
        return hashingService.sha256(idempotencyKey.trim());
    }

    public record Replay<T>(int statusCode, T body) {
    }

    private record StoredRecord(String requestHash, String responsePayload, int statusCode) {
    }
}
