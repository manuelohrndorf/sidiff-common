package org.sidiff.common.emf.modelstorage;

import java.io.IOException;
import java.util.*;

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.URIHandlerImpl;
import org.eclipse.emf.ecore.xmi.impl.URIHandlerImpl.PlatformSchemeAware;
import org.eclipse.emf.ecore.xmi.impl.XMLParserPoolImpl;
import org.sidiff.common.exceptions.SiDiffRuntimeException;
import org.sidiff.common.logging.LogEvent;
import org.sidiff.common.logging.LogUtil;

/**
 * <p>A resource set implementation with additional utility functions
 * to load and save EObjects and Resources.</p>
 * <p>The resource set allows registering file extensions so that
 * XmiIds/UUIDs will be generated for resources with these extensions.</p>
 * <p>The default load options include performance optimizations. The default
 * save options do as well, in addition to deresolving URIs to platform URIs if possible,
 * and recording dangling references instead of throwing exceptions.</p>
 * @author rmueller
 */
public class SiDiffResourceSet extends ResourceSetImpl {

	/**
	 * Creates a new resource set with default options.
	 */
	public static SiDiffResourceSet create() {
		return new SiDiffResourceSet();
	}

	/**
	 * Creates a new resource set with default options that
	 * uses XmiIds for models with the given file extensions.
	 */
	public static SiDiffResourceSet create(String ...extensions) {
		SiDiffResourceSet resourceSet = new SiDiffResourceSet();
		resourceSet.registerXmiIdResourceExtensions(extensions);
		return resourceSet;
	}


	private Map<Object, Object> saveOptions = new HashMap<>();
	private boolean logErrors = true;
	private boolean logWarnings = false;

	protected SiDiffResourceSet() {
		initLoadOptions(getLoadOptions());
		initSaveOptions(getSaveOptions());
		setURIResourceMap(new HashMap<>());
	}

	protected void initLoadOptions(Map<Object, Object> options) {
		// Generic caching to improve load/save
		options.put(XMLResource.OPTION_CONFIGURATION_CACHE, Boolean.TRUE);

		// Attachment to resource must _not_ be deferred, or else some objects (e.g. UML Type Literals)
		// end up without a resource when loaded.
		// This option used to work with Ecore and UML alike, so this is likely due to a new bug in EMF or UML.
		//options.put(XMLResource.OPTION_DEFER_ATTACHMENT, Boolean.TRUE);

		options.put(XMLResource.OPTION_DEFER_IDREF_RESOLUTION, Boolean.TRUE);

		//We do NOT use caching due to errors in case of forward references in large resources.
		options.put(XMLResource.OPTION_USE_PARSER_POOL, new XMLParserPoolImpl(false));

		options.put(XMLResource.OPTION_USE_XML_NAME_TO_FEATURE_MAP, new HashMap<>());

		// Unknown features must be recorded, because some valid UML models cannot be loaded
		// otherwise because of a FeatureNotFoundException "Feature 'bodyCondition' not found.".
		options.put(XMLResource.OPTION_RECORD_UNKNOWN_FEATURE, Boolean.TRUE);
	}

	protected void initSaveOptions(Map<Object, Object> options) {
		// Generic caching to improve load/save
		options.put(XMLResource.OPTION_CONFIGURATION_CACHE, Boolean.TRUE);

		// Lookup cache to improves performance when saving multiple documents with the same type
		options.put(XMLResource.OPTION_USE_CACHED_LOOKUP_TABLE, new ArrayList<>());

		// Only save resource if changed
		options.put(XMLResource.OPTION_SAVE_ONLY_IF_CHANGED, XMLResource.OPTION_SAVE_ONLY_IF_CHANGED_MEMORY_BUFFER);

		// The ExtendedMetaData must be set when using the Cached Lookup Table and/or Configuration Cache,
		// else this cache might store Lookup tables which do not have their respective ExtendedMetaData set,
		// resulting in a NullPointerException in XMLSaveImpl$Lookup.getDocumentRoot(XMLSaveImpl.java:2801)
		// when this Lookup is reused to save a resource that uses ExtendedMetaData.
		options.put(XMIResource.OPTION_EXTENDED_META_DATA, ExtendedMetaData.INSTANCE);

		// Handle dangling references by recording them in Resource.getErrors()
		options.put(XMLResource.OPTION_PROCESS_DANGLING_HREF, XMLResource.OPTION_PROCESS_DANGLING_HREF_RECORD);

		options.put(XMIResource.OPTION_SCHEMA_LOCATION, Boolean.TRUE);
		options.put(XMIResource.OPTION_URI_HANDLER, new FileToPlatformResourceDeresolve());
	}

