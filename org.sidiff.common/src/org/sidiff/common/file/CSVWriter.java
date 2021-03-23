package org.sidiff.common.file;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>Writes string data in CSV format to an arbitrary Writer.</p>
 * <p>The default column separator is "," and the default line
 * separator is the system's default.</p>
 * Example to get the CSV data as a string:
 * <pre>
 * CSVWriter.writeToString(csvWriter -> {
 * 	csvWriter.write("Foo", "Bar");
 * 	csvWriter.write(1, 2);
 * });
 * </pre>
 * @author rmueller
 */
public class CSVWriter implements AutoCloseable {

	private final Writer delegate;

	private CharSequence columnSeparator = ",";
	private CharSequence lineSeparator = System.lineSeparator();

	public CSVWriter(Writer delegate) {
		this.delegate = Objects.requireNonNull(delegate);
	}

	public void setColumnSeparator(CharSequence columnSeparator) {
		this.columnSeparator = columnSeparator;
	}

	public CharSequence getColumnSeparator() {
		return columnSeparator;
	}

	public void setLineSeparator(CharSequence lineSeparator) {
		this.lineSeparator = lineSeparator;
	}

	public CharSequence getLineSeparator() {
		return lineSeparator;
	}

	public void write(Object ...cols) throws IOException {
		write(Stream.of(cols).map(Objects::toString));
	}

	public void write(String ...cols) throws IOException {
		write(Stream.of(cols));
	}

	public void write(Stream<String> cols) throws IOException {
		delegate.write(cols.map(this::escapeSpecialCharacters).collect(Collectors.joining(columnSeparator)));
		delegate.write(lineSeparator.toString());
	}

	public void writeAll(Collection<String[]> lines) throws IOException {
		for(String[] line : lines) {
			write(line);
		}
	}

	@Override
	public void close() throws IOException {
		delegate.close();
	}

	private String escapeSpecialCharacters(String data) {
	    String escaped = data.replaceAll("\\R", " "); // replace newlines with spaces
	    if (escaped.contains(columnSeparator) || escaped.contains("\"") || escaped.contains("'")) {
	        return "\"" + escaped.replace("\"", "\"\"") + "\"";
	    }
	    return escaped;
	}

	/**
	 * Creates a new CSVWriter which writes to a char array. The CSVWriter
	 * runs the runnable, which can set attributes and write data.
	 * After the execution of the runnable, the written CSV data is returned as a string.
	 * @param runnable the runnable which makes use of the writer
	 * @return string result of running the runnable using a new writer
	 */
	public static String writeToString(IWriterRunnable runnable) {
		try(CharArrayWriter charArray = new CharArrayWriter(); CSVWriter csvWriter = new CSVWriter(charArray)) {
			runnable.useWriter(csvWriter);
			return charArray.toString();
		} catch (IOException e) {
			throw new AssertionError("IOException occurred using CharArrayWriter", e);
		}
	}

	@FunctionalInterface
	public interface IWriterRunnable {
		void useWriter(CSVWriter writer) throws IOException;
	}
}
