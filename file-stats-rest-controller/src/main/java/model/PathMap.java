package model;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import controller.ControllerForFileStats;

public class PathMap {

	private static final char DOT = '.';
//	private static final String UNIX_PATH_SEPARATOR = "//";
//	private static final String PATH_SEPARATOR = "\\";
	public final static Logger logger = Logger.getLogger(PathMap.class);
	public String path_string;
	public FileStatistics fileStatistics;
	public ArrayList<DirectoryFile> files = new ArrayList<DirectoryFile>();
	public HashMap<String, ArrayList<DirectoryFile>> directoryHierarchy = new HashMap<String, ArrayList<DirectoryFile>>();
	public String indexFilePath = "C:" + File.separator + "Users" + File.separator + "mayank.patel" + File.separator
			+ "Desktop" + File.separator + "Java Projects" + File.separator + "Assignment_2" + File.separator
			+ "file-stats-rest-controller" + File.separator + "Index";
	private ControllerForFileStats controllerForFileStats = new ControllerForFileStats();

	/**
	 * over loaded parameterized constructor to PathMap
	 * 
	 * @param path_to_folder
	 * @param fileStatistics
	 * @param controllerForFileStats
	 * @param isSubfolder
	 */
	public PathMap(String path_to_folder, FileStatistics fileStatistics,
			ControllerForFileStats controllerForFileStats) {
		logger.info("PathMap object created, Path: " + path_to_folder);
		path_string = path_to_folder;
		this.controllerForFileStats = controllerForFileStats;
		CreateIndex thread = new CreateIndex();
		this.fileStatistics = fileStatistics;
		thread.setPathMap(this, this.controllerForFileStats, this.fileStatistics);
		thread.run();
	}

	/**
	 * 
	 * @param directoryName
	 * @param currentPath
	 */
	private void sendWatcherEventUpdate(String directoryName, String currentPath) {
		this.controllerForFileStats.sendWatcherEvent(directoryHierarchy.get(extractNameFromPath(currentPath)),
				directoryName);
		System.out.println("Update sent");
		logger.info("Watcher event sent to client, DirectoryName: " + directoryName + " Path: " + currentPath);
	}

