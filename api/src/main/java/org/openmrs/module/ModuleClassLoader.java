/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.APIException;
import org.openmrs.util.OpenmrsClassLoader;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Standard implementation of module class loader. <br>
 * Code adapted from the Java Plug-in Framework (JPF) - LGPL - Copyright (C)<br>
 * 2004-2006 Dmitry Olshansky
 */
public class ModuleClassLoader extends URLClassLoader {
	
	private static final Logger log = LoggerFactory.getLogger(ModuleClassLoader.class);
	
	private final Module module;
	
	private Module[] requiredModules;
	
	private Module[] awareOfModules;
	
	private Map<URI, File> libraryCache;
	
	private boolean probeParentLoaderLast = true;
	
	private Set<String> providedPackages = new LinkedHashSet<>();
	
	private boolean disposed = false;
	
	
	/**
	 * @param module Module
	 * @param urls resources "managed" by this class loader
	 * @param parent parent class loader
	 * @param factory URL stream handler factory
	 * @see URLClassLoader#URLClassLoader(java.net.URL[], java.lang.ClassLoader,
	 *      java.net.URLStreamHandlerFactory)
	 */
	protected ModuleClassLoader(final Module module, final List<URL> urls, final ClassLoader parent,
	    final URLStreamHandlerFactory factory) {
		super(urls.toArray(new URL[urls.size()]), parent, factory);
		
		if (parent instanceof OpenmrsClassLoader) {
			throw new IllegalArgumentException("Parent must not be OpenmrsClassLoader nor null");
		} else if (parent instanceof ModuleClassLoader) {
			throw new IllegalArgumentException("Parent must not be ModuleClassLoader");
		}
		
		log.debug("URLs length: {}", urls.size());
		this.module = module;
		requiredModules = collectRequiredModuleImports(module);
		awareOfModules = collectAwareOfModuleImports(module);
		libraryCache = new WeakHashMap<>();
	}
	
	/**
	 * @param module the <code>Module</code> to load
	 * @param urls <code>List&lt;URL&gt;</code> of the resources "managed" by this class loader
	 * @param parent parent <code>ClassLoader</code>
	 * @see URLClassLoader#URLClassLoader(java.net.URL[], java.lang.ClassLoader)
	 */
	protected ModuleClassLoader(final Module module, final List<URL> urls, final ClassLoader parent) {
		this(module, urls, parent, null);
		
		File devDir = ModuleUtil.getDevelopmentDirectory(module.getModuleId());
		if (devDir != null) {
			File[] fileList = devDir.listFiles();
			if (fileList == null) {
				return;
			}
			for (File file : fileList) {
				if (!file.isDirectory()) {
					continue;
				}
				File dir = Paths.get(devDir.getAbsolutePath(), file.getName(), "target", "classes").toFile();
				if (dir.exists()) {
					Collection<File> files = FileUtils.listFiles(dir, new String[] { "class" }, true);
					addClassFilePackages(files, dir.getAbsolutePath().length() + 1);
				}
			}
		} else {
			for (URL url : urls) {
				providedPackages.addAll(ModuleUtil.getPackagesFromFile(OpenmrsUtil.url2file(url)));
			}
		}
	}
	
	private void addClassFilePackages(Collection<File> files, int dirLength) {
		for (File file : files) {
			String name = file.getAbsolutePath().substring(dirLength);
			Integer indexOfLastSlash = name.lastIndexOf(File.separator);
			if (indexOfLastSlash > 0) {
				String packageName = name.substring(0, indexOfLastSlash);
				packageName = packageName.replace(File.separator, ".");
				providedPackages.add(packageName);
				
			}
		}
	}
	
	/**
	 * @param module the <code>Module</code> to load
	 * @param urls <code>List&lt;URL&gt;</code> of thee resources "managed" by this class loader
	 * @see URLClassLoader#URLClassLoader(java.net.URL[])
	 */
	protected ModuleClassLoader(final Module module, final List<URL> urls) {
		this(module, urls, null);
	}
	
