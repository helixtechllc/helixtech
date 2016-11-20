package com.helixtech;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {
	// If this pathname does not denote a directory, then listFiles() returns
	// null.
	public static List<String> listFiles(String path) {
		List<String> results = new ArrayList<String>();

		File[] files = new File(path).listFiles();

                if (files != null) {
		for (File file : files) {
			if (file.isFile()) {
				results.add(file.getName());
			}
		}
		}
		return results;
	}

}