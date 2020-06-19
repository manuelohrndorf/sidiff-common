package org.sidiff.common.statistics;

import java.io.*;
import java.util.*;

/**
 * Utility class for counting things or measuring times.
 * @author wenzel
 * @author dreuling
 * @author rmueller
 */
public final class StatisticsUtil implements Serializable {

	/**
	 * Column separator.
	 */
	private static final String COL = ";";

	/**
	 * Row/Line separator.
	 */
	private static final String LINE_SEPERATOR = System.getProperty("line.separator");

	/**
	 *
	 */
	private static final long serialVersionUID = -6455875097937890547L;

	public enum StatisticType {
		Time, Size, Count, Other;
	}

	private static final String STAT_KEY_STARTTIME = "@@STARTOF@@";

	private SortedMap<String, Long> timeStatistic;
	private SortedMap<String, Integer> sizeStatistic;
	private SortedMap<String, Integer> countStatistic;
	private SortedMap<String, Object> otherStatistic;
	private transient boolean enabled = true;

	private static StatisticsUtil instance;

	private StatisticsUtil() {
		this.timeStatistic = new TreeMap<>();
		this.sizeStatistic = new TreeMap<>();
		this.countStatistic = new TreeMap<>();
		this.otherStatistic = new TreeMap<>();
	}

	private StatisticsUtil(
			Map<String, Long> timeStatistic,
			Map<String, Integer> sizeStatistic,
			Map<String, Integer> countStatistic,
			Map<String, Object> otherStatistic) {
		this.timeStatistic = new TreeMap<>(timeStatistic);
		this.sizeStatistic = new TreeMap<>(sizeStatistic);
		this.countStatistic = new TreeMap<>(countStatistic);
		this.otherStatistic = new TreeMap<>(otherStatistic);
	}

	/**
	 * @return {@link StatisticsUtil} the singleton instance.
	 */
	public static StatisticsUtil getInstance() {
		if (instance == null) {
			instance = new StatisticsUtil();
		}
		return instance;
	}

	/**
	 * Sets the singleton instance.
	 * @param instance new instance
	 */
	public static void setInstance(StatisticsUtil instance) {
		StatisticsUtil.instance = Objects.requireNonNull(instance);
	}

	/**
	 * {@link StatisticsUtil} factory.
	 *
	 * @return A new {@link StatisticsUtil}.
	 */
	public static StatisticsUtil createStatisticsUtil() {
		return new StatisticsUtil();
	}

	/**
	 * Creates a new StatisticsUtil containing the given data as other statistics.
	 * @param statisticData the data
	 * @return new statistics util with the given data
	 */
	public static StatisticsUtil createStatisticsUtil(Map<String,Object> statisticData) {
		StatisticsUtil statUtil = new StatisticsUtil();
		statisticData.forEach(statUtil::put);
		return statUtil;
	}

	/**
	 * Copy constructor, using a static method.
	 */
	public static StatisticsUtil copiedInstance(StatisticsUtil statisticsUtil) {
		return new StatisticsUtil(statisticsUtil.getTimeStatistic(), statisticsUtil.getSizeStatistic(),
				statisticsUtil.getCountStatistic(), statisticsUtil.getOtherStatistic());
	}

	/**
	 * Disables the singleton StatisticsUtil, used for performance reason.
	 * @deprecated Use <code>getInstance().setEnabled(false)</code>
	 * to disable the singleton StatisticsUtil object instead.
	 * The enabled-state is no longer global for all instances.
	 */
	public static void disable() {
		getInstance().setEnabled(false);
	}

	/**
	 * Reenables the singleton StatisticsUtil
	 * @deprecated Use <code>getInstance().setEnabled(true)</code>
	 * to reenable the singleton StatisticsUtil object instead.
	 * The enabled-state is no longer global for all instances.
	 */
	public static void reenable() {
		getInstance().setEnabled(true);
	}

	public Map<String, Long> getTimeStatistic() {
		return timeStatistic;
	}

	public Map<String, Integer> getSizeStatistic() {
		return sizeStatistic;
	}

	public Map<String, Integer> getCountStatistic() {
		return countStatistic;
	}

	public Map<String, Object> getOtherStatistic() {
		return otherStatistic;
	}

