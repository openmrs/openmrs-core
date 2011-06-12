package org.openmrs.util;

import java.io.File;
import java.io.FilenameFilter;
import junit.framework.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;

public class OpenmrsClassLoaderTest extends BaseContextSensitiveTest {
	
	/**
     * TODO : Determine the correct current cache when other tests have created another lib-cache
	 * @see OpenmrsClassLoader#deleteOldLibCaches(java.io.File)
	 * @verifies return current cache folders
	 */
	@Test
    @Ignore
	public void deleteOldLibCaches_shouldReturnOnlyCurrentCacheFolders() throws Exception {
		FilenameFilter cacheDirFilter = new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".openmrs-lib-cache");
			}
		};
		FilenameFilter lockFilter = new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.equals("lock");
			}
		};
		File oldCache = new File(System.getProperty("java.io.tmpdir"), "001.openmrs-lib-cache");
		//create old cache folder
		oldCache.mkdirs();
		File currentCache = new File(System.getProperty("java.io.tmpdir"), "002.openmrs-lib-cache");
		//create current cache folder
		currentCache.mkdirs();
		File tempDir = currentCache.getParentFile();
		int folderCount = 0;
		File tempFolder = new File(System.getProperty("java.io.tmpdir"));
		File[] listFiles = tempFolder.listFiles(cacheDirFilter);
		for (File cacheDir : listFiles) {
			if (cacheDir.list(lockFilter).length != 0) {
				folderCount++;
			}
		}
		OpenmrsClassLoader.deleteOldLibCaches(currentCache);
		//verify after deleting only one cache should exist
		Assert.assertEquals(folderCount + 1, tempDir.listFiles(cacheDirFilter).length);
		//verify that it is current cache
		Assert.assertEquals(tempDir.listFiles(cacheDirFilter)[0], currentCache);
	}
}
