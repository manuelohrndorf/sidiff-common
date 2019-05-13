package org.sidiff.common.aggregation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.sidiff.common.experiments.ExperimentalUtil;
import org.sidiff.common.statistics.StatisticsUtil;
import org.sidiff.common.statistics.StatisticsUtil.StatisticType;

/**
 * The class <code>AggregationUtil</code> provides utility methods for aggregating statistics
 * contained in {@link StatisticsUtil} and runs contained in {@link ExperimentalUtil}
 * by using an {@link Aggregation}.
 * @author Robert Müller
 * @see Aggregation
 */
public final class AggregationUtil {

	// class cannot be instantiated
	private AggregationUtil() {
		throw new AssertionError();
	}

	/**
	 * <p>Loads all {@link ExperimentalUtil} from the folder with the given path, aggregates
	 * them using the given {@link Aggregation} and stores the result in the folder.</p>
	 * <p>The path must denote an existing directory. All files contained in it having
	 * the file extension {@link ExperimentalUtil#FILE_EXTENSION} are loaded.</p>
	 * <p>If no such file exists, this method returns <code>null</code> without writing anything.</p>
	 * <p>Otherwise, all loaded {@link ExperimentalUtil} are aggregated using
	 * {@link #aggregateExperiments(Collection, Aggregation)}. The result of this operation is
	 * saved to the folder and also returned.</p>
	 * @param path the path of the folder
	 * @param aggregation the aggregation method
	 * @return result of the aggregation contained in a new {@link ExperimentalUtil},
	 * <code>null</code> if no appropriate file was found in the folder
	 * @throws IOException if some I/O operation failed (deserializing experiments, serializing result)
	 * @throws ClassNotFoundException if the class of a serialized object was not found
	 */
	public static ExperimentalUtil aggregateExperimentsFolder(String path, Aggregation aggregation)
			throws IOException, ClassNotFoundException {

		Assert.isNotNull(path, "path must not be null");
		Assert.isNotNull(aggregation, "aggregation must not be null");
		File directory = new File(path);
		Assert.isLegal(directory.isDirectory(), "file denoted by path is not a directory");

		// load all experiments from the folder
		Collection<ExperimentalUtil> experiments = new ArrayList<>();
		for(File file : directory.listFiles()) {
			// ignore everything that is not a file or doesn't have the .ser extension
			if(!file.isFile() || !file.getName().endsWith("." + ExperimentalUtil.FILE_EXTENSION)) {
				continue;
			}
			experiments.add(ExperimentalUtil.loadExperiment(file.getPath()));
		}
		if(experiments.isEmpty()) {
			// no experiments were found in this folder
			return null;
		}

		// aggregate all experiments and save the result in the folder
		ExperimentalUtil aggregate = aggregateExperiments(experiments, aggregation);
		String outputPath = new File(directory, directory.getName() + "_aggregated_"
				+ aggregation.name() + "." + ExperimentalUtil.FILE_EXTENSION).getPath();
		aggregate.saveExperiment(outputPath);
		return aggregate;
	}

