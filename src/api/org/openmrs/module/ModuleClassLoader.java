/*****************************************************************************
 * Adapted from the Java Plug-in Framework (JPF) - LGPL - Copyright (C) 2004-2006 Dmitry Olshansky
 *****************************************************************************/
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
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.util.OpenmrsClassLoader;
import org.openmrs.util.OpenmrsUtil;

/**
 * Standard implementation of module class loader.
 */
public class ModuleClassLoader extends URLClassLoader {
	static Log log = LogFactory.getLog(ModuleClassLoader.class);
	
	private final Module module;
	
	private Module[] publicImports;
	private Module[] privateImports;
	private Module[] reverseLookups;
	//private ModuleResourceLoader resourceLoader;
	//private Map<URL, ResourceFilter> resourceFilters;
	private Map<URL, File> libraryCache;
	private boolean probeParentLoaderLast = true;
	
	/**
	 * @param module Module
	 * @param urls resources "managed" by this class loader
	 * @param parent parent class loader
	 * @param factory URL stream handler factory
	 * @see URLClassLoader#URLClassLoader(java.net.URL[], java.lang.ClassLoader,
	 *	  java.net.URLStreamHandlerFactory)
	 */
	protected ModuleClassLoader(final Module module, final List<URL> urls,
			final ClassLoader parent, final URLStreamHandlerFactory factory) {
		super(urls.toArray(new URL[urls.size()]), parent, factory);
		log.debug("URLs length: " + urls.size());
		this.module = module;
		collectImports();
		//resourceLoader = ModuleResourceLoader.get(module);
		collectFilters();
		libraryCache = new WeakHashMap<URL, File>();
	}

	/**
	 * @param descr plug-in module
	 * @param urls resources "managed" by this class loader
	 * @param parent parent class loader
	 * @see URLClassLoader#URLClassLoader(java.net.URL[], java.lang.ClassLoader)
	 */
	protected ModuleClassLoader(final Module module, final List<URL> urls,
			final ClassLoader parent) {
		this(module, urls, parent, null);
	}

	/**
	 * @param aManager plug-in manager
	 * @param descr plug-in module
	 * @param urls resources "managed" by this class loader
	 * @see URLClassLoader#URLClassLoader(java.net.URL[])
	 */
	protected ModuleClassLoader(final Module module, final List<URL> urls) {
		this(module, urls, null);
	}
	
