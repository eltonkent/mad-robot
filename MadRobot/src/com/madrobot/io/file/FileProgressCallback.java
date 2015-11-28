package com.madrobot.io.file;

import java.io.File;

public interface FileProgressCallback {

	void beforeDelete(File file);

	void afterDelete(File file);

	/**
	 * When a directory is found in the given path
	 * 
	 * @param totalFilesContained
	 *            Total number of files/directories in the discovered directory.
	 */
	void onDirectoryDiscovered(int totalFilesContained);

}
