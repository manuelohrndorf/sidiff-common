package org.sidiff.common.stringresolver;

import org.eclipse.emf.ecore.EObject;
import org.sidiff.common.extension.ITypedExtension;
import org.sidiff.common.extension.TypedExtensionManager;

/**
 * A StringResolver is responsible for calculating an appropriate textual
 * representation of an object of a specific document type.
 * 
 * @author cpietsch
 * 
 */
public interface IStringResolver extends ITypedExtension {

	Description<IStringResolver> DESCRIPTION = Description.of(IStringResolver.class,
			"org.sidiff.common.stringresolver.string_resolver_extension", "string_resolver", "string_resolver");

	/**
	 * Manager for all registered string resolvers.
	 */
	TypedExtensionManager<IStringResolver> MANAGER = new TypedExtensionManager<IStringResolver>(DESCRIPTION);

	/**
	 * Resolves the textual representation of the given object
	 * 
	 * @param eObject
	 *            an object of which the textual representation shall be
	 *            resolved, not <code>null</code>
	 * @return a string representation of the object
	 */
	String resolve(EObject eObject);

	/**
	 * Resolves a qualified textual representation of the given object
	 * 
	 * @param eObject
	 *            an object of which the qualified textual representation shall
	 *            be resolved, not <code>null</code>
	 * @return a qualified string representation of the object
	 */
	String resolveQualified(EObject eObject);
}
