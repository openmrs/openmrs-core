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
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.ModuleClassLoader;
import org.openmrs.module.ModuleFactory;
import org.openmrs.module.ModuleUtil;
import org.openmrs.scheduler.SchedulerException;
import org.openmrs.scheduler.SchedulerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.ehcache.CacheManager;

/**
 * This classloader knows about the current ModuleClassLoaders and will attempt to load classes from
 * them if needed
 */
public class OpenmrsClassLoader extends URLClassLoader {
	
	private static Logger log = LoggerFactory.getLogger(OpenmrsClassLoader.class);
	
	private static File libCacheFolder;
	
	private static boolean libCacheFolderInitialized = false;
	
	// placeholder to hold mementos to restore
	private static Map<String, OpenmrsMemento> mementos = new WeakHashMap<>();
	
	/**
	 * Holds all classes that has been requested from this class loader. We use weak references so that
	 * module classes can be garbage collected when modules are unloaded.
	 */
	private Map<String, WeakReference<Class<?>>> cachedClasses = new ConcurrentHashMap<>();
	
	// suffix of the OpenMRS required library cache folder
	private static final String LIBCACHESUFFIX = ".openmrs-lib-cache";
	
	/**
	 * Creates the instance for the OpenmrsClassLoader
	 */
	public OpenmrsClassLoader(ClassLoader parent) {
		super(new URL[0], parent);
		
		if (parent instanceof OpenmrsClassLoader) {
			throw new IllegalArgumentException("Parent must not be OpenmrsClassLoader nor null");
		} else if (parent instanceof ModuleClassLoader) {
			throw new IllegalArgumentException("Parent must not be ModuleClassLoader");
		}
		
		OpenmrsClassLoaderHolder.INSTANCE = this;
		
		if (log.isDebugEnabled()) {
			log.debug("Creating new OpenmrsClassLoader instance with parent: " + parent);
		}
		
		//disable caching so the jars aren't locked
		//if performance is effected, this can be disabled in favor of
		//copying all opened jars to a temp location
		//(ala org.apache.catalina.loader.WebappClassLoader antijarlocking)
		URLConnection urlConnection = new OpenmrsURLConnection();
		urlConnection.setDefaultUseCaches(false);
	}
	
	/**
	 * Normal constructor. Sets this class as the parent classloader
	 */
	public OpenmrsClassLoader() {
		this(OpenmrsClassLoader.class.getClassLoader());
	}
	
	/**
	 * Private class to hold the one classloader used throughout openmrs. This is an alternative to
	 * storing the instance object on {@link OpenmrsClassLoader} itself so that garbage collection
	 * can happen correctly.
	 */
	private static class OpenmrsClassLoaderHolder {

		private OpenmrsClassLoaderHolder() {
		}
		
		private static OpenmrsClassLoader INSTANCE = null;
		
	}
	
	/**
	 * Get the static/singular instance of the module class loader
	 *
	 * @return OpenmrsClassLoader
	 */
	public static OpenmrsClassLoader getInstance() {
		if (OpenmrsClassLoaderHolder.INSTANCE == null) {
			OpenmrsClassLoaderHolder.INSTANCE = new OpenmrsClassLoader();
		}
		
		return OpenmrsClassLoaderHolder.INSTANCE;
	}
	
