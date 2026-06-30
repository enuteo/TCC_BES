package tcc.bes.api_monolito.identity.application;

import java.util.UUID;

public record ManagerIdentity(UUID id, String username, String displayName) {
}