	/**
	 * Creates class instance configured to load classes and resources for given module.
	 *
	 * @param module the <code>Module</code> to load
	 * @param parent parent <code>ClassLoader</code>
	 */
	public ModuleClassLoader(final Module module, final ClassLoader parent) {
		this(module, getUrls(module), parent);
	}
	
	/**
	 * @return returns this classloader's module
	 */
	public Module getModule() {
		return module;
	}
	
	public boolean isDisposed() {
		return disposed;
	}
	
	/**
	 * Get the base class url of the given <code>cls</code>. Used for checking against system class
	 * loads vs classloader loads
	 *
	 * @param cls Class name
	 * @return URL to the class
	 */
	private static URL getClassBaseUrl(final Class<?> cls) {
		ProtectionDomain pd = cls.getProtectionDomain();
		
		if (pd != null) {
			CodeSource cs = pd.getCodeSource();
			if (cs != null) {
				return cs.getLocation();
			}
			
		}
		
		return null;
	}
	
	/**
	 * Get all urls for all files in the given <code>module</code>
	 *
	 * @param module Module in which to look
	 * @return List&lt;URL&gt; of all urls found (and cached) in the module
	 */
	private static List<URL> getUrls(final Module module) {
		List<URL> result = new LinkedList<>();
		
		//if in dev mode, add development folder to the classpath
		List<String> devFolderNames = new ArrayList<>();
		File devDir = ModuleUtil.getDevelopmentDirectory(module.getModuleId());
		try {
			if (devDir != null) {
				File[] fileList = devDir.listFiles();
				if (fileList != null) {
					for (File file : fileList) {
						if (!file.isDirectory()) {
							continue;
						}
						File dir = Paths.get(devDir.getAbsolutePath(), file.getName(), "target", "classes").toFile();
						if (dir.exists()) {
							result.add(dir.toURI().toURL());
							devFolderNames.add(file.getName());
						}
					}
				}
			}
		}
		catch (MalformedURLException ex) {
			log.error("Failed to add development folder to the classpath", ex);
		}
		
		File tmpModuleDir = getLibCacheFolderForModule(module);
		
		//add module jar to classpath only if we are not in dev mode
		if (devDir == null) {
			File tmpModuleJar = new File(tmpModuleDir, module.getModuleId() + ".jar");
			
			if (!tmpModuleJar.exists()) {
				try {
					tmpModuleJar.createNewFile();
				}
				catch (IOException io) {
					log.warn("Unable to create tmpModuleFile", io);
				}
			}
			
			// copy the module jar into that temporary folder
			FileInputStream in = null;
			FileOutputStream out = null;
			try {
				in = new FileInputStream(module.getFile());
				out = new FileOutputStream(tmpModuleJar);
				OpenmrsUtil.copyFile(in, out);
			}
			catch (IOException io) {
				log.warn("Unable to copy tmpModuleFile", io);
			}
			finally {
				try {
					in.close();
				}
				catch (Exception e) { /* pass */}
				try {
					out.close();
				}
				catch (Exception e) { /* pass */}
			}
			
			// add the module jar as a url in the classpath of the classloader
			URL moduleFileURL;
			try {
				moduleFileURL = ModuleUtil.file2url(tmpModuleJar);
				result.add(moduleFileURL);
			}
			catch (MalformedURLException e) {
				log.warn("Unable to add files from module to URL list: " + module.getModuleId(), e);
			}
		}
		
		// add each defined jar in the /lib folder, add as a url in the classpath of the classloader
		try {
			log.debug("Expanding /lib folder in module");
			
			ModuleUtil.expandJar(module.getFile(), tmpModuleDir, "lib", true);
			File libdir = new File(tmpModuleDir, "lib");
			
			if (libdir != null && libdir.exists()) {
				Map<String, String> startedRelatedModules = new HashMap<>();
				for (Module requiredModule : collectRequiredModuleImports(module)) {
					startedRelatedModules.put(requiredModule.getModuleId(), requiredModule.getVersion());
				}
				for (Module awareOfModule : collectAwareOfModuleImports(module)) {
					startedRelatedModules.put(awareOfModule.getModuleId(), awareOfModule.getVersion());
				}
				
				// recursively get files
				Collection<File> files = FileUtils.listFiles(libdir, new String[] { "jar" }, true);
				for (File file : files) {
					
					//if in dev mode, do not put the module source jar files in the class path
					if (devDir != null) {
						boolean jarForDevFolder = false;
						for (String folderName : devFolderNames) {
							if (file.getName().startsWith(module.getModuleId() + "-" + folderName + "-")) {
								//e.g uiframework-api-3.3-SNAPSHOT.jar, webservices.rest-omod-common-2.14-SNAPSHOT.jar
								//webservices.rest-omod-1.11-2.14-SNAPSHOT.jar, webservices.rest-omod-1.10-2.14-SNAPSHOT.jar, etc
								jarForDevFolder = true;
								break;
							}
						}
						
						if (jarForDevFolder) {
							continue;
						}
					}
					
					URL fileUrl = ModuleUtil.file2url(file);
					
					boolean include = shouldResourceBeIncluded(module, fileUrl, OpenmrsConstants.OPENMRS_VERSION_SHORT,
					    startedRelatedModules);
					
					if (include) {
						log.debug("Including file in classpath: {}", fileUrl);
						result.add(fileUrl);
					} else {
						log.debug("Excluding file from classpath: {}", fileUrl);
					}
				}
			}
		}
		catch (MalformedURLException e) {
			log.warn("Error while adding module 'lib' folder to URL result list");
		}
		catch (IOException io) {
			log.warn("Error while expanding lib folder", io);
		}
		
		// add each xml document to the url list
		
		return result;
	}
	
