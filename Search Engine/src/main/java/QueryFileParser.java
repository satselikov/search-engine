import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Class responsible for parsing the queryfile and calling the searches
 * accordingly
 * 
 * @author stephen
 *
 */
public class QueryFileParser implements QueryFileParserInterface {

	/**
	 * Initialize the inverted index data structure
	 */
	private final InvertedIndex index;

	/**
	 * Initialize the search result data structure
	 */
	private final TreeMap<String, ArrayList<InvertedIndex.Result>> results;

	/**
	 * Constructor
	 * 
	 * @param index index data structure
	 */
	public QueryFileParser(InvertedIndex index) {
		this.index = index;
		this.results = new TreeMap<>();
	}

	@Override
	public void queryFile(Path queryFile, boolean exact) throws IOException {
		QueryFileParserInterface.super.queryFile(queryFile, exact);
	}

	@Override
	public void parseQueryLine(String line, boolean exact) {
		TreeSet<String> queryLine = TextFileStemmer.uniqueStems(line);
		String query = String.join(" ", queryLine);

		if (!query.isEmpty() && !results.containsKey(query)) {
			this.results.put(query, this.index.search(queryLine, exact));
		}
	}

	/**
	 * Helper method for SimpleJSONWriter to write the search result to JSON given a
	 * path
	 * 
	 * @param path the path we are writing to
	 * @throws IOException if an IO error occurs
	 */
	public void resultWriteJson(Path path) throws IOException {
		SimpleJsonWriter.asSearch(this.results, path);
	}

}