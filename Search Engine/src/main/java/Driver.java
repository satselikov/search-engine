import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * Class responsible for running this project based on the provided command-line
 * arguments. See the README for details.
 */
public class Driver {

	/**
	 * Initializes the classes necessary based on the provided command-line
	 * arguments. This includes (but is not limited to) how to build or search an
	 * inverted index.
	 *
	 * @param args flag/value pairs used to start this program
	 *
	 */
	public static void main(String[] args) {
		
		// store initial start time
		Instant start = Instant.now();
		
		ArgumentMap argument = new ArgumentMap(args);
		

		InvertedIndex index;
		InvertedIndexAdder adder;
		QueryFileParserInterface queryParser;

		int threads = 0;
		WorkQueue queue = null;

		WebCrawler crawler = null;

		// multithreading
		if (argument.hasFlag("-threads") || argument.hasFlag("-url")) {
			try {
				threads = argument.getInteger("-threads", 5);
			} catch (Exception e) {
				threads = 5;
			}
			if (threads <= 0) {
				threads = 5;
			}
			// initalize all the instances here
			queue = new WorkQueue(threads);

			ThreadSafeInvertedIndex threadsafeIndex = new ThreadSafeInvertedIndex();
			index = threadsafeIndex;
			adder = new MultiThreadIndexAdder(threadsafeIndex, queue);
			queryParser = new MultiThreadQueryFileParser(threadsafeIndex, queue);

			if (argument.hasFlag("-url")) {
				URL url = null;
				int total = 0;
				String seedURL = argument.getString("-url");
				if (argument.hasFlag("-max")) {
					total = argument.getInteger("-max", 1);
				}
				crawler = new WebCrawler(threadsafeIndex, queue, url, total);
				try {
					url = new URL(seedURL);
					crawler.crawl(url);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
			int port;
			if(argument.hasFlag("-server")){
				try {
					port = argument.getInteger("-server", 8080);
					System.out.println(port);
				}
				catch(Exception e) {
					port = 8080;
				}
				try {
					Server server = new Server(port);
					ServletHandler handler = new ServletHandler();
					ServletHolder holder = new ServletHolder(new SearchServlet(threadsafeIndex));
					handler.addServletWithMapping(holder , "/");
					server.setHandler(handler);
					server.start();
					server.join();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else { // if single threading (threads not present)
			index = new InvertedIndex();
			adder = new InvertedIndexAdder(index);
			queryParser = new QueryFileParser(index);
		}

		if (argument.hasFlag("-path") && argument.getPath("-path") != null) {
			Path path = argument.getPath("-path");

			try {
				adder.build(path);
			} catch (IOException e) {
				System.out.println("unable to build inverted index from given path " + path);
				return;
			}
		}

		if (argument.hasFlag("-index")) {
			Path output = argument.getPath("-index", Path.of("index.json"));

			try {
				index.toJson(output);
			} catch (IOException e) {
				System.out.println("unable to write to inverted index from given output " + output);
				return;
			}
		}

		if (argument.hasFlag("-counts")) {
			Path countOutput = argument.getPath("-counts", Path.of("counts.json"));
			try {
				SimpleJsonWriter.asObject(index.getCountMap(), countOutput);
			} catch (IOException e) {
				System.out.println("unable to write a count map from given output " + countOutput);
			}
		}

		if (argument.hasFlag("-queries")) {
			Path queryPath = argument.getPath("-queries");
			try {
				queryParser.queryFile(queryPath, argument.hasFlag("-exact"));
			} catch (Exception e) {
				System.out.println("unable to write query from given flag" + queryPath);
				return;
			}
		}

		if (argument.hasFlag("-results")) {
			Path results = argument.getPath("-results", Path.of("results.json"));
			try {
				queryParser.resultWriteJson(results);
			} catch (IOException e) {
				System.out.println("unable to write results from given flag" + results);
			}
		}

		if (queue != null) {
			queue.shutdown();
		}

		// calculate time elapsed and output
		Duration elapsed = Duration.between(start, Instant.now());
		double seconds = (double) elapsed.toMillis() / Duration.ofSeconds(1).toMillis();
		System.out.printf("Elapsed: %f seconds%n", seconds);
	}

}