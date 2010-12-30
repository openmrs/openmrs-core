/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
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
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.util.OpenmrsClassLoader;
import org.openmrs.util.OpenmrsUtil;

/**
 * Standard implementation of module class loader. <br/>
 * Code adapted from the Java Plug-in Framework (JPF) - LGPL - Copyright (C)<br/>
 * 2004-2006 Dmitry Olshansky
 */
public class ModuleClassLoader extends URLClassLoader {
	
	static Log log = LogFactory.getLog(ModuleClassLoader.class);
	
	private final Module module;
	
	private Module[] requiredModules;
	
	private Map<URL, File> libraryCache;
	
	private boolean probeParentLoaderLast = true;
	
	private Set<String> additionalPackages = new LinkedHashSet<String>();
	
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
		
		if (log.isDebugEnabled())
			log.debug("URLs length: " + urls.size());
		
		this.module = module;
		collectRequiredModuleImports();
		collectFilters();
		libraryCache = new WeakHashMap<URL, File>();
	}
	
	/**
	 * @param module the <code>Module</code> to load
	 * @param urls <code>List<URL></code> of the resources "managed" by this class loader
	 * @param parent parent <code>ClassLoader</code>
	 * @see URLClassLoader#URLClassLoader(java.net.URL[], java.lang.ClassLoader)
	 */
	protected ModuleClassLoader(final Module module, final List<URL> urls, final ClassLoader parent) {
		this(module, urls, parent, null);
		
		for (URL url : urls) {
			addAllAdditionalPackages(ModuleUtil.getPackagesFromFile(OpenmrsUtil.url2file(url)));
		}
	}
	
	/**
	 * @param module the <code>Module</code> to load
	 * @param urls <code>List<URL></code> of thee resources "managed" by this class loader
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
	 * @return List<URL> of all urls found (and cached) in the module
	 */
	private static List<URL> getUrls(final Module module) {
		List<URL> result = new LinkedList<URL>();
		
		File tmpModuleDir = getLibCacheFolderForModule(module);
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
		URL moduleFileURL = null;
		try {
			moduleFileURL = ModuleUtil.file2url(tmpModuleJar);
			result.add(moduleFileURL);
		}
		catch (MalformedURLException e) {
			log.warn("Unable to add files from module to URL list: " + module.getModuleId(), e);
		}
		
		// add each defined jar in the /lib folder, add as a url in the classpath of the classloader
		try {
			if (log.isDebugEnabled())
				log.debug("Expanding /lib folder in module");
			
			ModuleUtil.expandJar(module.getFile(), tmpModuleDir, "lib", true);
			File libdir = new File(tmpModuleDir, "lib");
			
			if (libdir != null && libdir.exists()) {
				// recursively get files
				Collection<File> files = (Collection<File>) FileUtils.listFiles(libdir, new String[] { "jar" }, true);
				for (File file : files) {
					if (log.isDebugEnabled())
						log.debug("Adding file to results: " + file.getAbsolutePath());
					result.add(ModuleUtil.file2url(file));
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
	 * @return List<URL> of new unique urls
	 * @see #getUrls(Module)
	 */
	private static List<URL> getUrls(final Module module, final URL[] existingUrls) {
		List<URL> urls = Arrays.asList(existingUrls);
		List<URL> result = new LinkedList<URL>();
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
	protected void collectRequiredModuleImports() {
		// collect imported modules (exclude duplicates)
		Map<String, Module> publicImportsMap = new WeakHashMap<String, Module>(); //<module ID, Module>
		
		for (String moduleId : ModuleConstants.CORE_MODULES.keySet()) {
			Module module = ModuleFactory.getModuleById(moduleId);
			
			if (module == null && !ModuleUtil.ignoreCoreModules()) {
				log.error("Unable to find an openmrs core loaded module with id: " + moduleId);
				throw new APIException(
				        "Should not be here.  All 'core' required modules by the api should be started and their classloaders should be available");
			}
			
			// if this is already the classloader for one of the core modules, don't put it on the import list
			if (module != null && !moduleId.equals(this.getModule().getModuleId())) {
				publicImportsMap.put(moduleId, module);
			}
		}
		
		for (String requiredPackage : getModule().getRequiredModules()) {
			Module requiredModule = ModuleFactory.getModuleByPackage(requiredPackage);
			if (ModuleFactory.isModuleStarted(requiredModule)) {
				publicImportsMap.put(requiredModule.getModuleId(), requiredModule);
			}
		}
		requiredModules = publicImportsMap.values().toArray(new Module[publicImportsMap.size()]);
		
	}
	
	/**
	 * Get and cache the filters for this module (not currently implemented)
	 */
	protected void collectFilters() {
		//		if (resourceFilters == null) {
		//			resourceFilters = new WeakHashMap<URL, ResourceFilter>();
		//		} else {
		//			resourceFilters.clear();
		//		}
		
		// TODO even need to iterate over libraries here?
		//for (Library lib : getModule().getLibraries()) {
		//resourceFilters.put(
		//		ModuleFactory.getPathResolver().resolvePath(lib,
		//				lib.getPath()), new ResourceFilter(lib));
		//}
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
			StringBuffer buf = new StringBuffer();
			buf.append("New code URL's populated for module " + getModule() + ":\r\n");
			for (URL u : newUrls) {
				buf.append("\t");
				buf.append(u);
				buf.append("\r\n");
			}
			log.debug(buf.toString());
		}
		collectRequiredModuleImports();
		// repopulate resource URLs
		//resourceLoader = ModuleResourceLoader.get(getModule());
		collectFilters();
		for (Iterator<Map.Entry<URL, File>> it = libraryCache.entrySet().iterator(); it.hasNext();) {
			if (it.next().getValue() == null) {
				it.remove();
			}
		}
	}
	
	/**
	 * @see org.openmrs.module.ModuleFactory#stopModule(Module,boolean)
	 */
	public void dispose() {
		if (log.isDebugEnabled())
			log.debug("Disposing of ModuleClassLoader: " + this);
		
		for (Iterator<File> it = libraryCache.values().iterator(); it.hasNext();) {
			it.next().delete();
		}
		
		libraryCache.clear();
		//resourceFilters.clear();
		requiredModules = null;
		//resourceLoader = null;
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
		Class<?> result = null;
		if (probeParentLoaderLast) {
			try {
				result = loadClass(name, resolve, this, null);
			}
			catch (ClassNotFoundException cnfe) {
				if (getParent() != null)
					result = getParent().loadClass(name);
			}
			catch (NullPointerException e) {
				log.debug("Error while attempting to load class: " + name + " from: " + this.toString());
			}
			if (result == null) {
				if (getParent() != null)
					result = getParent().loadClass(name);
			}
		} else {
			try {
				if (getParent() != null)
					result = getParent().loadClass(name);
			}
			catch (ClassNotFoundException cnfe) {
				result = loadClass(name, resolve, this, null);
			}
		}
		
		if (result != null)
			return result;
		
		throw new ClassNotFoundException(name);
	}
	
	/**
	 * Custom loadClass implementation to allow for loading from a given ModuleClassLoader and skip
	 * the modules that have been tried already
	 * 
	 * @param name String path and name of the class to load
	 * @param resolve boolean whether or not to resolve this class before returning
	 * @param requestor ModuleClassLoader with which to try loading
	 * @param seenModules Set<String> moduleIds that have been tried already
	 * @return Class that has been loaded or null if none
	 * @throws ClassNotFoundException if no class found
	 */
	protected Class<?> loadClass(final String name, final boolean resolve, final ModuleClassLoader requestor,
	        Set<String> seenModules) throws ClassNotFoundException {
		
		if (log.isTraceEnabled()) {
			log.trace("loading " + name + " " + getModule() + " seenModules: " + seenModules + " requestor: " + requestor
			        + " resolve? " + resolve);
			StringBuilder output = new StringBuilder();
			for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
				if (element.getClassName().contains("openmrs"))
					output.append("+ ");
				output.append(element);
				output.append("\n");
			}
			log.trace("stacktrace: " + output.toString());
		}
		
		if ((seenModules != null) && seenModules.contains(getModule().getModuleId())) {
			return null;
		}
		
		// make sure the module is started
		if ((this != requestor) && !ModuleFactory.isModuleStarted(getModule())) {
			String msg = "can't load class " + name + ", module " + getModule() + " is not started yet";
			log.warn(msg);
			
			throw new ClassNotFoundException(msg);
		}
		
		// the class ultimately returned (if found)
		Class<?> result = null;
		
		result = findLoadedClass(name);
		
		if (result != null) {
			checkClassVisibility(result, requestor);
			
			/*if (resolve) {
				resolveClass(result);
			}*/

			// found an already loaded class in this moduleclassloader
			return result;
		}
		
		synchronized (this) {
			// we didn't find a loaded class and this isn't a class
			// from another module
			try {
				result = findClass(name);
			}
			catch (LinkageError le) {
				throw le;
			}
			catch (ClassNotFoundException cnfe) {
				// ignore
			}
			
			// we were able to "find" a class
			if (result != null) {
				checkClassVisibility(result, requestor);
				
				if (resolve) {
					resolveClass(result);
				}
				
				return result; // found class in this module
			}
			
		}
		
		// initialize the array if need be
		if (seenModules == null)
			seenModules = new HashSet<String>();
		
		// add this module to the list of modules we've tried already
		seenModules.add(getModule().getModuleId());
		
		// look through this module's imports to see if the class
		// can be loaded from them
		for (Module publicImport : requiredModules) {
			if (seenModules.contains(publicImport.getModuleId()))
				continue;
			
			ModuleClassLoader mcl = ModuleFactory.getModuleClassLoader(publicImport);
			
			// the mcl will be null if a required module isn't started yet (like at openmrs startup)
			if (mcl != null) {
				result = mcl.loadClass(name, resolve, requestor, seenModules);
			}
			
			if (result != null) {
				/*if (resolve) {
					resolveClass(result);
				}*/
				break; // found class in publicly imported module
			}
		}
		
		return result;
	}
	
	/**
	 * Checking the given class's visibility in this module
	 * 
	 * @param cls Class to check
	 * @param requestor ModuleClassLoader to check against
	 * @throws ClassNotFoundException
	 */
	protected void checkClassVisibility(final Class<?> cls, final ModuleClassLoader requestor) throws ClassNotFoundException {
		
		if (this == requestor)
			return;
		
		URL lib = getClassBaseUrl(cls);
		
		if (lib == null)
			return; // cls is a system class
			
		ClassLoader loader = cls.getClassLoader();
		
		if (!(loader instanceof ModuleClassLoader))
			return;
		
		if (loader != this) {
			((ModuleClassLoader) loader).checkClassVisibility(cls, requestor);
		} else {
			//			ResourceFilter filter = (ResourceFilter) resourceFilters.get(lib);
			//			if (filter == null) {
			//				log.warn("class not visible, no class filter found, lib=" + lib
			//						+ ", class=" + cls + ", this=" + this
			//						+ ", requestor=" + requestor);
			//				throw new ClassNotFoundException("class "
			//						+ cls.getName() + " is not visible for module "
			//						+ requestor.getModule().getModuleId()
			//						+ ", no filter found for library " + lib);
			//			}
			//			if (!filter.isClassVisible(cls.getName())) {
			//				log.warn("class not visible, lib=" + lib
			//						+ ", class=" + cls + ", this=" + this
			//						+ ", requestor=" + requestor);
			//				throw new ClassNotFoundException("class "
			//						+ cls.getName() + " is not visible for module "
			//						+ requestor.getModule().getModuleId());
			//			}
		}
	}
	
	/**
	 * @see java.lang.ClassLoader#findLibrary(java.lang.String)
	 */
	@Override
	protected String findLibrary(final String name) {
		if ((name == null) || "".equals(name.trim()))
			return null;
		
		if (log.isTraceEnabled()) {
			log.trace("findLibrary(String): name=" + name + ", this=" + this);
		}
		String libname = System.mapLibraryName(name);
		String result = null;
		//TODO
		//PathResolver pathResolver = ModuleFactory.getPathResolver();
		//		for (Library lib : getModule().getLibraries()) {
		//			if (lib.isCodeLibrary()) {
		//				continue;
		//			}
		//			URL libUrl = null; //pathResolver.resolvePath(lib, lib.getPath() + libname);
		//			if (log.isDebugEnabled()) {
		//				log.debug("findLibrary(String): trying URL " + libUrl);
		//			}
		//			File libFile = OpenmrsUtil.url2file(libUrl);
		//			if (libFile != null) {
		//				if (log.isDebugEnabled()) {
		//					log.debug("findLibrary(String): URL " + libUrl
		//							+ " resolved as local file " + libFile);
		//				}
		//				if (libFile.isFile()) {
		//					result = libFile.getAbsolutePath();
		//					break;
		//				}
		//				continue;
		//			}
		//			// we have some kind of non-local URL
		//			// try to copy it to local temporary file
		//			libFile = (File) libraryCache.get(libUrl);
		//			if (libFile != null) {
		//				if (libFile.isFile()) {
		//					result = libFile.getAbsolutePath();
		//					break;
		//				}
		//				libraryCache.remove(libUrl);
		//			}
		//			if (libraryCache.containsKey(libUrl)) {
		//				// already tried to cache this library
		//				break;
		//			}
		//			libFile = cacheLibrary(libUrl, libname);
		//			if (libFile != null) {
		//				result = libFile.getAbsolutePath();
		//				break;
		//			}
		//		}
		if (log.isTraceEnabled()) {
			log
			        .trace("findLibrary(String): name=" + name + ", libname=" + libname + ", result=" + result + ", this="
			                + this);
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
		if (libraryCache.containsKey(libUrl)) {
			return libraryCache.get(libUrl);
		}
		
		File result = null;
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
			libraryCache.put(libUrl, result);
			
			if (log.isDebugEnabled()) {
				log.debug("library " + libname + " successfully cached from URL " + libUrl + " and saved to local file "
				        + result);
			}
			
		}
		catch (IOException ioe) {
			log.error("can't cache library " + libname + " from URL " + libUrl, ioe);
			libraryCache.put(libUrl, null);
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
		List<URL> result = new LinkedList<URL>();
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
	 * @param seenModules Set<String> modules that have been checked already
	 * @return URL to resource
	 * @see #findResource(String)
	 */
	protected URL findResource(final String name, final ModuleClassLoader requestor, Set<String> seenModules) {
		if (log.isTraceEnabled()) {
			if (name != null && name.contains("starter")) {
				if (seenModules != null)
					log.trace("seenModules.size: " + seenModules.size());
				log.trace("name: " + name);
				for (URL url : getURLs()) {
					log.trace("url: " + url);
				}
			}
		}
		
		if ((seenModules != null) && seenModules.contains(getModule().getModuleId()))
			return null;
		
		URL result = super.findResource(name);
		if (result != null) { // found resource in this module class path
			if (isResourceVisible(name, result, requestor)) {
				return result;
			}
			log.debug("Resource is not visible");
			return null;
		}
		
		//		if (resourceLoader != null) {
		//			result = resourceLoader.findResource(name);
		//			log.debug("Result from resourceLoader: " + result);
		//			if (result != null) { // found resource in this module resource libraries
		//				if (isResourceVisible(name, result, requestor)) {
		//					return result;
		//				}
		//				log.debug("result from resourceLoader is not visible");
		//				return null;
		//			}
		//		}
		
		if (seenModules == null)
			seenModules = new HashSet<String>();
		
		seenModules.add(getModule().getModuleId());
		
		for (Module publicImport : requiredModules) {
			if (seenModules.contains(publicImport.getModuleId()))
				continue;
			
			ModuleClassLoader mcl = ModuleFactory.getModuleClassLoader(publicImport);
			
			if (mcl != null)
				result = mcl.findResource(name, requestor, seenModules);
			
			if (result != null) {
				break; // found resource in publicly imported module
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
	 * @param seenModules Set<String> moduleIds that have been checked already
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
		//		if (resourceLoader != null) {
		//			for (Enumeration enm = resourceLoader.findResources(name);
		//					enm.hasMoreElements();) {
		//				URL url = (URL) enm.nextElement();
		//				if (isResourceVisible(name, url, requestor)) {
		//					result.add(url);
		//				}
		//			}
		//		}
		if (seenModules == null) {
			seenModules = new HashSet<String>();
		}
		seenModules.add(getModule().getModuleId());
		for (Module publicImport : requiredModules) {
			if (seenModules.contains(publicImport.getModuleId()))
				continue;
			
			ModuleClassLoader mcl = ModuleFactory.getModuleClassLoader(publicImport);
			
			if (mcl != null)
				mcl.findResources(result, name, requestor, seenModules);
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
		/*log.debug("isResourceVisible(URL, ModuleClassLoader): URL=" + url
				+ ", requestor=" + requestor);*/
		if (this == requestor) {
			return true;
		}
		@SuppressWarnings("unused")
		URL lib;
		try {
			String file = url.getFile();
			lib = new URL(url.getProtocol(), url.getHost(), file.substring(0, file.length() - name.length()));
		}
		catch (MalformedURLException mue) {
			log.error("can't get resource library URL", mue);
			return false;
		}
		//		ResourceFilter filter = (ResourceFilter) resourceFilters.get(lib);
		//		if (filter == null) {
		//			log.warn("no resource filter found for library "
		//					+ lib + ", name=" + name
		//					+ ", URL=" + url + ", this=" + this
		//					+ ", requestor=" + requestor);
		//			return false;
		//		}
		//		if (!filter.isResourceVisible(name)) {
		//			log.warn("resource not visible, name=" + name
		//					+ ", URL=" + url + ", this=" + this
		//					+ ", requestor=" + requestor);
		//			return false;
		//		}
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
		if (result == null || !"jar".equals(result.getProtocol()))
			return result;
		
		File tmpFolder = getLibCacheFolderForModule(module);
		
		return OpenmrsClassLoader.expandURL(result, tmpFolder);
	}
	
	/**
	 * Package names that this module should try to load. All classes/packages within the omod and
	 * the lib folder are already checked, this method/variable are used for extreme circumstances
	 * where an omod needs to know about another after being loaded
	 * 
	 * @return the additionalPackages
	 */
	public Set<String> getAdditionalPackages() {
		return additionalPackages;
	}
	
	/**
	 * @param additionalPackages the package names to set that this module contains that are outside
	 *            the normal omod and omod/lib folders
	 */
	public void setAdditionalPackages(Set<String> additionalPackages) {
		this.additionalPackages = additionalPackages;
	}
	
	/**
	 * Convenience method to add another package name to the list of packages provided by this
	 * module
	 * 
	 * @param additionalPackage string package name
	 * @see #setProvidedPackages(Set)
	 */
	public void addAdditionalPackage(String additionalPackage) {
		if (this.additionalPackages == null)
			this.additionalPackages = new LinkedHashSet<String>();
		
		// its pointless to add a package that is below the module's package
		// name because we are automatically looking at that in the classloader
		if (!additionalPackage.startsWith(module.getPackageName()))
			this.additionalPackages.add(additionalPackage);
	}
	
	/**
	 * Convenience method to add a bunch of package names to the list of packages provided by this
	 * module
	 * 
	 * @param providedPackages list/set of strings that are package names
	 * @see #setProvidedPackages(Set)
	 */
	public void addAllAdditionalPackages(Collection<String> providedPackages) {
		if (this.additionalPackages == null)
			this.additionalPackages = new LinkedHashSet<String>();
		
		for (String provPackage : providedPackages)
			// its pointless to add a package that is below the module's package
			// name because we are automatically looking at that in the classloader
			addAdditionalPackage(provPackage);
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "{ModuleClassLoader: uid=" + System.identityHashCode(this) + "; " + module + "}";
	}
	
}
