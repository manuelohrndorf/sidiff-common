package org.sidiff.common.emf.access.path.conditions;

import java.util.function.Predicate;

import org.eclipse.emf.ecore.EObject;
import org.sidiff.common.emf.access.path.impl.EMFPathStepImpl;

/**
 * Abstract definition of a condition. Conditions can be used
 * to filter the elements that are traversed by a path.
 * @author wenzel
 *
 */
public abstract class EMFCondition implements Predicate<EObject> {

	protected String argumentString = null;
	
	public EMFCondition(String argumentString) {
		this.argumentString = argumentString;
	}
	
	public abstract void initCondition(EMFPathStepImpl step);
	
	@Override
	public abstract boolean test(EObject item);

}
