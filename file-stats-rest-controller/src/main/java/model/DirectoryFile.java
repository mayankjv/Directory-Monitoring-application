package model;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;

//This class stores all the attributes of a single file along with all the member functions that are needed to calculate the values of the attributes.

public class DirectoryFile implements Serializable {

	private static final long serialVersionUID = 9034550065999063545L;
	public final static Logger logger = Logger.getLogger(DirectoryFile.class);
	private File file;
	private String type;
	private String name;
	private int lines;
	private long words;
	private long size;
	private long last_modified;
	private HashMap<String, Integer> tokens = new HashMap<String, Integer>();
	HashSet<String> stopwords = new HashSet<>();;

	/**
	 * A parameterized Constructor that sets all the attributes of a file by calling
	 * suitable functions.
	 * 
	 * @param f
	 */
	DirectoryFile(File file, HashSet<String> stopwords) {
		this.file = file;
		calculateStats();
		logger.info("New DirectoryFile created, name: " + file.getName());
		this.stopwords = stopwords;
	}

	/**
	 * Getter Method for getting the File object associated with an
	 * 
	 * @return object of File for this DirectoryFile
	 */
	public File get_file() {
		return file;
	}

	/**
	 * Getter method for Name of the file
	 * 
	 * @return name of file.
	 */
	public String get_file_name() {
		return name;
	}

	/**
	 * Getter method for file type
	 * 
	 * @return type of file.
	 */
	public String get_type() {
		return type;
	}

	/**
	 * Getter method for Number of lines in the file
	 * 
	 * @return number of lines.
	 */
	public int get_lines() {
		return lines;
	}

	/**
	 * Getter method for number of words in the file
	 * 
	 * @return number of words.
	 */
	public long get_words() {
		return words;
	}

	/**
	 * Getter method for the size of the file
	 * 
	 * @return size of the file.
	 */
	public long get_size() {
		return size;
	}

	/**
	 * Getter method for the last modified time stamp of the file
	 * 
	 * @return last modified date of a file.
	 */
	public long get_last_modified() {
		return last_modified;
	}

	/**
	 * Getter method for tokens of the file
	 * 
	 * @return tokens and their respective counts.
	 */
	public HashMap<String, Integer> getTokens() {
		return tokens;
	}

	/**
	 * Method to set last modified time stamp
	 * 
	 * @param lastModified
	 */
	public void setLastModified(long lastModified) {

		this.last_modified = lastModified;

	}

	/**
	 * Method to set the size of the file
	 * 
	 * @param size
	 */
	public void setSize(long size) {

		this.size = size;

	}

	/**
	 * Method to set the number of lines in the file
	 * 
	 * @param lines
	 */
	public void setLines(int lines) {
		this.lines = lines;
	}

	/**
	 * Method to set the Name of the file
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Method to set the file type
	 * 
	 * @param type
	 */
	public void setType(String type) {
		this.type = type;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		DirectoryFile f = (DirectoryFile) o;
		if (f.get_file_name().equals(this.get_file_name()))
			return true;
		return false;
	}

	/**
	 * Method to set the number of words in the file
	 * 
	 * @param words
	 */
	public void setWords(long words) {
		this.words = words;
	}

	/**
	 * Method to set tokens in the file
	 * 
	 * @param words
	 */
	public void setTokens(HashMap<String, Integer> tokens) {
		this.tokens = tokens;
	}

	/**
	 * function that is responsible for calculating all the meta data about a file.
	 */
	public void calculateStats() {

		if (file.isDirectory()) {
			this.setType("Folder");
			this.setName(file.getName());
			this.setLines(-1);
			this.setWords(-1);
			this.setSize(-1);
			this.setLastModified(-1);
		} else {

			calculateNameAndType();
			try {
				calculateWordsAndTokens();
			} catch (IOException e) {

			}
			this.setSize(file.length());
			this.setLastModified(file.lastModified());
		}
	}

	/**
	 * function that calculates the number of words and the list and count of tokens
	 * in a file.
	 * 
	 * @throws IOException
	 */
	private void calculateWordsAndTokens() throws IOException {

		int lines = 0;
		FileInputStream fileInputStream = new FileInputStream(file);

		CharsetDecoder charsetDecoder = Charset.forName("UTF-8").newDecoder();

		charsetDecoder.onMalformedInput(CodingErrorAction.REPLACE);

		InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, charsetDecoder);

		BufferedReader reader = new BufferedReader(inputStreamReader);

		HashMap<String, Integer> tokens = new HashMap<String, Integer>();
		String temp = "";
		long words = 0;
		ArrayList<String> tokensInOneLine = new ArrayList<>();
		while (temp != null) {
			if (temp.contains(charsetDecoder.replacement())) {
				this.setWords(-1);
				this.setTokens(null);
				this.setLines(-1);
				this.setType("binary");
				logger.info("Binary file: " + file.getAbsolutePath());
				reader.close();
				return;
			}
			try {
				temp = reader.readLine();
				StringTokenizer st = new StringTokenizer(temp, ":/,. -!*<>?\"~`^*{}[];()@=+\\");
				while (st.hasMoreTokens()) {
					String tk = st.nextToken();
					tokensInOneLine.add(tk.trim());
				}
				words += tokensInOneLine.size();
				for (String word : tokensInOneLine) {
					if (this.stopwords.contains(word))
						continue;
					else if (tokens.containsKey(word)) {
						int curr = tokens.get(word);
						tokens.put(word, curr + 1);
					} else
						tokens.put(word, 1);
				}
				tokensInOneLine = new ArrayList<>();
				lines++;
			} catch (NullPointerException e) {
			}
		}
		this.setWords(words);
		this.setTokens(tokens);
		this.setLines(lines);
		reader.close();

	}

	/**
	 * function that calculates the name and the type of the file
	 */
	private void calculateNameAndType() {
		String fileName = file.getName();
		if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
			this.setName(fileName.substring(0, fileName.lastIndexOf(".")));

		} else
			this.setName("");
		this.setType(fileName.substring(fileName.lastIndexOf(".") + 1));
	}

}