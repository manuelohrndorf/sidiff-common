package org.sidiff.core.annotation.impl;

import java.util.*;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.resource.Resource;
import org.sidiff.common.emf.EMFAdapter;
import org.sidiff.common.emf.access.EMFModelAccess;
import org.sidiff.common.emf.annotation.AnnotateableElement;
import org.sidiff.common.exceptions.SiDiffRuntimeException;
import org.sidiff.common.io.IOUtil;
import org.sidiff.common.xml.XMLParser;
import org.sidiff.core.annotation.AnnotationService;
import org.sidiff.core.annotation.Annotator;


public class AnnotationServiceImpl implements AnnotationService {

	public static final String EXECUTED_ANNOATIONS = "org.sidiff.core.annotation.Annotator.executedAnnotations";

	// Holds Annotators by Type
	private Map<String,Map<EClass,Annotator>> annotators = new TreeMap<String, Map<EClass,Annotator>>();
	private Map<String,Set<String>> keyDependencies = new TreeMap<String, Set<String>>();


	@Override
	public void annotate(Resource model) {
		assert(model!=null) : "Missing model (null)";

		// Annotate all
		internal_annotate(model, keyDependencies.keySet());
	}

	@Override
	public void removeAnnotations(Resource model) {
		assert(model!=null) : "Missing model (null)";

		// Remove all
		internal_remove(model,keyDependencies.keySet());

	}

	@Override
	public void annotate(Resource model, Set<String> keySet) {
		if(keyDependencies.keySet().containsAll(keySet)){
			internal_annotate(model, computeTransitiveClosure(keySet));
		} else {
			throw new AnnotationException("Set contains non configured key!"+keySet);
		}
	}

	@Override
	public void removeAnnotations(Resource model, Set<String> keySet) {

		if(keyDependencies.keySet().containsAll(keySet)){
			internal_remove(model,keySet);
		} else {
			throw new AnnotationException("Set contains non configured key!"+keySet);
		}	
	}

	@Override
	public void annotate(Resource model, String key) {

		// Annotate known Key
		if(keyDependencies.containsKey(key)){
			annotate(model, Collections.singleton(key));
		} else {
			throw new IllegalArgumentException("Unknown annotation key "+key);
		}

	}

	@Override
	public void removeAnnotations(Resource model, String key) {
		assert(model!=null&&key!=null) : "Required model/key (null)";

		if(keyDependencies.containsKey(key)){
			internal_remove(model, Collections.singleton(key));
		}
	}

