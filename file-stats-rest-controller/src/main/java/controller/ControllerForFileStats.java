package controller;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.String;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
//import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import org.springframework.web.bind.annotation.RestController;
import com.google.gson.Gson;
import org.springframework.http.MediaType;
import model.CreateIndex;
import model.DirectoryFile;
import model.FileStatistics;
import model.PathMap;
import model.WatchThread;
import java.util.*;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

@RestController
public class ControllerForFileStats {

	private static final char PATH_SEPARATOR = '\\';
	private static final String FORWARD_SLASH = "/";
	private static final String STRING_PATH_SEPARATOR = "\\\\";
	private static final String NO_FOLDER_FOUND = "404";
	private static final String DIRECTORY_ALREADY_PRESENT = "100";
	public static HashSet<String> stopwords = new HashSet<String>(
			Arrays.asList("a", "an", "the", "of", "on", "as", "at", "by", "you", "these", "this", "that"));;

	@Autowired
	SimpMessagingTemplate socketResponse;

	@Autowired
	private ServletContext servletContext;

	public final static Logger logger = Logger.getLogger(ControllerForFileStats.class);

	/**
	 * Private variables
	 */
	private FileStatistics fileStatistics;
	private WatchThread watcherThread;
	private int noOfPaths;
	private HashSet<String> allFolders;

	/**
	 * constructor
	 */
	public ControllerForFileStats() {
		fileStatistics = new FileStatistics(this);
		allFolders = new HashSet<String>();
		noOfPaths = 0;
		watcherThread = new WatchThread();

	}

	/**
	 * this function takes the name of the folder as parameter and returns all files
	 * in a given folder
	 * 
	 * @param path
	 * @return list of files and folders in a given folder
	 * @throws InterruptedException
	 * @throws IOException
	 */
	@RequestMapping("/filesInThisFolder")
	public ArrayList<DirectoryFile> getListOfFiles(@RequestParam(value = "folder_name") String folderName)
			throws IOException, InterruptedException {
		logger.info("Request Recieved for files in folder: " + folderName);
		if (noOfPaths == 0)
			return null;
		ArrayList<DirectoryFile> res = fileStatistics.filesInThisFolder(folderName);
		logger.info("Response Sent for files in folder: " + folderName);
		return res;
	}

	/**
	 * this function is called when a user enters a new path
	 * 
	 * @param path
	 * @return list of all the folders added so far
	 */
	@RequestMapping("/addPath")
	public Set<String> addAPath(@RequestParam(value = "path") String path) {

		logger.info("Request Recieved to add path: " + path);

		// return error message if the path has already been indexed
		if (allFolders.contains(path.substring(path.lastIndexOf(PATH_SEPARATOR) + 1))) {
			Set<String> alreadyPresent = new HashSet<>();
			alreadyPresent.add(DIRECTORY_ALREADY_PRESENT);
			return alreadyPresent;
		}

		// if this is the first path, start watcher thread
		File testIfFileExists = new File(path);

		// If the path is not a valid path return null
		if (!testIfFileExists.exists()) {
			return null;
		}
		path = path.replace(FORWARD_SLASH, STRING_PATH_SEPARATOR);

		PathMap pathMap = new PathMap(path, fileStatistics, this);

		fileStatistics.addNewPath(path, pathMap);

		HashSet<String> res = fileStatistics.getAllFolders();

		allFolders.add(extractNameFromPath(path));

		logger.info("Path added: " + path);
		if (noOfPaths == 0) {
			watcherThread.setPath(path, fileStatistics);
			watcherThread.start();
		}

		// else just register this new directory with the watch service that is already
		// running
		else {
			watcherThread.registerNewPath(path);
		}
		noOfPaths++;

		return res;
	}

	@RequestMapping("/addStopword")
	public Set<String> addAStopword(@RequestParam(value = "word") String word) {
		if (stopwords.contains(word))
			return null;
		else {
			stopwords.add(word);
		}
		return stopwords;
	}

