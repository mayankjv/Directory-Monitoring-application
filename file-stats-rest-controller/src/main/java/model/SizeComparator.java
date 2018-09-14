package model;

import java.util.Comparator;

import model.DirectoryFile;

//Implementing Comparator interface and overriding the method compare to sort on the basis of File Size
class SizeComparatorAsc implements Comparator<DirectoryFile> {

	public int compare(DirectoryFile file1, DirectoryFile file2) {
		return (int) (file1.get_size() - file2.get_size());
	}
}

class SizeComparatorDesc implements Comparator<DirectoryFile> {

	public int compare(DirectoryFile file1, DirectoryFile file2) {
		return (int) (file2.get_size() - file1.get_size());
	}
}