package tcc.bes.api_monolito.shared.internal;

import org.junit.jupiter.api.Test;
import tcc.bes.api_monolito.shared.error.ApiException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InternalRequestAuthTest {

    @Test
    void shouldRejectMissingOrInvalidToken() {
        InternalRequestAuth auth = new InternalRequestAuth("expected-token");

        assertThatThrownBy(() -> auth.require(null))
                .isInstanceOf(ApiException.class)
                .hasMessage("Internal service token is invalid.");

        assertThatThrownBy(() -> auth.require("wrong-token"))
                .isInstanceOf(ApiException.class)
                .hasMessage("Internal service token is invalid.");
    }

    @Test
    void shouldAcceptExpectedToken() {
        new InternalRequestAuth("expected-token").require("expected-token");
    }
}
