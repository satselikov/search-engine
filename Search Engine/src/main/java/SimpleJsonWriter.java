import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;


/** 
 * Outputs several simple data structures in "pretty" JSON format where newlines
 * are used to separate elements and nested elements are indented using tabs.
 *
 * Warning: This class is not thread-safe. If multiple threads access this class
 * concurrently, access must be synchronized externally.
 *
 * @author Stephen Tselikov
 * @author CS 212 Software Development
 * @author University of San Francisco
 * @version Fall 2020
 *
 */
public class SimpleJsonWriter {

	/**
	 * Writes the elements as a pretty JSON array.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param level    the initial indent level
	 * @throws IOException if an IO error occurs
	 */
	public static void asArray(Collection<Integer> elements, Writer writer, int level) throws IOException {
		Iterator<Integer> iter = elements.iterator();
		level++;
		writer.write("[");
		if (iter.hasNext()) {
			writer.write("\n");
			indent(iter.next(), writer, level);
		}
		while (iter.hasNext()) {
			writer.write(",\n");
			indent(iter.next(), writer, level);
		}
		writer.write("\n");
		indent(writer, level - 1);
		writer.write("]");
	}

	/**
	 * Writes the elements as a pretty JSON object.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param level    the initial indent level
	 * @throws IOException if an IO error occurs
	 */
	public static void asObject(Map<String, Integer> elements, Writer writer, int level) throws IOException {
		Iterator<String> iter = elements.keySet().iterator();
		level++;
		writer.write("{");
		if (iter.hasNext()) {
			writer.write("\n");
			String key = iter.next().toString();
			Integer value = elements.get(key);
			indent(writer, level);
			writer.write("\"" + key + "\": " + value);
		}
		while (iter.hasNext()) {
			writer.write(",\n");
			String key = iter.next().toString();
			Integer value = elements.get(key);
			indent(writer, level);
			writer.write("\"" + key + "\": " + value);
		}
		writer.write("\n}");
	}

	/**
	 * Writes the elements as a pretty JSON object with a nested array. The generic
	 * notation used allows this method to be used for any type of map with any type
	 * of nested collection of integer objects.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param level    the initial indent level
	 * @throws IOException if an IO error occurs
	 */
	public static void asNestedArray(Map<String, ? extends Collection<Integer>> elements, Writer writer, int level)
			throws IOException {
		Iterator<String> iter = elements.keySet().iterator();
		writer.write("{");

		if (iter.hasNext()) {
			String key = iter.next();
			writer.write("\n");
			indent(writer, level + 1);
			writer.write('"');
			writer.write(key);
			writer.write('"');
			writer.write(": ");
			asArray(elements.get(key), writer, level + 1);
		}
		while (iter.hasNext()) {
			writer.write(",\n");
			String key = iter.next();
			indent(writer, level + 1);
			writer.write('"');
			writer.write(key);
			writer.write('"');
			writer.write(": ");
			asArray(elements.get(key), writer, level + 1);
		}
		writer.write("\n");
		indent(writer, level);
		writer.write("}");
	}

	/**
	 * Writes the elements as a pretty JSON object with a double nested array to
	 * accommodate the InvertedIndex data structure.
	 *
	 * @param index  data structure being passed in
	 * @param writer the writer to use
	 * @param level  the initial indent level
	 * @throws IOException if an IO error occurs
	 */
	public static void asIndex(TreeMap<String, Map<String, TreeSet<Integer>>> index, Writer writer, int level)
			throws IOException {
		Iterator<String> iter = index.keySet().iterator();
		//level++;
		writer.write("{");
		if (iter.hasNext()) {
			String key = iter.next().toString();
			writer.write("\n");
			indent(writer, level + 1);
			writer.write('"');
			writer.write(key);
			writer.write('"');
			writer.write(": ");
			asNestedArray(index.get(key), writer, level+1);
		}
		while (iter.hasNext()) {
			String key = iter.next().toString();
			writer.write(",\n");
			indent(writer, level + 1);
			writer.write('"');
			writer.write(key);
			writer.write('"');
			writer.write(": ");
			asNestedArray(index.get(key), writer, level+1);
		}
		writer.write("\n");
		indent(writer, level);
		writer.write("}");
	}

