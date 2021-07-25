package org.sidiff.common.stringresolver;

import java.util.HashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.sidiff.common.reflection.ReflectionUtil;

/**
 * Utility class for string operations, e.g. converting objects to strings, counting substrings, etc.
 * @author wenzel
 *
 */
public class StringUtil {

	private static HashMap<Class<?>,StringResolver> resolvers = new HashMap<>();

	/**
	 * Registers a string resolver.
	 */
	public static boolean addStringResolver(StringResolver resolver) {
		assert resolver!=null : "Cannot register null!";

		if(!resolvers.containsKey(resolver.dedicatedClass())) {
			return resolvers.put(resolver.dedicatedClass(),resolver)!=null;
		}
		throw new IllegalArgumentException("Resolver for type '"+resolver.dedicatedClass().getName()+"' already registered!");
	}

	/**
	 * Removes a registered string resolver.
	 */
	public static boolean removeStringResolver(Class<?> resolverClass) {
		return resolvers.remove(resolverClass)!=null;
	}


	/**
	 * Removes a registered string resolver.
	 */
	public static boolean removeStringResolver(StringResolver resolver) {
		for(Class<?> key : resolvers.keySet()) {
			if(resolvers.get(key) == resolver) {
				resolvers.remove(key);
				return true;
			}
		}
		return false;
	}


	/**
	 * Converts an objects into string representation. The conversion is delegated
	 * to the respective string resolver that has to be registered beforehand.
	 */
	public static String resolve(Object object) {
		if(object == null) {
			return "null";
		}

		// Resolve lazy objects (supplier lambdas)
		if(object instanceof Supplier<?>) {
			return resolve(((Supplier<?>)object).get());
		}

		Class<?> objectClass = object.getClass();
		if (resolvers.containsKey(objectClass)) {
			return resolvers.get(objectClass).resolve(object);
		}

		// Get best resolver applicable
		StringResolver bestMatch = null;
		int bestInheritanceDistance = Integer.MAX_VALUE;
		for(StringResolver resolver : resolvers.values()){
			if(resolver.dedicatedClass().isAssignableFrom(objectClass)) {
				int currentInheritanceDistance = ReflectionUtil.computeInheritanceDistance(resolver.dedicatedClass(), objectClass);
				if(currentInheritanceDistance < bestInheritanceDistance) {
					bestInheritanceDistance = currentInheritanceDistance;
					bestMatch = resolver;
				}
			}
		}
		if(bestMatch != null) {
			return bestMatch.resolve(object);
		}
		return object.toString();
	}


	/**
	 * Concats the string representation of each given object into a big string which is then returned.
	 *
	 * Resolvable objects, such as exceptions, are handled specially, with other objects, the normal toString() is used.
	 *
	 * @param message the list of objects to concat.
	 * @return the concatted string representation of the object.
	 */
	public static String resolve(Object... objects) {
		return Stream.of(objects).map(StringUtil::resolve).collect(Collectors.joining(" "));
	}

	/**
	 * Generiert eine frei definierbare einrueckung und haengt diese
	 * an einen StingBuffer an.
	 *
	 * @param buffer Der StringBuffer, dem eine einrueckung angehangen werden soll.
	 * @param level anzahl der whitespaces, aus denen die Einrueckung bestehen soll.
	 * @param startWithNewLine ob die einrueckung mit einer neuen zeile begonnen werden soll.
	 */
	public static void appendIndentation(StringBuilder buffer, int level, boolean startWithNewLine) {
		assert buffer!=null : "Missing Buffer (null)";
		assert level>=0 : "Illegal indentation level:"+level;

		if(startWithNewLine){
			buffer.append("\n");
		}
		for(int i=0;i<level;i++){
			buffer.append(" ");
		}
	}
}
