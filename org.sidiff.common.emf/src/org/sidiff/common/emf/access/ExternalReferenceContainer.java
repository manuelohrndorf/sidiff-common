package org.sidiff.common.emf.access;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.resource.Resource;

/**
 * Simple container class for external links.
 * 
 * @author kehrer
 */
public class ExternalReferenceContainer {

	/**
	 * external links: resource -> package registry
	 */
	private List<ExternalReference> registryReferences;

	/**
	 * external links: resource -> another resource (within the same resource
	 * set)
	 */
	private List<ExternalReference> resourceSetReferences;

	/**
	 * The actually referenced Registry models.
	 */
	private Set<Resource> referencedRegistryModels;

	/**
	 * The actually referenced ResourceSet models.
	 */
	private Set<Resource> referencedResourceSetModels;

	public ExternalReferenceContainer(
			List<ExternalReference> registryReferences,
			List<ExternalReference> resourceSetReferences) {

		this.registryReferences = new ArrayList<>(registryReferences);
		this.resourceSetReferences = new ArrayList<>(resourceSetReferences);

		this.referencedRegistryModels = new HashSet<>();
		findResources(referencedRegistryModels, registryReferences);

		this.referencedResourceSetModels = new HashSet<>();
		findResources(referencedResourceSetModels, resourceSetReferences);
	}

	public List<ExternalReference> getRegistryReferences() {
		return Collections.unmodifiableList(registryReferences);
	}

	public List<ExternalReference> getResourceSetReferences() {
		return Collections.unmodifiableList(resourceSetReferences);
	}

	public Set<Resource> getReferencedRegistryModels() {
		return Collections.unmodifiableSet(referencedRegistryModels);
	}

	public Set<Resource> getReferencedResourceSetModels() {
		return Collections.unmodifiableSet(referencedResourceSetModels);
	}

	/**
	 * Extract the actually referenced Resources from the external references
	 * and add them to importedModels.
	 * 
	 * @param importedModels
	 * @param externalReferences
	 */
	private static void findResources(Set<Resource> importedModels, List<ExternalReference> externalReferences) {
		for (ExternalReference externalReference : externalReferences) {
			Resource resource = externalReference.getExternalResource();
			if (resource != null) {
				importedModels.add(resource);
			}
		}
	}
}
