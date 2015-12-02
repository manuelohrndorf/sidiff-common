package org.sidiff.common.henshin;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.henshin.model.Rule;
import org.eclipse.emf.henshin.model.Unit;

public class HenshinUnitAnalysis {

	/**
	 * Returns all rules which are contained/nested in the given unit.
	 * 
	 * @param unit
	 *            the unit containing the rules.
	 * @return all rules of the unit.
	 */
	public static List<Rule> getRules(Unit unit) {
		List<Rule> rules = new ArrayList<Rule>();

		for (Unit subUnit : unit.getSubUnits(true)) {
			if (subUnit instanceof Rule) {
				rules.add((Rule) subUnit);
			}
		}

		return rules;
	}
}