	/**
	 * Creates class instance configured to load classes and resources for
	 * given module.
	 * @param aManager module manager instance
	 * @param descr module module
	 * @param parent parent class loader, usually this is the application class loader
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

	private static URL getClassBaseUrl(final Class cls) {
		ProtectionDomain pd = cls.getProtectionDomain();
		if (pd != null) {
			CodeSource cs = pd.getCodeSource();
			if (cs != null) {
				return cs.getLocation();
			}
		}
		return null;
	}

	private static List<URL> getUrls(final Module module) {
		List<URL> result = new LinkedList<URL>();
		
		File tmpModuleDir = getLibCacheFolderForModule(module);
		File tmpModuleJar = new File(tmpModuleDir, module.getModuleId() + ".jar");
		
		log.debug("Copying module file into: " + tmpModuleDir.getAbsolutePath());
		
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
				if (in != null)
					in.close();
			}
			catch (IOException e) {
				log.error("Error while closing in stream for jar", e);
			}

			try {
				if (out != null)
					out.close();
			}
			catch (IOException e) {
				log.error("Error while closing out stream for jar", e);
			}
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
			log.debug("Expanding /lib folder in module");
			ModuleUtil.expandJar(module.getFile(), tmpModuleDir, "lib", true);
			File libdir = new File(tmpModuleDir, "lib");
			if (libdir != null && libdir.exists())
				for (File file : libdir.listFiles()) {
					log.debug("Adding file to results: " + file.getAbsolutePath());
					result.add(ModuleUtil.file2url(file));
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
	
	public static File getLibCacheFolderForModule(Module module) {
		File tmpModuleDir = new File(OpenmrsClassLoader.getLibCacheFolder(), module.getModuleId());
		
		// each module gets its own folder named /moduleId/
		if (!tmpModuleDir.exists()) {
			tmpModuleDir.mkdir();
			tmpModuleDir.deleteOnExit();
		}
		return tmpModuleDir;
	}

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

	protected void collectImports() {
		// collect imported modules (exclude duplicates)
		Map<String, Module> publicImportsMap = new WeakHashMap<String, Module>(); //<module ID, Module>
		Map<String, Module> privateImportsMap = new WeakHashMap<String, Module>(); //<module ID, Module>
		for (String requiredId : getModule().getRequiredModules()) {
			Module requiredModule = ModuleFactory.getModuleById(requiredId);
			if (ModuleFactory.isModuleStarted(requiredModule)) {
				publicImportsMap.put(requiredModule.getModuleId(), requiredModule);
			} else {
				privateImportsMap.put(requiredModule.getModuleId(), requiredModule);
			}
		}
		publicImports = (Module[]) publicImportsMap.values().toArray(
				new Module[publicImportsMap.size()]);
		privateImports =
			(Module[]) privateImportsMap.values().toArray(
				new Module[privateImportsMap.size()]);
		// collect reverse look up modules (exclude duplicates)
		Map<String, Module> reverseLookupsMap = new WeakHashMap<String, Module>();
		for (Module module : ModuleFactory.getLoadedModules()) {
			if (module.equals(getModule())
					|| publicImportsMap.containsKey(module.getModuleId())
					|| privateImportsMap.containsKey(module.getModuleId())) {
				continue;
			}
			for (String requiredModuleId : module.getRequiredModules()) {
				Module requiredModule = ModuleFactory.getModuleById(requiredModuleId);
				if (!requiredModule.equals(getModule())) {
					continue;
				}
				reverseLookupsMap.put(module.getModuleId(), module);
				break;
			}
		}
		reverseLookups =
			(Module[]) reverseLookupsMap.values().toArray(
				new Module[reverseLookupsMap.size()]);
	}
	
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
			buf.append("New code URL's populated for module "
					+ getModule() + ":\r\n");
			for (URL u : newUrls) {
				buf.append("\t");
				buf.append(u);
				buf.append("\r\n");
			}
			log.debug(buf.toString());
		}
		collectImports();
		// repopulate resource URLs
		//resourceLoader = ModuleResourceLoader.get(getModule());
		collectFilters();
		for (Iterator it = libraryCache.entrySet().iterator(); it.hasNext();) {
			if (((Map.Entry) it.next()).getValue() == null) {
				it.remove();
			}
		}
	}
	
	/**
	 * @see org.openmrs.module.ModuleClassLoader#dispose()
	 */
	public void dispose() {
		log.debug("Disposing of ModuleClassLoader: " + this);
		
		for (Iterator it = libraryCache.values().iterator(); it.hasNext();) {
			((File) it.next()).delete();
		}
		libraryCache.clear();
		//resourceFilters.clear();
		reverseLookups = null;
		privateImports = null;
		publicImports = null;
		//resourceLoader = null;
	}
	
	protected void setProbeParentLoaderLast(final boolean value) {
		probeParentLoaderLast = value;
	}
	
	/**
	 * @see java.lang.ClassLoader#loadClass(java.lang.String, boolean)
	 */
	protected Class<?> loadClass(final String name, final boolean resolve)
			throws ClassNotFoundException {
		/*log.debug("loadClass(String, boolean): name=" + name + ", this="
				+ this);*/
		Class result = null;
		if (probeParentLoaderLast) {
			try {
				result = loadClass(name, resolve, this, null);
			} catch (ClassNotFoundException cnfe) {
				if (getParent() != null)
					result = getParent().loadClass(name);
			} catch (NullPointerException e) {
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
			} catch (ClassNotFoundException cnfe) {
				result = loadClass(name, resolve, this, null);
			}
		}
		if (result != null) {
			return result;
		}
		throw new ClassNotFoundException(name);
	}
	
