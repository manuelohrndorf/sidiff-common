package org.sidiff.core.annotators;

import java.util.Collection;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.sidiff.common.emf.EMFAdapter;
import org.sidiff.common.emf.annotation.AnnotateableElement;
import org.sidiff.core.annotation.Annotator;

/**
 * This abstract annotator composes a path for a given Object. The path is computed by concatenation of parent
 * Path and some Segment data, provided by concrete PathAnnotators. 
 *  * 
 */
public abstract class AbstractPathAnnotator extends Annotator {

	public static final String PATH_DELIMITER = "/";
	
	public AbstractPathAnnotator(EPackage documentType, String annotationKey, String parameter, EClass acceptedType, Collection<String> requiredAnnotations) {
		super(documentType, annotationKey, parameter, acceptedType, requiredAnnotations, ExecutionOrder.PRE);
	}

	@Override
	protected Object computeAnnotationValue(EObject object) {
		StringBuffer path = new StringBuffer();
		
		EObject container = object.eContainer();
		if (container != null) {
			AnnotateableElement ae_parent = EMFAdapter.INSTANCE.adapt(object.eContainer(), AnnotateableElement.class);
			if(ae_parent.hasAnnotation(getAnnotationKey())){
				// Extend Path
				path.append(ae_parent.getAnnotation(getAnnotationKey(), String.class));
			} else {
				// New partition
				container.eClass().getName();
				path.append(PATH_DELIMITER);
			}
			
			path.append(PATH_DELIMITER);
			path.append(object.eContainmentFeature().getName());
			// Die indexposition hier mit einzubeziehen ist zu restriktiv, da u.U
			// durchaus erlaubte verschiebung dazu fuehren, dass korrespondierende
			// objekte nicht gefunden werden. Umgekehrt nutzt der hash matcher spaeter
			// den index bereits aus um ggf. mehrdeutigkeiten aufzuloesen!
		} else {
			// No Container, root Object!
			path.append(getDocumentType().getNsURI());
			path.append(PATH_DELIMITER);
		}
		
		path.append(PATH_DELIMITER);
		path.append(getPathSegment(object));
		
		return path.toString();
	}
	
	protected abstract String getPathSegment(EObject object);

	}