	/**
	 * Determines whether or not the given resource should be available on the classpath based on
	 * OpenMRS version and/or modules' version. It uses the conditionalResources section specified
	 * in config.xml. Resources that are not mentioned as conditional resources are included by
	 * default. All conditions for a conditional resource to be included must match.
	 *
	 * @param module
	 * @param fileUrl
	 * @return true if it should be included <strong>Should</strong> return true if file matches and
	 *         openmrs version matches <strong>Should</strong> return false if file matches but
	 *         openmrs version does not <strong>Should</strong> return true if file does not match
	 *         and openmrs version does not match <strong>Should</strong> return true if file
	 *         matches and module version matches <strong>Should</strong> return false if file
	 *         matches and module version does not match <strong>Should</strong> return false if
	 *         file matches and openmrs version matches but module version does not match
	 *         <strong>Should</strong> return false if file matches and module not found
	 *         <strong>Should</strong> return true if file does not match and module version does
	 *         not match
	 */
	static boolean shouldResourceBeIncluded(Module module, URL fileUrl, String openmrsVersion,
	        Map<String, String> startedRelatedModules) {
		//all resources are included by default
		boolean include = true;
		
		for (ModuleConditionalResource conditionalResource : module.getConditionalResources()) {
			if (fileUrl.getPath().matches(".*" + conditionalResource.getPath() + "$")) {
				//if a resource matches a path of contidionalResource then it must meet all conditions
				include = false;
				
				//openmrsPlatformVersion is optional
				if (StringUtils.isNotBlank(conditionalResource.getOpenmrsPlatformVersion())) {
					include = ModuleUtil.matchRequiredVersions(openmrsVersion,
					    conditionalResource.getOpenmrsPlatformVersion());
					
					if (!include) {
						return false;
					}
				}
				
				//modules are optional
				if (conditionalResource.getModules() != null) {
					for (ModuleConditionalResource.ModuleAndVersion conditionalModuleResource : conditionalResource
					        .getModules()) {
						if ("!".equals(conditionalModuleResource.getVersion())) {
							include = !ModuleFactory.isModuleStarted(conditionalModuleResource.getModuleId());
							if (!include) {
								return false;
							}
						} else {
							String moduleVersion = startedRelatedModules.get(conditionalModuleResource.getModuleId());
							if (moduleVersion != null) {
								include = ModuleUtil.matchRequiredVersions(moduleVersion,
								    conditionalModuleResource.getVersion());
								
								if (!include) {
									return false;
								}
							}
						}
					}
					
				}
			}
		}
		
		return include;
	}
	
