package org.sidiff.common.emf.modelstorage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.URIHandlerImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLParserPoolImpl;
import org.sidiff.common.exceptions.SiDiffRuntimeException;

/**
 * <p>A resource set implementation with additional utility functions
 * to load and save EObjects and Resources.</p>
 * <p>The resource set allows registering file extensions so that
 * XmiIds/UUIDs will be generated for resources with these extensions.</p>
 * <p>The default load options include performance optimizations. The default
 * save options do as well, in addition to deresolving URIs to platform URIs if possible,
 * and recording dangling references instead of throwing exceptions.</p>
 * @author Robert Müller
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

	protected SiDiffResourceSet() {
		initLoadOptions(getLoadOptions());
		initSaveOptions(getSaveOptions());
		setURIResourceMap(new HashMap<>());
	}

	protected void initLoadOptions(Map<Object, Object> options) {
		options.put(XMLResource.OPTION_DEFER_ATTACHMENT, Boolean.TRUE);
		options.put(XMLResource.OPTION_DEFER_IDREF_RESOLUTION, Boolean.TRUE);
		options.put(XMLResource.OPTION_USE_PARSER_POOL, new XMLParserPoolImpl());
		options.put(XMLResource.OPTION_USE_XML_NAME_TO_FEATURE_MAP, new HashMap<Object, Object>());
	}

	protected void initSaveOptions(Map<Object, Object> options) {
		options.put(XMLResource.OPTION_CONFIGURATION_CACHE, Boolean.TRUE);
		options.put(XMLResource.OPTION_USE_CACHED_LOOKUP_TABLE, new ArrayList<Object>());
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
		Set<Resource> savedResources = new HashSet<>(); // only save each resource at most once
		for(EObject eObject : eObjects) {
			Resource resource = eObject.eResource();
			if(resource == null) {
				throw new IllegalArgumentException("root is not contained in a resource: " + eObject);
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
			resource.save(getSaveOptions());
		} catch (IOException e) {
			throw new SiDiffRuntimeException("Could not save " + resource, "Saving resource failed", e);
		}
	}
	
	/**
	 * Saves all resources of this resource set.
	 */
	public void saveAllResources() {
		for(Resource resource : getResources()) {
			saveResource(resource);
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
	 * URI will be replaced by the last segment
	 */
	public static class DeresolveLastSegment extends URIHandlerImpl {
		@Override
		public URI deresolve(URI uri) {
			return URI.createURI(uri.lastSegment());
		}
	}

	/**
	 * URI will be replaced by the shortest relative URI.
	 */
	public static class DeresolveRelative extends URIHandlerImpl {
		@Override
		public URI deresolve(URI uri){
			return !uri.isPlatform() || (uri.segmentCount() > 0 && baseURI.segmentCount() > 0 && uri.segment(0).equals(baseURI.segment(0)))
					? super.deresolve(uri) : uri;
		}
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
