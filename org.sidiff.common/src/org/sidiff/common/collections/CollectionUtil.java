package org.sidiff.common.collections;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>Contains various utility functions to work with collections.</p>
 * <p>Most of these methods are short and can be used as templates
 * on how to use the {@link Stream} API. If you further use the 
 * collection returned by any of these methods with Streams or
 * with this methods of this class, you should instead combine
 * the underlying stream operations to improve performance,
 * as each of these methods creates a new collection.</p>
 * <p>This utility class replaces the deprecated ClassificationUtil,
 * FilterUtil, ViewUtil and CollectionView.</p>
 * @author Robert Müller
 *
 */
public class CollectionUtil {

	private CollectionUtil() {
		throw new AssertionError();
	}

	/**
	 * Filters the given collection, returning a collection with only those elements that
	 * satisfy the given predicate. To invert the {@link Predicate}, use {@link Predicate#negate()}.
	 * @param collection the collection
	 * @param predicate the predicate
	 * @return filtered collection
	 */
	public static <T> Collection<T> filterCollection(Collection<T> collection, Predicate<T> predicate) {
		return collection.stream().filter(predicate).collect(Collectors.toList());
	}

	/**
	 * Filters the given list, returning a list with only those elements that
	 * satisfy the given predicate. To invert the {@link Predicate}, use {@link Predicate#negate()}.
	 * @param list the list
	 * @param predicate the predicate
	 * @return filtered list
	 */
	public static <T> List<T> filterList(List<T> list, Predicate<T> predicate) {
		return list.stream().filter(predicate).collect(Collectors.toList());
	}

	/**
	 * Filters the given set, returning a set with only those elements that
	 * satisfy the given predicate. To invert the {@link Predicate}, use {@link Predicate#negate()}.
	 * @param set the set
	 * @param predicate the predicate
	 * @return filtered set
	 */
	public static <T> Set<T> filterSet(Set<T> set, Predicate<T> predicate) {
		return set.stream().filter(predicate).collect(Collectors.toSet());
	}

	/**
	 * Combines the given collection of collections into a single collection with all contained elements.
	 * @param collection the collection of collections
	 * @return all collections merged
	 */
	public static <T> Collection<T> combineCollections(Collection<Collection<T>> collection) {
		return collection.stream()
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
	}

	/**
	 * Combines the given collection of lists into a single list with all contained elements.
	 * @param collection the collection of lists
	 * @return all lists merged
	 */
	public static <T> List<T> combineLists(Collection<List<T>> collection) {
		return collection.stream()
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}

	/**
	 * Combines the given collection of sets into a single set with all contained elements.
	 * @param collection the collection of sets
	 * @return all sets merged
	 */
	public static <T> Set<T> combineSets(Collection<Set<T>> collection) {
		return collection.stream()
				.flatMap(Set::stream)
				.collect(Collectors.toSet());
	}

	/**
	 * Combines the given collection of maps into a single map with all contained entries.
	 * @param collection the collection of maps
	 * @return all maps merged
	 * @throws IllegalStateException if a key is duplicated
	 */
	public static <K,V> Map<K,V> combineMaps(Collection<Map<K,V>> collection) {
		return collection.stream()
				.flatMap(map -> map.entrySet().stream())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	/**
	 * Groups a collection with the given classification function.
	 * @param collection the collection
	 * @param grouping grouping function, assigning a key to each value
	 * @return map of the grouped collection
	 */
	public static <K,V> Map<K, ? extends Collection<V>> groupCollection(Collection<V> collection, Function<V,K> grouping) {
		return collection.stream().collect(Collectors.groupingBy(grouping));
	}

	/**
	 * Groups a collection with the given classification function.
	 * @param collection the collection
	 * @param grouping grouping function, assigning a key to each value
	 * @return map of the grouped collection, collecting values in a list
	 */
	public static <K,V> Map<K, List<V>> groupList(Collection<V> collection, Function<V,K> grouping) {
		return collection.stream().collect(Collectors.groupingBy(grouping, Collectors.toList()));
	}

	/**
	 * Groups a collection with the given classification function.
	 * The returned map is sorted using the given comparator.
	 * @param collection the collection
	 * @param grouping grouping function, assigning a key to each value
	 * @param comparator the comparator for the sorted map
	 * @return map of the grouped collection, collecting values in a list
	 */
	public static <K,V> SortedMap<K, List<V>> groupList(Collection<V> collection, Function<V,K> grouping, Comparator<K> comparator) {
		return collection.stream().collect(Collectors.groupingBy(grouping, () -> new TreeMap<>(comparator), Collectors.toList()));
	}

	/**
	 * Groups a collection with the given classification function.
	 * @param collection the collection
	 * @param grouping grouping function, assigning a key to each value
	 * @return map of the grouped collection, collecting values in a set
	 */
	public static <K,V> Map<K, Set<V>> groupSet(Collection<V> collection, Function<V,K> grouping) {
		return collection.stream().collect(Collectors.groupingBy(grouping, Collectors.toSet()));
	}

	/**
	 * Groups a collection with the given classification function.
	 * The returned map is sorted using the given comparator.
	 * @param collection the collection
	 * @param grouping grouping function, assigning a key to each value
	 * @param comparator the comparator for the sorted map
	 * @return map of the grouped collection, collecting values in a set
	 */
	public static <K,V> SortedMap<K, Set<V>> groupSet(Collection<V> collection, Function<V,K> grouping, Comparator<K> comparator) {
		return collection.stream().collect(Collectors.groupingBy(grouping, () -> new TreeMap<>(comparator), Collectors.toSet()));
	}
}