	/**
	 * Get the library cache folder for the given module. Each module has a different cache folder
	 * to ease cleanup when unloading a module while openmrs is running
	 *
	 * @param module Module which the cache will be used for
	 * @return File directory where the files will be placed
	 */
	public static File getLibCacheFolderForModule(Module module) {
		File tmpModuleDir = new File(OpenmrsClassLoader.getLibCacheFolder(), module.getModuleId());
		
		// each module gets its own folder named /moduleId/
		if (!tmpModuleDir.exists()) {
			tmpModuleDir.mkdir();
			tmpModuleDir.deleteOnExit();
		}
		return tmpModuleDir;
	}
	
	/**
	 * Get all urls for the given <code>module</code> that are not already in the
	 * <code>existingUrls</code>
	 *
	 * @param module Module in which to get urls
	 * @param existingUrls Array of URLs to skip
	 * @return List&lt;URL&gt; of new unique urls
	 * @see #getUrls(Module)
	 */
	private static List<URL> getUrls(final Module module, final URL[] existingUrls) {
		List<URL> urls = Arrays.asList(existingUrls);
		List<URL> result = new LinkedList<>();
		for (URL url : getUrls(module)) {
			if (!urls.contains(url)) {
				result.add(url);
			}
		}
		return result;
	}
	
	/**
	 * Get and cache the imports for this module. The imports should just be the modules that set as
	 * "required" by this module
	 */
	protected static Module[] collectRequiredModuleImports(Module module) {
		// collect imported modules (exclude duplicates)
		//<module ID, Module>
		Map<String, Module> publicImportsMap = new WeakHashMap<>();
		
		for (String moduleId : ModuleConstants.CORE_MODULES.keySet()) {
			Module coreModule = ModuleFactory.getModuleById(moduleId);
			
			if (coreModule == null && !ModuleUtil.ignoreCoreModules()) {
				log.error("Unable to find an openmrs core loaded module with id: " + moduleId);
				throw new APIException("Module.error.shouldNotBeHere", (Object[]) null);
			}
			
			// if this is already the classloader for one of the core modules, don't put it on the import list
			if (coreModule != null && !moduleId.equals(module.getModuleId())) {
				publicImportsMap.put(moduleId, coreModule);
			}
		}
		
		for (String requiredPackage : module.getRequiredModules()) {
			Module requiredModule = ModuleFactory.getModuleByPackage(requiredPackage);
			if (ModuleFactory.isModuleStarted(requiredModule)) {
				publicImportsMap.put(requiredModule.getModuleId(), requiredModule);
			}
		}
		return publicImportsMap.values().toArray(new Module[publicImportsMap.size()]);
		
	}
	
	/**
	 * Get and cache the imports for this module. The imports should just be the modules that set as
	 * "aware of" by this module
	 */
	protected static Module[] collectAwareOfModuleImports(Module module) {
		// collect imported modules (exclude duplicates)
		//<module ID, Module>
		Map<String, Module> publicImportsMap = new WeakHashMap<>();
		
		for (String awareOfPackage : module.getAwareOfModules()) {
			Module awareOfModule = ModuleFactory.getModuleByPackage(awareOfPackage);
			if (ModuleFactory.isModuleStarted(awareOfModule)) {
				publicImportsMap.put(awareOfModule.getModuleId(), awareOfModule);
			}
		}
		return publicImportsMap.values().toArray(new Module[publicImportsMap.size()]);
		
	}
	
