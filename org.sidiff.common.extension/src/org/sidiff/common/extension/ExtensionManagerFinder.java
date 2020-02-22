package org.sidiff.common.extension;

import java.lang.reflect.Field;

import org.sidiff.common.extension.internal.ExtensionsPlugin;

/**
 * <p>Helper class to locate an {@link ExtensionManager} for an extension
 * class based on the "MANAGER" naming convention using reflection.</p>
 * <p>Should not be used unless absolutely required because of poor performance when using reflection.</p>
 * @author rmueller
 */
public class ExtensionManagerFinder {

	private ExtensionManagerFinder() {
		throw new AssertionError();
	}

	@SuppressWarnings("unchecked")
	public static <T extends IExtension> ExtensionManager<? extends T> findManager(Class<? super T> extensionClass) {
		try {
			Field mgrField = extensionClass.getDeclaredField("MANAGER");
			Object mgrObject = mgrField.get(null);
			if(mgrObject instanceof ExtensionManager) {
				// There could be more checks here. The generic type is unchecked, but
				// by convention IFooBar.MANAGER should always extend ExtensionManager<IFooBar>.
				return (ExtensionManager<? extends T>)mgrObject;
			}
			throw new IllegalArgumentException("MANAGER member is not an instance of ExtensionManager: " + mgrObject);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | ClassCastException e) {
			ExtensionsPlugin.logError("Could not find extension manager in " + extensionClass, e);
		}
		return null;
	}
}
