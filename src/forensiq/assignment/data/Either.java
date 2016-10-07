package forensiq.assignment.data;

/**
 * Encode two possible outcomes, success and failure.
 */
public class Either<S, F> {
    private final S _success;
    private final F _failure;
    public final boolean is_success;

    private Either(S success, F failure, boolean is_success) {
        _success = success;
        _failure = failure;
        this.is_success = is_success;
    }

    public static <S, F> Either<S, F> succeed(S result) {
        return new Either<>(result, null, true);
    }
    public static <S, F> Either<S, F> fail(F result) {
        return new Either<>(null, result, false);
    }

    public S asSuccess() {
        assert this.is_success;
        return _success;
    }

    public F asFailure() {
        assert ! this.is_success;
        return _failure;
    }

    @Override
    public String toString() {
        return is_success ? "Success: " + asSuccess() : "Failure: " + asFailure();
    }

}