	public Map<String, ?> getStatistic(StatisticType type) {
		switch(type) {
			case Time: return timeStatistic;
			case Size: return sizeStatistic;
			case Count: return countStatistic;
			case Other: return otherStatistic;
			default: return null;
		}
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Resets the StatisticsUtil. All data will be removed.
	 */
	public void reset() {
		this.timeStatistic.clear();
		this.sizeStatistic.clear();
		this.countStatistic.clear();
		this.otherStatistic.clear();
	}

	/**
	 * Resets all counters whose keys start with the given prefix.
	 *
	 * @param keyPrefix
	 */
	public void resetCounters(String keyPrefix) {
		if (enabled) {
			reset(keyPrefix, countStatistic);
		}
	}

	/**
	 * Resets all sizes (integer values) whose keys start with the given prefix.
	 *
	 * @param keyPrefix
	 */
	public void resetSizes(String keyPrefix) {
		if (enabled) {
			reset(keyPrefix, sizeStatistic);
		}
	}

	/**
	 * Resets all times whose keys start with the given prefix.
	 *
	 * @param keyPrefix
	 */
	public void resetTimes(String keyPrefix) {
		if (enabled) {
			reset(keyPrefix, timeStatistic);
		}
	}

	/**
	 * Resets other statistics whose keys start with the given prefix.
	 *
	 * @param keyPrefix
	 */
	public void resetOthers(String keyPrefix) {
		if (enabled) {
			reset(keyPrefix, otherStatistic);
		}
	}

	/**
	 * Resets all stored values whose keys start with the given prefix.
	 *
	 * @param keyPrefix
	 */
	public void reset(String keyPrefix) {
		if (enabled) {
			resetCounter(keyPrefix);
			resetSizes(keyPrefix);
			resetTime(keyPrefix);
			resetOthers(keyPrefix);
		}
	}

	/**
	 * Resets all statistics whose keys start with the given prefix.
	 *
	 * @param keyPrefix
	 * @param statistic
	 */
	private void reset(String keyPrefix, Map<String, ?> statistic) {
		if (enabled) {
			statistic.keySet().removeIf(key -> key.startsWith(keyPrefix));
		}
	}

	/**
	 * Starts the measurement of time for a given key.
	 *
	 * @param key
	 */
	public void start(String key) {
		if (enabled) {
			long time = timeStatistic.getOrDefault(key, 0L);
			timeStatistic.put(STAT_KEY_STARTTIME + key, System.currentTimeMillis() - time);
		}
	}

	/**
	 * Stop the measurement of time for a given key. The time difference between the start time and the stop time will be stored.
	 *
	 * @param key
	 * @return
	 */
	public float stop(String key) {
		if (enabled) {
			long stop = System.currentTimeMillis();
			Long startO = timeStatistic.remove(STAT_KEY_STARTTIME + key);
			long start = startO == null ? 0 : startO.longValue();
			long time = stop - start;
			timeStatistic.put(key, time);
			return time / 1000f;
		}
		return 0;
	}

	/**
	 * Removes the measurement of time for a given key.
	 *
	 * @param key
	 */
	public void resetTime(String key) {
		if (enabled) {
			timeStatistic.remove(STAT_KEY_STARTTIME + key);
		}
	}

	/**
	 * Returns the time that has been measured for the given key.
	 *
	 * @param key
	 * @return
	 */
	public Long getTime(String key) {
		if (enabled) {
			return timeStatistic.getOrDefault(key, 0L) / 1000L;
		}
		return 0L;
	}

	/**
	 * Returns the data that is stored for the given key.
	 *
	 * @param key
	 * @return
	 */
	public Object getObject(String key) {
		if (enabled) {
			return otherStatistic.get(key);
		}
		return null;
	}

	/**
	 * Returns the string data that is stored for the given key.
	 *
	 * @param key
	 * @return
	 */
	public String getString(String key) {
		if (enabled) {
			return (String) otherStatistic.get(key);
		}
		return null;
	}

	/**
	 * Returns the integer data that is stored for the given key.
	 *
	 * @param key
	 * @return
	 */
	public int getInt(String key) {
		if (enabled) {
			Integer i = (Integer) otherStatistic.get(key);
			return i == null ? 0 : i.intValue();
		}
		return 0;
	}

	/**
	 * Stores an arbitrary data value for the given key.
	 *
	 * @param key
	 * @param value
	 */
	public void put(String key, Object value) {
		put(key, StatisticType.Other, value);
	}

	/**
	 * Stores an arbitrary data value for the given key.
	 *
	 * @param key
	 * @param type
	 * @param value
	 */
	public void put(String key, StatisticType type, Object value) {
		if (enabled) {
			switch(type) {
				case Count:
					if(value instanceof Integer) {
						countStatistic.put(key, (Integer)value);
					} else {
						otherStatistic.put(key, value);
					}
					break;
				case Size:
					if(value instanceof Integer) {
						sizeStatistic.put(key, (Integer)value);
					} else {
						otherStatistic.put(key, value);
					}
					break;
				case Time:
					if(value instanceof Long) {
						timeStatistic.put(key, (Long)value);
					} else {
						otherStatistic.put(key, value);
					}
					break;
				case Other:
					otherStatistic.put(key, value);
					break;
			}
			otherStatistic.put(key, value);
		}
	}

	/**
	 * Stores an time  value for the given key.
	 *
	 * @param key
	 * @param value
	 */
	public void putTime(String key, long value) {
		if (enabled) {
			timeStatistic.put(key, value);
		}
	}

	/**
	 * Stores an integer value for the given key.
	 *
	 * @param key
	 * @param value
	 */
	public void put(String key, int value) {
		if (enabled) {
			otherStatistic.put(key, value);
		}
	}

	/**
	 * Stores a size (integer value) of a given key.
	 *
	 * @param key
	 * @param value
	 */
	public void putSize(String key, int value) {
		if (enabled) {
			sizeStatistic.put(key, value);
		}
	}

	/**
	 * Returns the size (integer value) of a given key.
	 *
	 * @param key
	 * @return
	 */
	public int getSize(String key) {
		if (enabled) {
			return sizeStatistic.get(key);
		}
		return 0;
	}

	/**
	 * Returns the counter of the given key.
	 *
	 * @param key
	 * @return
	 */
	public int getCounter(String key) {
		if (enabled && countStatistic.get(key) != null) {
			return countStatistic.get(key);
		}
		return 0;
	}

	/**
	 * Resets the counter of the given key.
	 *
	 * @param key
	 */
	public void resetCounter(String key) {
		if (enabled) {
			countStatistic.put(key, 0);
		}
	}

	/**
	 * Increases the counter for the given key.
	 *
	 * @param key
	 */
	public void count(String key) {
		if (enabled) {
			countStatistic.compute(key, (k, v) -> v == null ? 0 : v+1);
		}
	}



	/**
	 * Fuegt data.value(data.key) an pos in map.value(data.key) ein.
	 * Fehlende Elemente in der Liste werden mit null aufgef�llt
	 * @param map
	 * @param data
	 * @param pos
	 */
	private static void addToMap(Map<String, List<Object>> map, Map<String, ?> data, int pos) {
		for (Map.Entry<String, ?> e : data.entrySet()) {
			List<Object> d = map.get(e.getKey());
			if (d == null) {
				d = new ArrayList<>();
				map.put(e.getKey(), d);
			}
			while (d.size() <= pos) {
				d.add(null);
			}
			d.set(pos, e.getValue());
		}
	}

	/**
	 * Füllt alle map.value mit null auf, bis size erreicht ist
	 * @param map
	 * @param size
	 */
	private static void fillMap(Map<String, List<Object>> map, int size) {
		for (List<Object> d : map.values()) {
			while (d.size() < size) {
				d.add(null);
			}
		}
	}

	/**
	 * Setzt alle null-Werte in allen map.value an pos mit einem Default-Wert
	 * @param map
	 * @param value Default-Wert
	 * @param pos
	 */
	private static void setDefaultToMap(Map<String, List<Object>> map, Object value, int pos) {
		for (List<Object> d : map.values()) {
			Object v = d.get(pos);
			if (v == null) {
				d.set(pos, value);
			}
		}
	}

	/**
	 * Schreibt alles in eine CSV.
	 * Jeder Key wird eine Zeile. Time, Size, Count und Other wird je eine Spalte.
	 * Sind keine Daten vorhanden bleibt eine Zelle leer
	 * @param file
	 * @throws IOException
	 */
	public void writeToCsv(String file) throws IOException {
		Map<String, List<Object>> map = new HashMap<>();
		addToMap(map, timeStatistic, 0);
		addToMap(map, sizeStatistic, 1);
		addToMap(map, countStatistic, 2);
		addToMap(map, otherStatistic, 3);
		fillMap(map, 4);
		setDefaultToMap(map, "", 0);
		setDefaultToMap(map, "", 1);
		setDefaultToMap(map, "", 2);
		setDefaultToMap(map, "", 3);
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(file), true))) {
			writer.append("Name").append(COL).append("Time").append(COL).append("Size").append(COL)
				.append("Count").append(COL).append("Other").append(LINE_SEPERATOR);
			writer.append("String").append(COL).append("Decimal").append(COL).append("Integer")
				.append(COL).append("Integer").append(COL).append("String").append(LINE_SEPERATOR);
			for (Map.Entry<String, List<Object>> e : map.entrySet()) {
				writer.append(e.getKey());
				for (Object o : e.getValue()) {
					writer.append(COL).append(o.toString());
				}
				writer.append(LINE_SEPERATOR);
			}
			writer.flush();
		}
	}

	/**
	 * Fuegt die verschiedenen *Statistic-Maps in einer Map zusammen.
	 * Die Keys werden mit " :: Name", wobei Name der Name der Statistik ist, erweitert.
	 * @return
	 */
	public Map<String, Object> getUnifiedStatistics() {
		List<Map<String, ?>> maps = new ArrayList<>();
		maps.add(timeStatistic);
		maps.add(sizeStatistic);
		maps.add(countStatistic);
		maps.add(otherStatistic);
		List<String> extras = Arrays.asList(new String[] { "(ms)", "", "", "" });
		Map<String, Object> result = new HashMap<>();
		for (int i = 0; i < maps.size(); i++) {
			Map<String, ?> map = maps.get(i);
			String extra = extras.get(i);
			for (Map.Entry<String, ?> e : map.entrySet()) {
				result.put(e.getKey() + " " + extra , e.getValue());
			}
		}
		return result;
	}

	/**
	 * Erstellt den String, der in die von writeCSV2 erstellte Datei gespeichert wird
	 * @param maps
	 * @param extras
	 * @return
	 */
	private static String getCsv2Data(List<Map<String, ?>> maps, List<String> extras) {
		StringBuilder sb1 = new StringBuilder();
		StringBuilder sb2 = new StringBuilder();
		StringBuilder sb3 = new StringBuilder();
		for (int i = 0; i < maps.size(); i++) {
			Map<String, ?> map = maps.get(i);
			String extra = extras.get(i);
			for (Map.Entry<String, ?> e : map.entrySet()) {
				sb1.append(e.getKey()).append(extra != null ? " :: " + extra : "").append(COL);
				String name = e.getValue().getClass().getSimpleName();
				if ("Long".equals(name) || "Integer".equals(name)) {
					name = "Integer";
				} else if ("Float".equals(name) || "Double".equals(name)) {
					name = "Decimal";
				} else if ("Boolean".equals(name)) {
					name = "Boolean";
				} else {
					name = "String";
				}
				sb2.append(name).append(COL);
				sb3.append(e.getValue()).append(COL);
			}
		}
		return sb1.substring(0, sb1.length() - 1) + LINE_SEPERATOR + sb2.substring(0, sb2.length() - 1) + LINE_SEPERATOR + sb3.substring(0, sb3.length() - 1);
	}

	/**
	 * Schreibt die Statistik in eine CSV-Datei.
	 * Jeder Key wird eine Spalte. Die Maps werden zusammengefasst, die Keys erhalten eine Erweiterung um zu unterscheiden,
	 * aus welcher *Statistics-Map sie stammen.
	 * @param file
	 * @throws IOException
	 */
	public void writeToCsv2(String file) throws IOException {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(file), true))) {
			List<Map<String, ?>> maps = new ArrayList<>();
			maps.add(timeStatistic);
			maps.add(sizeStatistic);
			maps.add(countStatistic);
			maps.add(otherStatistic);
			List<String> extras = Arrays.asList("Time", "Size", "Count", "String");
			writer.write(getCsv2Data(maps, extras));
		}
	}

	/**
	 * Appends a textual dump of the stored information to the given string builder.
	 * @param sb the string builder
	 */
	public void dump(StringBuilder sb) {
		if (enabled) {
			sb.append("*********************Statistics*********************").append(LINE_SEPERATOR);
			dumpStatisticsSection(sb, "Time statistics (in ms):", timeStatistic);
			sb.append(LINE_SEPERATOR);
			dumpStatisticsSection(sb, "Count statistics:", countStatistic);
			sb.append(LINE_SEPERATOR);
			dumpStatisticsSection(sb, "Size statistics:", sizeStatistic);
			sb.append(LINE_SEPERATOR);
			dumpStatisticsSection(sb, "Other statistics:", otherStatistic);
			sb.append("**************************************************************");
		}
	}

	/**
	 * Returns a textual dump of the stored information.
	 *
	 * @return
	 */
	public String dump() {
		StringBuilder sb = new StringBuilder();
		dump(sb);
		return sb.toString();
	}

	private static void dumpStatisticsSection(StringBuilder sb, String title, Map<String,?> statistics) {
		sb.append(title).append(LINE_SEPERATOR);
		for (Map.Entry<String, ?> entry : statistics.entrySet()) {
			sb.append(entry.getKey()).append(" = ").append(entry.getValue()).append(LINE_SEPERATOR);
		}
	}

	@Override
	public String toString() {
		return "StatisticsUtil{enabled=" + enabled
				+ ", time=" + timeStatistic
				+ ", size=" + sizeStatistic
				+ ", count=" + countStatistic
				+ ", other=" + otherStatistic
				+ "}";
	}

	/**
	 * Reads a serialized StatisticsUtil from the object input stream.
	 * Used for compatibility with older revisions of this class.
	 * @param in object input stream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		timeStatistic = new TreeMap<>((Map<String,Long>)in.readObject());
		sizeStatistic = new TreeMap<>((Map<String,Integer>)in.readObject());
		countStatistic = new TreeMap<>((Map<String,Integer>)in.readObject());
		otherStatistic = new TreeMap<>((Map<String,Object>)in.readObject());
		enabled = true;
	}
}
