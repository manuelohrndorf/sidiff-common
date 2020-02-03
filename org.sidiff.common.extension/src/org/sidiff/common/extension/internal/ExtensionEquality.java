package org.sidiff.common.extension.internal;

import java.util.Objects;
import java.util.function.BiPredicate;

import org.sidiff.common.extension.IExtension;

/**
 * @author rmueller
 */
public class ExtensionEquality<T extends IExtension> implements BiPredicate<T,T> {

	private static final ExtensionEquality<? extends IExtension> INSTANCE = new ExtensionEquality<>();

	// type safe wrapper around singleton instance
	@SuppressWarnings("unchecked")
	public static <T extends IExtension> ExtensionEquality<T> getInstance() {
		return (ExtensionEquality<T>)INSTANCE;
	}

	@Override
	public boolean test(T lhs, T rhs) {
		return lhs == rhs || lhs != null && rhs != null && Objects.equals(lhs.getKey(), rhs.getKey());
	}
}