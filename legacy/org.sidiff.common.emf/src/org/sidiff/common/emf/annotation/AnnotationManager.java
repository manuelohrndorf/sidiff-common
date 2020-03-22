package org.sidiff.common.emf.annotation;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.sidiff.common.collections.CollectionUtil;
import org.sidiff.common.converter.ConverterUtil;
import org.sidiff.common.emf.EMFAdapter;
import org.sidiff.common.emf.EMFUtil;
import org.sidiff.common.logging.LogEvent;
import org.sidiff.common.logging.LogUtil;
import org.sidiff.common.reflection.ReflectionUtil;
import org.sidiff.common.xml.XMLParser;
import org.sidiff.common.xml.XMLWriter;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class AnnotationManager {
	
	private static final String DOC_TYPE = "/workspaces_eclipse/sidiff_core/org.sidiff.common.emf/resources/org.sidiff.common.emf.annotations.dtd";
//	private static final String DOC_TYPE = "http://pi.informatik.uni-siegen.de/SiDiff/org.sidiff.common.emf.annotationPersistence.dtd";

	private static final String ROOT = "Annotations";
	private static final String OBJECT = "Object";
	private static final String ANNOTATION = "Annotation";
	
	private static final String ATTR_RESOURCE = "Resource";
	private static final String ATTR_ID = "id";
	private static final String ATTR_KEY = "key";
	private static final String ATTR_DATA = "data";
	private static final String ATTR_TYP = "typ";
	
	public static void serialize(Resource resource, String file){
		try (OutputStream outStream = new FileOutputStream(file)) {
			XMLWriter writer = new XMLWriter(outStream);
			writer.initDocument(DOC_TYPE, null, ROOT, Collections.singletonMap(ATTR_RESOURCE, resource.getURI().toString()));
	
			AnnotateableElement element = EMFAdapter.INSTANCE.adapt(resource, AnnotateableElement.class);
			if(!element.getAnnotations().isEmpty()){
				writeAnnotations(writer, element, null);
			}
			
			for(EObject eObject : CollectionUtil.asIterable(resource.getAllContents())){
				element = EMFAdapter.INSTANCE.adapt(eObject, AnnotateableElement.class);
				if(!element.getAnnotations().isEmpty()){
					writeAnnotations(writer, element, EMFUtil.getEObjectID(eObject));
				}
			}
			writer.finishDocument();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void writeAnnotations(XMLWriter writer, AnnotateableElement element, String id){
		
		
		writer.generateStartTag(OBJECT, (id!=null)? Collections.singletonMap(ATTR_ID, id) : null);
		
		
		// ** Serialize Elements
		Map<String, String> xmlAttributes = new LinkedHashMap<String, String>();
		for(String key : element.getAnnotations()){
			Object annotation = element.getAnnotation(key, Object.class);
			xmlAttributes.put(ATTR_KEY, key);
			xmlAttributes.put(ATTR_TYP, annotation.getClass().getName());
			xmlAttributes.put(ATTR_DATA, ConverterUtil.marshal(annotation));
			writer.generateEmptyTag(ANNOTATION, xmlAttributes);
		}
		writer.generateEndTag(OBJECT);
	}
	
	public static void deserialize(final Resource resource, String file){
		try {
			XMLParser.parseXML(new InputSource(new FileInputStream(file)),
					new DefaultHandler(){
				
					AnnotateableElement element = null;
					@Override
					public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
						
						if(localName.equals(OBJECT)){
							String id = attributes.getValue(uri, ATTR_ID);
							element = EMFAdapter.INSTANCE.adapt((id==null)?
									resource : resource.getEObject(id), AnnotateableElement.class);
						} else if(localName.equals(ANNOTATION)){
							assert(element!=null) : "Missing Element to perform annotation! "+attributes;
							handleAnnotation(element, attributes);
						}
					}	
				});
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}		
	}
	
	private static void handleAnnotation(AnnotateableElement element, Attributes attributes){
		String key = attributes.getValue(ATTR_KEY);
		String typeName = attributes.getValue(ATTR_TYP);
		
		Object annotation = null;
		try {
			annotation = ConverterUtil.unmarshal(ReflectionUtil.loadClass(typeName), attributes.getValue(ATTR_DATA));
			element.setAnnotation(key, annotation);
		} catch (Exception e) {
			LogUtil.log(LogEvent.ERROR, String.format("Error while deserializing %s (Exception: %s, Message: %s)", element, e.getClass().getSimpleName(), e.getMessage()));
		}
	}
	
	public static void disposeAnnotations(AnnotateableElement element) {
		for(String annotationKey : element.getAnnotations()){
			element.removeAnnotation(annotationKey);
		}
	}
	
	public static void disposeAnnotations(Resource resource) {
		AnnotateableElement element = EMFAdapter.INSTANCE.adapt(resource, AnnotateableElement.class);
		disposeAnnotations(element);
		
		for(EObject eObject : CollectionUtil.asIterable(resource.getAllContents())) {
			element = EMFAdapter.INSTANCE.adapt(eObject, AnnotateableElement.class);
			disposeAnnotations(element);
		}
	}

}
