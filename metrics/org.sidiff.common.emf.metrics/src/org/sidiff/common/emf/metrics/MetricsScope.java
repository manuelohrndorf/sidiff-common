package org.sidiff.common.emf.metrics;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.sidiff.common.emf.access.EMFModelAccess;

/**
 * The MetricsScope configures for which notifiers the metrics
 * are calculated, given a selected context notifier from which applicable
 * context notifiers are determined by settings which are public attributes
 * of this class.
 * @author rmueller
 */
public class MetricsScope {

	private final Notifier selectedContext;

	/**
	 * Whether to include the ResourceSet containing the selected context.
	 */
	public boolean includeParentResourceSet = true;

	/**
	 * Whether to include the Resource containing the selected context.
	 */
	public boolean includeParentResource = true;

	/**
	 * Whether to include EObjects contained in selected Resources or EObjects.
	 */
	public boolean includeContainedObjects = true;

	public MetricsScope(Notifier selectedContext) {
		this.selectedContext = Objects.requireNonNull(selectedContext);
	}

	public Set<String> getDocumentTypes() {
		return EMFModelAccess.getDocumentTypes(selectedContext);
	}

	public Stream<Notifier> getApplicableContexts(Class<? extends Notifier> contextType) {
		return Stream.of(
				Stream.of(selectedContext),
				getResourceSetContexts(selectedContext),
				getResourceContexts(selectedContext),
				getEObjectContexts(selectedContext))
			.flatMap(s -> s) // combine the different streams into one
			.filter(contextType::isInstance)
			.distinct();
	}

	private Stream<Notifier> getResourceSetContexts(Notifier context) {
		ResourceSet resourceSet = null;
		if(context instanceof ResourceSet) {
			resourceSet = (ResourceSet)context;
		} else if(includeParentResourceSet) {
			resourceSet = EMFModelAccess.getResourceSet(context);			
		}
		return Stream.ofNullable(resourceSet);
	}

	private Stream<Notifier> getResourceContexts(Notifier context) {
		Resource resource = null;
		if(context instanceof Resource) {
			resource = (Resource)context;
		} else if(context instanceof ResourceSet) {
			return ((ResourceSet)context).getResources().stream().flatMap(this::getResourceContexts);
		} else if(includeParentResource) {
			resource = EMFModelAccess.getResource(context);
		}
		if(resource == null) {
			return Stream.empty();
		}
		if(includeContainedObjects) {
			return resource.getContents().stream().flatMap(this::getEObjectContexts);
		}
		return Stream.empty();
	}

	private Stream<Notifier> getEObjectContexts(Notifier context) {
		EObject object = null;
		if(context instanceof EObject) {
			object = (EObject)context;
		}
		if(object == null) {
			return Stream.empty();
		}

		if(includeContainedObjects) {
			return Stream.of(
					Stream.of(context),
					object.eContents().stream().flatMap(this::getEObjectContexts))
				.flatMap(s -> s);
		}
		return Stream.of(context);
	}
}
