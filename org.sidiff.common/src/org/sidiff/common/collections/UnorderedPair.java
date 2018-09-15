package org.sidiff.common.collections;

import java.util.Objects;

/**
 * <p>An unordered, immutable pair of two objects of generic types.</p>
 * <p>An unordered pair is equal to its inverse pair:<br>
 * <code>&lt;first, second&gt; == &lt;second, first&gt;</code>.</p>
 * @author Robert Müller
 *
 * @param <T> the type of the first object
 * @param <S> the type of the second object
 */
public final class UnorderedPair<T,S> extends AbstractPair<T,S> {

	private UnorderedPair(T first, S second) {
		super(first, second);
	}

	@Override
	public int hashCode() {
		// The hash code of <A,B> must equal that of <B,A>
		// so we use the commutative multiplication to
		// mix the two "ordered" hash codes.
		return Objects.hash(getFirst(), getSecond())
				* Objects.hash(getSecond(), getFirst());
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(!(obj instanceof UnorderedPair<?,?>)) {
			return false;
		}
		UnorderedPair<?,?> other = (UnorderedPair<?,?>) obj;
		return (Objects.equals(getFirst(), other.getFirst())
					&& Objects.equals(getSecond(), other.getSecond()))
				|| Objects.equals(getFirst(), other.getSecond())
					&& Objects.equals(getSecond(), other.getFirst());
	}

	/**
	 * Creates a new unordered Pair consisting of the two objects.
	 * @param first the first object
	 * @param second the second object
	 * @return new unordered pair of <code>&lt;first, second&gt;</code>
	 * (equal to <code>&lt;second, first&gt;</code>)
	 */
	public static <T,S> UnorderedPair<T,S> of(T first, S second) {
		return new UnorderedPair<>(first, second);
	}
}
