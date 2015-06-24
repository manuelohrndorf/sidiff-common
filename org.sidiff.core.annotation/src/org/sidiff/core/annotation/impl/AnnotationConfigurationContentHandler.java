package org.sidiff.core.annotation.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.ecore.*;
import org.sidiff.common.emf.access.EMFMetaAccess;
import org.sidiff.common.emf.exceptions.UnknownDocumentTypeException;
import org.sidiff.common.exceptions.SiDiffRuntimeException;
import org.sidiff.common.util.ReflectionUtil;
import org.sidiff.core.annotation.Annotator;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class AnnotationConfigurationContentHandler implements ContentHandler {

	private static final String OPERATION_PREFIX = "org.sidiff.core.annotators";

	private static final String SEPERATOR = ",";	

	private static final String ELEM_ANNOTATIONS = "Annotations";	
	private static final String ATT_DOCTYPE = "documentType";

	private static final String ELEM_ANNOTATION = "Annotation";
	private static final String ATT_NODETYPE = "nodeType";

	private static final String ELEM_SYNTH_ATTR = "SyntheticAttribute";
	private static final String ATT_SA_OPERATION = "operation";
	private static final String ATT_SA_NAME = "attributeName";
	private static final String ATT_SA_PARAMETER = "parameter";
	private static final String ATT_SA_REQUIRES = "requires";	

	private ArrayList<Annotator> annotators;
	
	private EPackage documentType;

	private EClassifier currentClassifier = null;

	public ArrayList<Annotator> getAnnotators() {
		return annotators;
	}

	public String getDocumentType() {
		return documentType.getNsURI();
	}

	@Override
	public void startDocument() throws SAXException {
		annotators = new ArrayList<Annotator>();		
		documentType = null;
	}

	@Override
	public void endDocument() throws SAXException {
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		if (localName.equalsIgnoreCase(ELEM_ANNOTATIONS)) {
			processGlobalSettings(atts);
		} else if (localName.equalsIgnoreCase(ELEM_ANNOTATION)) {
			prepareAnnotation(atts);
		} else if (localName.equalsIgnoreCase(ELEM_SYNTH_ATTR)) {
			processAttribute(atts);
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {	
		if (localName.equalsIgnoreCase(ELEM_ANNOTATION)) {
			finishAnnotation();
		} 
	}

	private void prepareAnnotation(Attributes atts) {
		String currentClassifierName = atts.getValue(ATT_NODETYPE);
		
		if (currentClassifierName != null && !"".equals(currentClassifierName)) {
			EClassifier classifier = EMFMetaAccess.getMetaObjectByName(documentType.getNsURI(), currentClassifierName);
			if (classifier == null) {
				if(currentClassifierName.equals("EObject")){
					currentClassifier= EcorePackage.eINSTANCE.getEObject();
				} else {
					throw new SiDiffRuntimeException("Illegal classifier " + currentClassifierName);
				}
			} else {
				this.currentClassifier = classifier;
			}
		}
	}

	private void processAttribute(Attributes atts) {

		String attributeName = atts.getValue(ATT_SA_NAME);
		String className = atts.getValue(ATT_SA_OPERATION);
		String parameter = atts.getValue(ATT_SA_PARAMETER);
		String requires = atts.getValue(ATT_SA_REQUIRES);
		
		if (attributeName != null && !"".equals(attributeName) && className != null && !"".equals(className)) {

			// List<EClassifier> acceptedNodeTypes = new ArrayList<EClassifier>(this.currentClassifiers);
			List<String> requiredAttributes = null;

			// Required Attributes
			if (requires != null && !"".equalsIgnoreCase(requires)) {
				requiredAttributes = new ArrayList<String>();
				for (String attribute : requires.split(SEPERATOR)) {
					if (!requiredAttributes.add(attribute)) {
						throw new SiDiffRuntimeException("Duplicate required Attribute! " + attribute);
					}
				}
			} else {
				requiredAttributes = Collections.emptyList();
			}

			// Classname
			if (className.indexOf(".") == -1) {
				className = OPERATION_PREFIX + "." + className;
			}
			// Instantiate annotator and add it to internal list
			annotators.add(ReflectionUtil.createInstance(className,Annotator.class, documentType, attributeName, parameter, currentClassifier, requiredAttributes));
			
		} else {
			throw new SiDiffRuntimeException(this, "Synthetic Attribute must have attributes 'attributeName' and 'operation'!");
		}
	}

	private void processGlobalSettings(Attributes atts) {
		String documentType = atts.getValue(ATT_DOCTYPE);
		if (documentType != null && !"".equalsIgnoreCase(documentType)) {			
			if (EPackage.Registry.INSTANCE.getEPackage(documentType)!=null){
				this.documentType = EPackage.Registry.INSTANCE.getEPackage(documentType);
			} else {
				throw new UnknownDocumentTypeException("Document type unknown ",documentType);
			}
		} else {
			throw new SiDiffRuntimeException("Missing Documenttype!");
		}
	}

	private void finishAnnotation() {
		currentClassifier = null;
	}

	// ////////////////////////////////////////////////////////////////////////////////

	public void characters(char[] ch, int start, int length) throws SAXException {
	}

	public void endPrefixMapping(String prefix) throws SAXException {
	}

	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
	}

	public void processingInstruction(String target, String data) throws SAXException {
	}

	public void setDocumentLocator(Locator locator) {
	}

	public void skippedEntity(String name) throws SAXException {
	}

	public void startPrefixMapping(String prefix, String uri) throws SAXException {
	}

}
