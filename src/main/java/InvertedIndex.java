import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/** 
 * Class responsible for storing the Inverted Index Data structure.
 *
 */
public class InvertedIndex {

	/**
	 * Class responsible for storing the Result variables as well as some helper
	 * methods
	 */
	public class Result implements Comparable<Result> {

		/**
		 * Initialize where variable
		 */
		private String where;

		/**
		 * Initialize count variable
		 */
		private int count;

		/**
		 * Initialize score variable
		 */
		private double score;

		/**
		 * Constructor, initialize everything to 0 and pass in the location
		 * 
		 * @param where where the result is
		 */
		public Result(String where) {
			this.where = where;
			this.count = 0;
			this.score = 0;
		}

		/**
		 * Getter for where the result is
		 * 
		 * @return String
		 */
		public String getWhere() {
			return this.where;
		}

		/**
		 * Getter for the count
		 * 
		 * @return Integer
		 */
		public Integer getCount() {
			return this.count;
		}

		/**
		 * Getter for the score
		 * 
		 * @return double
		 */
		public double getScore() {
			return this.score;
		}

		/**
		 * Method responsible for calculating the score score = total matches / total
		 * words
		 * 
		 * @param word the word
		 */
		private void calcScore(String word) {
			this.count += index.get(word).get(where).size();
			this.score = (double) this.count / countMap.get(where);
		}

		@Override
		public int compareTo(Result result) {
			if (this.score == result.score && this.count == result.count) {
				return this.where.compareToIgnoreCase(result.where);
			} else if (this.score == result.score) {
				return Integer.compare(result.count, this.count);
			} else {
				return Double.compare(result.score, this.score);
			}
		}

		@Override
		public String toString() {
			return "where=" + where + ", count=" + count + ", score=" + score;
		}

	}

	/**
	 * Index data structure initialization
	 */
	private final TreeMap<String, Map<String, TreeSet<Integer>>> index;

	/**
	 * The count map initialization
	 */
	private final TreeMap<String, Integer> countMap;

	/**
	 * Getter for the countMap
	 * 
	 * @return Map
	 */
	public Map<String, Integer> getCountMap() {
		return Collections.unmodifiableMap(countMap);
	}

	/**
	 * Constructor for the class
	 */
	public InvertedIndex() {
		this.index = new TreeMap<String, Map<String, TreeSet<Integer>>>();
		this.countMap = new TreeMap<String, Integer>();
	}

	/**
	 * Adds a given word, it's location path, and position in the text file to the
	 * Inverted Index data structure
	 *
	 * @param word     adds the word
	 * @param location adds the path, which file the word was found
	 * @param position where the word was in the file
	 */
	public void add(String word, String location, Integer position) {
		this.index.putIfAbsent(word, new TreeMap<>());
		this.index.get(word).putIfAbsent(location, new TreeSet<>());
		this.index.get(word).get(location).add(position);
		if (this.countMap.getOrDefault(location, 0) < position) {
			this.countMap.put(location, position);
		}

	}

	/**
	 * If the word is a key in the index, this returns an unmodifiable view of the
	 * inner map's keys
	 * 
	 * @param word the word
	 * @return Set
	 */
	public Set<String> getLocations(String word) {
		return contains(word) ? Collections.unmodifiableSet(this.index.get(word).keySet()) : Collections.emptySet();
	}

	/**
	 * Method returns, safely, the set of positions for that word and location
	 * 
	 * @param word     the word
	 * @param location the location
	 * @return Set
	 */
	public Set<Integer> getPositions(String word, String location) {
		return contains(word, location) ? Collections.unmodifiableSet(this.index.get(word).get(location))
				: Collections.emptySet();
	}

	/**
	 * Method returns the size of the index
	 * 
	 * @return int
	 */
	public int size() {
		return index.size();
	}

	/**
	 * Method returns the size of how many paths are attached to a word
	 * 
	 * @param word the word
	 * @return int
	 */
	public int numLocation(String word) {
		return contains(word) ? index.get(word).size() : 0;
	}

