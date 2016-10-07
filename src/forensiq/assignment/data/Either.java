package forensiq.assignment.data;

import java.util.function.Function;

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

    /**
     * Only apply {@code next} if {@code this} is successful., otherwise re-wrap the failure.
     * */
    public <S2> Either<S2, F> fmap(Function<S, S2> next) {
        if (is_success) {
            return Either.succeed(next.apply(_success));
        } else {
            return Either.fail(_failure);
        }
    }
}