	/**
	 * It loads classes from the web container class loader first (parent class loader) and then
	 * tries module class loaders.
	 * 
	 * @see java.lang.ClassLoader#loadClass(java.lang.String, boolean)
	 * @should load class from cache second time
	 * @should not load class from cache if class loader has been disposed
	 * @should load class from parent first
	 * @should load class if two module class loaders have same packages
	 */
	@Override
	public synchronized Class<?> loadClass(String name, final boolean resolve) throws ClassNotFoundException {
		// Check if the class has already been requested from this class loader
		Class<?> c = getCachedClass(name);
		if (c == null) {
			// We do not try to load classes using this.findClass on purpose.
			// All classes are loaded by web container or by module class loaders.
			
			// First try loading from modules such that we allow modules to load
			// different versions of the same libraries that may already be used
			// by core or the web container. An example is the chartsearch module
			// which uses different versions of lucene and solr from core
			String packageName = StringUtils.substringBeforeLast(name, ".");
			Set<ModuleClassLoader> moduleClassLoaders = ModuleFactory.getModuleClassLoadersForPackage(packageName);
			for (ModuleClassLoader moduleClassLoader : moduleClassLoaders) {
				try {
					c = moduleClassLoader.loadClass(name);
					break;
				}
				catch (ClassNotFoundException e) {
					// Continue trying...
				}
			}
			
			if (c == null) {
				// Finally try loading from web container
				c = getParent().loadClass(name);
			}
			
			cacheClass(name, c);
		}
		
		if (resolve) {
			resolveClass(c);
		}
		
		return c;
	}
	
	private Class<?> getCachedClass(String name) {
		WeakReference<Class<?>> ref = cachedClasses.get(name);
		if (ref != null) {
			Class<?> loadedClass = ref.get();
			if (loadedClass == null || loadedClass.getClassLoader() == null) {
				// Class has been garbage collected
				cachedClasses.remove(name);
				loadedClass = null;
			} else if (loadedClass.getClassLoader() instanceof ModuleClassLoader) {
				ModuleClassLoader moduleClassLoader = (ModuleClassLoader) loadedClass.getClassLoader();
				if (moduleClassLoader.isDisposed()) {
					// Class has been unloaded
					cachedClasses.remove(name);
					loadedClass = null;
				}
			}
			
			return loadedClass;
		}
		return null;
	}
	
	private void cacheClass(String name, Class<?> clazz) {
		cachedClasses.put(name, new WeakReference<>(clazz));
	}
	
	/**
	 * @see java.net.URLClassLoader#findResource(java.lang.String)
	 */
	@Override
	public URL findResource(final String name) {
		if (log.isTraceEnabled()) {
			log.trace("finding resource: " + name);
		}
		
		URL result;
		for (ModuleClassLoader classLoader : ModuleFactory.getModuleClassLoaders()) {
			result = classLoader.findResource(name);
			if (result != null) {
				return result;
			}
		}
		
		// look for the resource in the parent
		result = super.findResource(name);
		
		// expand the jar url if necessary
		if (result != null && "jar".equals(result.getProtocol()) && name.contains("openmrs")) {
			result = expandURL(result, getLibCacheFolder());
		}
		
		return result;
	}
	
	/**
	 * @see java.net.URLClassLoader#findResources(java.lang.String)
	 */
	@Override
	public Enumeration<URL> findResources(final String name) throws IOException {
		Set<URI> results = new HashSet<>();
		for (ModuleClassLoader classLoader : ModuleFactory.getModuleClassLoaders()) {
			Enumeration<URL> urls = classLoader.findResources(name);
			while (urls.hasMoreElements()) {
				URL result = urls.nextElement();
				if (result != null) {
					try {
						results.add(result.toURI());
					}
					catch (URISyntaxException e) {
						throwInvalidURI(result, e);
					}
				}
			}
		}
		
		for (Enumeration<URL> en = super.findResources(name); en.hasMoreElements();) {
			URL url = en.nextElement();
			try {
				results.add(url.toURI());
			}
			catch (URISyntaxException e) {
				throwInvalidURI(url, e);
			}
		}
		
		List<URL> resources = new ArrayList<>(results.size());
		for (URI result : results) {
			resources.add(result.toURL());
		}
		
		return Collections.enumeration(resources);
	}
	
	private void throwInvalidURI(URL url, Exception e) throws IOException {
		throw new IOException(url.getPath() + " is not a valid URI", e);
	}
	
	/**
	 * Searches all known module classloaders first, then parent classloaders
	 *
	 * @see java.lang.ClassLoader#getResourceAsStream(java.lang.String)
	 */
	@Override
	public InputStream getResourceAsStream(String file) {
		for (ModuleClassLoader classLoader : ModuleFactory.getModuleClassLoaders()) {
			InputStream result = classLoader.getResourceAsStream(file);
			if (result != null) {
				return result;
			}
		}
		
		return super.getResourceAsStream(file);
	}
	
