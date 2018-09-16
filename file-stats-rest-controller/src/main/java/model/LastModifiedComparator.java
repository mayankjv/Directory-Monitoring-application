package model;

import java.util.Comparator;

import model.DirectoryFile;

//Implementing Comparator interface and overriding the method compare to sort on the basis of Last modified timestamp
class LastModifiedComparatorAsc implements Comparator<DirectoryFile> {

	public int compare(DirectoryFile file1, DirectoryFile file2) {
		return (int) (file2.get_last_modified() - file1.get_last_modified());
	}
}

class LastModifiedComparatorDesc implements Comparator<DirectoryFile> {

	public int compare(DirectoryFile file1, DirectoryFile file2) {
		return (int) (file1.get_last_modified() - file2.get_last_modified());
	}
}