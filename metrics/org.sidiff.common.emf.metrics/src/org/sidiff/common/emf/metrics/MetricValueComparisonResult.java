package org.sidiff.common.emf.metrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MetricValueComparisonResult {

	private final List<Object> originObjects;
	private final List<Object> changedObjects;
	private final double numericOffset;
	private final ChangeJudgement changeJudgement;

	MetricValueComparisonResult(ComparisonType comparisonType, List<Object> originObjects, List<Object> changedObjects) {
		this.originObjects = new ArrayList<>(originObjects);
		this.changedObjects = new ArrayList<>(changedObjects);
		this.numericOffset = deriveNumbericOffset(originObjects, changedObjects);
		this.changeJudgement = deriveChangeJudgement(comparisonType, numericOffset);
	}

	private static double deriveNumbericOffset(List<Object> originObjects, List<Object> changedObjects) {
		if(originObjects.size() == 1 && changedObjects.size() == 1) {
			Object originObj = originObjects.get(0);
			Object changedObj = changedObjects.get(0);
			if(originObj instanceof Number && changedObj instanceof Number) {
				double originDouble = ((Number)originObj).doubleValue();
				double changedDouble = ((Number)changedObj).doubleValue();
				return changedDouble - originDouble;
			}
		}
		return Double.NaN;
	}

	private static ChangeJudgement deriveChangeJudgement(ComparisonType comparisonType, double offset) {
		if(Double.isNaN(offset) || comparisonType == ComparisonType.UNSPECIFIED) {
			return ChangeJudgement.NONE;
		}
		if(offset < 0) {
			return comparisonType == ComparisonType.LOWER_IS_BETTER ? ChangeJudgement.GOOD : ChangeJudgement.BAD;
		} else if(offset > 0) {
			return comparisonType == ComparisonType.HIGHER_IS_BETTER ? ChangeJudgement.GOOD : ChangeJudgement.BAD;
		} else {
			return ChangeJudgement.UNCHANGED;
		}
	}

	public List<Object> getOriginObjects() {
		return Collections.unmodifiableList(originObjects);
	}

	public List<Object> getChangedObjects() {
		return Collections.unmodifiableList(changedObjects);
	}

	public double getNumericOffset() {
		return numericOffset;
	}

	public String getNumericOffsetAsString() {
		if(Double.isNaN(numericOffset)) {
			return "<uncomparable>";
		}
		if(numericOffset > 0) {
			return "+" + numericOffset;
		} else if(numericOffset < 0) {
			return "" + numericOffset;				
		}
		return "± 0.0";
	}

	public ChangeJudgement getChangeJudgement() {
		return changeJudgement;
	}

	@Override
	public String toString() {
		return "ComparisonResult[origin=" + MetricsUtil.getLabel(originObjects)
			+ ", changed=" + MetricsUtil.getLabel(changedObjects)
			+ ", numericOffset=" + getNumericOffset()
			+ ", judgement=" + getChangeJudgement() + "]";
	}
}