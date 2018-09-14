package model;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import org.apache.log4j.Logger;
import controller.ControllerForFileStats;

public class CreateIndex {

	public final static Logger logger = Logger.getLogger(CreateIndex.class);
	private static final String PATH_SEPARATOR = "\\";
	private PathMap pathMap;
	private FileStatistics fileStatistics;
	private ControllerForFileStats controllerForFileStats = new ControllerForFileStats();
	public int status = 0;
	public int total = 0;
	public int current = 0;

	/**
	 * function used to set the parameters before the thread is run.
	 * 
	 * @param pathMap
	 * @param controllerForFileStats
	 */
	public void setPathMap(PathMap pathMap, ControllerForFileStats controllerForFileStats,
			FileStatistics fileStatistics) {
		this.pathMap = pathMap;
		this.controllerForFileStats = controllerForFileStats;
		this.fileStatistics = fileStatistics;
		calculateTotalNumberOfFiles(pathMap.path_string);

	}

	/**
	 * Running the thread in background
	 */
	public void run() {
		addFoldersToMap(pathMap.path_string);
		pathMap.dumpInFile();
	}

	/**
	 * This function calculates total number of files present inside a folder and in
	 * the sub-folders.
	 * 
	 * @param path
	 */
	private void calculateTotalNumberOfFiles(String path) {

		Path path_ = Paths.get(path);
		FileCounter countFiles = new FileCounter();
		try {
			Files.walkFileTree(path_, countFiles);
		} catch (IOException e) {
			logger.error("Exception in walkFileTree");
		}
		total = countFiles.totalFiles - 1;
		System.out.println("Total : " + total);
	}

	/**
	 * This method traverses through the folders and the subfolders and stores all
	 * the files that are present in a HashMap
	 * 
	 * @param path
	 */
	public void addFoldersToMap(String path) {
		String key = path;
		if (!pathMap.directoryHierarchy.containsKey(extractNameFromPath(key))) {
			pathMap.directoryHierarchy.put(extractNameFromPath(key), new ArrayList<DirectoryFile>());
			if (!fileStatistics.pathStringMap.containsKey(path)) {
				fileStatistics.pathStringMap.put(path, pathMap);
			}

		}
		try {
			File directory = new File(path);
			File[] fList = directory.listFiles();
			for (int i = 0; i < fList.length; i++) {
				File file = fList[i];
				if (file.isFile()) {
					DirectoryFile to_be_added = new DirectoryFile(file,ControllerForFileStats.stopwords);
					pathMap.files.add(to_be_added);
					ArrayList<DirectoryFile> ret = pathMap.directoryHierarchy.get(extractNameFromPath(key));
					ret.add(to_be_added);
					pathMap.directoryHierarchy.put(extractNameFromPath(key), ret);
					if (!fileStatistics.pathStringMap.containsKey(path)) {
						fileStatistics.pathStringMap.put(path, pathMap);
					}

					current++;
					status = (int) (current * 100) / total;
					sendStatus(status);
				} else {

					DirectoryFile to_be_added = new DirectoryFile(file,ControllerForFileStats.stopwords);
					pathMap.files.add(to_be_added);
					ArrayList<DirectoryFile> ret = pathMap.directoryHierarchy.get(extractNameFromPath(key));
					ret.add(to_be_added);
					pathMap.directoryHierarchy.put(extractNameFromPath(key), ret);
					if (!fileStatistics.pathStringMap.containsKey(path)) {
						fileStatistics.pathStringMap.put(path, pathMap);
					}

					String new_key = path + PATH_SEPARATOR + file.getName();
					if (!pathMap.directoryHierarchy.containsKey(extractNameFromPath(new_key))) {
						pathMap.directoryHierarchy.put(extractNameFromPath(new_key), new ArrayList<DirectoryFile>());
						if (!fileStatistics.pathStringMap.containsKey(new_key)) {
							fileStatistics.pathStringMap.put(path, pathMap);
						}
					}
					current++;
					status = (int) (current * 100) / total;
					sendStatus(status);
					addFoldersToMap(new_key);
				}
			}
			logger.info("All folders added for path: " + path);
		} catch (Exception e) {
			logger.error("Exception in addFoldersToMap, path:" + path);
		}
	}

	/**
	 * function that sends the status of indexing to frontend via Web Socket
	 * 
	 * @param status
	 */
	public void sendStatus(int status) {
		System.out.println("Current: " + current);
		this.controllerForFileStats.sendProgress(status);
		logger.info("Status sent to client: " + status);
	}

	/**
	 * function that takes path as a parameter and returns the name of the file
	 * 
	 * @param path
	 * @return name of the folder/file.
	 */
	public String extractNameFromPath(String path) {

		if (path.contains(PATH_SEPARATOR)) {
			String res = path.substring(path.lastIndexOf(PATH_SEPARATOR) + 1);
			return res;
		} else {
			return path;
		}
	}

}

class FileCounter implements FileVisitor<Object> {

	int totalFiles;

	FileCounter() {
		totalFiles = 0;
	}

	@Override
	public FileVisitResult preVisitDirectory(Object dir, BasicFileAttributes attrs) throws IOException {
		totalFiles++;
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFile(Object file, BasicFileAttributes attrs) throws IOException {
		totalFiles++;
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFileFailed(Object file, IOException exc) throws IOException {
		totalFiles++;
		CreateIndex.logger.error("Failed to load file:" + file);
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult postVisitDirectory(Object dir, IOException exc) throws IOException {

		return FileVisitResult.CONTINUE;
	}

}
