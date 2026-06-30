package tcc.bes.api_monolito.shared.idempotency;

public record IdempotentResult<T>(int statusCode, T body) {
}
