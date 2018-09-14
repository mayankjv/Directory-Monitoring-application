package model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;


public class CalculateStats extends Thread {

	
	DirectoryFile directoryFile;
	File thisFile;
	
	public void setFile(DirectoryFile directoryFile) {
		this.directoryFile=directoryFile;
		thisFile= directoryFile.get_file();
	}
	
	public void run() {
		
		if(thisFile.isDirectory()) {
			directoryFile.setType("Folder");
			directoryFile.setName(thisFile.getName());
			directoryFile.setLines(-1);
			directoryFile.setWords(-1);
			directoryFile.setSize(-1);
			directoryFile.setLastModified(-1);

		}
		else {
			calculateNameAndType();
			try {
				calculateLines();
				calculateWordsAndTokens();
			} catch (IOException e) {
				
			}
			directoryFile.setSize(thisFile.length());
			directoryFile.setLastModified(thisFile.lastModified());
		}
	}

	private void calculateWordsAndTokens() throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(thisFile), "UTF-8"));
		HashSet<String> stopwords= new HashSet<>(Arrays.asList("a","an","the","of","on"));
		HashMap<String,Integer> tokens= new HashMap<String,Integer>();
		String temp="";
		long words=0;
		String[] words_;
		while(temp != null){
			try{
				temp= reader.readLine();
				words_= temp.split("\\s+");
				words+= words_.length;
				for(String word : words_) {
					if(stopwords.contains(word))
						continue;
					else if(tokens.containsKey(word)) {
						int curr= tokens.get(word);
						tokens.put(word,curr+1 );
					}
					else
						tokens.put(word, 1);
				}
			}
			catch(NullPointerException e){
			}
		}
		directoryFile.setWords(words);
		directoryFile.setTokens(tokens);
		reader.close();

	}
	
	
	private void calculateNameAndType() {
		String fileName = thisFile.getName();
		if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0){
			directoryFile.setName(fileName.substring(0,fileName.lastIndexOf(".")));

		}
		else directoryFile.setName("");		
		directoryFile.setType(fileName.substring(fileName.lastIndexOf(".")+1));
	}
	
	private void calculateLines() throws IOException {
		int lines=0;
		BufferedReader reader = new BufferedReader(new FileReader(thisFile));
		while (reader.readLine() != null) lines++;
		reader.close();
		directoryFile.setLines(lines);
	}
}
