package tcc.bes.api_monolito.shared.observability;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DomainMetrics {

    private final MeterRegistry meterRegistry;
    private final JdbcTemplate jdbcTemplate;

    public DomainMetrics(
            MeterRegistry meterRegistry,
            JdbcTemplate jdbcTemplate,
            @Value("${app.module.waiting-room.enabled:true}") boolean waitingRoomEnabled,
            @Value("${app.module.reservation.enabled:true}") boolean reservationEnabled
    ) {
        this.meterRegistry = meterRegistry;
        this.jdbcTemplate = jdbcTemplate;
        if (waitingRoomEnabled) {
            Gauge.builder("waitingroom_entries_waiting", this, DomainMetrics::countWaitingEntries)
                    .description("Current number of waiting entries")
                    .register(meterRegistry);
        }
        if (reservationEnabled) {
            Gauge.builder("reservation_holds_active", this, DomainMetrics::countActiveHolds)
                    .description("Current number of active holds")
                    .register(meterRegistry);
            Gauge.builder("reservation_stock_units", this, metrics -> metrics.sumStock("available_quantity"))
                    .tag("state", "available")
                    .description("Aggregated resource stock units")
                    .register(meterRegistry);
            Gauge.builder("reservation_stock_units", this, metrics -> metrics.sumStock("held_quantity"))
                    .tag("state", "held")
                    .description("Aggregated resource stock units")
                    .register(meterRegistry);
            Gauge.builder("reservation_stock_units", this, metrics -> metrics.sumStock("confirmed_quantity"))
                    .tag("state", "confirmed")
                    .description("Aggregated resource stock units")
                    .register(meterRegistry);
        }
    }

    public void entryCreated() {
        meterRegistry.counter("waitingroom_entries_created", "result", "created").increment();
    }

    public void entryCompleted(String state) {
        meterRegistry.counter("waitingroom_entries_completed", "state", state.toLowerCase()).increment();
    }

    public void workerCycle(String result) {
        meterRegistry.counter("waitingroom_worker_cycles", "result", result).increment();
    }

    public void reservationCreated() {
        meterRegistry.counter("reservation_holds_created", "result", "held").increment();
    }

    public void reservationCompleted(String state) {
        meterRegistry.counter("reservation_completed", "state", state.toLowerCase()).increment();
    }

    private double countWaitingEntries() {
        return count("SELECT COUNT(*) FROM entries WHERE state = 'WAITING'");
    }

    private double countActiveHolds() {
        return count("SELECT COUNT(*) FROM reservations WHERE state = 'HELD'");
    }

    private double sumStock(String column) {
        Number value = jdbcTemplate.queryForObject("SELECT COALESCE(SUM(" + column + "), 0) FROM resources", Number.class);
        return value == null ? 0 : value.doubleValue();
    }

    private double count(String sql) {
        Number value = jdbcTemplate.queryForObject(sql, Number.class);
        return value == null ? 0 : value.doubleValue();
    }
}