	/**
	 * Aggregates the statistics of all runs of the given {@link ExperimentalUtil}s using
	 * the given {@link Aggregation}. The keys of all runs of all experiments are matched,
	 * and for every keys, all corresponding runs (i.e. {@link StatisticsUtil}) are
	 * aggregated using {@link #aggregateStatistics(Collection, Aggregation)} and stored
	 * in the result {@link ExperimentalUtil} under the same key.
	 * @param experiments the experiments
	 * @param aggregation the aggregation method
	 * @return results of the aggregation contained in a new {@link ExperimentalUtil}
	 */
	public static ExperimentalUtil aggregateExperiments(Collection<ExperimentalUtil> experiments, Aggregation aggregation) {

		Assert.isNotNull(experiments, "experiments must not be null");
		Assert.isNotNull(aggregation, "aggregation must not be null");
		Assert.isLegal(!experiments.isEmpty(), "experiments must not be empty");

		// collect all StatisticsUtil for every run
		Map<String, Collection<StatisticsUtil>> runs = new HashMap<>();
		String experimentName = null;
		final int size = experiments.size();
		for(ExperimentalUtil experiment : experiments) {
			if(experimentName == null) {
				experimentName = experiment.getName();
			}
			for(Map.Entry<String, StatisticsUtil> entry : experiment.getRuns().entrySet()) {
				if(!runs.containsKey(entry.getKey())) {
					runs.put(entry.getKey(), new ArrayList<StatisticsUtil>(size));
				}
				runs.get(entry.getKey()).add(entry.getValue());
			}
		}

		// aggregate the collection of StatisticsUtil for every run and store in another ExperimentalUtil
		ExperimentalUtil aggregate = ExperimentalUtil.createExperimentalUtil(experimentName);
		for(Map.Entry<String, Collection<StatisticsUtil>> run : runs.entrySet()) {
			aggregate.getRuns().put(run.getKey(), aggregateStatistics(run.getValue(), aggregation));
		}
		return aggregate;
	}

	/**
	 * Aggregates all statistics of the given {@link StatisticsUtil}s using the given {@link Aggregation}.
	 * The keys of all statistics that the containers hold are matched and all values for a given key
	 * are aggregated and stored in the result container under the same key.
	 * @param statistics the statistics containers
	 * @param aggregation the aggregation method
	 * @return results of the aggregation contained in a new {@link StatisticsUtil}
	 */
	public static StatisticsUtil aggregateStatistics(Collection<StatisticsUtil> statistics, Aggregation aggregation) {

		Assert.isNotNull(statistics, "statistics must not be null");
		Assert.isNotNull(aggregation, "aggregation must not be null");
		Assert.isLegal(!statistics.isEmpty(), "statistics must not be empty");

		// initialize collection for all values for all keys for all statistic types
		Map<StatisticType, Map<String, Collection<Number>>> collections = new EnumMap<>(StatisticType.class);
		for(StatisticType type : StatisticType.values()) {
			collections.put(type, new HashMap<>());
		}

		// collect values for every key for every type for every StatisticsUtil
		final int size = statistics.size();
		for(StatisticsUtil s : statistics) {
			for(StatisticType type : StatisticType.values()) {
				collectValues(s.getStatistic(type), collections.get(type), size);
			}
		}

		// aggregate values for every key and store results in another StatisticUtil
		StatisticsUtil aggregate = StatisticsUtil.createStatisticsUtil();
		for(StatisticType type : StatisticType.values()) {
			aggregateEntries(collections.get(type), aggregate.getStatistic(type), aggregation);
		}
		return aggregate;
	}

	//
	// internal methods

	static void collectValues(Map<String, Object> entryStatistics,
			Map<String, Collection<Number>> collection, int size) {

		for(Map.Entry<String, Object> entry : entryStatistics.entrySet()) {
			if(!collection.containsKey(entry.getKey())) {
				collection.put(entry.getKey(), new ArrayList<>(size));
			}
			//If value is already a number 
			if(entry.getValue() instanceof Number) {
				collection.get(entry.getKey()).add((Number)entry.getValue());
			}
			//Otherwise we try to convert the value
			else if(entry.getValue() instanceof String) {
				try {
					Number numberValue = Double.parseDouble((String) entry.getValue());
					collection.get(entry.getKey()).add(numberValue);
				} catch (NumberFormatException e) {
					//Nothing to do if not convertible
				}
			}
		}
	}

	static void aggregateEntries(Map<String, Collection<Number>> collection,
			Map<String, Object> aggregate, Aggregation aggregation) {

		for(Map.Entry<String, Collection<Number>> entry : collection.entrySet()) {
			double values[] = new double[entry.getValue().size()];
			int i = 0;
			for(Number value : entry.getValue()) {
				values[i] = value.doubleValue();
				i++;
			}

			aggregate.put(entry.getKey(), aggregation.aggregate(values));
		}
	}
}
