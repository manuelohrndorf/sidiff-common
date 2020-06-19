package org.sidiff.common.experiments;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sidiff.common.compat.StatisticsCompatObjectInputStream;
import org.sidiff.common.statistics.StatisticsUtil;

/**
 * Utility class for data management of {@link StatisticsUtil}s. Is can be used for
 * experiments in conjunction with the {@link StatisticsUtil} and the {@link ChartsUtil}
 * @author dreuling
 * @author rmueller
 */
public final class ExperimentalUtil implements Serializable {

	/**
	 * File extension for serialized instances of this class. Without leading dot.
	 */
	public static final String FILE_EXTENSION = "ser";

	private static final long serialVersionUID = 4920634384535018404L;


	/**
	 * Column separator.
	 */
	private static final String COL = ";";
	private static final String LINESEP = System.getProperty("line.separator");

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

	/**
	 * Global singleton
	 */
	private static ExperimentalUtil instance;

	/**
	 * Name of the Experiment
	 */
	private String experimentName;

	/**
	 * Date of the experiment
	 */
	private String date;

	/**
	 * Map of all runs contained in this experiment
	 */
	private Map<String, StatisticsUtil> experimentRuns;

	/**
	 * Constructor
	 *
	 * @param experimentName
	 *            name of the experiment
	 */
	private ExperimentalUtil(String experimentName) {
		this.experimentName = experimentName;
		this.date = getTodayDateFormatted();
		this.experimentRuns = new HashMap<>();
	}

	/**
	 * Helper method for getting the current date
	 *
	 * @return today's date as formatted String
	 */
	private String getTodayDateFormatted() {
		return DATE_FORMAT.format(new Date());
	}

	/**
	 * Returns the singleton ExperimentalUtil instance.
	 * The instance must be initialized with {@link #newInstance(String)} before calling this method.
	 * @return global instance of this util
	 * @throws IllegalStateException if no singleton instance was created yet
	 */
	public static ExperimentalUtil getInstance() {
		if(instance == null) {
			throw new IllegalStateException("No singleton instance was created with newInstance");
		}
		return instance;
	}

	/**
	 * Creates and sets a new global instance of this class.
	 * @param experimentName Name of the experiment
	 * @return new global instance of this util
	 */
	public static ExperimentalUtil newInstance(String experimentName) {
		instance = new ExperimentalUtil(experimentName);
		return instance;
	}

	/**
	 * Creates a new ExperimentalUtil with the given experiment name.
	 * @param experimentName the experiment name
	 * @return newly created ExperimentalUtil
	 */
	public static ExperimentalUtil createExperimentalUtil(String experimentName) {
		return new ExperimentalUtil(experimentName);
	}

	/**
	 * Clears the global ExperimentalUtil instance.
	 */
	public static void clearInstance() {
		instance = null;
	}

	/**
	 * Re-creates the global ExperimentalUtil instance using the current experiment's name.
	 */
	public void recreateInstance() {
		newInstance(experimentName);
	}

	/**
	 * Start an experimental run
	 *
	 * @param experimentRun
	 *            name of the run to start
	 */
	public void startRun(String experimentRun) {

		assert experimentRuns.get(experimentRun) == null : "ExperimentRun " + experimentRun + " already started!";
		StatisticsUtil.getInstance().reset();
		experimentRuns.put(experimentRun, StatisticsUtil.getInstance());

	}

	/**
	 * Stops an previously started experimental run
	 *
	 * @param experimentRun
	 *            name of the run to stop
	 */
	public void stopRun(String experimentRun) {

		assert experimentRuns.get(experimentRun) != null : "ExperimentRun " + experimentRun
				+ " has not been started!";

		// Clone StatisticsUtil and reset it afterwards
		experimentRuns.put(experimentRun, StatisticsUtil.copiedInstance(StatisticsUtil.getInstance()));
		StatisticsUtil.getInstance().reset();
	}

	public void loadRun(String experimentRun) {
		assert experimentRuns.get(experimentRun) != null : "ExperimentRun " + experimentRun + " not found!";
		StatisticsUtil statUtil = experimentRuns.get(experimentRun);
		StatisticsUtil.setInstance(statUtil);
	}

