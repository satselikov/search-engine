import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/** 
 * Class responsible for building the Inverted Index Data structure.
 */
public class InvertedIndexAdder {

	/**
	 * Instantiate the index instance
	 *
	 */
	private final InvertedIndex index;

	/**
	 * Constructor for IndexAdder
	 *
	 * @param index the inverted index data structure
	 */
	public InvertedIndexAdder(InvertedIndex index) {
		this.index = index;
	}

	/**
	 * Initialize the snowball stemmer
	 */
	public static final SnowballStemmer.ALGORITHM DEFAULT = SnowballStemmer.ALGORITHM.ENGLISH;

	/**
	 * Method responsible for parsing a path, stemming its content, and adding to
	 * the index (passing in the index as a parameter)
	 * 
	 * @param path  the path
	 * @param index the index
	 * @throws IOException If an IO error occurs, throw exception
	 */
	public static void addToIndex(Path path, InvertedIndex index) throws IOException {
		int counter = 1;
		Stemmer stemmer = new SnowballStemmer(DEFAULT);

		try (BufferedReader reader = Files.newBufferedReader((path), StandardCharsets.UTF_8);) {
			String line = null;
			String location = path.toString();
			while ((line = reader.readLine()) != null) {
				String[] parsedline = TextParser.parse(line);
				for (String word : parsedline) {
					word = stemmer.stem(word).toString();
					index.add(word, location, counter);
					counter++;
				}
			}
		}
	}

	/**
	 * Uses BufferedReader to read through the file and loop each line, parsing each
	 * line, stemming each word, and adding the word, location, position to the
	 * invertedIndex data structure.
	 *
	 * @param path the path given
	 * @throws IOException throws an IOException if the path is invalid
	 */
	public void addToIndex(Path path) throws IOException {
		addToIndex(path, this.index);
	}

	/**
	 * Method responsible to either traverse given path or add that to index if it
	 * is not a directory
	 * 
	 * @param path the path
	 * @throws IOException throws and IOEXception if the path is invalid
	 */
	public void build(Path path) throws IOException {
		if (Files.isDirectory(path)) {
			traverseDirectory(path);
		} else {
			addToIndex(path);
		}
	}

	/**
	 * Function used to traverse directory when path is a directory instead of a
	 * file
	 *
	 * @param directory path we want to traverse
	 * @throws IOException           throws exception if invalid path
	 * @throws FileNotFoundException throws exception if file is not found
	 */
	private void traverseDirectory(Path directory) throws IOException, FileNotFoundException {
		try (DirectoryStream<Path> listing = Files.newDirectoryStream(directory)) {
			for (Path path : listing) {
				if (Files.isDirectory(path)) {
					traverseDirectory(path);
				} else {
					String lower = path.toString().toLowerCase();
					if ((lower.endsWith(".txt")) || (lower.endsWith(".text"))) {
						addToIndex(path);
					}
				}
			}
		}
	}
}