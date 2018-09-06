package org.sidiff.common.collections;

import java.util.Objects;

/**
 * An immutable pair of two objects of generic types.
 * @author Robert Müller
 *
 * @param <T> the type of the first object
 * @param <S> the type of the second object
 */
public final class Pair<T,S> {

	private final T first;
	private final S second;

	private Pair(T first, S second) {
		this.first = first;
		this.second = second;
	}

	/**
	 * @return the first object
	 */
	public T getFirst() {
		return first;
	}

	/**
	 * @return the second object
	 */
	public S getSecond() {
		return second;
	}

	@Override
	public int hashCode() {
		return Objects.hash(first, second);
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(!(obj instanceof Pair<?,?>)) {
			return false;
		}
		Pair<?,?> other = (Pair<?,?>) obj;
		return Objects.equals(first, other.first)
				&& Objects.equals(second, other.second);
	}

	/**
	 * Creates a new Pair consisting of the two objects.
	 * @param first the first object
	 * @param second the second object
	 * @return new pair of &lt;first, second&gt;
	 */
	public static <T,S> Pair<T,S> of(T first, S second) {
		return new Pair<>(first, second);
	}
}
