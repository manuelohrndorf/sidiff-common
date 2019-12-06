package org.sidiff.common.collections;

import java.util.Objects;

/**
 * <p>An ordered, immutable pair of two objects of generic types.</p>
 *  <p>An ordered pair is not equal to its inverse pair:<br>
 * <code>&lt;first, second&gt; != &lt;second, first&gt;</code>.</p>
 * @author rmueller
 *
 * @param <T> the type of the first object
 * @param <S> the type of the second object
 */
public final class Pair<T,S> extends AbstractPair<T,S> {

	private Pair(T first, S second) {
		super(first, second);
	}

	@Override
	public String toString() {
		return "Pair[" + getFirst() + ", " + getSecond() + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(getFirst(), getSecond());
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
		return Objects.equals(getFirst(), other.getFirst())
				&& Objects.equals(getSecond(), other.getSecond());
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
