package org.sidiff.common.collections;

/**
 * An abstract pair of two objects of generic types.
 * Subclasses must override {@link #equals(Object)} and
 * {@link #hashCode()} to specify the equality of pairs.
 * @author Robert Müller
 *
 * @param <T> the type of the first object
 * @param <S> the type of the second object
 */
public abstract class AbstractPair<T,S> {

	private final T first;
	private final S second;

	AbstractPair(T first, S second) {
		this.first = first;
		this.second = second;
	}

	/**
	 * @return the first object
	 */
	public final T getFirst() {
		return first;
	}

	/**
	 * @return the second object
	 */
	public final S getSecond() {
		return second;
	}

	@Override
	public abstract int hashCode();

	@Override
	public abstract boolean equals(Object arg0);
}
