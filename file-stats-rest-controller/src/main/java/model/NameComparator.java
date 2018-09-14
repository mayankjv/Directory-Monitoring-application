package model;

import java.util.Comparator;

import model.DirectoryFile;

//Implementing Comparator interface and overriding the method compare to sort on the basis of File Name
class NameComparatorAsc implements Comparator<DirectoryFile> {

	public int compare(DirectoryFile file1, DirectoryFile file2) {
		return file1.get_file_name().compareTo(file2.get_file_name());
	}
}

class NameComparatorDesc implements Comparator<DirectoryFile> {

	public int compare(DirectoryFile file1, DirectoryFile file2) {
		return -1 * (file1.get_file_name().compareTo(file2.get_file_name()));
	}
}
