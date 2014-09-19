package org.openmrs.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import junit.framework.Assert;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleClassLoader;
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
	
	/**
	 * @verifies load class from longest match first
	 * @see OpenmrsClassLoader#loadModuleClass(String, java.util.Collection)
	 */
	@Test
	public void loadModuleClass_shouldLoadClassFromLongestMatchFirst() throws Exception {
		String className = "openmrs.module.reporting.ui.Report";
		
		Collection<ModuleClassLoader> moduleClassLoaders = new ArrayList<ModuleClassLoader>();
		ModuleClassLoader moduleClassLoader = Mockito.mock(ModuleClassLoader.class);
		
		Mockito.when(moduleClassLoader.getModule()).thenReturn(
		    new Module("reporting", "", "openmrs.module.reporting", "", "", ""));
		Mockito.when(moduleClassLoader.getAdditionalPackages()).thenReturn(new HashSet<String>());
		Mockito.when(moduleClassLoader.loadClass(className)).thenReturn(null);
		
		ModuleClassLoader moduleClassLoader2 = Mockito.mock(ModuleClassLoader.class);
		
		Mockito.when(moduleClassLoader2.getModule()).thenReturn(
		    new Module("reporting.ui", "", "openmrs.module.reporting.ui", "", "", ""));
		Mockito.when(moduleClassLoader2.getAdditionalPackages()).thenReturn(new HashSet<String>());
		Mockito.when(moduleClassLoader2.loadClass(className)).thenReturn(null);
		
		moduleClassLoaders.add(moduleClassLoader);
		moduleClassLoaders.add(moduleClassLoader2);
		
		OpenmrsClassLoader classLoader = new OpenmrsClassLoader();
		
		classLoader.loadModuleClass(className, moduleClassLoaders);
		
		InOrder inOrder = Mockito.inOrder(moduleClassLoader, moduleClassLoader2);
		
		inOrder.verify(moduleClassLoader2, Mockito.atLeastOnce()).loadClass(className);
		inOrder.verify(moduleClassLoader, Mockito.never()).loadClass(className);
	}
	
	/**
	 * @verifies if longest match fails should try other matches
	 * @see OpenmrsClassLoader#loadModuleClass(String, java.util.Collection)
	 */
	@Test
	public void loadModuleClass_shouldIfLongestMatchFailsShouldTryOtherMatches() throws Exception {
		String className = "openmrs.module.reporting.ui.Report";
		
		Collection<ModuleClassLoader> moduleClassLoaders = new ArrayList<ModuleClassLoader>();
		ModuleClassLoader moduleClassLoader = Mockito.mock(ModuleClassLoader.class);
		
		Mockito.when(moduleClassLoader.getModule()).thenReturn(
		    new Module("reporting", "", "openmrs.module.reporting", "", "", ""));
		Mockito.when(moduleClassLoader.getAdditionalPackages()).thenReturn(new HashSet<String>());
		Mockito.when(moduleClassLoader.loadClass(className)).thenReturn(null);
		
		ModuleClassLoader moduleClassLoader2 = Mockito.mock(ModuleClassLoader.class);
		
		Mockito.when(moduleClassLoader2.getModule()).thenReturn(
		    new Module("reporting.ui", "", "openmrs.module.reporting.ui", "", "", ""));
		Mockito.when(moduleClassLoader2.getAdditionalPackages()).thenReturn(new HashSet<String>());
		Mockito.when(moduleClassLoader2.loadClass(className)).thenThrow(ClassNotFoundException.class);
		
		moduleClassLoaders.add(moduleClassLoader);
		moduleClassLoaders.add(moduleClassLoader2);
		
		OpenmrsClassLoader classLoader = new OpenmrsClassLoader();
		
		classLoader.loadModuleClass(className, moduleClassLoaders);
		
		InOrder inOrder = Mockito.inOrder(moduleClassLoader, moduleClassLoader2);
		
		inOrder.verify(moduleClassLoader2, Mockito.atLeastOnce()).loadClass(className);
		inOrder.verify(moduleClassLoader, Mockito.atLeastOnce()).loadClass(className);
	}
}