	public Set<String> availableKeys(){

		return this.keyDependencies.keySet();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<String> executedKeys(Resource model) {

		AnnotateableElement aobject = EMFAdapter.INSTANCE.adapt(model, AnnotateableElement.class);	
		return Collections.unmodifiableSet(aobject.getOrCreateAnnotation(EXECUTED_ANNOATIONS, HashSet.class));
	}

	@Override
	public String configure(Object... configData) {

		
		if (configData.length == 1 && configData[0] instanceof String) {

			String configDataString = (String) configData[0];
			
			// Determine how to process input file by file extension
			if(configDataString.endsWith(".xml")) {
				AnnotationConfigurationContentHandler acch = new AnnotationConfigurationContentHandler();
				XMLParser.parseStream(IOUtil.getInputStream(configDataString), acch);
				configureAnnotators(acch.getAnnotators());
				return acch.getDocumentType();
			}
			else if(configDataString.endsWith(".annotation")) {				
				//TODO uncomment when SiCnf Ready
				/*
				try {
					
					//Load the XML file via stream
					AnnotationConfigurationContentHandler acch = new AnnotationConfigurationContentHandler();
					ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
					
					//Get the document from converter
					Document configDoc = AnnotationConverter.convertAnnotationConfig(
							AnnotationConverter.loadAnnotationConfigFromFile(configDataString));
					
					//Transform document into stream
					DOMSource xmlSource = new DOMSource(configDoc);
					Result outputTarget = new StreamResult(outputStream);
					TransformerFactory.newInstance().newTransformer().transform(xmlSource, outputTarget);
					InputStream istream = new ByteArrayInputStream(outputStream.toByteArray());
					
					//Parse the stream to configuration handler
					XMLParser.parseStream(istream, acch);					
					
					configureAnnotators(acch.getAnnotators());
					return acch.getDocumentType();	
					
				} catch (ParserConfigurationException e) {
					e.printStackTrace();
					System.out.println("Error in SiConf Annotation Converter configuration");
				} catch (TransformerConfigurationException e) {
					e.printStackTrace();
				} catch (TransformerException e) {
					e.printStackTrace();
				} catch (TransformerFactoryConfigurationError e) {
					e.printStackTrace();
				}			
			*/
			}
			
		}
		 
		throw new SiDiffRuntimeException("Invalid Configuration data. Need exactly one parameter (the file string of the annotation configuration file), but got the following config. data: ", configData);
	}

	@Override
	public Dictionary<String, String> getProperties() {
		// no special properties
		return null;
	}

	@Override
	public void deconfigure() {

		// Drop Configuration
		this.annotators.clear();
		this.keyDependencies.clear();
	}

	private void configureAnnotators(Collection<Annotator> annotators){
		assert(annotators!=null) : "Nothing to configure (null)";

		for(Annotator annotator : annotators){
			String key = annotator.getAnnotationKey();

			// Eintragen des Annotators
			Map<EClass,Annotator> annotators4key = this.annotators.get(key);
			if(annotators4key==null){
				annotators4key = new HashMap<EClass,Annotator>();
				this.annotators.put(key, annotators4key);
			}

			if(annotators4key.containsKey(annotator.getType())) {
				throw new IllegalArgumentException("Invalid Configuration! Duplicate entry for key "+key+" of type "+annotator.getType().getName());
			}

			annotators4key.put(annotator.getType(),annotator);

			// Eintragen der abbhaengigkeiten
			Set<String> dependencies = this.keyDependencies.get(key);
			if(dependencies==null){
				dependencies = new HashSet<String>();
				this.keyDependencies.put(key, dependencies);
			}
			dependencies.addAll(annotator.getRequiredAnnotations());
		}

		// Check Configuration...
		computeTransitiveClosure(this.keyDependencies.keySet());
	}

	private Set<String> computeTransitiveClosure(Set<String> keys){

		Set<String> closure = new HashSet<String>();

		for(String key : keys){
			computeTransitiveClosure(key, closure, new HashSet<String>());
			closure.add(key);
		}

		return closure;
	}

	private Set<String> computeTransitiveClosure(String key,Set<String> closure,Set<String> visited){

		if(this.keyDependencies.containsKey(key)&&!visited.contains(key)){
			// key is configured and has not been visited before. 
			visited.add(key); // current top down run

			closure.add(key); // add to common closure
			Collection<String> dependencies = this.keyDependencies.get(key);
			for(String dependency : dependencies){
				if(!closure.contains(dependency)){
					// not been visited before -> go down
					computeTransitiveClosure(dependency,closure, visited);
				}
			}
			visited.remove(key); // going up 
		} else {
			throw new AnnotationException("Unaccomplishable key '"+key+"' (non provided requirement or cycle)");
		}

		return closure;
	}

	/**
	 * Interne Methode zum durchfuehren der annotationen. 
	 * 
	 * @param model Das zu annotierende Modell.
	 * @param keySet Die Menge der zu annotierenden Keys. 
	 */
	@SuppressWarnings("unchecked")
	private void internal_annotate(Resource model, Set<String> keys) {

		AnnotateableElement aobject = EMFAdapter.INSTANCE.adapt(model, AnnotateableElement.class);	

		Set<String> providedKeys = aobject.getOrCreateAnnotation(EXECUTED_ANNOATIONS, HashSet.class);
		Set<String> openKeys = new HashSet<String>(keys);

		// perform missing annotations
		openKeys.removeAll(providedKeys);

		while(!openKeys.isEmpty()){

			Set<String> executeable = computeExecuteableKeys(openKeys, providedKeys);
			if(!executeable.isEmpty()){
				AnnotationVisitor visitor = new AnnotationVisitor(this.annotators, executeable);
				openKeys.removeAll(executeable);
				EMFModelAccess.traverse(model, visitor);
				providedKeys.addAll(executeable);
			} else {
				throw new AnnotationException("No more executable Keys '",openKeys,"'");
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void internal_remove(Resource model, Set<String> keys) {

		AnnotateableElement aobject = EMFAdapter.INSTANCE.adapt(model, AnnotateableElement.class);	
		Set<String> computedKeys = aobject.getOrCreateAnnotation(EXECUTED_ANNOATIONS, HashSet.class);

		Set<String> keysToRemove = new HashSet<String>(keys);
		keysToRemove.retainAll(computedKeys); // Nur tatsaechlich berechnete
		computedKeys.removeAll(keysToRemove);

		Set<String> partition = computeTransitiveClosure(computedKeys);
		if(computedKeys.containsAll(partition)){
			// We don't break any dependency, so we can proceed
			RemoveAnnotationVisitor remove = new RemoveAnnotationVisitor(keysToRemove);
			EMFModelAccess.traverse(model, remove);
		} else {
			partition.removeAll(computedKeys);
			throw new AnnotationException("Revokation of '",keys,"' breaks dependencies:",partition);
		}
	}


	private Set<String> computeExecuteableKeys(Set<String> openKeys, Set<String> providedKeys) {

		Set<String> executeable = new HashSet<String>();
		for(String key : openKeys){
			if(providedKeys.containsAll(this.keyDependencies.get(key))){
				executeable.add(key);
			}
		}
		return executeable;
	}
}
