import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** 
 * Query Filer Parser Interface file
 * 
 * @author stephen
 *
 */
public interface QueryFileParserInterface {

	/**
	 * Default method for parsing the query file
	 * 
	 * @param queryFile the file being passed in
	 * @param exact which search to use
	 * @throws IOException throws exception  if an IO Exception occurs
	 */
	default void queryFile(Path queryFile, boolean exact) throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(queryFile, StandardCharsets.UTF_8);) {
			String line;
			while ((line = reader.readLine()) != null) {
				parseQueryLine(line, exact);
			}
		}
	}
	
	/**
	 * Parses the query line 
	 * 
	 * @param line the line 
	 * @param exact which search to use
	 */
	public void parseQueryLine(String line, boolean exact);
	
	/**
	 * writes the result to json given a path
	 * 
	 * @param path the path 
	 * @throws IOException throws exception if an IO Exception occurs
	 */
	public void resultWriteJson(Path path) throws IOException;
}
