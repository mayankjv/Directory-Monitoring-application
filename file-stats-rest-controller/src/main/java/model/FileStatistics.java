package model;

import java.util.*;
import org.springframework.stereotype.Service;

import controller.ControllerForFileStats;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;

import org.apache.log4j.Logger;

/**
 * @author mayank.patel This program takes the path to a directory(folder) as an
 *         input from the user and monitors the directory continuously for
 *         changes. Apart from monitoring, it also allows user to list all the
 *         files present in that directory as well as the sub-directories. The
 *         listing can be done in sorted manner or in random manner according to
 *         user requirements. The user can also perform a search based on
 *         attributes of the files like Name and size . The only public class is
 *         names FileStatistics which contains the main method and is
 *         responsible for allowing user to interact with the application.
 * 
 * 
 */

@Service
public class FileStatistics {

	private static final String PATH_SEPARATOR = "\\";
	public final static Logger logger = Logger.getLogger(FileStatistics.class);
	private HashSet<String> allPaths;
	private HashMap<String, PathMap> path;
	private ArrayList<DirectoryFile> result;
	public int status = 0;
	public HashMap<String, PathMap> pathStringMap;

	/**
	 * non-parameterised constructor called when controller object needs not be
	 * passed
	 */
	public FileStatistics() {
		logger.info("Object of FileStatistics Instantiated");
		path = new HashMap<>();
		pathStringMap = new HashMap<>();
		allPaths = new HashSet<String>();
	}

	/**
	 * constructor that accepts controller object to send status of progress bar
	 * 
	 * @param controllerForFileStats
	 */
	public FileStatistics(ControllerForFileStats controllerForFileStats) {
		logger.info("Object of FileStatistics Instantiated");
		path = new HashMap<>();
		pathStringMap = new HashMap<>();
		allPaths = new HashSet<String>();
	}

	/**
	 * method used to add a new path to the system.
	 * 
	 * @param pathString
	 */
	public void addNewPath(String pathString, PathMap newPath) {

		path.put(extractNameFromPath(pathString), newPath);
		pathStringMap.put(pathString, newPath);
		if (allPaths.contains(extractNameFromPath(pathString)))
			return;
		String name = extractNameFromPath(pathString);
		allPaths.add(name);
		logger.info("Path added : " + path);
	}

	/**
	 * this method returns files inside a folder.
	 * 
	 * @param folderName
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public ArrayList<DirectoryFile> filesInThisFolder(String folderName) throws IOException, InterruptedException {

		for (String folder : path.keySet()) {
			PathMap pathMap = path.get(folder);
			for (String folder_ : pathMap.directoryHierarchy.keySet()) {
				if (folder_.equals(folderName)) {
					result = pathMap.directoryHierarchy.get(folder_);
					return result;
				}
			}
		}
		return null;
	}

	/**
	 * function to open a file when a user clicks on it.
	 * 
	 * @param FileName
	 */
	public ArrayList<String> openFile(String FileName) {
		for (DirectoryFile directoryFile : result) {
			if (directoryFile.get_file_name().equals(FileName)) {
				FileInputStream fileInputStream;
				try {
					fileInputStream = new FileInputStream(directoryFile.get_file());
				} catch (FileNotFoundException e1) {

					logger.error(
							"Exception declaring fileInputStream object for the file:" + directoryFile.get_file_name());
					return null;
				}

				CharsetDecoder charsetDecoder = Charset.forName("UTF-8").newDecoder();

				charsetDecoder.onMalformedInput(CodingErrorAction.REPLACE);

				InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, charsetDecoder);

				BufferedReader reader = new BufferedReader(inputStreamReader);

				String temp = "";

				ArrayList<String> result = new ArrayList<>();

				while (temp != null) {
					if (temp.contains(charsetDecoder.replacement())) {
						try {
							reader.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return null;
					}
					try {
						temp = reader.readLine();
						if (temp != null)
							result.add(temp);

					} catch (Exception e) {
						try {
							reader.close();
						} catch (IOException e1) {
						}
						return null;
					}
				}
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				return result;
			}

		}
		return null;

	}

	/**
	 * function that returns the value of result for a query
	 * 
	 * @return list of files.
	 */
	public ArrayList<DirectoryFile> getResult() {
		return result;
	}

	/**
	 * function that returns all the folders
	 * 
	 * @return names of all folders.
	 */
	public HashSet<String> getAllFolders() {
		return allPaths;
	}

	/**
	 * function that takes path as a parameter and returns the name of the file
	 * 
	 * @param path
	 * @return name of the file/folder.
	 */
	public String extractNameFromPath(String path) {

		if (path.contains(PATH_SEPARATOR)) {
			String res = path.substring(path.lastIndexOf(PATH_SEPARATOR) + 1);
			return res;
		} else {
			return path;
		}
	}

