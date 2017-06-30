package org.sidiff.common.emf.modelstorage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMLParserPoolImpl;

public class UUIDResource extends XMIIDResourceImpl {
	
	/**
	 * Initialize (unloaded) resource.
	 * 
	 * @param uri
	 */
	public UUIDResource(URI uri) {
		super(uri);
		
		Map<Object, Object> saveOptions = getDefaultSaveOptions();
		saveOptions.put(XMLResource.OPTION_CONFIGURATION_CACHE, Boolean.TRUE);
		saveOptions.put(XMLResource.OPTION_USE_CACHED_LOOKUP_TABLE, new ArrayList<Object>());
		
		Map<Object, Object> loadOptions = getDefaultLoadOptions();
		loadOptions.put(XMLResource.OPTION_DEFER_ATTACHMENT, Boolean.TRUE);
		loadOptions.put(XMLResource.OPTION_DEFER_IDREF_RESOLUTION, Boolean.TRUE);
		loadOptions.put(XMLResource.OPTION_USE_DEPRECATED_METHODS, Boolean.TRUE);
		loadOptions.put(XMLResource.OPTION_USE_PARSER_POOL, new XMLParserPoolImpl());
		loadOptions.put(XMLResource.OPTION_USE_XML_NAME_TO_FEATURE_MAP, new HashMap<Object, Object>());

		// 15.5.2. Caching Intrinsic IDs
		setIntrinsicIDToEObjectMap(new HashMap<String, EObject>());
	}
	
	/**
	 * Initialize and loads the resource into a resource set.
	 * 
	 * @param uri
	 * @param resourceSet
	 */
	public UUIDResource(URI uri, ResourceSet resourceSet) {
		this(uri);
		
		try {
			load(resourceSet.getLoadOptions());
			resourceSet.getResources().add(this);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static UUIDResource createUUIDResource(URI uri){
		UUIDResource resource = new UUIDResource(uri);
		new ResourceSetImpl().getResources().add(resource);
		return resource;
		
	}
	
	@Override
	protected boolean useIDs() {
		return true;
	}
	
	@Override
	protected boolean useUUIDs() {
		return true;
	}
	
	@Override
	protected boolean assignIDsWhileLoading() {
		return true;
	}
	
	@Override
	public String getID(EObject eObject) {		
		String uuid = super.getID(eObject);

		// Generate new UUID:
		if (uuid == null) {
			uuid = EcoreUtil.generateUUID();
			getEObjectToIDMap().put(eObject, uuid);
			getIDToEObjectMap().put(uuid, eObject);
		}
		
		
		return uuid;
	}
	
}
