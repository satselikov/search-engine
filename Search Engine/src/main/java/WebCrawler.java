import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/** 
 * Class Responsible for crawling the web rather than local files
 * @author stephen
 *
 */
public class WebCrawler {

	/**
	 * stemmer used in the add
	 */
	public static final SnowballStemmer.ALGORITHM DEFAULT = SnowballStemmer.ALGORITHM.ENGLISH;

	/**
	 * initialize thread safe inverted index data structure
	 */
	private final ThreadSafeInvertedIndex index;

	/**
	 * initialize the queue
	 */
	public WorkQueue queue;
	
	/**
	 * initialize the set that holds all of the links visited
	 */
	private HashSet<URL> links;

	/**
	 * total number of URLs to crawl (including the seed URL)
	 */
	private int total;

	/**
	 * Constructor 
	 * @param index index data structure 
	 * @param queue the work queue 
	 * @param url the seed URL
	 * @param total total number of URLs to crawl
	 */
	public WebCrawler(ThreadSafeInvertedIndex index, WorkQueue queue, URL url, int total) {
		this.index = index;
		this.queue = queue;
		this.total = total;
		this.links = new HashSet<URL>();

	}

	/**
	 * crawl function called by the driver (starts the king task)
	 * @param url seed url
	 */
	public void crawl(URL url) {
		links.add(url);
		queue.execute(new Task(url));
		queue.finish();
	}

	/**
	 * Add function that parses the given html and adds it to the index data
	 * structure
	 * 
	 * @param url   location
	 * @param html  the content
	 * @param index data structure
	 * @throws IOException throws exception if an IO error occurs
	 */
	private void addHtmlToIndex(String url, String html, ThreadSafeInvertedIndex index) throws IOException {
		SnowballStemmer stemmer = new SnowballStemmer(TextFileStemmer.DEFAULT);
		int counter = 1;
		String[] parsedLine = TextParser.parse(html);
		
		for (String word : parsedLine) {
			word = stemmer.stem(word).toString();
			index.add(word, url, counter);
			counter++;
		}
	}

	/**
	 * Fetches, cleans, parses the url (gets the links)
	 * 
	 * @param url given url
	 * @return String
	 */
	public String parse(URL url) {
		//redirects 3 to avoid infinite crawl
		String fetchedHtml = HtmlFetcher.fetch(url, 3);
		if (fetchedHtml == null) {
			return "";
		} else {
			fetchedHtml = HtmlCleaner.stripBlockElements(fetchedHtml);
			ArrayList<URL> urls = LinkParser.getValidLinks(url, fetchedHtml);
			fetchedHtml = HtmlCleaner.stripHtml(fetchedHtml);
			traverseUrls(urls); 
			return fetchedHtml;
		}
	}
	
	/**
	 * Helper method for parse. Traverses the given url list and checks the links
	 * data structure to see if each of the urls have been visited. If all
	 * conditions pass, executes a mini-task
	 * 
	 * @param urls the list of valid links
	 */
	public void traverseUrls(ArrayList<URL> urls) {
		for (URL each : urls) {
			if (!links.contains(each)) {
				if (links.size() < total) {
					links.add(each);
					queue.execute(new Task(each));
				}
			}
		}
	}

	/**
	 * Task class for the work queue, implements runnable and launches threads for the web crawler
	 * @author stephen
	 *
	 */
	private class Task implements Runnable {

		/**
		 * url being passed in
		 */
		private final URL url;

		/**
		 * Constructor for the task class
		 * @param url the url 
		 */
		public Task(URL url) {
			this.url = url;
		}

		@Override
		public void run() {
			String fetch = parse(url);
			ThreadSafeInvertedIndex local = new ThreadSafeInvertedIndex(); 
			try {
				addHtmlToIndex(url.toString(), fetch, local);
			} catch (IOException e) {
				e.printStackTrace();
			}
			index.merge(local);
		}
	}
}
