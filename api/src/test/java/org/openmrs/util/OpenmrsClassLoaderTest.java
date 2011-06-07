package org.openmrs.util;

import java.io.File;
import java.io.FilenameFilter;
import junit.framework.Assert;
import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;

public class OpenmrsClassLoaderTest extends BaseContextSensitiveTest {
	
	/**
	 * @see OpenmrsClassLoader#deleteOldLibCaches(java.io.File)
	 * @verifies return current cache folders
	 */
	@Test
	public void deleteOldLibCaches_shouldReturnOnlyCurrentCacheFolders() throws Exception {
		FilenameFilter cacheDirFilter = new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".openmrs-lib-cache");
			}
		};
		File oldCache = new File(System.getProperty("java.io.tmpdir"), "oldCache.openmrs-lib-cache");
		//create old cache folder
		oldCache.mkdirs();
		File currentCache = new File(System.getProperty("java.io.tmpdir"), "currentCache.openmrs-lib-cache");
		//create current cache folder
		currentCache.mkdirs();
		File tempDir = currentCache.getParentFile();
		// two caches should exist
		Assert.assertEquals(tempDir.listFiles(cacheDirFilter).length, 2);
		OpenmrsClassLoader.deleteOldLibCaches(currentCache);
		//verify after deleting only one cache should exist
		Assert.assertEquals(tempDir.listFiles(cacheDirFilter).length, 1);
		//verify that it is current cache
		Assert.assertEquals(tempDir.listFiles(cacheDirFilter)[0], currentCache);
	}
}
