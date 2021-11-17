import java.io.IOException;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class responsible for multithreading the query file parser
 * 
 * @author stephen
 *
 */
public class MultiThreadIndexAdder extends InvertedIndexAdder {

	/**
	 * Initalize the Logger
	 */
	private final static Logger log = LogManager.getRootLogger();

	/**
	 * Initialize the thread safe index data structure
	 */
	private final ThreadSafeInvertedIndex index;

	/**
	 * Initialize the work queue instance
	 */
	private final WorkQueue queue;

	/**
	 * Constructor
	 * 
	 * @param index index
	 * @param queue the Work Queue
	 */
	public MultiThreadIndexAdder(ThreadSafeInvertedIndex index, WorkQueue queue) {
		super(index);
		this.index = index;
		this.queue = queue;
	}

	@Override
	public void build(Path path) throws IOException {
		super.build(path);
		queue.finish();
	}

	@Override
	public void addToIndex(Path path) throws IOException {
		queue.execute(new Task(path));
	}

	/**
	 * Task for the workqueue implements runnable for the thread process
	 * 
	 * @author stephen
	 *
	 */
	private class Task implements Runnable {

		/**
		 * path
		 */
		private final Path path;

		/**
		 * Constructor
		 * 
		 * @param path the path
		 */
		public Task(Path path) {
			this.path = path;
		}

		@Override
		public void run() {
			try {

				InvertedIndex local = new InvertedIndex();
				InvertedIndexAdder.addToIndex(path, local);
				index.merge(local);

			} catch (Exception e) {
				log.warn("Could not add given path to index: " + path);
			}
		}

	}

}
