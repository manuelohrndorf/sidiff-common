package org.sidiff.common.util;

/**
 * Utility class for basic time measurements. Usage example:
 * <pre>
 * long timeTaken = StopWatch.measureNano(() -> {
 * 	// code to measure
 * })
 * </pre>
 * @author rmueller
 */
public class StopWatch {

	public static long measureNano(Runnable runnable) {
		long start = System.nanoTime();
		runnable.run();
		return System.nanoTime() - start;
	}

	public static long measureMilli(Runnable runnable) {
		return measureNano(runnable) / 1_000_000;
	}
}
