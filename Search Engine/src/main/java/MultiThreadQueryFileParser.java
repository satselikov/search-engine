import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** 
 * Class responsible for multithreading the query file parser
 * 
 * @author stephen
 *
 */
public class MultiThreadQueryFileParser implements QueryFileParserInterface {

	/**
	 * Initialize Logger
	 */
	private final static Logger log = LogManager.getRootLogger();

	/**
	 * initialize instance of the index data structure
	 */
	private final ThreadSafeInvertedIndex index;

	/**
	 * initialize instance of the results map
	 */
	private final TreeMap<String, ArrayList<InvertedIndex.Result>> results;

	/**
	 * Initialize instance of work queue
	 */
	private final WorkQueue queue;

	/**
	 * Constructor for the multithreaded query parser
	 * 
	 * @param index the index being passed in (thread safe in this case)
	 * @param queue the work queue
	 */
	public MultiThreadQueryFileParser(ThreadSafeInvertedIndex index, WorkQueue queue) {
		this.results = new TreeMap<>();
		this.index = index;
		this.queue = queue;
	}

	@Override
	public void queryFile(Path queryFile, boolean exact) throws IOException {
		QueryFileParserInterface.super.queryFile(queryFile, exact);
		queue.finish();
	}

	@Override
	public void parseQueryLine(String line, boolean exact) {
		queue.execute(new Task(line, exact));
	}

	@Override
	public void resultWriteJson(Path path) throws IOException {
		synchronized(results) {
			SimpleJsonWriter.asSearch(this.results, path);
		}
	}

	/**
	 * Task for the workqueue implements runnable for the thread process
	 * 
	 * @author stephen
	 *
	 */
	private class Task implements Runnable {
		/**
		 * line being passed in to the task
		 */
		private final String line;
		/**
		 * which search to use
		 */
		private final boolean exact;

		/**
		 * Constructor
		 * 
		 * @param line  the query
		 * @param exact which search to use
		 */
		public Task(String line, boolean exact) {
			this.line = line;
			this.exact = exact;
		}

		@Override
		public void run() {
			try {
				TreeSet<String> queryLine = TextFileStemmer.uniqueStems(line);
				String query = String.join(" ", queryLine);

				if (!query.isEmpty()) {
					synchronized (results) {
						if (results.containsKey(query)) {
							return;
						}
					}
					ArrayList<InvertedIndex.Result> temp = index.search(queryLine, exact);

					synchronized (results) {
						results.put(query, temp);
					}
				}
			} catch (Exception e) {
				log.error("Could not add given line: " + line);
			}
		}
	}
}
