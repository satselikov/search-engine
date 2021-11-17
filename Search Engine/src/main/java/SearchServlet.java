import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.StringSubstitutor;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

// More XSS Prevention:
// https://www.owasp.org/index.php/XSS_(Cross_Site_Scripting)_Prevention_Cheat_Sheet

// Apache Comments:
// https://commons.apache.org/proper/commons-lang/download_lang.cgi

/**
 * The servlet class responsible for setting up the search servlet.
 *
 */
public class SearchServlet extends HttpServlet {

	/**
	 * serial version?
	 */
	private static final long serialVersionUID = 1L;

	/** The title to use for this webpage. */
	private static final String TITLE = "Tselikov Search Engine";

	/** The logger to use for this servlet. */
	private static Logger log = Log.getRootLogger();

	/** The thread-safe data structure to use for storing messages. */

	private ThreadSafeInvertedIndex index;

	/** Template for HTML. **/
	private final String htmlTemplate;

	/**
	 * Data Structure used to store the search results
	 */
	private ArrayList<String> output;

	/**
	 * Data Structure used to store the user history
	 */
	private ArrayList<String> history;

	/**
	 * Initializes this message board. Each message board has its own collection of
	 * messages.
	 * 
	 * @param index index structure
	 * 
	 * @throws IOException if unable to read template
	 */
	public SearchServlet(ThreadSafeInvertedIndex index) throws IOException {
		super();
		this.index = index;
		this.output = new ArrayList<>();
		this.history = new ArrayList<>();
		htmlTemplate = Files.readString(Path.of("html", "engine.html"), StandardCharsets.UTF_8);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");

		log.info("MessageServlet ID " + this.hashCode() + " handling GET request.");

		Map<String, String> values = new HashMap<>();
		values.put("title", TITLE);
		values.put("method", "POST");
		values.put("action", request.getServletPath());

		values.put("results", String.join("\n", output));
		values.put("history", String.join("\n", history));
		values.put("timestamp", getDate());

		StringSubstitutor replacer = new StringSubstitutor(values);
		String html = replacer.replace(htmlTemplate);

		PrintWriter out = response.getWriter();
		out.println(html);

		output.clear();
		out.flush();
		response.setStatus(HttpServletResponse.SC_OK);

	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");

		String query = request.getParameter("query");
		history.add(query);

		if (request.getParameter("clear") != null) {
			history.clear();
		}

		// prevent cross site scripting
		query = StringEscapeUtils.escapeHtml4(query);

		TreeSet<String> querySet = TextFileStemmer.uniqueStems(query);

		ArrayList<InvertedIndex.Result> results;

		if (request.getParameter("exact") != null) {
			results = index.exactSearch(querySet);
		} else {
			results = index.partialSearch(querySet);
		}

		for (InvertedIndex.Result each : results) {
			String formatted = "<p><a href=\"" + each.getWhere() + "\">" + each.getWhere() + "</a></p>";
			output.add(formatted);
		}
		response.setStatus(HttpServletResponse.SC_OK);
		response.sendRedirect(request.getServletPath());

	}

	/**
	 * Returns the date and time in a long format. For example: "12:00 am on
	 * Saturday, January 01 2000".
	 *
	 * @return current date and time
	 */
	private static String getDate() {
		String format = "hh:mm a 'on' EEEE, MMMM dd yyyy";
		DateFormat formatter = new SimpleDateFormat(format);
		return formatter.format(new Date());
	}
}