	/**
	 * @see org.openmrs.module.ModuleClassLoader#modulesSetChanged()
	 */
	protected void modulesSetChanged() {
		List<URL> newUrls = getUrls(getModule(), getURLs());
		for (URL u : newUrls) {
			addURL(u);
		}
		
		if (log.isDebugEnabled()) {
			StringBuilder buf = new StringBuilder();
			buf.append("New code URL's populated for module ").append(getModule()).append(":\r\n");
			for (URL u : newUrls) {
				buf.append("\t");
				buf.append(u);
				buf.append("\r\n");
			}
			log.debug(buf.toString());
		}
		requiredModules = collectRequiredModuleImports(getModule());
		awareOfModules = collectAwareOfModuleImports(getModule());
		libraryCache.entrySet().removeIf(uriFileEntry -> uriFileEntry.getValue() == null);
	}
	
	/**
	 * @see org.openmrs.module.ModuleFactory#stopModule(Module, boolean)
	 */
	public void dispose() {
		log.debug("Disposing of ModuleClassLoader: {}", this);
		for (File file : libraryCache.values()) {
			file.delete();
		}
		
		libraryCache.clear();
		requiredModules = null;
		awareOfModules = null;
		disposed = true;
	}
	
	/**
	 * Allow the probe parent loader last variable to be set. Usually this is set to true to allow
	 * modules to override and create their own classes
	 *
	 * @param value boolean true/false whether or not to look at the parent classloader last
	 */
	public void setProbeParentLoaderLast(final boolean value) {
		probeParentLoaderLast = value;
	}
	
	/**
	 * @see java.lang.ClassLoader#loadClass(java.lang.String, boolean)
	 */
	@Override
	protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
		// Check if the class has already been loaded by this class loader
		Class<?> result = findLoadedClass(name);
		if (result == null) {
			if (probeParentLoaderLast) {
				try {
					result = loadClass(name, resolve, this, null);
				}
				catch (ClassNotFoundException cnfe) {
					// Continue trying...
				}
				
				if (result == null && getParent() != null) {
					result = getParent().loadClass(name);
				}
			} else {
				try {
					if (getParent() != null) {
						result = getParent().loadClass(name);
					}
				}
				catch (ClassNotFoundException cnfe) {
					// Continue trying...
				}
				
				if (result == null) {
					result = loadClass(name, resolve, this, null);
				}
			}
		}
		
		if (resolve) {
			resolveClass(result);
		}
		