	/**
	 * Generates a chart with axes using many default values for a more clean
	 * execution and less parameters to configure
	 *
	 * @param xAxisLabel
	 *            label of x axis
	 * @param threeD
	 *            whether to use 3D or 2D
	 * @param transposed
	 *            whether to transpose the chart
	 * @param filename
	 *            filename to save the chart under
	 */
	/*
	public void generateChartWithAxes(String xAxisLabel, Boolean threeD, Boolean transposed, String filename) {

		String yaxisSize = "Number of Elements";
		String yaxisTime = "TimeConsumption(ms)";
		String yaxisCount = "Iterations Counted";

		HashMap<String, Boolean> stackedAxes = new HashMap<String, Boolean>();
		stackedAxes.put(yaxisCount, true);
		stackedAxes.put(yaxisTime, true);
		stackedAxes.put(yaxisSize, true);

		HashMap<String, Boolean> logarithmicAxes = new HashMap<String, Boolean>();
		logarithmicAxes.put(yaxisCount, false);
		logarithmicAxes.put(yaxisTime, false);
		logarithmicAxes.put(yaxisSize, false);

		HashMap<String, Boolean> percentageAxes = new HashMap<String, Boolean>();
		percentageAxes.put(yaxisCount, false);
		percentageAxes.put(yaxisTime, false);
		percentageAxes.put(yaxisSize, false);

		HashMap<String, Boolean> showSeriesLabel = new HashMap<String, Boolean>();
		HashMap<String, SeriesWithAxesType> seriesTypes = new HashMap<String, SeriesWithAxesType>();

		for (String runs : experimentRuns.keySet()) {
			for (String series : experimentRuns.get(runs).getCountStatistic().keySet()) {
				showSeriesLabel.put(series, false);
				seriesTypes.put(series, SeriesWithAxesType.LineChart);
			}
			for (String series : experimentRuns.get(runs).getTimeStatistic().keySet()) {
				showSeriesLabel.put(series, false);
				seriesTypes.put(series, SeriesWithAxesType.BarChart);
			}
			for (String series : experimentRuns.get(runs).getSizeStatistic().keySet()) {
				showSeriesLabel.put(series, false);
				seriesTypes.put(series, SeriesWithAxesType.LineChart);
			}
		}

		HashMap<String, Map<String, Number[]>> axisTovalueMap = new HashMap<String, Map<String, Number[]>>();
		HashMap<String, Number[]> sizeValueMap = new HashMap<String, Number[]>();
		HashMap<String, Number[]> timeValueMap = new HashMap<String, Number[]>();
		HashMap<String, Number[]> countValueMap = new HashMap<String, Number[]>();

		// Sort exp Runs
		ArrayList<String> expRuns = new ArrayList<String>(experimentRuns.keySet());
		Collections.sort(expRuns);

		// All experiment runs need to have values for all series
		for (String countSer : experimentRuns.get(expRuns.get(0)).getCountStatistic().keySet()) {
			Number[] countValues = new Number[experimentRuns.size()];
			int i = 0;
			for (String run : expRuns) {
				countValues[i] = (Number) experimentRuns.get(run).getCountStatistic().get(countSer);
				i++;
			}
			countValueMap.put(countSer, countValues);
		}
		for (String timeSer : experimentRuns.get(expRuns.get(0)).getTimeStatistic().keySet()) {
			Number[] timeValues = new Number[experimentRuns.size()];
			int i = 0;
			for (String run : expRuns) {
				timeValues[i] = (Number) experimentRuns.get(run).getTimeStatistic().get(timeSer);
				i++;

			}
			timeValueMap.put(timeSer, timeValues);
		}
		for (String sizeSer : experimentRuns.get(expRuns.get(0)).getSizeStatistic().keySet()) {
			Number[] sizeValues = new Number[experimentRuns.size()];
			int i = 0;
			for (String run : expRuns) {
				sizeValues[i] = (Number) experimentRuns.get(run).getSizeStatistic().get(sizeSer);
				i++;

			}
			sizeValueMap.put(sizeSer, sizeValues);
		}

		if (countValueMap.size() > 0)
			axisTovalueMap.put(yaxisCount, countValueMap);
		if (timeValueMap.size() > 0)
			axisTovalueMap.put(yaxisTime, timeValueMap);
		if (sizeValueMap.size() > 0)
			axisTovalueMap.put(yaxisSize, sizeValueMap);

		ChartsUtil.getInstance().writeChartWithAxes(experimentName, xAxisLabel, threeD, transposed, stackedAxes,
				logarithmicAxes, percentageAxes, showSeriesLabel, expRuns, seriesTypes, axisTovalueMap, filename);

	}
	*/