	@RequestMapping("/getStopwords")
	public Set<String> getStopwords() {
		return stopwords;
	}

	/**
	 * function to provide search results
	 * 
	 * @param pattern
	 * @param folderName
	 * @return list of files that match the search query
	 */
	@RequestMapping("/search")
	public ArrayList<DirectoryFile> searchPattern(@RequestParam(value = "name") String name,
			@RequestParam(value = "name_filter") String name_filter, @RequestParam(value = "type") String type,
			@RequestParam(value = "type_filter") String type_filter, @RequestParam(value = "size") String size,
			@RequestParam(value = "size_filter") String size_filter, @RequestParam(value = "keyword") String keyword,
			@RequestParam(value = "folder") String folderName) {
		logger.info("Request Recieved to search " + name + " in folder " + folderName);
		ArrayList<DirectoryFile> res = fileStatistics.getSearchResults(name, name_filter, type, type_filter, size,
				size_filter, keyword, folderName);
		logger.info("Search results returned");
		return res;
	}

	/**
	 * function to open file in the viewer
	 * 
	 * @param fileName
	 * @return contents of file stores as an ArrayList of String
	 */
	@RequestMapping("/openFileInViewer")
	public ArrayList<String> openFile(@RequestParam(value = "fileName") String fileName) {
		ArrayList<String> result = fileStatistics.openFile(fileName);
		return result;
	}

	/**
	 * function that returns the last added folder to the system
	 * 
	 * @return all folders successfully indexed so far
	 */
	@RequestMapping("/allFolders")
	public HashSet<String> getListOfPaths() {
		if (allFolders.size() == 0) {
			HashSet<String> notFound = new HashSet<>();
			notFound.add(NO_FOLDER_FOUND);
			return notFound;
		} else {
			return allFolders;
		}
	}

	/**
	 * this function returns all the tokens of a given file on user request
	 * 
	 * @param name
	 * @return tokens and their counts for a given file name
	 */
	@RequestMapping("/tokensForAFile")
	public String getTokens(@RequestParam(value = "fileName") String name) {
		logger.info("Request Recieved for tokens in:  " + name);
		HashMap<String, Integer> result = fileStatistics.getTokens(name);
		long total_tokens = 0;
		for (String key : result.keySet()) {
			total_tokens += result.get(key);
		}
		Gson g = new Gson();
		String jsonString = "{ \"tokens\" :  {";
		int i = 0;
		if (result.size() == 0) {
			return null;
		}
		for (String token : result.keySet()) {
			jsonString = jsonString + " \"T" + i + "\":   { \"token\" : \"" + token + "\", \"count\" :"
					+ result.get(token) + "},";
			i++;
		}
		System.out.println(jsonString + "\n\n");
		jsonString = jsonString.substring(0, jsonString.length() - 1) + "} , ";
		System.out.println(jsonString + "\n\n");
		jsonString = jsonString + "\"total\":" + total_tokens;
		jsonString = jsonString + "}";
		return g.toJson(jsonString);
	}

	/**
	 * This function takes a path as parameter and extracts file/folder name from
	 * it.
	 * 
	 * @param path
	 * @return name of the folder/file.
	 */
	public String extractNameFromPath(String path) {

		if (path.contains("\\")) {
			String res = path.substring(path.lastIndexOf("\\") + 1);
			return res;
		} else {
			return path;
		}
	}

	/**
	 * this function sends the progress of indexing in order to display progress
	 * bar.
	 * 
	 * @param status
	 */
	public void sendProgress(int status) {
		Gson g = new Gson();
		String jsonString = "{ \"name\" :" + status + "} ";
		socketResponse.convertAndSend("/socketresponse/status", g.toJson(jsonString));
	}

