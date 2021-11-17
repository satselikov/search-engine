import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/** 
 * Class responsible for making the invertedindex thread safe using the
 * SimpleReadWriteLock
 * 
 * @author stephen
 *
 */
public class ThreadSafeInvertedIndex extends InvertedIndex {

	/**
	 * lock instance
	 */
	private final SimpleReadWriteLock lock;

	/**
	 * Constructor
	 */
	public ThreadSafeInvertedIndex() {
		super();
		lock = new SimpleReadWriteLock();
	}

	@Override
	public Map<String, Integer> getCountMap() {
		lock.readLock().lock();
		try {
			return super.getCountMap();
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public void add(String word, String location, Integer position) {
		lock.writeLock().lock();
		try {
			super.add(word, location, position);
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public Set<String> getLocations(String word) {
		lock.readLock().lock();
		try {
			return super.getLocations(word);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public Set<Integer> getPositions(String word, String location) {
		lock.readLock().lock();
		try {
			return super.getPositions(word, location);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public int size() {
		lock.readLock().lock();
		try {
			return super.size();
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public int numLocation(String word) {
		lock.readLock().lock();
		try {
			return super.numLocation(word);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public int numPosition(String word, String location) {
		lock.readLock().lock();
		try {
			return super.numPosition(word, location);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public boolean contains(String word) {
		lock.readLock().lock();
		try {
			return super.contains(word);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public boolean contains(String word, String location) {
		lock.readLock().lock();
		try {
			return super.contains(word, location);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public boolean contains(String word, String location, int position) {
		lock.readLock().lock();
		try {
			return super.contains(word, location, position);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public String toString() {
		lock.readLock().lock();
		try {
			return super.toString();
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public void toJson(Path path) throws IOException {
		lock.readLock().lock();
		try {
			super.toJson(path);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public ArrayList<Result> exactSearch(Set<String> queries) {
		lock.readLock().lock();
		try {
			return super.exactSearch(queries);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public ArrayList<Result> partialSearch(Set<String> queries) {
		lock.readLock().lock();
		try {
			return super.partialSearch(queries);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public void merge(InvertedIndex local) {
		lock.writeLock().lock();
		try {
			super.merge(local);
		} finally {
			lock.writeLock().unlock();
		}
	}
}