	/**
	 * Generates a chart with axes. Can be more fine tuned than the other method
	 * name equally.
	 *
	 * @param xAxisLabel
	 *            label of x axis
	 * @param threeD
	 *            whether to use 3D or 2D
	 * @param transposed
	 *            whether to transpose the chart
	 * @param showStatistic
	 *            Map defining which statistics to include in the chart
	 * @param stacked
	 *            Map defining for each statistic type whether to stack the
	 *            series
	 * @param logarithmic
	 *            Map defining for each statistic type whether to use
	 *            logarithmic scale
	 * @param percentage
	 *            Map defining for each statistic type whether to use percentage
	 *            on y-axis
	 * @param showLabels
	 *            Map defining for each statistic type whether to show labels in
	 *            graph
	 * @param seriesType
	 *            Map defining for each statistic type which seriesType shall be
	 *            used
	 * @param filename
	 *            filename to save the chart under
	 */
	/*
	public void generateChartWithAxes(String xAxisLabel, Boolean threeD, Boolean transposed,
			Map<StatisticType, Boolean> showStatistic, Map<StatisticType, Boolean> stacked,
			Map<StatisticType, Boolean> logarithmic, Map<StatisticType, Boolean> percentage,
			Map<StatisticType, Boolean> showLabels, Map<StatisticType, SeriesWithAxesType> seriesType,
			String filename) {

		String yaxisSize = "Number of Elements";
		String yaxisTime = "TimeConsumption(ms)";
		String yaxisCount = "Iterations Counted";

		HashMap<String, Boolean> stackedAxes = new HashMap<String, Boolean>();
		for (StatisticType st : stacked.keySet()) {
			if (st == StatisticType.Count)
				stackedAxes.put(yaxisCount, stacked.get(st));
			if (st == StatisticType.Time)
				stackedAxes.put(yaxisTime, stacked.get(st));
			if (st == StatisticType.Size)
				stackedAxes.put(yaxisSize, stacked.get(st));
		}
		HashMap<String, Boolean> logarithmicAxes = new HashMap<String, Boolean>();
		for (StatisticType st : logarithmic.keySet()) {
			if (st == StatisticType.Count)
				logarithmicAxes.put(yaxisCount, logarithmic.get(st));
			if (st == StatisticType.Time)
				logarithmicAxes.put(yaxisTime, logarithmic.get(st));
			if (st == StatisticType.Size)
				logarithmicAxes.put(yaxisSize, logarithmic.get(st));
		}
		HashMap<String, Boolean> percentageAxes = new HashMap<String, Boolean>();
		for (StatisticType st : percentage.keySet()) {
			if (st == StatisticType.Count) {
				percentageAxes.put(yaxisCount, percentage.get(st));
				if (percentage.get(st))
					yaxisCount += "(%)";
			}
			if (st == StatisticType.Time) {
				percentageAxes.put(yaxisTime, percentage.get(st));
				if (percentage.get(st))
					yaxisTime.replace("ms", "%");
			}
			if (st == StatisticType.Size) {
				percentageAxes.put(yaxisSize, percentage.get(st));
				if (percentage.get(st))
					yaxisSize += "(%)";
			}
		}
		HashMap<String, Boolean> showSeriesLabel = new HashMap<String, Boolean>();
		HashMap<String, SeriesWithAxesType> seriesTypes = new HashMap<String, SeriesWithAxesType>();

		for (StatisticType st : showLabels.keySet()) {
			if (st == StatisticType.Count) {
				for (String runs : experimentRuns.keySet()) {
					for (String series : experimentRuns.get(runs).getCountStatistic().keySet()) {
						showSeriesLabel.put(series, showLabels.get(st));
						seriesTypes.put(series, seriesType.get(st));
					}
				}
			}
			if (st == StatisticType.Time) {
				for (String runs : experimentRuns.keySet()) {
					for (String series : experimentRuns.get(runs).getTimeStatistic().keySet()) {
						showSeriesLabel.put(series, showLabels.get(st));
						seriesTypes.put(series, seriesType.get(st));
					}
				}
			}
			if (st == StatisticType.Size) {
				for (String runs : experimentRuns.keySet()) {
					for (String series : experimentRuns.get(runs).getSizeStatistic().keySet()) {
						showSeriesLabel.put(series, showLabels.get(st));
						seriesTypes.put(series, seriesType.get(st));
					}
				}
			}
		}

		HashMap<String, Map<String, Number[]>> axisTovalueMap = new HashMap<String, Map<String, Number[]>>();
		HashMap<String, Number[]> sizeValueMap = new HashMap<String, Number[]>();
		HashMap<String, Number[]> timeValueMap = new HashMap<String, Number[]>();
		HashMap<String, Number[]> countValueMap = new HashMap<String, Number[]>();

		// Sort exp Runs
		ArrayList<String> expRuns = new ArrayList<String>(experimentRuns.keySet());
		Collections.sort(expRuns);

		// All experiment runs need to have values for all series
		for (String countSer : experimentRuns.get(expRuns.get(0)).getCountStatistic().keySet()) {
			Number[] countValues = new Number[experimentRuns.size()];
			int i = 0;
			for (String run : expRuns) {
				countValues[i] = (Number) experimentRuns.get(run).getCountStatistic().get(countSer);
				i++;
			}
			countValueMap.put(countSer, countValues);
		}
		for (String timeSer : experimentRuns.get(expRuns.get(0)).getTimeStatistic().keySet()) {
			Number[] timeValues = new Number[experimentRuns.size()];
			int i = 0;
			for (String run : expRuns) {
				timeValues[i] = (Number) experimentRuns.get(run).getTimeStatistic().get(timeSer);
				i++;

			}
			timeValueMap.put(timeSer, timeValues);
		}
		for (String sizeSer : experimentRuns.get(expRuns.get(0)).getSizeStatistic().keySet()) {
			Number[] sizeValues = new Number[experimentRuns.size()];
			int i = 0;
			for (String run : expRuns) {
				sizeValues[i] = (Number) experimentRuns.get(run).getSizeStatistic().get(sizeSer);
				i++;

			}
			sizeValueMap.put(sizeSer, sizeValues);
		}

		if (showStatistic.get(StatisticType.Count) && countValueMap.size() > 0)
			axisTovalueMap.put(yaxisCount, countValueMap);
		if (showStatistic.get(StatisticType.Time) && timeValueMap.size() > 0)
			axisTovalueMap.put(yaxisTime, timeValueMap);
		if (showStatistic.get(StatisticType.Size) && sizeValueMap.size() > 0)
			axisTovalueMap.put(yaxisSize, sizeValueMap);

		ChartsUtil.getInstance().writeChartWithAxes(experimentName, xAxisLabel, threeD, transposed, stackedAxes,
				logarithmicAxes, percentageAxes, showSeriesLabel, expRuns, seriesTypes, axisTovalueMap, filename);

	}
*/
	/**
	 * Generates a chart without axes.
	 *
	 * @param experimentRun
	 *            Name of experiment run to use for chart
	 * @param st
	 *            StatisticType to use for the chart
	 * @param threeD
	 *            whether to render the chart in 3D or 2D
	 * @param showLabels
	 *            whether to show labels in the chart
	 * @param seriesType
	 *            defines which series type to use
	 * @param filename
	 *            filename to save the chart under
	 */
	/*
	public void generateChartWithoutAxes(String experimentRun, StatisticType st, Boolean threeD, Boolean showLabels,
			SeriesWithoutAxesType seriesType, String filename) {

		String measurementTitle = null;
		Map<String, Number> valueMap = new HashMap<String, Number>();

		if (st == StatisticType.Count) {
			measurementTitle = "Counted Iterations";
			for (String series : experimentRuns.get(experimentRun).getCountStatistic().keySet()) {
				valueMap.put(series, (Number) experimentRuns.get(experimentRun).getCountStatistic().get(series));
			}
		}
		if (st == StatisticType.Size) {
			measurementTitle = "Number of Elements";
			for (String series : experimentRuns.get(experimentRun).getSizeStatistic().keySet()) {
				valueMap.put(series, (Number) experimentRuns.get(experimentRun).getSizeStatistic().get(series));
			}
		}
		if (st == StatisticType.Time) {
			measurementTitle = "TimeConsumption(ms)";
			for (String series : experimentRuns.get(experimentRun).getTimeStatistic().keySet()) {
				valueMap.put(series, (Number) experimentRuns.get(experimentRun).getTimeStatistic().get(series));
			}
		}

		ChartsUtil.getInstance().writeChartWithoutAxes(experimentName + "\n" + experimentRun, measurementTitle, threeD,
				showLabels, seriesType, valueMap, filename);

	}
*/
	/**
	 * Method for saving current experiment, including - name - experiment runs
	 * (with all statistics included)
	 *
	 * @param filePath
	 *            path to save the experiment under
	 * @throws IOException
	 */
	public void saveExperiment(String filePath) throws IOException {
		try(ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filePath))) {
			out.writeObject(this);
		}
	}

	/**
	 * Method for loading an experiment.
	 *
	 * @param filename
	 *            file to load as experiment
	 * @return instantiated experiment loaded
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static ExperimentalUtil loadExperiment(String filename) throws IOException, ClassNotFoundException {
		try(ObjectInputStream in = new StatisticsCompatObjectInputStream(new FileInputStream(filename))) {
			ExperimentalUtil expUtil = (ExperimentalUtil) in.readObject();
			instance = expUtil;
			return instance;
		}
	}

	/**
	 * Ermittelt den gemeinsamen BIRT-Typ aus einem Object und einem anderen Birt-Typ (optional)
	 * @param o
	 * @param currentType Anderer BIRT-Typ oder null
	 * @return Gemeinsamer Typ (allgemeinster ist String)
	 */
	private String getBirtType(Object o, String currentType) {
		String name = o.getClass().getSimpleName();
		if ("Long".equals(name) || "Integer".equals(name)) {
			name = "Integer";
		} else if ("Float".equals(name) || "Double".equals(name)) {
			name = "Decimal";
		} else if ("Boolean".equals(name)) {
			name = "Boolean";
		} else {
			name = "String";
		}
		if (currentType == null || currentType.equals(name)) {
			return name;
		} else if ("Integer".equals(currentType) && "Decimal".equals(name)
				|| "Decimal".equals(currentType) && "Integer".equals(name)) {
			return "Decimal";
		} else {
			return "String";
		}
	}

	/**
	 * Schreibt das Experiment in eine BIRT-kompatible CSV-Datei (in BIRT m�glicherweise SSV genannt)
	 * Jede Eigenschaft (Key) wird eine Spalte, jedes Experiment eine Zeile.
	 * Spalten, die bei einem Experiment nicht vorhanden sind, bleiben leer
	 * @note Kommt BIRT mit leeren Zellen zurecht?
	 * @param filename
	 * @throws IOException
	 */
	public void writeToCSV(String filename) throws IOException{
		Map<String, String> types= new HashMap<>();
		Set<String> cols = new HashSet<>();

		/* Spalten und deren Typ ermitteln */
		for (Map.Entry<String, StatisticsUtil> run : experimentRuns.entrySet()){
			for (Map.Entry<String, Object> stat : run.getValue().getUnifiedStatistics().entrySet()){
				cols.add(stat.getKey());
				String type = types.get(stat.getKey());
				type = getBirtType(stat.getValue(), type);
				types.put(stat.getKey(), type);
			}
		}
		/* Aus den Set List machen */
		List<String> colList = new ArrayList<>(cols);
		Collections.sort(colList);
		/* In Datei schreiben */
		Path csvPath = Paths.get(filename);
		Files.deleteIfExists(csvPath);
		try (BufferedWriter writer = Files.newBufferedWriter(csvPath, StandardCharsets.UTF_8, StandardOpenOption.CREATE)) {

			/* Namen-Zeile schreiben */
			writer.append("Run");
			for (String col : colList){
				writer.append(COL+col);
			}

			/* Datentypen-Zeile schreiben */
			/*
			sb.append(ROW+"String");
			for (String col : colList){
				sb.append(COL+types.get(col));
			}*/
			/* Experiemnt-Zeile schreiben */
			for (Map.Entry<String, StatisticsUtil> run : experimentRuns.entrySet()){
				writer.append(LINESEP);
				writer.append(run.getKey());
				Map<String, Object> stats = run.getValue().getUnifiedStatistics();
				for (String col : colList){
					Object value=stats.get(col);
					writer.append(COL+(value != null ? value.toString() : ""));
				}
			}
		}
	}

	/**
	 * Dumps the current experiment with its statistics
	 *
	 * @return string dumped
	 */
	public String dump() {
		StringBuilder sb = new StringBuilder();
		sb.append("********************* ").append(this.experimentName)
			.append(" Statistics *********************").append(LINESEP).append(LINESEP);
		for (Map.Entry<String, StatisticsUtil> run : experimentRuns.entrySet()) {
			sb.append("---------- ").append(run.getKey()).append(" ----------").append(LINESEP);
			run.getValue().dump(sb);
			sb.append(LINESEP);
		}
		sb.append(LINESEP).append(LINESEP);
		sb.append("******************************************");
		return sb.toString();
	}

	public Map<String, StatisticsUtil> getRuns(){
		return this.experimentRuns;
	}

	public String getName() {
		return this.experimentName;
	}

	public String getDate() {
		return this.date;
	}
}
