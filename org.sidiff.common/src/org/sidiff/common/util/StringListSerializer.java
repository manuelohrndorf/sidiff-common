package org.sidiff.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A <code>StringListSerializer</code> serializes a list of strings
 * using a delimiter and an escape string, and unserializes previously
 * serialized strings.
 * The same delimiter and escape strings must be used for serialization and deserialization.
 * @author Robert M�ller
 *
 */
public class StringListSerializer {

	private final String DELIMITER;
	private final String ESCAPE;

	private final String ESCAPED_DELIMITER;
	private final String ESCAPED_ESCAPE;

	private final Pattern DESERIALIZE_PATTERN;
	private final Pattern ESCAPE_PATTERN;
	private final Pattern UNESCAPE_PATTERN;
	private final Pattern ESCAPE_ESCAPE_PATTERN;
	private final Pattern UNESCAPE_ESCAPE_PATTERN;

	/**
	 * Shared default list serializer using <code>;</code> as delimiter and <code>\</code> as escape string.
	 */
	public static final StringListSerializer DEFAULT = new StringListSerializer();

	/**
	 * Creates a new list serializer using <code>;</code> as delimiter and <code>\</code> as escape string.
	 */
	public StringListSerializer() {
		this(";");
	}

	/**
	 * Creates a new list serializer using the given delimiter and <code>\</code> as escape string.
	 * @param delimiter the delimiter
	 */
	public StringListSerializer(String delimiter) {
		this(delimiter, "\\");
	}

	/**
	 * Creates a new list serializer using the given delimiter and escape string.
	 * @param delimiter the delimiter
	 * @param escape the escape
	 */
	public StringListSerializer(String delimiter, String escape) {
		DELIMITER = delimiter;
		ESCAPE = escape;
		ESCAPED_DELIMITER = ESCAPE + DELIMITER;
		ESCAPED_ESCAPE = ESCAPE + ESCAPE;
		final String qDelimiter = Pattern.quote(delimiter);
		final String qEscape = Pattern.quote(escape);
		DESERIALIZE_PATTERN = Pattern.compile("(?<!" + qEscape + ")" + qDelimiter);
		ESCAPE_PATTERN = Pattern.compile(qDelimiter);
		UNESCAPE_PATTERN = Pattern.compile(qEscape + qDelimiter);
		ESCAPE_ESCAPE_PATTERN = Pattern.compile(qEscape);
		UNESCAPE_ESCAPE_PATTERN = Pattern.compile(qEscape + qEscape);
	}

	/**
	 * Serializes a list of strings using a delimiter and escape string
	 * for subsequent deserialization with {@link #deserialize(String)}.
	 * @param list the list
	 * @return serialized list
	 */
	public String serialize(List<String> list) {
		StringBuilder builder = new StringBuilder();
		for(String item : list) {
			if(builder.length() > 0)
				builder.append(DELIMITER);
			builder.append(escapeDelimiter(escapeEscape(item)));
		}
		return builder.toString();
	}

	/**
	 * Deserializes a string previously serialized with {@link #serialize(List)}.
	 * @param string the string
	 * @return list of deserialized strings
	 */
	public List<String> deserialize(String string) {
		List<String> items = new ArrayList<String>();
		for(String item : DESERIALIZE_PATTERN.split(string)) {
			items.add(unescapeEscape(unescapeDelimiter(item)));
		}
		return items;
	}

	private String escapeDelimiter(String string) {
		return replaceAll(ESCAPE_PATTERN, string, ESCAPED_DELIMITER);
	}

	private String unescapeDelimiter(String string) {
		return replaceAll(UNESCAPE_PATTERN, string, DELIMITER);
	}

	private String escapeEscape(String string) {
		return replaceAll(ESCAPE_ESCAPE_PATTERN, string, ESCAPED_ESCAPE);
	}

	private String unescapeEscape(String string) {
		return replaceAll(UNESCAPE_ESCAPE_PATTERN, string, ESCAPE);
	}

	private static String replaceAll(Pattern pattern, String string, String replacement) {
		return pattern.matcher(string).replaceAll(Matcher.quoteReplacement(replacement));
	}
}