	/**
	 * Searches all known module classloaders first, then parent classloaders
	 *
	 * @see java.lang.ClassLoader#getResources(java.lang.String)
	 */
	@Override
	public Enumeration<URL> getResources(String packageName) throws IOException {
		Set<URI> results = new HashSet<>();
		for (ModuleClassLoader classLoader : ModuleFactory.getModuleClassLoaders()) {
			Enumeration<URL> urls = classLoader.getResources(packageName);
			while (urls.hasMoreElements()) {
				URL result = urls.nextElement();
				if (result != null) {
					try {
						results.add(result.toURI());
					}
					catch (URISyntaxException e) {
						throwInvalidURI(result, e);
					}
				}
			}
		}
		
		for (Enumeration<URL> en = super.getResources(packageName); en.hasMoreElements();) {
			URL url = en.nextElement();
			try {
				results.add(url.toURI());
			}
			catch (URISyntaxException e) {
				throwInvalidURI(url, e);
			}
		}
		
		List<URL> resources = new ArrayList<>(results.size());
		for (URI result : results) {
			resources.add(result.toURL());
		}
		
		return Collections.enumeration(resources);
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Openmrs" + super.toString();
	}
	
	/**
	 * Destroy the current instance of the classloader. Note**: After calling this and after the new
	 * service is set up, All classes using this instance should be flushed. This would allow all
	 * java classes that were loaded by the old instance variable to be gc'd and modules to load in
	 * new java classes
	 *
	 * @see #flushInstance()
	 */
	public static void destroyInstance() {
		
		// remove all thread references to this class
		// Walk up all the way to the root thread group
		ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
		ThreadGroup parent;
		while ((parent = rootGroup.getParent()) != null) {
			rootGroup = parent;
		}
		
		log.info("this classloader hashcode: " + OpenmrsClassLoaderHolder.INSTANCE.hashCode());
		
		//Shut down and remove all cache managers.
		List<CacheManager> knownCacheManagers = CacheManager.ALL_CACHE_MANAGERS;
		while (!knownCacheManagers.isEmpty()) {
			CacheManager cacheManager = CacheManager.ALL_CACHE_MANAGERS.get(0);
			try {
				//This shuts down and removes the cache manager.
				cacheManager.shutdown();
				
				//Just in case the the timer does not stop, set the cacheManager 
				//timer to null because it references this class loader.
				Field field = cacheManager.getClass().getDeclaredField("cacheManagerTimer");
				field.setAccessible(true);
				field.set(cacheManager, null);
			}
			catch (Exception ex) {
				log.error(ex.getMessage(), ex);
			}
		}
		
		OpenmrsClassScanner.destroyInstance();
		
		OpenmrsClassLoaderHolder.INSTANCE = null;
	}
	