		return result;
	}
	
	/**
	 * Custom loadClass implementation to allow for loading from a given ModuleClassLoader and skip
	 * the modules that have been tried already
	 * 
	 * @param name String path and name of the class to load
	 * @param resolve boolean whether or not to resolve this class before returning
	 * @param requestor ModuleClassLoader with which to try loading
	 * @param seenModules Set&lt;String&gt; moduleIds that have been tried already
	 * @return Class that has been loaded
	 * @throws ClassNotFoundException if no class found
	 */
	protected synchronized Class<?> loadClass(final String name, final boolean resolve, final ModuleClassLoader requestor,
	        Set<String> seenModules) throws ClassNotFoundException {
		
		if (log.isTraceEnabled()) {
			log.trace("Loading " + name + " " + getModule() + ", seenModules: " + seenModules + ", requestor: " + requestor
			        + ", resolve? " + resolve);
			StringBuilder output = new StringBuilder();
			for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
				if (element.getClassName().contains("openmrs")) {
					output.append("+ ");
				}
				output.append(element);
				output.append("\n");
			}
			log.trace("Stacktrace: " + output.toString());
		}
		
		// Check if we already tried this class loader
		if ((seenModules != null) && seenModules.contains(getModule().getModuleId())) {
			throw new ClassNotFoundException("Can't load class " + name + " from module " + getModule().getModuleId()
			        + ". It has been tried before.");
		}
		
		// Make sure the module is started
		if ((this != requestor) && !ModuleFactory.isModuleStarted(getModule())) {
			String msg = "Can't load class " + name + ", because module " + getModule().getModuleId()
			        + " is not yet started.";
			log.warn(msg);
			
			throw new ClassNotFoundException(msg);
		}
		
		// Check if the class has already been loaded by this class loader
		Class<?> result = findLoadedClass(name);
		
		// Try loading the class with this class loader 
		if (result == null) {
			try {
				result = findClass(name);
			}
			catch (ClassNotFoundException e) {
				// Continue trying...
			}
		}
		
		// We were able to "find" a class
		if (result != null) {
			checkClassVisibility(result, requestor);
			
			return result;
		}
		
		// Look through this module's imports to see if the class
		// can be loaded from them.
		
		if (seenModules == null) {
			seenModules = new HashSet<>();
		}
		
		// Add this module to the list of modules we've tried already
		seenModules.add(getModule().getModuleId());
		
		List<Module> importedModules = new ArrayList<>();
		if (requiredModules != null) {
			Collections.addAll(importedModules, requiredModules);
		}
		if (awareOfModules != null) {
			Collections.addAll(importedModules, awareOfModules);
		}
		
		for (Module importedModule : importedModules) {
			if (seenModules.contains(importedModule.getModuleId())) {
				continue;
			}
			
			ModuleClassLoader moduleClassLoader = ModuleFactory.getModuleClassLoader(importedModule);
			
			// Module class loader may be null if module has not been started yet
			if (moduleClassLoader != null) {
				try {
					result = moduleClassLoader.loadClass(name, resolve, requestor, seenModules);
					
					return result;
				}
				catch (ClassNotFoundException e) {
					// Continue trying...
				}
			}
		}
		
		throw new ClassNotFoundException(name);
	}
	
	/**
	 * Checking the given class's visibility in this module
	 *
	 * @param cls Class to check
	 * @param requestor ModuleClassLoader to check against
	 * @throws ClassNotFoundException
	 */
	protected void checkClassVisibility(final Class<?> cls, final ModuleClassLoader requestor)
	        throws ClassNotFoundException {
		
		if (this == requestor) {
			return;
		}
		
		URL lib = getClassBaseUrl(cls);
		
		if (lib == null) {
			// cls is a system class
			return;
		}
		
		ClassLoader loader = cls.getClassLoader();
		
		if (!(loader instanceof ModuleClassLoader)) {
			return;
		}
		
		if (loader != this) {
			((ModuleClassLoader) loader).checkClassVisibility(cls, requestor);
		}
	}
	
	/**
	 * @see java.lang.ClassLoader#findLibrary(java.lang.String)
	 */
	@Override
	protected String findLibrary(final String name) {
		if ((name == null) || "".equals(name.trim())) {
			return null;
		}
		
		if (log.isTraceEnabled()) {
			log.trace("findLibrary(String): name=" + name + ", this=" + this);
		}
		String libname = System.mapLibraryName(name);
		String result = null;
		
		if (log.isTraceEnabled()) {
			log.trace(
			    "findLibrary(String): name=" + name + ", libname=" + libname + ", result=" + result + ", this=" + this);
		}
		
		return result;
	}
	
	/**
	 * Saves the given library in the openmrs cache. This prevents locking of jars/files by servlet
	 * container
	 *
	 * @param libUrl URL to the library/jar file
	 * @param libname name of the jar that will be the name of the cached file
	 * @return file that is now copied and cached
	 */
	protected File cacheLibrary(final URL libUrl, final String libname) {
		File cacheFolder = OpenmrsClassLoader.getLibCacheFolder();
		
		URI libUri;
		try {
			libUri = libUrl.toURI();
		}
		catch (URISyntaxException e) {
			throw new IllegalArgumentException(libUrl.getPath() + " is not a valid URI", e);
		}
		
		if (libraryCache.containsKey(libUri)) {
			return libraryCache.get(libUri);
		}
		
		File result;
		try {
			if (cacheFolder == null) {
				throw new IOException("can't initialize libraries cache folder");
			}
			
			// create the directory to hold the jar's files
			File libCacheModuleFolder = new File(cacheFolder, getModule().getModuleId());
			
			// error while creating the file
			if (!libCacheModuleFolder.exists() && !libCacheModuleFolder.mkdirs()) {
				throw new IOException("can't create cache folder " + libCacheModuleFolder);
			}
			
			// directory within the specific folder within the cache
			result = new File(libCacheModuleFolder, libname);
			
			// copy the file over to the cache
			InputStream in = OpenmrsUtil.getResourceInputStream(libUrl);
			try {
				FileOutputStream fileOut = new FileOutputStream(result);
				OutputStream out = new BufferedOutputStream(fileOut);
				try {
					OpenmrsUtil.copyFile(in, out);
				}
				finally {
					try {
						out.close();
					}
					catch (Exception e) { /* pass */}
					try {
						fileOut.close();
					}
					catch (Exception e) {}
				}
			}
			finally {
				try {
					in.close();
				}
				catch (Exception e) { /* pass */}
			}
			
			// save a link to the cached file
			libraryCache.put(libUri, result);
			
			log.debug("library {} successfully cached from URL {} and saved to local file {}", libname, libUrl, result);
			
		}
		catch (IOException ioe) {
			log.error("can't cache library " + libname + " from URL " + libUrl, ioe);
			libraryCache.put(libUri, null);
			result = null;
		}
		
		return result;
	}
	
	/**
	 * If a resource is found within a jar, that jar URL is converted to a temporary file and a URL
	 * to that is returned
	 *
	 * @see java.lang.ClassLoader#findResource(java.lang.String)
	 */
	@Override
	public URL findResource(final String name) {
		URL result = findResource(name, this, null);
		
		return expandIfNecessary(result);
	}
	
	/**
	 * @see java.lang.ClassLoader#findResources(java.lang.String)
	 */
	@Override
	public Enumeration<URL> findResources(final String name) throws IOException {
		List<URL> result = new LinkedList<>();
		findResources(result, name, this, null);
		
		// expand all of the "jar" urls
		for (URL url : result) {
			url = expandIfNecessary(url);
		}
		
		return Collections.enumeration(result);
	}
	
	/**
	 * Find a resource (image, file, etc) in the module structure
	 *
	 * @param name String path and name of the file
	 * @param requestor ModuleClassLoader in which to look
	 * @param seenModules Set&lt;String&gt; modules that have been checked already
	 * @return URL to resource
	 * @see #findResource(String)
	 */
	protected URL findResource(final String name, final ModuleClassLoader requestor, Set<String> seenModules) {
		if (log.isTraceEnabled() && name != null && name.contains("starter")) {
			if (seenModules != null) {
				log.trace("seenModules.size: " + seenModules.size());
			}
			log.trace("name: " + name);
			for (URL url : getURLs()) {
				log.trace("url: " + url);
			}
		}
		
		if ((seenModules != null) && seenModules.contains(getModule().getModuleId())) {
			return null;
		}
		
		URL result = super.findResource(name);
		// found resource in this module class path
		if (result != null) {
			if (isResourceVisible(name, result, requestor)) {
				return result;
			}
			log.debug("Resource is not visible");
			return null;
		}
		
		if (seenModules == null) {
			seenModules = new HashSet<>();
		}
		
		seenModules.add(getModule().getModuleId());
		
		if (requiredModules != null) {
			for (Module publicImport : requiredModules) {
				if (seenModules.contains(publicImport.getModuleId())) {
					continue;
				}
				ModuleClassLoader mcl = ModuleFactory.getModuleClassLoader(publicImport);
				
				if (mcl != null) {
					result = mcl.findResource(name, requestor, seenModules);
				}
				
				if (result != null) {
					// found resource in required module
					return result;
				}
			}
		}
		
		//look through the aware of modules.
		for (Module publicImport : awareOfModules) {
			if (seenModules.contains(publicImport.getModuleId())) {
				continue;
			}
			
			ModuleClassLoader mcl = ModuleFactory.getModuleClassLoader(publicImport);
			
			if (mcl != null) {
				result = mcl.findResource(name, requestor, seenModules);
			}
			
			if (result != null) {
				// found resource in aware of module
				return result;
			}
		}
		
		return result;
	}
	
	/**
	 * Find all occurrences of a resource (image, file, etc) in the module structure
	 *
	 * @param result URL of the file found
	 * @param name String path and name of the file to find
	 * @param requestor ModuleClassLoader in which to start
	 * @param seenModules Set&lt;String&gt; moduleIds that have been checked already
	 * @throws IOException
	 * @see #findResources(String)
	 * @see #findResource(String, ModuleClassLoader, Set)
	 */
	protected void findResources(final List<URL> result, final String name, final ModuleClassLoader requestor,
	        Set<String> seenModules) throws IOException {
		if ((seenModules != null) && seenModules.contains(getModule().getModuleId())) {
			return;
		}
		for (Enumeration<URL> enm = super.findResources(name); enm.hasMoreElements();) {
			URL url = enm.nextElement();
			if (isResourceVisible(name, url, requestor)) {
				result.add(url);
			}
		}
		
		if (seenModules == null) {
			seenModules = new HashSet<>();
		}
		seenModules.add(getModule().getModuleId());
		if (requiredModules != null) {
			for (Module publicImport : requiredModules) {
				if (seenModules.contains(publicImport.getModuleId())) {
					continue;
				}
				
				ModuleClassLoader mcl = ModuleFactory.getModuleClassLoader(publicImport);
				
				if (mcl != null) {
					mcl.findResources(result, name, requestor, seenModules);
				}
			}
		}
		
		//look through the aware of modules.
		for (Module publicImport : awareOfModules) {
			if (seenModules.contains(publicImport.getModuleId())) {
				continue;
			}
			
			ModuleClassLoader mcl = ModuleFactory.getModuleClassLoader(publicImport);
			
			if (mcl != null) {
				mcl.findResources(result, name, requestor, seenModules);
			}
		}
	}
	
	/**
	 * Check if the given resource (image, file, etc) is visible by this classloader
	 *
	 * @param name String path and name to check
	 * @param url URL of the library file
	 * @param requestor ModuleClassLoader in which to look
	 * @return true/false whether this resource is visibile by this classloader
	 */
	protected boolean isResourceVisible(final String name, final URL url, final ModuleClassLoader requestor) {
		if (this == requestor) {
			return true;
		}
		try {
			String file = url.getFile();
			new URL(url.getProtocol(), url.getHost(), file.substring(0, file.length() - name.length()));
		}
		catch (MalformedURLException mue) {
			log.error("can't get resource library URL", mue);
			return false;
		}
		
		return true;
	}
	
	/**
	 * Expands the URL into the temporary folder if the URL points to a resource inside of a jar
	 * file
	 *
	 * @param result
	 * @return URL to the expanded result or null if an error occurred
	 */
	private URL expandIfNecessary(URL result) {
		if (result == null || !"jar".equals(result.getProtocol())) {
			return result;
		}
		
		File tmpFolder = getLibCacheFolderForModule(module);
		
		return OpenmrsClassLoader.expandURL(result, tmpFolder);
	}
	
	/**
	 * Contains all class packages provided by the module, including those contained in jars.
	 * <p>
	 * It is used by {@link OpenmrsClassLoader#loadClass(String, boolean)} and in particular
	 * {@link ModuleFactory#getModuleClassLoadersForPackage(String)} to quickly find possible
	 * loaders for the given class. Although it takes some time to extract all provided packages
	 * from a module, it pays off when loading classes. It is much faster to query a map of packages
	 * than iterating over all class loaders to find which one to use.
	 * 
	 * @return the provided packages
	 */
	public Set<String> getProvidedPackages() {
		return providedPackages;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "{ModuleClassLoader: uid=" + System.identityHashCode(this) + "; " + module + "}";
	}
	
}
