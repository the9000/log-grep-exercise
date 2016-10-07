package forensiq.assignment.data;

/**
 * The trivial 2-tuple class.
 * Many implementations exist. Let's not depend on them for simplicity's sake.
 */
public class Pair<A, B> {
    public final A first;
    public final B second;

    public Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }
}