	protected Class loadClass(final String name, final boolean resolve,
			final ModuleClassLoader requestor, Set<String> seenModules)
			throws ClassNotFoundException {
		/*log.debug("loadClass(String, boolean, ...): name=" + name + ", this="
				+ this);*/
		if ((seenModules != null) && seenModules.contains(getModule().getModuleId())) {
			return null;
		}
		if ((this != requestor)
				&& !ModuleFactory.isModuleStarted(getModule())) {
			String msg = "can't load class " + name + ", module " 
				+ getModule() + " is not started yet";
			log.warn(msg);
			throw new ClassNotFoundException(msg);
		}
		Class result = null;
		synchronized (this) {
			result = findLoadedClass(name);
			if (result != null) {
				if (log.isDebugEnabled() && name.contains("hello")) {
					log.debug("loadClass(...): found loaded class, class="
							+ result + ", this="
							+ this + ", requestor=" + requestor);
				}
				checkClassVisibility(result, requestor);
				/*if (resolve) {
					resolveClass(result);
				}*/
				return result; // found already loaded class in this module
			}
			try {
				synchronized (getClass()) {
					result = findClass(name);
				}
			} catch (LinkageError le) {
				if (log.isDebugEnabled() && name.contains("hello")) {
					log.debug("loadClass(...): class loading failed,"
							+ " name=" + name + ", this=" 
							+ this + ", requestor=" + requestor, le);
				}
				throw le;
			 } catch (ClassNotFoundException cnfe) {
				// ignore
			}
			if (result != null) {
				if (log.isDebugEnabled() && name.contains("hello")) {
					log.debug("loadClass(...): found class, class="
							+ result + ", this="
							+ this + ", requestor=" + requestor);
				}
				checkClassVisibility(result, requestor);
				if (resolve) {
					resolveClass(result);
				}
				return result; // found class in this module
			}
		}
		if (seenModules == null) {
			seenModules = new HashSet<String>();
		}
		if (log.isDebugEnabled() && name.contains("hello")) {
			log.debug("loadClass(...): class not found, name="
					+ name + ", this="
					+ this + ", requestor=" + requestor);
		}
		seenModules.add(getModule().getModuleId());
		
		for (Module publicImport : publicImports) {
			if (seenModules.contains(publicImport.getModuleId())) {
				continue;
			}
			result = ((ModuleClassLoader) ModuleFactory
					.getModuleClassLoader(publicImport)).loadClass(
							name, resolve, requestor, seenModules);
			if (result != null) {
				/*if (resolve) {
					resolveClass(result);
				}*/
				break; // found class in publicly imported module
			}
		}
		
		if ((this == requestor) && (result == null)) {
			for (Module privateImport : privateImports) {
				if (seenModules.contains(privateImport.getModuleId())) {
					continue;
				}
				result = ((ModuleClassLoader) ModuleFactory
						.getModuleClassLoader(privateImport)).loadClass(
								name, resolve, requestor, seenModules);
				if (result != null) {
					/*if (resolve) {
						resolveClass(result);
					}*/
					break; // found class in privately imported module
				}
			}
		}
		if ((this == requestor) && (result == null)) {
			for (Module reverseLookup : reverseLookups) {
				if (seenModules.contains(reverseLookup.getModuleId())) {
					continue;
				}
				if (!ModuleFactory.isModuleStarted(reverseLookup)) {
					continue;
				}
				result = ((ModuleClassLoader) ModuleFactory
						.getModuleClassLoader(reverseLookup)).loadClass(
								name, resolve, requestor, seenModules);
				if (result != null) {
					/*if (resolve) {
						resolveClass(result);
					}*/
					break; // found class in module that marks itself as
						   // allowed reverse look up
				}
			}
		}
		return result;
	}
	
