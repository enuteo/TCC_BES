package tcc.bes.api_monolito.reservation.application;

public record HoldDecision(Result result, ReservationView reservation) {

    public static HoldDecision held(ReservationView reservation) {
        return new HoldDecision(Result.HELD, reservation);
    }

    public static HoldDecision waitForRecoverableCapacity() {
        return new HoldDecision(Result.WAIT_FOR_RECOVERABLE_CAPACITY, null);
    }

    public static HoldDecision unfulfillable() {
        return new HoldDecision(Result.UNFULFILLABLE, null);
    }

    public enum Result {
        HELD,
        WAIT_FOR_RECOVERABLE_CAPACITY,
        UNFULFILLABLE
    }
}
