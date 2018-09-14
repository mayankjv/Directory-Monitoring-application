package model;

import java.util.ArrayList;

class KMPSearch {
	public int[] kmp(char[] text, char[] pattern) {
		int[] temporaryArray = calculateTemporaryArray(pattern);
		int textIterator = 0, patternIterator = 0;
		ArrayList<Integer> result = new ArrayList<Integer>();
		while (textIterator < text.length && patternIterator < pattern.length) {
			if (text[textIterator] == pattern[patternIterator]) {
				textIterator++;
				patternIterator++;
				if (patternIterator == pattern.length) {
					result.add(textIterator - pattern.length);
					patternIterator = 0;
				}
			} else {
				if (patternIterator != 0) {
					patternIterator = temporaryArray[patternIterator - 1];
				} else
					textIterator++;
			}
		}
		int resultArray[] = new int[result.size()];
		textIterator = 0;
		for (Integer k : result)
			resultArray[textIterator++] = k;
		return resultArray;
	}

	/**
	 * calculates temporary array which helps us determine which suffix is also a
	 * prefix.
	 * 
	 * @param pattern
	 * @return
	 */
	private int[] calculateTemporaryArray(char[] pattern) {
		int size = pattern.length;
		int[] res = new int[size];
		res[0] = 0;
		int index = 0;
		for (int i = 1; i < size; i++) {
			if (pattern[i] == pattern[index]) {
				res[i] = index + 1;
				index++;
			} else {
				if (index != 0) {
					index = res[index - 1];
				} else
					res[i] = index + 1;
			}
		}
		return res;
	}
}