	/**
	 * Method returns the size of how many positions are at a given location for a
	 * certain word
	 * 
	 * @param word     the word
	 * @param location the location
	 * @return int
	 */
	public int numPosition(String word, String location) {
		return contains(word, location) ? index.get(word).get(location).size() : 0;
	}

	/**
	 * Method checks if the index contains a passed in word
	 * 
	 * @param word the word
	 * @return boolean
	 */
	public boolean contains(String word) {
		return index.containsKey(word);
	}

	/**
	 * Method checks to see if the index contains a word at a given location
	 * 
	 * @param word     the word
	 * @param location the location
	 * @return boolean
	 */
	public boolean contains(String word, String location) {
		return contains(word) && index.get(word).containsKey(location);
	}

	/**
	 * Method checks to see if the index contains a word at a given location at a
	 * given position
	 * 
	 * @param word     the word
	 * @param location the location
	 * @param position the position
	 * @return boolean
	 */
	public boolean contains(String word, String location, int position) {
		return contains(word, location) ? index.get(word).get(location).contains(position) : false;
	}

	/**
	 * ToString method
	 */
	@Override
	public String toString() {
		return "InvertedIndex : [index=" + index + "]";
	}

	/**
	 * Method responsible for performing the exact search given a query Line
	 * 
	 * @param queries the query line
	 * @return ArrayList
	 */
	public ArrayList<Result> exactSearch(Set<String> queries) {
		ArrayList<Result> Result = new ArrayList<>();
		HashMap<String, Result> total = new HashMap<>();
		for (String query : queries) {
			if (index.containsKey(query)) {
				buildSearch(Result, query, total);
			}
		}
		Collections.sort(Result);
		return Result;
	}

	/**
	 * Method responsible for performing the partial search given a queryLine
	 * 
	 * @param queries the query line
	 * @return ArrayList
	 */
	public ArrayList<Result> partialSearch(Set<String> queries) {
		ArrayList<Result> Result = new ArrayList<>();
		HashMap<String, Result> total = new HashMap<>();
		for (String query : queries) {
			for (String stem : this.index.keySet()) {
				if (stem.startsWith(query)) {
					buildSearch(Result, stem, total);
				}
			}
		}
		Collections.sort(Result);
		return Result;
	}

	/**
	 * Helper Method for QueryFileParser to determine which search to use
	 * 
	 * @param queries queries being passed in
	 * @param exact   boolean to determine search
	 * @return ArrayList
	 */
	public ArrayList<Result> search(Set<String> queries, boolean exact) {
		return exact ? exactSearch(queries) : partialSearch(queries);
	}

	/**
	 * Helper method for both the exact and partial search. Goes through the
	 * location and builds a new result if the location is not present
	 * 
	 * @param Result the search Result
	 * @param query  the query
	 * @param total  the location linked to the result
	 */
	private void buildSearch(ArrayList<Result> Result, String query, HashMap<String, Result> total) {
		for (String location : this.index.get(query).keySet()) {
			if (!total.containsKey(location)) {
				Result result = new Result(location);
				total.put(location, result);
				Result.add(result);
			}
			total.get(location).calcScore(query);
		}
	}

	/**
	 * Function called by the driver to output the index in a pretty JSON format
	 * 
	 * @param path the path passed in
	 * @throws IOException if path is invalid
	 */
	public void toJson(Path path) throws IOException {
		SimpleJsonWriter.asIndex(index, path);
	}

	/**
	 * Method responsible for merging the inverted index with the current index
	 * 
	 * @param local local inverted index data structure
	 */
	public void merge(InvertedIndex local) {
		for (String key : local.index.keySet()) {
			this.index.putIfAbsent(key, local.index.get(key));
			for (String location : local.index.get(key).keySet()) {
				if (this.index.get(key).containsKey(location)) {
					this.index.get(key).get(location).addAll(local.index.get(key).get(location));
				} else {
					this.index.get(key).put(location, local.index.get(key).get(location));
				}
			}
		}
		for (String key : local.countMap.keySet()) {
			if (this.countMap.containsKey(key)) {
				if (this.countMap.get(key) < local.countMap.get(key)) {
					this.countMap.put(key, local.countMap.get(key));
				}
			} else {
				this.countMap.put(key, local.countMap.get(key));
			}
		}
	}
}