	/**
	 * Registers the given file extensions in this resource set's factory
	 * registry to create resources with generated XmiIds/UUIDs for them.
	 * @param extensions the extensions to register
	 */
	public void registerXmiIdResourceExtensions(String ...extensions) {
		Map<String,Object> factories = getResourceFactoryRegistry().getExtensionToFactoryMap();
		for(String extension : extensions) {
			factories.put(extension, XmiIdResourceFactoryImpl.INSTANCE);
		}
	}

	/**
	 * Unregisters the given file extensions in this resource set's factory
	 * registry, if they were previously mapped to create resources with
	 * generated XmiIds/UUIDs.
	 * @param extensions the extensions to unregister
	 */
	public void unregisterXmiIdResourceExtensions(String ...extensions) {
		Map<String,Object> factories = getResourceFactoryRegistry().getExtensionToFactoryMap();
		for(String extension : extensions) {
			factories.remove(extension, XmiIdResourceFactoryImpl.INSTANCE);
		}
	}

	/**
	 * Returns a modifiable map of the {@link Resource#save(Map) save options}
	 * that this resource set uses.
	 * @return save options map
	 */
	public Map<Object, Object> getSaveOptions() {
		return saveOptions;
	}

	/**
	 * Saves the Resource that contains the given EObject.
	 * @param eObject the object to save
	 * @throws IllegalArgumentException if the object is not contained in a resource,
	 * or it is not contained in this resource set
	 */
	public void saveEObject(EObject eObject) {
		saveEObjects(Collections.singleton(eObject));
	}

	/**
	 * Saves the Resources that contains the given EObjects.
	 * Each resource is saved at most once.
	 * @param eObjects the objects to save
	 * @throws IllegalArgumentException if any object is not contained in a resource,
	 * or it is not contained in this resource set
	 */
	public void saveEObjects(Collection<? extends EObject> eObjects) {
		Set<Resource> savedResources = new HashSet<>();
		for(EObject eObject : eObjects) {
			Assert.isLegal(eObject != null, "Cannot save null EObjects");
			Resource resource = eObject.eResource();
			if(resource == null) {
				throw new IllegalArgumentException("Root is not contained in a resource: " + eObject);
			} else if(savedResources.add(resource)) {
				saveResource(resource);
			}
		}
	}

	/**
	 * Saves the given resource using this resource set's save options.
	 * The resource must be contained in this resource set.
	 * @param resource the resource to save
	 * @throws IllegalArgumentException if the resource is not contained in this resource set
	 * @throws SiDiffRuntimeException if saving failed
	 */
	public void saveResource(Resource resource) {
		Assert.isLegal(resource.getResourceSet() == this, "The resource is not contained in this resource set");
		try {
			resource.getErrors().clear();
			resource.getWarnings().clear();
			resource.save(getSaveOptions());
			if(isLogErrors()) {
				for(Diagnostic diag : resource.getErrors()) {
					LogUtil.log(LogEvent.ERROR, "Saved resource has error: " + resource.getURI(), diag);
				}
			}
			if(isLogWarnings()) {
				for(Diagnostic diag : resource.getWarnings()) {
					LogUtil.log(LogEvent.WARNING, "Saved resource has warning: " + resource.getURI(), diag);
				}
			}
		} catch (IOException e) {
			throw new SiDiffRuntimeException("Could not save " + resource, "Saving resource failed", e);
		}
	}

	/**
	 * Saves all resources of this resource set, except empty resources with errors,
	 * which are the result of having proxy references to deleted resources.
	 */
	public void saveAllResources() {
		for(Resource resource : getResources()) {
			if(!resource.getContents().isEmpty() || resource.getErrors().isEmpty()) {
				// Ignore empty resources with errors. Those usually remain after deleting a resource externally.
				// Subsequent fixing of proxy references leaves an empty resource with a "File does not exist" error in the set.
				saveResource(resource);
			}
		}
	}

	/**
	 * Saves the given root object to a new resource at the given URI.
	 * @param root the root object
	 * @param uri the URI
	 */
	public void saveEObjectAs(EObject root, URI uri) {
		internalSaveEObjectsAs(Collections.singleton(root), uri);
	}