	/**
	 * Send a watcher event to client side.
	 * 
	 * @param result
	 * @param directoryName
	 */
	public void sendWatcherEvent(ArrayList<DirectoryFile> result, String directoryName) {
		Gson g = new Gson();
		String jsonString = "{ \"directory_name\" :\"" + directoryName + "\", \"files\" :" + g.toJson(result) + "}";
		socketResponse.convertAndSend("/socketresponse/watcher", jsonString);
	}

	/**
	 * Function to download a file at file path to the client's machine.
	 * 
	 * @param resonse
	 * @param filePath
	 * @throws IOException
	 */
	@GetMapping("/downloadFile")
	public Integer downloadFile3(HttpServletResponse resonse, @RequestParam(defaultValue = "") String filePath) {

		if (filePath.equals(""))
			return 0;
		File file = new File(filePath);
		MediaType mediaType = getMediaTypeForFileName(this.servletContext, file.getName());

		resonse.setContentType(mediaType.getType());
		resonse.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName());

		resonse.setContentLength((int) file.length());

		try {

			BufferedInputStream inStream = new BufferedInputStream(new FileInputStream(file));
			BufferedOutputStream outStream = new BufferedOutputStream(resonse.getOutputStream());

			byte[] buffer = new byte[1024];
			int bytesRead = 0;
			while ((bytesRead = inStream.read(buffer)) != -1) {
				outStream.write(buffer, 0, bytesRead);
			}
			outStream.flush();
			inStream.close();
		} catch (Exception e) {
			logger.error("Exception in downloading file!");
			return 0;
		}
		return 0;
	}

	/**
	 * gets the media type for a given file
	 * 
	 * @param servletContext
	 * @param fileName
	 * @return
	 */
	public MediaType getMediaTypeForFileName(ServletContext servletContext, String fileName) {
		String mimeType = servletContext.getMimeType(fileName);
		try {
			MediaType mediaType = MediaType.parseMediaType(mimeType);
			return mediaType;
		} catch (Exception e) {
			return MediaType.APPLICATION_OCTET_STREAM;
		}
	}

	/**
	 * receives request for the folder summary from the client side and returns the
	 * folder summary
	 * 
	 * @param path
	 * @return
	 */
	@RequestMapping("/folderSummary")
	public String getFolderSummary(@RequestParam(value = "path") String path) {
		System.out.println("Path : " + path);
		Path path_ = Paths.get(path);
		FolderSummary folderSummary = new FolderSummary();
		try {
			Files.walkFileTree(path_, folderSummary);
		} catch (IOException e) {
			logger.error("Exception in walkFileTree");
		}
		File file = path_.toFile();
		HashMap<String, String> map = new HashMap<>();
		map.put("totalfiles", String.valueOf(folderSummary.totalFiles));
		map.put("totalsize", String.valueOf(folderSummary.totalSize));
		map.put("smallest", String.valueOf(folderSummary.smallestFile));
		map.put("largest", String.valueOf(folderSummary.largestFile));
		Date date = new Date(file.lastModified() * 1000);
		map.put("lastmodified", String.valueOf(date));
		Gson g = new Gson();
		return g.toJson(map);
	}

}

/**
 * implements FileVisitor interface and overrides it's methods to calculate
 * folder summary while performing a Tree Walk
 * 
 * @author mayank.patel
 *
 */
class FolderSummary implements FileVisitor<Object> {

	public int totalFiles;
	public long totalSize;
	public long largestFile;
	public long smallestFile;

	FolderSummary() {
		totalFiles = 0;
		totalSize = 0;
		largestFile = Long.MIN_VALUE;
		smallestFile = Long.MAX_VALUE;

	}

	@Override
	public FileVisitResult preVisitDirectory(Object dir, BasicFileAttributes attrs) throws IOException {
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFile(Object file, BasicFileAttributes attrs) throws IOException {
		System.out.println("Visit file !");
		totalFiles++;
		Path path = (Path) file;
		File file_ = path.toFile();
		totalSize += file_.length();
		if (file_.length() > largestFile)
			largestFile = file_.length();
		if (file_.length() < smallestFile)
			smallestFile = file_.length();
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
