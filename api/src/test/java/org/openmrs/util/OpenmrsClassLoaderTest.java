/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

import java.io.File;
import java.io.FilenameFilter;

import junit.framework.Assert;

import org.junit.Test;

public class OpenmrsClassLoaderTest {
	
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
		//create old caches
		File oldCache = new File(System.getProperty("java.io.tmpdir"), "001.openmrs-lib-cache");
		File olderCache = new File(System.getProperty("java.io.tmpdir"), "2001.openmrs-lib-cache");
		oldCache.mkdirs();
		olderCache.mkdirs();
		File currentCache = new File(System.getProperty("java.io.tmpdir"), "002.openmrs-lib-cache");
		//create current cache folder
		currentCache.mkdirs();
		File tempDir = currentCache.getParentFile();
		int beforeDelete = tempDir.listFiles(cacheDirFilter).length;
		OpenmrsClassLoader.deleteOldLibCaches(currentCache);
		int afterDelete = tempDir.listFiles(cacheDirFilter).length;
		//verify after deleting only one cache should exist
		if (beforeDelete > 1)
			Assert.assertTrue(beforeDelete > afterDelete);
	}
}