	/**
	 * Writes the elements as a pretty JSON array to file.
	 *
	 * @param index the data structure to write to the file
	 * @param path  the file path to use
	 * @throws IOException if an IO error occurs
	 */
	public static void asIndex(TreeMap<String, Map<String, TreeSet<Integer>>> index, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			asIndex(index, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON array.
	 *
	 * @param index data structure
	 * @return String
	 */
	public static String asIndex(TreeMap<String, Map<String, TreeSet<Integer>>> index) {
		try {
			StringWriter writer = new StringWriter();
			asIndex(index, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Indents using a tab character by the number of times specified.
	 *
	 * @param writer the writer to use
	 * @param times  the number of times to write a tab symbol
	 * @throws IOException if an IO error occurs
	 */
	public static void indent(Writer writer, int times) throws IOException {
		for (int i = 0; i < times; i++) {
			writer.write('\t');
		}
	}

	/**
	 * Indents and then writes the integer element.
	 *
	 * @param element the element to write
	 * @param writer  the writer to use
	 * @param times   the number of times to indent
	 * @throws IOException if an IO error occurs
	 *
	 * @see #indent(Writer, int)
	 */
	public static void indent(Integer element, Writer writer, int times) throws IOException {
		indent(writer, times);
		writer.write(element.toString());
	}

	/**
	 * Indents and then writes the text element surrounded by {@code " "} quotation
	 * marks.
	 *
	 * @param element the element to write
	 * @param writer  the writer to use
	 * @param times   the number of times to indent
	 * @throws IOException if an IO error occurs
	 *
	 * @see #indent(Writer, int)
	 */
	public static void indent(String element, Writer writer, int times) throws IOException {
		indent(writer, times);
		writer.write('"');
		writer.write(element);
		writer.write('"');
	}

	/**
	 * Writes a map entry in pretty JSON format.
	 *
	 * @param entry  the nested entry to write
	 * @param writer the writer to use
	 * @param level  the initial indentation level
	 * @throws IOException if an IO error occurs
	 */
	public static void writeEntry(Entry<String, Integer> entry, Writer writer, int level) throws IOException {
		writer.write('\n');
		indent(entry.getKey(), writer, level);
		writer.write(": ");
		writer.write(entry.getValue().toString());
	}

	/**
	 * Writes the elements as a pretty JSON array to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see #asArray(Collection, Writer, int)
	 */
	public static void asArray(Collection<Integer> elements, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			asArray(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON array.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see #asArray(Collection, Writer, int)
	 */
	public static String asArray(Collection<Integer> elements) {
		try {
			StringWriter writer = new StringWriter();
			asArray(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the elements as a pretty JSON object to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see #asObject(Map, Writer, int)
	 */
	public static void asObject(Map<String, Integer> elements, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			asObject(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON object.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see #asObject(Map, Writer, int)
	 */
	public static String asObject(Map<String, Integer> elements) {
		try {
			StringWriter writer = new StringWriter();
			asObject(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the elements as a nested pretty JSON object to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see #asNestedArray(Map, Writer, int)
	 */
	public static void asNestedArray(Map<String, ? extends Collection<Integer>> elements, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			asNestedArray(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a nested pretty JSON object.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see #asNestedArray(Map, Writer, int)
	 */
	public static String asNestedArray(Map<String, ? extends Collection<Integer>> elements) {
		try {
			StringWriter writer = new StringWriter();
			asNestedArray(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the search results as a nested pretty JSON object to file.
	 * 
	 * @param results the search results
	 * @param path    path we are writing to
	 * @throws IOException if an IO error occurs
	 */
	public static void asSearch(TreeMap<String, ArrayList<InvertedIndex.Result>> results, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			asSearch(results, writer, 0);
		}

	}

	/**
	 * Method responsible for writing the outermost level of the JSON object
	 * 
	 * @param results the search results
	 * @param writer  the writer
	 * @param level   the level of indentation
	 * @throws IOException if an IO error occurs
	 */
	private static void asSearch(Map<String, ArrayList<InvertedIndex.Result>> results, Writer writer, int level)
			throws IOException {
		writer.write("{");

		var iter = results.entrySet().iterator();
		level++;
		if (iter.hasNext()) {
			writer.write("\n");
			// System.out.println("first iter.next" + iter.next());
			writeSearchEntry(iter.next(), writer, level);
		}

		while (iter.hasNext()) {
			// System.out.println("iter.next: " + iter.next());
			writer.write(",\n");
			writeSearchEntry(iter.next(), writer, level);
		}
		writer.write("\n}");
	}

	/**
	 * Helper method for asSearch, responsible for writing the query into the JSON
	 * object
	 * 
	 * @param entry  the query
	 * @param writer the writer
	 * @param level  the level of indentation
	 * @throws IOException if an IO error occurs
	 */
	public static void writeSearchEntry(Map.Entry<String, ArrayList<InvertedIndex.Result>> entry, Writer writer,
			int level) throws IOException {
		indent(writer, level);
		writer.write('"');
		writer.write(entry.getKey());
		writer.write('"');
		writer.write(": ");
		asResultList(entry.getValue(), writer, level);

	}

	/**
	 * Helper Method for writeSearchEnry, responsible for the inner level of the
	 * JSON object
	 * 
	 * @param list   the list of results
	 * @param writer the writer
	 * @param level  the level of indentation
	 * @throws IOException if IO Error occurs
	 */
	public static void asResultList(ArrayList<InvertedIndex.Result> list, Writer writer, int level) throws IOException {
		writer.write("[");

		var it = list.iterator();
		if (it.hasNext()) {
			writer.write("\n");
			writeResult(it.next(), writer, level + 1);
		}
		while (it.hasNext()) {
			writer.write(",\n");
			// System.out.println(it.next());
			writeResult(it.next(), writer, level + 1);
		}
		writer.write("\n\t]");
	}

	/**
	 * Helper method for asResultList that is responsible for writing the location,
	 * count, and score of the result
	 * 
	 * @param result where count score of the search result - individual
	 * @param writer the writer
	 * @param level  the level
	 * @throws IOException if an IO Error occurs
	 */
	public static void writeResult(InvertedIndex.Result result, Writer writer, int level) throws IOException {
		DecimalFormat FORMATTER = new DecimalFormat("0.00000000");
		indent(writer, level + 1); // 2
		writer.write("{\n");
		indent(writer, level + 2); // 3
		writer.write('"');
		writer.write("where");
		writer.write('"');
		writer.write(": ");
		writer.write('"');
		writer.write(result.getWhere());
		writer.write('"');
		writer.write(",\n");
		indent(writer, level + 2); // 3
		writer.write('"');
		writer.write("count");
		writer.write('"');
		writer.write(": " + result.getCount() + ",\n");
		indent(writer, level + 2); // 3
		writer.write('"');
		writer.write("score");
		writer.write('"');
		writer.write(": " + FORMATTER.format(result.getScore()));
		writer.write("\n");
		indent(writer, level + 1); // 2
		writer.write("}");
	}

}