	/**
	 * Sets the class loader, for all threads referencing a destroyed openmrs class loader, 
	 * to the current one.
	 */
	public static void setThreadsToNewClassLoader() {
		//Give ownership of all threads loaded by the old class loader to the new one.
		//Examples of such threads are: Keep-Alive-Timer, MySQL Statement Cancellation Timer, etc
		//That way they will no longer hold onto the destroyed OpenmrsClassLoader and hence
		//allow it to be garbage collected after a spring application context refresh, when a new one is created.
		Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()]);
		for (Thread thread : threadArray) {
			
			ClassLoader classLoader = thread.getContextClassLoader();
			
			//Some threads have a null class loader reference. e.g Finalizer, Reference Handler, etc
			if (classLoader == null) {
				continue;
			}
			
			//Threads referencing the current class loader are good.
			if (classLoader == getInstance()) {
				continue;
			}
			
			//For threads referencing any destroyed class loader, point them to the new one.
			if (classLoader instanceof OpenmrsClassLoader) {
				thread.setContextClassLoader(getInstance());
			}
		}
	}
	
	// List all threads and recursively list all subgroup
	private static List<Thread> listThreads(ThreadGroup group, String indent) {
		List<Thread> threadToReturn = new ArrayList<>();
		
		log.error(indent + "Group[" + group.getName() + ":" + group.getClass() + "]");
		int nt = group.activeCount();
		Thread[] threads = new Thread[nt * 2 + 10]; //nt is not accurate
		nt = group.enumerate(threads, false);
		
		// List every thread in the group
		for (int i = 0; i < nt; i++) {
			Thread t = threads[i];
			log.error(indent
			        + "  Thread["
			        + t.getName()
			        + ":"
			        + t.getClass()
			        + ":"
			        + (t.getContextClassLoader() == null ? "null cl" : t.getContextClassLoader().getClass().getName() + " "
			                + t.getContextClassLoader().hashCode()) + "]");
			threadToReturn.add(t);
		}
		
		// Recursively list all subgroups
		int ng = group.activeGroupCount();
		ThreadGroup[] groups = new ThreadGroup[ng * 2 + 10];
		ng = group.enumerate(groups, false);
		
		for (int i = 0; i < ng; i++) {
			threadToReturn.addAll(listThreads(groups[i], indent + "  "));
		}
		
		return threadToReturn;
	}
	
	public static void onShutdown() {
		
		//Since we are shutting down, stop all threads that reference the openmrs class loader.
		Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()]);
		for (Thread thread : threadArray) {
			
			ClassLoader classLoader = thread.getContextClassLoader();
			
			//Threads like Finalizer, Reference Handler, etc have null class loader reference.
			if (classLoader == null) {
				continue;
			}
			
			if (classLoader instanceof OpenmrsClassLoader) {
				try {
					//Set to WebappClassLoader just in case stopping fails.
					thread.setContextClassLoader(classLoader.getParent());
					
					//Stopping the current thread will halt all current cleanup.
					//So do not ever ever even attempt stopping it. :)
					if (thread == Thread.currentThread()) {
						continue;
					}
					
					log.info("onShutdown Stopping thread: " + thread.getName());
					thread.stop();
				}
				catch (Exception ex) {
					log.error(ex.getMessage(), ex);
				}
			}
		}
		
		clearReferences();
	}
	
	/**
	 * This clears any references this classloader might have that will prevent garbage collection. <br>
	 * <br>
	 * Borrowed from Tomcat's WebappClassLoader#clearReferences() (not javadoc linked intentionally) <br>
	 * The only difference between this and Tomcat's implementation is that this one only acts on
	 * openmrs objects and also clears out static java.* packages. Tomcat acts on all objects and
	 * does not clear our static java.* objects.
	 *
	 * @since 1.5
	 */
	protected static void clearReferences() {
		
		// Unregister any JDBC drivers loaded by this classloader
		Enumeration<Driver> drivers = DriverManager.getDrivers();
		while (drivers.hasMoreElements()) {
			Driver driver = drivers.nextElement();
			if (driver.getClass().getClassLoader() == getInstance()) {
				try {
					DriverManager.deregisterDriver(driver);
				}
				catch (SQLException e) {
					log.warn("SQL driver deregistration failed", e);
				}
			}
		}
		
		// Null out any static or final fields from loaded classes,
		// as a workaround for apparent garbage collection bugs
		for (WeakReference<Class<?>> refClazz : getInstance().cachedClasses.values()) {
			if (refClazz == null) {
				continue;
			}
			Class<?> clazz = refClazz.get();
			if (clazz != null && clazz.getName().contains("openmrs")) { // only clean up openmrs classes
				try {
					Field[] fields = clazz.getDeclaredFields();
					for (Field field : fields) {
						int mods = field.getModifiers();
						if (field.getType().isPrimitive() || (field.getName().contains("$"))) {
							continue;
						}
						if (Modifier.isStatic(mods)) {
							try {
								// do not clear the log field on this class yet
								if (clazz.equals(OpenmrsClassLoader.class) && "log".equals(field.getName())) {
									continue;
								}
								field.setAccessible(true);
								if (Modifier.isFinal(mods)) {
									if (!(field.getType().getName().startsWith("javax."))) {
										nullInstance(field.get(null));
									}
								} else {
									field.set(null, null);
									if (log.isDebugEnabled()) {
										log.debug("Set field " + field.getName() + " to null in class " + clazz.getName());
									}
								}
							}
							catch (Exception t) {
								if (log.isDebugEnabled()) {
									log.debug("Could not set field " + field.getName() + " to null in class "
											+ clazz.getName(), t);
								}
							}
						}
					}
				}
				catch (Exception t) {
					if (log.isDebugEnabled()) {
						log.debug("Could not clean fields for class " + clazz.getName(), t);
					}
				}
			}
		}
		
		// now we can clear the log field on this class
		OpenmrsClassLoader.log = null;
		
		getInstance().cachedClasses.clear();
	}
	
	/**
	 * Used by {@link #clearReferences()} upon application close. <br>
	 * <br>
	 * Borrowed from Tomcat's WebappClassLoader.
	 *
	 * @param instance the object whose fields need to be nulled out
	 */
	protected static void nullInstance(Object instance) {
		if (instance == null) {
			return;
		}
		Field[] fields = instance.getClass().getDeclaredFields();
		for (Field field : fields) {
			int mods = field.getModifiers();
			if (field.getType().isPrimitive() || (field.getName().contains("$"))) {
				continue;
			}
			try {
				field.setAccessible(true);
				if (Modifier.isStatic(mods) && Modifier.isFinal(mods)) {
					// Doing something recursively is too risky
					continue;
				} else {
					Object value = field.get(instance);
					if (null != value) {
						Class<?> valueClass = value.getClass();
						if (!loadedByThisOrChild(valueClass)) {
							if (log.isDebugEnabled()) {
								log.debug("Not setting field " + field.getName() + " to null in object of class "
										+ instance.getClass().getName() + " because the referenced object was of type "
										+ valueClass.getName() + " which was not loaded by this WebappClassLoader.");
							}
						} else {
							field.set(instance, null);
							if (log.isDebugEnabled()) {
								log.debug("Set field " + field.getName() + " to null in class "
										+ instance.getClass().getName());
							}
						}
					}
				}
			}
			catch (Exception e) {
				if (log.isDebugEnabled()) {
					log.debug("Could not set field " + field.getName() + " to null in object instance of class "
							+ instance.getClass().getName(), e);
				}
			}
		}
	}
	
	/**
	 * Determine whether a class was loaded by this class loader or one of its child class loaders. <br>
	 * <br>
	 * Borrowed from Tomcat's WebappClassLoader
	 */
	protected static boolean loadedByThisOrChild(Class<?> clazz) {
		boolean result = false;
		for (ClassLoader classLoader = clazz.getClassLoader(); null != classLoader; classLoader = classLoader.getParent()) {
			if (classLoader.equals(getInstance())) {
				result = true;
				break;
			}
		}
		return result;
	}
	
	/**
	 * This method should be called before destroying the instance
	 *
	 * @see #destroyInstance()
	 */
	public static void saveState() {
		try {
			String key = SchedulerService.class.getName();
			if (!Context.isRefreshingContext()) {
				mementos.put(key, Context.getSchedulerService().saveToMemento());
			}
		}
		catch (Exception t) {
			// pass
		}
	}
	
	/**
	 * This method should be called after restoring the instance
	 *
	 * @see #destroyInstance()
	 * @see #saveState()
	 */
	public static void restoreState() {
		try {
			String key = SchedulerService.class.getName();
			Context.getSchedulerService().restoreFromMemento(mementos.get(key));
		}
		catch (APIException e) {
			// pass
		}
		mementos.clear();
	}
	
	/**
	 * All objects depending on the old classloader should be restarted here Should be called after
	 * destoryInstance() and after the service is restarted
	 *
	 * @see #destroyInstance()
	 */
	public static void flushInstance() {
		try {
			SchedulerService service = null;
			try {
				service = Context.getSchedulerService();
			}
			catch (APIException e2) {
				// if there isn't a scheduler service yet, ignore error
				log.warn("Unable to get scheduler service", e2);
			}
			if (service != null) {
				service.rescheduleAllTasks();
			}
		}
		catch (SchedulerException e) {
			log.error("Failed to restart scheduler tasks", e);
		}
	}
	
	/**
	 * Get the temporary "work" directory for expanded jar files
	 *
	 * @return temporary location for storing the libraries
	 */
	public static File getLibCacheFolder() {
		// cache the location for all calls until OpenMRS is restarted
		if (libCacheFolder != null) {
			return libCacheFolderInitialized ? libCacheFolder : null;
		}
		
		synchronized (ModuleClassLoader.class) {
			libCacheFolder = new File(OpenmrsUtil.getApplicationDataDirectory(), LIBCACHESUFFIX);
			
			if (log.isDebugEnabled()) {
				log.debug("libraries cache folder is " + libCacheFolder);
			}
			
			if (libCacheFolder.exists()) {
				// clean up and empty the folder if it exists (and is not locked)
				try {
					OpenmrsUtil.deleteDirectory(libCacheFolder);
					
					libCacheFolder.mkdirs();
				}
				catch (IOException io) {
					log.warn("Unable to delete: " + libCacheFolder.getName());
				}
			} else {
				// otherwise just create the dir structure
				libCacheFolder.mkdirs();
			}
			
			// mark the lock and entire library cache to be deleted when the jvm exits
			libCacheFolder.deleteOnExit();
			
			// mark the lib cache folder as ready
			libCacheFolderInitialized = true;
		}
		
		return libCacheFolder;
	}
	
	/**
	 * Expand the given URL into the given folder
	 *
	 * @param result URL of the file to expand
	 * @param folder File (directory) to place the expanded file
	 * @return the URL at the expanded location
	 */
	public static URL expandURL(URL result, File folder) {
		String extForm = result.toExternalForm();
		// trim out "jar:file:/ and ascii spaces"
		if (OpenmrsConstants.UNIX_BASED_OPERATING_SYSTEM) {
			extForm = extForm.replaceFirst("jar:file:", "").replaceAll("%20", " ");
		} else {
			extForm = extForm.replaceFirst("jar:file:/", "").replaceAll("%20", " ");
		}
		
		if (log.isDebugEnabled()) {
			log.debug("url external form: " + extForm);
		}
		
		int i = extForm.indexOf("!");
		String jarPath = extForm.substring(0, i);
		String filePath = extForm.substring(i + 2); // skip over both the '!' and the '/'
		
		if (log.isDebugEnabled()) {
			log.debug("jarPath: " + jarPath);
			log.debug("filePath: " + filePath);
		}
		
		File file = new File(folder, filePath);
		
		if (log.isDebugEnabled()) {
			log.debug("absolute path: " + file.getAbsolutePath());
		}
		
		try {
			// if the file has been expanded already, return that
			if (file.exists()) {
				return file.toURI().toURL();
			} else {
				// expand the url and return a url to the temp file
				File jarFile = new File(jarPath);
				if (!jarFile.exists()) {
					log.warn("Cannot find jar at: " + jarFile + " for url: " + result);
					return null;
				}
				
				ModuleUtil.expandJar(jarFile, folder, filePath, true);
				return file.toURI().toURL();
			}
		}
		catch (IOException io) {
			log.warn("Unable to expand url: " + result, io);
			return null;
		}
	}
	
	/**
	 * This class exists solely so OpenmrsClassLoader can call the (should be static) method
	 * <code>URLConnection.setDefaultUseCaches(Boolean)</code>. This causes jars opened to not be
	 * locked (and allows for the webapp to be reloadable).
	 */
	private class OpenmrsURLConnection extends URLConnection {
		
		public OpenmrsURLConnection() {
			super(null);
		}
		
		@Override
		public void connect() throws IOException {
			
		}
		
	}
}
