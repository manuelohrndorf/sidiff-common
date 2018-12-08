package org.sidiff.common.aggregation;

import java.util.Arrays;

/**
 * <p>An <code>Aggregation</code> aggregates a sample of input values.</p>
 * <p>The following aggregations are supported: {@link #MIN}, {@link #MAX},
 * {@link #SUM}, {@link #AVG}, {@link #MEDIAN}, {@link #VARIANCE}, {@link #STDDEV}.</p>
 * @author Robert Müller
 * @see AggregationUtil
 */
public enum Aggregation {

	/**
	 * This {@link Aggregation} finds the smallest of the input values.
	 * @see Math#min(double, double)
	 */
	MIN {
		@Override
		public double aggregate(double[] values) {
			double min = values[0];
			for(int i = 1; i < values.length; i++) {
				// using java.lang.Math because it handles special double values better
				min = Math.min(min, values[i]);
			}
			return min;
		}
	},

	/**
	 * This {@link Aggregation} finds the greatest of the input values.
	 * @see Math#max(double, double)
	 */
	MAX {
		@Override
		public double aggregate(double[] values) {
			double max = values[0];
			for(int i = 1; i < values.length; i++) {
				// using java.lang.Math because it handles special double values better
				max = Math.max(max, values[i]);
			}
			return max;
		}
	},

	/**
	 * This {@link Aggregation} calculates the sum of the input values.
	 */
	SUM {
		@Override
		public double aggregate(double[] values) {
			double sum = 0.0;
			for(double value : values) {
				sum += value;
			}
			return sum;
		}
	},

	/**
	 * This {@link Aggregation} calculates the average/mean of the input values.
	 * This is defined as the sum of all values divided by the total number of values.
	 */
	AVG {
		@Override
		public double aggregate(double[] values) {
			return SUM.aggregate(values) / values.length;
		}
	},

	/**
	 * This {@link Aggregation} calculates the median of the input values.
	 * The input values are sorted in ascending order.
	 * For an odd number of input values, the median is the middle value.
	 * For an even number of input values, the median is the mean of the two middle values.
	 */
	MEDIAN {
		@Override
		public double aggregate(double[] values) {
			Arrays.sort(values);
			if(values.length % 2 == 0) {
				// for arrays with even length, the median here is defined
				// as the mean value of the two middle values
				return (values[values.length/2-1] + values[values.length/2]) / 2.0;
			}
			// for arrays with odd length, the median is the middle value
			return values[values.length/2];
		}
	},

	/**
	 * <p>This {@link Aggregation} calculates the variance <code>Var(X)</code> given the
	 * sample of values for the random variable <code>X</code>. The probability for
	 * each input value is calculated depending on how often it occurs in the sample.</p>
	 * <p>This is equivalent to the {@link #STDDEV standard deviation} squared.</p>
	 */
	VARIANCE {
		@Override
		public double aggregate(double[] values) {
			
			final double avg = AVG.aggregate(values);
			double[] squaredDiffs = new double[values.length];
			int i = 0;
			for(double value : values) {
				double squaredDiff = Math.pow((value - avg), 2);
				squaredDiffs[i] = squaredDiff;
				i++;
			}
		
			double variance = AVG.aggregate(squaredDiffs);
			return variance;
		}
	},
	
	/**
	 * <p>This {@link Aggregation} calculates the coeffecient of varation</p>.
	 */
	CV {
		@Override
		public double aggregate(double[] values) {
			
			final double stddev = STDDEV.aggregate(values);
			final double avg = AVG.aggregate(values);
			
			double cv = stddev / avg;
			if(Double.isNaN(cv))
				cv = 0.0;
			System.out.println("CV:" + stddev);
			return cv;
		}
	},

	/**
	 * <p>This {@link Aggregation} calculates the discrete standard deviation given
	 * the sample of values for the random variable.</p>
	 * <p>This is equivalent to the (positive) square-root of the {@link #VARIANCE variance}.</p>
	 */
	STDDEV {
		@Override
		public double aggregate(double[] values) {
			return Math.sqrt(VARIANCE.aggregate(values));
		}
	};

	/**
	 * Aggregates the given input values and returns the result of the aggregation.
	 * @param values the input values, must neither be <code>null</code>, nor empty
	 * @return result of the aggregation
	 */
	public abstract double aggregate(double values[]);
}