	/**
	 * method to return file name search results
	 * 
	 * @param pattern
	 * @param folderName
	 * @return search results
	 */
	public ArrayList<DirectoryFile> getSearchResults(String name, String name_filter, String type, String type_filter,
			String size, String size_filter, String keyword, String folderName) {
		PathMap path_ = path.get(folderName);
		ArrayList<DirectoryFile> search_results = new ArrayList<DirectoryFile>();
		int count = 0;
		ArrayList<DirectoryFile> temp = path_.get_files();
		if (!name_filter.equals("filter") && !name.equals("")) {
			count++;
			if (name_filter.equals("contains")) {
				KMPSearch obj = new KMPSearch();
				for (int i = 0; i < temp.size(); i++) {
					int[] arr = obj.kmp(temp.get(i).get_file_name().toCharArray(), name.toCharArray());
					if (arr.length != 0) {
						search_results.add(temp.get(i));
					}
				}
			} else {
				for (int i = 0; i < temp.size(); i++) {
					if ((temp.get(i).get_file_name().equals(name))) {
						search_results.add(temp.get(i));
					}
				}
			}
		}
		if (!type_filter.equals("filter") && !type.equals("")) {
			if (count != 0) {
				temp = new ArrayList<>(search_results);
				search_results = new ArrayList<>();
			}
			count++;
			if (type_filter.equals("contains")) {
				KMPSearch obj = new KMPSearch();
				for (int i = 0; i < temp.size(); i++) {
					int[] arr = obj.kmp(temp.get(i).get_type().toCharArray(), type.toCharArray());
					if (arr.length != 0) {
						search_results.add(temp.get(i));
					}
				}
			} else {
				for (int i = 0; i < temp.size(); i++) {
					if ((temp.get(i).get_type().equals(type))) {
						search_results.add(temp.get(i));
					}
				}
			}
		}
		if (!size_filter.equals("filter") && !size.equals("")) {
			if (count != 0) {
				temp = new ArrayList<>(search_results);
				search_results = new ArrayList<>();
			}
			count++;
			if (size_filter.equals("great")) {
				int size_ = Integer.parseInt(size);
				for (int i = 0; i < temp.size(); i++) {
					if ((temp.get(i).get_size() > size_)) {
						search_results.add(temp.get(i));
					}
				}
			} else {
				int size_ = Integer.parseInt(size);
				for (int i = 0; i < temp.size(); i++) {
					if ((temp.get(i).get_size() < size_)) {
						search_results.add(temp.get(i));
					}
				}
			}
		}
		if (!keyword.equals("")) {
			if (count != 0) {
				temp = new ArrayList<>(search_results);
				search_results = new ArrayList<>();
			}
			count++;
			KeywordCountComparator keywordCountComparator = new KeywordCountComparator();
			keywordCountComparator.set_keyword(keyword);
			for (int i = 0; i < temp.size(); i++) {
				if (temp.get(i).getTokens().containsKey(keyword)) {
					search_results.add(temp.get(i));
				}
			}
			Collections.sort(search_results, keywordCountComparator);

		}
		result = search_results;
		return search_results;

	}

	/**
	 * method that returns the tokens for a particular file.
	 * 
	 * @param name
	 * @return tokens with their respective counts.
	 */
	public HashMap<String, Integer> getTokens(String name) {
		for (String path_string : path.keySet()) {
			PathMap current = path.get(path_string);
			ArrayList<DirectoryFile> al = current.get_files();
			for (DirectoryFile file : al) {
				if (file.get_file_name().equals(name)) {
					return file.getTokens();
				}
			}
		}
		return null;
	}

	/**
	 * Method that triggers the change in the primary data structure in case of a
	 * watcher event
	 * 
	 * @param name
	 * @param path_string
	 * @param kind        is 0 when create event, 12 when file is deleted, 11 when
	 *                    folder is deleted and 2 when modification event
	 */
	public void updateHashMap(String name, String path_string, int kind) {
		try {
			PathMap path;
			String folderPath = path_string.substring(0, path_string.indexOf(name) - 1);
			for (String key : pathStringMap.keySet()) {
				PathMap pathMap = pathStringMap.get(key);
				if (pathMap.directoryHierarchy.containsKey(extractNameFromPath(folderPath))) {
					path = pathMap;
					path.updateMap(name, path_string, kind);
					result = pathStringMap.get(folderPath).get_map().get(pathStringMap.get(folderPath).path_string);
					break;
				}
			}
		} catch (Exception e) {
			logger.error(
					"exception in updateHashMap name: " + name + " path_string: " + path_string + " kind: " + kind);
		}
	}

}