	/**
	 * function used to dump the data of primary data structure (HashMap) in json
	 * file
	 */
	public void dumpInFile() {
		try {
			File file = new File(Paths.get("").toAbsolutePath().toString() + File.separator + "index.ser");

			FileOutputStream fos = new FileOutputStream(file);

			BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fos);

			ObjectOutputStream objStream1 = new ObjectOutputStream(bufferedOutputStream);

			objStream1.writeObject(directoryHierarchy);

			logger.info("Index Created !");

			objStream1.close();

			bufferedOutputStream.close();

			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception dumping in the file");
		}
	}

	/**
	 * public method that will help the user to get the list of the files present in
	 * a given path_string.
	 * 
	 * @return files
	 */
	public ArrayList<DirectoryFile> get_files() {
		return this.files;
	}

	/**
	 * function that returns the value of HashMap to the controller
	 * 
	 * @return directory hierarchy map.
	 */
	public HashMap<String, ArrayList<DirectoryFile>> get_map() {
		return directoryHierarchy;
	}

	/**
	 * Overridden Equals method, that compares two PathMaps based on their Path
	 * strings.
	 */
	@Override
	public boolean equals(Object p) {
		if (p == this)
			return true;
		PathMap p1 = (PathMap) p;
		return p1.path_string.equals(this.path_string);
	}

	@Override
	public int hashCode() {
		return (int) (new DirectoryFile(new File(this.path_string), ControllerForFileStats.stopwords))
				.get_last_modified();
	}

	/**
	 * This method updates the value of the Map when a watcher event is encountered
	 * 
	 * @param name
	 * @param path
	 * @param typeOfEvent
	 */
	public void updateMap(String name, String path, int typeOfEvent) {
		try {
			System.out.println("Update Map called in PathMap");
			System.out.println("File Name: " + name);
			System.out.println("path: " + path);
			// 0 is used for event of kind Create
			if (typeOfEvent == 0) {
				File file = new File(path);
				if (file.isDirectory()) {
					logger.info("Watcher event: Folder added, path: " + path);
					directoryHierarchy.put(extractNameFromPath(path), new ArrayList<DirectoryFile>());
					ArrayList<DirectoryFile> ret = directoryHierarchy
							.get(extractNameFromPath(path.substring(0, path.indexOf(name) - 1)));
					ret.add(new DirectoryFile(file, ControllerForFileStats.stopwords));
					directoryHierarchy.put(extractNameFromPath(path.substring(0, path.indexOf(name) - 1)), ret);
				} else {
					logger.info("Watcher event: File added, path: " + path);
					DirectoryFile to_be_added = new DirectoryFile(file, ControllerForFileStats.stopwords);
					String pm = path.substring(0, path.indexOf(name) - 1);
					files.add(to_be_added);
					ArrayList<DirectoryFile> ret;
					if (directoryHierarchy.containsKey(extractNameFromPath(pm))) {
						ret = directoryHierarchy.get(extractNameFromPath(pm));
						int currIndex = 0;
						for (DirectoryFile directoryFile : ret) {
							if ((directoryFile.get_file_name() + DOT + directoryFile.get_type()).equals(name)) {
								ret.remove(currIndex);
								break;
							}
							currIndex++;
						}
						ret.add(to_be_added);
					} else
						ret = new ArrayList<DirectoryFile>();
					directoryHierarchy.put(extractNameFromPath(pm), ret);
				}
			}
			// 12 is used when a file is being deleted
			else if (typeOfEvent == 12) {
				logger.info("Watcher event: File deleted, path: " + path);
				String pm = path.substring(0, path.indexOf(name) - 1);
				ArrayList<DirectoryFile> ret = directoryHierarchy.get(extractNameFromPath(pm));
				for (DirectoryFile current : ret) {
					if (current.get_file_name().equals(name.substring(0, name.indexOf(DOT))))
						ret.remove(current);
				}
				directoryHierarchy.put(extractNameFromPath(pm), ret);
			}
			// 11 is used when a folder is deleted
			else if (typeOfEvent == 11) {
				logger.info("Watcher event: Folder deleted, path: " + path);
				ArrayList<DirectoryFile> temp = directoryHierarchy.get(extractNameFromPath(path));
				deleteFolder(temp, path, name);
				String pm = path.substring(0, path.indexOf(name) - 1);
				ArrayList<DirectoryFile> ret = directoryHierarchy.get(extractNameFromPath(pm));
				for (DirectoryFile current : ret) {
					if (current.get_file_name().equals(name)) {
						ret.remove(current);
					}
				}
				directoryHierarchy.put(extractNameFromPath(pm), ret);
			}
			// modification event
			else {

				logger.info("Watcher event: Modification, path: " + path);
				updateMap(name, path, 12);
				updateMap(name, path, 0);
			}
		} catch (Exception e) {
			logger.error("exception while updating map!");
		}
		String currentDirectory = path.substring(0, path.indexOf(name) - 1);

		// sending the watched event processed to frontend through we socket
		sendWatcherEventUpdate(currentDirectory.substring(currentDirectory.lastIndexOf(File.separator) + 1),
				currentDirectory);

	}

	/**
	 * this funstion deleted all the subfolders and files from the map when a folder
	 * is deleted from memory.
	 * 
	 * @param temp
	 * @param path
	 * @param name
	 */
	private void deleteFolder(ArrayList<DirectoryFile> temp, String path, String name) {
		for (DirectoryFile f : temp) {
			if (f.get_type().equals("Folder")) {
				String newPath = path.substring(0, path.indexOf(name) - 1);
				deleteFolder(directoryHierarchy.get(extractNameFromPath(newPath)), newPath,
						newPath.substring(newPath.lastIndexOf(File.separator) + 1));
			}
		}
		logger.info("Folder deleted from map, path: " + path);
	}

	/**
	 * function that takes path as a parameter and returns the name of the file
	 * 
	 * @param path
	 * @return name of the folder/file.
	 */
	public String extractNameFromPath(String path) {

		if (path.contains(File.separator)) {
			String res = path.substring(path.lastIndexOf(File.separator) + 1);
			return res;
		} else {
			return path;
		}
	}

}
