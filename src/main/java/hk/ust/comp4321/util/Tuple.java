package hk.ust.comp4321.util;

/**
 * A record representing a tuple of two values.
 * @param left The first element of the tuple
 * @param right The second element of the tuple
 * @param <L> The type of the first element of the tuple
 * @param <R> The type of the second element of the tuple
 */
public record Tuple<L, R>(L left, R right) {}
