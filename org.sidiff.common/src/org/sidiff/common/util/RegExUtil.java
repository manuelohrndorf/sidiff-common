package org.sidiff.common.util;

import java.util.regex.Pattern;

/**
 * Utility class for working with regular expressions.
 * @author Robert Müller
 * @see Patterns
 */
public class RegExUtil {

	/**
	 * Adds spaces to a camel case string, e.g. <code>RegEXUtil -> Reg EX Util</code>
	 * @param camelCase the camel case string
	 * @return the string with spaces inserted at the word boundaries
	 */
	public static String addSpacesToCamelCase(String camelCase) {
		return Patterns.SPLIT_CAMEL_CASE.get().matcher(camelCase).replaceAll(" ");
	}

	/**
	 * Enum containing regular expression patterns.
	 * Use {@link #get()} to retrieve a {@link Pattern}.
	 */
	public enum Patterns {
		/**
		 * A pattern to split CamelCase word (a-z, A-Z):
		 * <ul>
		 * <li>CamelCase -> Camel, Case</li>
		 * <li>camelCase -> camel, Case</li>
		 * <li>CAMELCase -> CAMEL, Case</li>
		 * </ul>
		 */
		SPLIT_CAMEL_CASE("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");


		private final Pattern pattern;

		private Patterns(String regex) {
			this.pattern = Pattern.compile(regex);
		}

		public Pattern get() {
			return pattern;
		}
	}
}
