package model;

import java.util.Comparator;

public class KeywordCountComparator implements Comparator<DirectoryFile> {

	private String keyword;

	KeywordCountComparator() {
		keyword = "";
	}

	public void set_keyword(String key) {
		keyword = key;
	}

	public int compare(DirectoryFile file1, DirectoryFile file2) {
		return file2.getTokens().get(keyword).compareTo(file1.getTokens().get(keyword));
	}
}
