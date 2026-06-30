package tcc.bes.api_monolito.shared.persistence;

import java.sql.Timestamp;
import java.time.Instant;

public final class JdbcTimestamps {

    private JdbcTimestamps() {
    }

    public static Timestamp timestamp(Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }
}
