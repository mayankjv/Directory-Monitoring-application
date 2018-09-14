package model;

/**
 * The task of watching a directory is carried out in a separate thread so that it can always listen to the changes while the user is using the application.
 */

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;

import org.apache.log4j.Logger;

/**
 * A class which is responsible for continuously monitoring a directory along
 * with all its subdirectories. It contains a single istance of WatchService and
 * multiple keys, each of which is assigned to watch a particular folder.
 * 
 * @author mayank.patel
 *
 */
public class WatchThread extends Thread {

	private static final String PATH_SEPARATOR = "\\";

	public final static Logger logger = Logger.getLogger(WatchThread.class);
	private WatchService watchService;
	private HashMap<Path, WatchKey> map;
	private String path = "";
	private FileStatistics fileStatistics;

	/**
	 * method for getting the parent directory path for registering the directory
	 * and the sub directories
	 * 
	 * @param path_string
	 * @param fileStatistics
	 */
	public void setPath(String path_string, FileStatistics fileStatistics) {
		path = path_string;
		this.fileStatistics = fileStatistics;

	}
	public void registerNewPath(String path) {

		File directory = new File(path);
		Path _directory = Paths.get(path);

		map = new HashMap<Path, WatchKey>();
		try {
			registerDirectory(_directory);
		} catch (IOException e1) {
		}

		// fList contains the list of all the files and folders at a given path.
		File[] fList = directory.listFiles();
		// A loop that registers all the subdirectories of the folder with the
		// watchservice so that they can be monitored for changes
		for (int i = 0; i < fList.length; i++) {
			File file = fList[i];
			// When the current entry in the list is a directory, it needs to be registered
			// with the watchService
			if (file.isDirectory()) {
				try {
					String new_path = path + PATH_SEPARATOR + file.getName();
					Path _directoryToWatch = Paths.get(new_path);
					registerDirectory(_directoryToWatch);
				} catch (IOException e) {
					System.err.println(e);
				}
			}
		}

		System.out.println("New Path Registered: "+path);
		
	}
	/**
	 * Run method of the thread that will be executed in parallel with the main
	 * thread
	 */
	public void run() {

		try {
			watchService = FileSystems.getDefault().newWatchService(); // instantiating watchService
			File directory = new File(path);
			Path _directory = Paths.get(path);

			map = new HashMap<Path, WatchKey>();
			registerDirectory(_directory);

			// fList contains the list of all the files and folders at a given path.
			File[] fList = directory.listFiles();
			// A loop that registers all the subdirectories of the folder with the
			// watchservice so that they can be monitored for changes
			for (int i = 0; i < fList.length; i++) {
				File file = fList[i];
				// When the current entry in the list is a directory, it needs to be registered
				// with the watchService
				if (file.isDirectory()) {
					try {
						String new_path = path + PATH_SEPARATOR + file.getName();
						Path _directoryToWatch = Paths.get(new_path);
						registerDirectory(_directoryToWatch);
					} catch (IOException e) {
						System.err.println(e);
					}
				}
			}
		} catch (Exception e) {
			System.err.println(e);
		}
		Boolean valid = true;
		// Infinite loop to keep the Watch Thread always powered up and running.
		do {
			try {
				// This statement will wait until an event is encoutered.
				WatchKey watchKey = watchService.take();
				for (WatchEvent<?> event : watchKey.pollEvents()) {

					// When the event is creation of a new Entry
					if (StandardWatchEventKinds.ENTRY_CREATE.equals(event.kind())) {
						String fileName = event.context().toString();
						// Getting the path in order to add the new file to the ArrayList that is
						// maintained in PathMap class
						Path dir = (Path) watchKey.watchable();
						String pathToNewDirectory = dir.resolve((Path) event.context()).toString();
						File temp = new File(pathToNewDirectory);
						// If the newly added entry is a folder, registering it with the watchService
						if (temp.isDirectory()) {
							logger.info("Folder Created: " + fileName);
							Path innerFolder = Paths.get(pathToNewDirectory);
							registerDirectory(innerFolder);
							fileStatistics.updateHashMap(fileName, pathToNewDirectory, 0);
						}
						// If the newly added entry is a file, adding it to the ArrayList
						else {
							logger.info("File Created:" + fileName);
							fileStatistics.updateHashMap(fileName, pathToNewDirectory, 0);
						}
					}
					// When the new entry is a Delete event
					else if (StandardWatchEventKinds.ENTRY_DELETE.equals(event.kind())) {
						String fileName = event.context().toString();
						// Getting the path.
						Path dir = (Path) watchKey.watchable();
						String path_to_be_passed = dir.resolve((Path) event.context()).toString();
						if (fileName.indexOf('.') < 0) {
							logger.info("Folder deleted:" + fileName);
							fileStatistics.updateHashMap(fileName, path_to_be_passed, 11);
						}
						// if the deleted entry is a file, display a message and delete the entry from
						// the ArrayList
						else {
							logger.info("File Deleted:" + fileName);
							fileStatistics.updateHashMap(fileName, path_to_be_passed, 12);
						}

					}
					// When the event is a Modification
					else {
						String fileName = event.context().toString();
						Path dir = (Path) watchKey.watchable();
						String path_to_be_passed = dir.resolve((Path) event.context()).toString();
						fileStatistics.updateHashMap(fileName, path_to_be_passed, 2);
						System.out.println("updateHashMap called for modification event");
						System.out.println("File Name: "+fileName);
						System.out.println("Path: "+path_to_be_passed);
					}

				}
				valid = watchKey.reset();
				// when a folder is deleted, the corresponding watchkey is deleted
				if (!valid)
					map.remove(watchKey);
			} catch (Exception e) {
				continue;
			}

		} while (!map.isEmpty());
	}

	/**
	 * funtion that registers a directory with the watcher service
	 * 
	 * @param _directory
	 * @throws IOException
	 */
	private void registerDirectory(Path _directory) throws IOException {
		// Registering the directory with watchService so as to monitor it.
		WatchKey key = _directory.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
				StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
		// Putting the key created above in the map
		map.put(_directory, key);
	}

}