	/**
	 * Saves the given root objects to a new resource at the given URI.
	 * @param roots the root objects
	 * @param uri the URI
	 */
	public void saveEObjectsAs(Collection<? extends EObject> roots, URI uri) {
		// copy the collection because it might be resource.getContents(),
		// in which case the modification will fail
		internalSaveEObjectsAs(new ArrayList<>(roots), uri);
	}

	protected void internalSaveEObjectsAs(Collection<? extends EObject> roots, URI uri) {
		Assert.isLegal(roots.stream().noneMatch(Objects::isNull), "Cannot save null EObjects");
		Resource resource = createResource(uri);
		resource.getContents().clear();
		resource.getContents().addAll(roots);
		saveResource(resource);
	}

	/**
	 * Sets the URI of the resource to the given URI and saves the resource.
	 * @param resource the resource
	 * @param uri the new URI
	 */
	public void saveResourceAs(Resource resource, URI uri) {
		resource.setURI(uri);
		saveResource(resource);
	}

	@Override
	public Resource getResource(URI uri, boolean loadOnDemand) {
		Resource resource = super.getResource(uri, loadOnDemand);
		if(resource == null) {
			return null;
		}
		if(isLogErrors()) {
			for(Diagnostic diag : resource.getErrors()) {
				LogUtil.log(LogEvent.ERROR, "Loaded resource has error: " + resource.getURI(), diag);
			}
		}
		if(isLogWarnings()) {
			for(Diagnostic diag : resource.getWarnings()) {
				LogUtil.log(LogEvent.WARNING, "Loaded resource has warning: " + resource.getURI(), diag);
			}
		}
		return resource;
	}

	/**
	 * Loads the model with the given URI.
	 * If the URI has a fragment, the EObject with this fragment,
	 * if it matches the given type, is returned.
	 * Else, the first root object of the resource which
	 * matches the given type is returned.
	 * If no suitable object is found, <code>null</code> is returned.
	 * @param uri the URI of the model, with optional fragment
	 * @param type the type of the loaded EObject
	 * @return object of the given type loaded from the resource, <code>null</code> if none
	 */
	public <T extends EObject> T loadEObject(URI uri, Class<T> type) {
		Resource resource = getResource(uri, true);
		if(uri.fragment() != null) {
			EObject object = resource.getEObject(uri.fragment());
			if(type.isInstance(object)) {
				return type.cast(object);
			}
		}
		for(EObject object : resource.getContents()) {
			if(type.isInstance(object)) {
				return type.cast(object);
			}
		}
		return null;
	}

	/**
	 * Loads a model into this resource set and saves it to a different location.
	 * @param source the source model URI
	 * @param destination the target model URI
	 */
	public void copyResource(URI source, URI destination) {
		Resource sourceResource = getResource(source, true);
		saveResourceAs(sourceResource, destination);
	}

	/**
	 * Sets whether errors during resource loading and saving should be logged.
	 * @param logErrors <code>true</code> to log errors, <code>false</code> otherwise
	 */
	public void setLogErrors(boolean logErrors) {
		this.logErrors = logErrors;
	}

	/**
	 * Returns whether errors during resource loading and saving are logged by this resource set.
	 * @return <code>true</code> if errors are logged, <code>false</code> otherwise
	 */
	public boolean isLogErrors() {
		return logErrors;
	}

	/**
	 * Sets whether warnings during resource loading and saving should be logged.
	 * @param logWarnings <code>true</code> to log warnings, <code>false</code> otherwise
	 */
	public void setLogWarnings(boolean logWarnings) {
		this.logWarnings = logWarnings;
	}

	/**
	 * Returns whether warnings during resource loading and saving are logged by this resource set.
	 * @return <code>true</code> if warnings are logged, <code>false</code> otherwise
	 */
	public boolean isLogWarnings() {
		return logWarnings;
	}


	/**
	 * URI will be replaced by the last segment (filename).
	 */
	public static class DeresolveLastSegment extends URIHandlerImpl {
		@Override
		public URI deresolve(URI uri) {
			return URI.createURI(uri.lastSegment());
		}
	}

	/**
	 * URI will be replaced by the shortest relative URI.
	 * @deprecated Use existing {@link PlatformSchemeAware} directly.
	 */
	public static class DeresolveRelative extends PlatformSchemeAware {
		// deprecated
	}

	/**
	 * Deresolves file URIs to platform resource URIs.
	 */
	public static class FileToPlatformResourceDeresolve extends URIHandlerImpl {
		@Override
		public URI deresolve(URI uri) {
			return super.deresolve(EMFStorage.toPlatformURI(uri));
		}
	}
}