	protected void checkClassVisibility(final Class cls,
			final ModuleClassLoader requestor)
			throws ClassNotFoundException {
		/*log.debug("checkClassVisibility(Class, ModuleClassLoader): class="
				+ cls.getName() + ", requestor=" + requestor
				+ ", this=" + this);*/
		if (this == requestor) {
			return;
		}
		URL lib = getClassBaseUrl(cls);
		if (lib == null) {
			return; // cls is a system class
		}
		ClassLoader loader = cls.getClassLoader();
		if (!(loader instanceof ModuleClassLoader)) {
			return;
		}
		if (loader != this) {
			((ModuleClassLoader) loader).checkClassVisibility(cls,
					requestor);
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
	protected String findLibrary(final String name) {
		if ((name == null) || "".equals(name.trim())) {
			return null;
		}
		if (log.isDebugEnabled()) {
			log.debug("findLibrary(String): name=" + name
					+ ", this=" + this);
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
		if (log.isDebugEnabled()) {
			log.debug("findLibrary(String): name=" + name
					+ ", libname=" + libname
					+ ", result=" + result
					+ ", this=" + this);
		}
		return result;
	}

	protected synchronized File cacheLibrary(final URL libUrl,
			final String libname) {
		File cacheFolder = OpenmrsClassLoader.getLibCacheFolder();
		if (libraryCache.containsKey(libUrl)) {
			return (File) libraryCache.get(libUrl);
		}
		File result = null;
		try {
			if (cacheFolder == null) {
				throw new IOException(
						"can't initialize libraries cache folder");
			}
			File libCacheModuleFolder = new File(cacheFolder,
					getModule().getModuleId());
			if (!libCacheModuleFolder.exists()
					&& !libCacheModuleFolder.mkdirs()) {
				throw new IOException("can't create cache folder "
						+ libCacheModuleFolder);
			}
			result = new File(libCacheModuleFolder, libname);
			InputStream in = OpenmrsUtil.getResourceInputStream(libUrl);
			try {
				OutputStream out = new BufferedOutputStream(
						new FileOutputStream(result));
				try {
					OpenmrsUtil.copyFile(in, out);
				} finally {
					out.close();
				}
			} finally {
				in.close();
			}
			libraryCache.put(libUrl, result);
			if (log.isDebugEnabled()) {
				log.debug("library " + libname
						+ " successfully cached from URL " + libUrl
						+ " and saved to local file " + result);
			}
		} catch (IOException ioe) {
			log.error("can't cache library " + libname
					+ " from URL " + libUrl, ioe);
			libraryCache.put(libUrl, null);
			result = null;
		}
		return result;
	}

	/**
	 * If a resource is found within a jar, that jar URL is converted to 
	 * a temporary file and a URL to that is returned
	 * @see java.lang.ClassLoader#findResource(java.lang.String)
	 */
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

	protected URL findResource(final String name,
			final ModuleClassLoader requestor, Set<String> seenModules) {
		
		if (log.isDebugEnabled()) {
			if (name != null && name.contains("starter")) {
				if (seenModules != null) log.debug("seenModules.size: " + seenModules.size());
				log.debug("name: " + name);
				for (URL url : getURLs()) {
					log.debug("url: " + url);
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
		for (int i = 0; i < publicImports.length; i++) {
			if (seenModules.contains(publicImports[i].getModuleId())) {
				continue;
			}
			result = ((ModuleClassLoader) ModuleFactory
					.getModuleClassLoader(publicImports[i])).findResource(
							name, requestor, seenModules);
			if (result != null) {
				break; // found resource in publicly imported module
			}
		}
		
		if ((this == requestor) && (result == null)) {
			for (int i = 0; i < privateImports.length; i++) {
				if (seenModules.contains(privateImports[i].getModuleId())) {
					continue;
				}
				result = ((ModuleClassLoader) ModuleFactory
						.getModuleClassLoader(privateImports[i])).findResource(
								name, requestor, seenModules);
				if (result != null) {
					break; // found resource in privately imported module
				}
			}
		}
		
		if ((this == requestor) && (result == null)) {
			for (Module reverseLookup : reverseLookups) {
				if (seenModules.contains(reverseLookup.getModuleId())) {
					continue;
				}
				result = ((ModuleClassLoader) ModuleFactory
						.getModuleClassLoader(reverseLookup)).findResource(
								name, requestor, seenModules);
				if (result != null) {
					break; // found resource in module that marks itself as
						   // allowed reverse look up
				}
			}
		}
		
		return result;
		
	}

	protected void findResources(final List<URL> result, final String name,
			final ModuleClassLoader requestor, Set<String> seenModules)
			throws IOException {
		if ((seenModules != null) && seenModules.contains(getModule().getModuleId())) {
			return;
		}
		for (Enumeration enm = super.findResources(name);
				enm.hasMoreElements();) {
			URL url = (URL) enm.nextElement();
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
		for (Module publicImport : publicImports) {
			if (seenModules.contains(publicImport.getModuleId())) {
				continue;
			}
			((ModuleClassLoader) ModuleFactory.getModuleClassLoader(
					publicImport)).findResources(result, name,
							requestor, seenModules);
		}
		if (this == requestor) {
			for (Module privateImport : privateImports) {
				if (seenModules.contains(privateImport.getModuleId())) {
					continue;
				}
				((ModuleClassLoader) ModuleFactory.getModuleClassLoader(
						privateImport)).findResources(result, name,
								requestor, seenModules);
			}
			for (Module reverseLookup : reverseLookups) {
				if (seenModules.contains(reverseLookup.getModuleId())) {
					continue;
				}
				((ModuleClassLoader) ModuleFactory.getModuleClassLoader(
						reverseLookup)).findResources(result, name,
								requestor, seenModules);
			}
		}
	}
	
	protected boolean isResourceVisible(final String name, final URL url,
			final ModuleClassLoader requestor) {
		/*log.debug("isResourceVisible(URL, ModuleClassLoader): URL=" + url
				+ ", requestor=" + requestor);*/
		if (this == requestor) {
			return true;
		}
		URL lib;
		try {
			String file = url.getFile();
			lib = new URL(url.getProtocol(), url.getHost(),
					file.substring(0, file.length() - name.length()));
		} catch (MalformedURLException mue) {
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
	 * Expands the URL into the temporary folder if the URL points to a 
	 * resource inside of a jar file
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
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "{ModuleClassLoader: uid="
			+ System.identityHashCode(this) + "; "
			+ module + "}";
	}
	
//	protected static final class ResourceFilter {
//		private boolean isPublic;
//		private Set<String> entries;
//
//		protected ResourceFilter(final Library lib) {
//			entries = new HashSet<String>();
//			for (Iterator it = lib.getExports().iterator(); it.hasNext();) {
//				String exportPrefix = (String) it.next();
//				if ("*".equals(exportPrefix)) {
//					isPublic = true;
//					entries.clear();
//					break;
//				}
//				if (!lib.isCodeLibrary()) {
//					exportPrefix = exportPrefix.replace('\\', '.')
//						.replace('/', '.');
//					if (exportPrefix.startsWith(".")) {
//						exportPrefix = exportPrefix.substring(1);
//					}
//				}
//				entries.add(exportPrefix);
//			}
//		}
//		
//		protected boolean isClassVisible(final String className) {
//			if (isPublic) {
//				return true;
//			}
//			if (entries.isEmpty()) {
//				return false;
//			}
//			if (entries.contains(className)) {
//				return true;
//			}
//			int p = className.lastIndexOf('.');
//			if (p == -1) {
//				return false;
//			}
//			return entries.contains(className.substring(0, p) + ".*");
//		}
//
//		protected boolean isResourceVisible(final String resPath) {
//			// quick check
//			if (isPublic) {
//				return true;
//			}
//			if (entries.isEmpty()) {
//				return false;
//			}
//			// translate "path spec" -> "full class name"
//			String str = resPath.replace('\\', '.').replace('/', '.');
//			if (str.startsWith(".")) {
//				str = str.substring(1);
//			}
//			if (str.endsWith(".")) {
//				str = str.substring(0, str.length() - 1);
//			}
//			return isClassVisible(str);
//		}
//	}
//
//	protected static class ModuleResourceLoader extends URLClassLoader {
//		private static Log log = LogFactory.getLog(ModuleResourceLoader.class);
//
//		static ModuleResourceLoader get(final Module module) {
//			final List<URL> urls = new LinkedList<URL>();
//			for (Library lib : module.getLibraries()) {
//				if (lib.isCodeLibrary()) {
//					continue;
//				}
//				//urls.add(ModuleFactory.getPathResolver().resolvePath(lib,
//				//		lib.getPath()));
//			}
//			if (log.isDebugEnabled()) {
//				StringBuffer buf = new StringBuffer();
//				buf.append("Resource URL's populated for module " + module
//						+ ":\r\n");
//				for (Iterator it = urls.iterator(); it.hasNext();) {
//					buf.append("\t");
//					buf.append(it.next());
//					buf.append("\r\n");
//				}
//				log.trace(buf.toString());
//			}
//			if (urls.isEmpty()) {
//				return null;
//			}
//			/*return new ModuleResourceLoader((URL[]) urls.toArray(
//			new URL[urls.size()]));*/
//			return (ModuleResourceLoader) AccessController.doPrivileged(
//					new PrivilegedAction<Object>() {
//				public Object run() {
//					return new ModuleResourceLoader(urls);
//				}
//			});
//		}
//
//		/**
//		 * Creates loader instance configured to load resources only from given
//		 * URLs.
//		 * @param urls array of resource URLs
//		 */
//		public ModuleResourceLoader(final List<URL> urls) {
//			super((URL[])urls.toArray());
//		}
//
//		/**
//		 * @see java.lang.ClassLoader#findClass(java.lang.String)
//		 */
//		protected Class<?> findClass(final String name)
//				throws ClassNotFoundException {
//			throw new ClassNotFoundException(name);
//		}
//
//		/**
//		 * @see java.lang.ClassLoader#loadClass(java.lang.String, boolean)
//		 */
//		protected Class<?> loadClass(final String name, final boolean resolve)
//				throws ClassNotFoundException {
//			throw new ClassNotFoundException(name);
//		}
//	}
}
