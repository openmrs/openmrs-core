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
package org.openmrs.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.ModuleClassLoader;
import org.openmrs.module.ModuleFactory;
import org.openmrs.module.ModuleUtil;
import org.openmrs.scheduler.SchedulerException;
import org.openmrs.scheduler.SchedulerService;

/**
 * This classloader knows about the current ModuleClassLoaders and will attempt to load classes from
 * them if needed
 */
public class OpenmrsClassLoader extends URLClassLoader {
	
	private static Log log = LogFactory.getLog(OpenmrsClassLoader.class);
	
	private static File libCacheFolder;
	
	private static boolean libCacheFolderInitialized = false;
	
	// placeholder to hold mementos to restore
	private static Map<String, OpenmrsMemento> mementos = new WeakHashMap<String, OpenmrsMemento>();
	
	// holds a list of all classes that this classloader loaded so that they can be cleaned up
	private Set<Class<?>> loadedClasses = new HashSet<Class<?>>();
	
	/**
	 * Creates the instance for the OpenmrsClassLoader
	 */
	public OpenmrsClassLoader(ClassLoader parent) {
		super(new URL[0], parent);
		OpenmrsClassLoaderHolder.INSTANCE = this;
		
		if (log.isDebugEnabled())
			log.debug("Creating new OpenmrsClassLoader instance with parent: " + parent);
		
		//disable caching so the jars aren't locked
		// if performance is effected, this can be disabled in favor of
		//  copying all opened jars to a temp location
		//  (ala org.apache.catalina.loader.WebappClassLoader antijarlocking)
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
		
		private static OpenmrsClassLoader INSTANCE = null;
		
	}
	
	/**
	 * Get the static/singular instance of the module class loader
	 * 
	 * @return OpenmrsClassLoader
	 */
	public static OpenmrsClassLoader getInstance() {
		if (OpenmrsClassLoaderHolder.INSTANCE == null)
			OpenmrsClassLoaderHolder.INSTANCE = new OpenmrsClassLoader();
		
		return OpenmrsClassLoaderHolder.INSTANCE;
	}
	
	/**
	 * @see java.lang.ClassLoader#loadClass(java.lang.String, boolean)
	 */
	@Override
    public Class<?> loadClass(String name, final boolean resolve) throws ClassNotFoundException {
		for (ModuleClassLoader classLoader : ModuleFactory.getModuleClassLoaders()) {
			// this is to prevent unnecessary looping over providedPackages
			boolean tryToLoad = name.startsWith(classLoader.getModule().getPackageName());
			
			// the given class name doesn't match the config.xml package in this module,
			// check the "providedPackage" list to see if its in a lib
			if (!tryToLoad) {
			
				for (String providedPackage : classLoader.getAdditionalPackages()) {
					// break out early if we match a package
					if (name.startsWith(providedPackage)) {
						tryToLoad = true;
						break;
					}
				}
			}
			
			if (tryToLoad) {
				try {
					//if (classLoader.isLoadingFromParent() == false)
					Class<?> c = classLoader.loadClass(name);
					loadedClasses.add(c);
					return c;
				}
				catch (ClassNotFoundException e) {
					//log.debug("Didn't find entry for: " + name);
				}
			}
		}

		/* See org.mortbay.jetty.webapp.WebAppClassLoader.loadClass, from
  		 * http://dist.codehaus.org/jetty/jetty-6.1.10/jetty-6.1.10-src.zip */		
		ClassNotFoundException ex= null;

		try {
			Class<?> c = getParent().loadClass(name);
			loadedClasses.add(c);
			return c;
		} catch (ClassNotFoundException e) {
			ex = e;			
		}

		try {
			Class<?> c = this.findClass(name);
			return c;
		} catch (ClassNotFoundException e) {
			ex = e;			
		}

		throw ex;
	}
	
	/**
	 * @see java.net.URLClassLoader#findResource(java.lang.String)
	 */
	@Override
    public URL findResource(final String name) {
		if (log.isTraceEnabled())
			log.trace("finding resource: " + name);
		
		URL result;
		for (ModuleClassLoader classLoader : ModuleFactory.getModuleClassLoaders()) {
			result = classLoader.findResource(name);
			if (result != null)
				return result;
		}
		
		// look for the resource in the parent
		result = super.findResource(name);
		
		// expand the jar url if necessary
		if (result != null && result.getProtocol().equals("jar") && name.contains("openmrs")) {
			result = expandURL(result, getLibCacheFolder());
		}
		
		return result;
	}
	
	/**
	 * @see java.net.URLClassLoader#findResources(java.lang.String)
	 */
	@Override
    public Enumeration<URL> findResources(final String name) throws IOException {
		Set<URL> results = new HashSet<URL>();
		for (ModuleClassLoader classLoader : ModuleFactory.getModuleClassLoaders()) {
			Enumeration<URL> urls = classLoader.findResources(name);
			while (urls.hasMoreElements()) {
				URL result = urls.nextElement();
				if (result != null)
					results.add(result);
			}
		}
		
		for (Enumeration<URL> en = super.findResources(name); en.hasMoreElements();) {
			results.add(en.nextElement());
		}
		
		return Collections.enumeration(results);
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
			if (result != null)
				return result;
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
		Set<URL> results = new HashSet<URL>();
		for (ModuleClassLoader classLoader : ModuleFactory.getModuleClassLoaders()) {
			Enumeration<URL> urls = classLoader.getResources(packageName);
			while (urls.hasMoreElements()) {
				URL result = urls.nextElement();
				if (result != null)
					results.add(result);
			}
		}
		
		for (Enumeration<URL> en = super.getResources(packageName); en.hasMoreElements();) {
			results.add(en.nextElement());
		}
		
		return Collections.enumeration(results);
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
		OpenmrsClassLoaderHolder.INSTANCE = null;
	}
	
	public static void onShutdown() {
		clearReferences();
	}
	
	/**
	 * This clears any references this classloader might have that will prevent garbage collection. <br/>
	 * <br/>
	 * Borrowed from Tomcat's WebappClassLoader#clearReferences() (not javadoc linked intentionally) <br/>
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
		for (Class<?> clazz : getInstance().loadedClasses) {
			if (clazz != null && clazz.getName().contains("openmrs")) { // only clean up openmrs classes
				try {
					Field[] fields = clazz.getDeclaredFields();
					for (int i = 0; i < fields.length; i++) {
						Field field = fields[i];
						int mods = field.getModifiers();
						if (field.getType().isPrimitive() || (field.getName().indexOf("$") != -1)) {
							continue;
						}
						if (Modifier.isStatic(mods)) {
							try {
								// do not clear the log field on this class yet
								if (clazz.equals(OpenmrsClassLoader.class) && field.getName().equals("log"))
									continue;
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
							catch (Throwable t) {
								if (log.isDebugEnabled()) {
									log.debug("Could not set field " + field.getName() + " to null in class "
									        + clazz.getName(), t);
								}
							}
						}
					}
				}
				catch (Throwable t) {
					if (log.isDebugEnabled()) {
						log.debug("Could not clean fields for class " + clazz.getName(), t);
					}
				}
			}
		}
		
		// now we can clear the log field on this class
		OpenmrsClassLoader.log = null;
		
		getInstance().loadedClasses.clear();
	}
	
	/**
	 * Used by {@link #clearReferences()} upon application close. <br/>
	 * <br/>
	 * Borrowed from Tomcat's WebappClassLoader.
	 * 
	 * @param instance the object whose fields need to be nulled out
	 */
	protected static void nullInstance(Object instance) {
		if (instance == null) {
			return;
		}
		Field[] fields = instance.getClass().getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			int mods = field.getModifiers();
			if (field.getType().isPrimitive() || (field.getName().indexOf("$") != -1)) {
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
			catch (Throwable t) {
				if (log.isDebugEnabled()) {
					log.debug("Could not set field " + field.getName() + " to null in object instance of class "
					        + instance.getClass().getName(), t);
				}
			}
		}
	}
	
	/**
	 * Determine whether a class was loaded by this class loader or one of its child class loaders. <br/>
	 * <br/>
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
		
		// TODO our services should implement a common
		// OpenmrsService so this can be generalized
		try {
			String key = SchedulerService.class.getName();
			if (!Context.isRefreshingContext())
				mementos.put(key, Context.getSchedulerService().saveToMemento());
		}
		catch (Throwable t) {
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
		// TODO our services should implement a common
		// OpenmrsService so this can be generalized
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
		if (libCacheFolder != null)
			return libCacheFolderInitialized ? libCacheFolder : null;
		
		synchronized (ModuleClassLoader.class) {
			libCacheFolder = new File(System.getProperty("java.io.tmpdir"), System.currentTimeMillis()
			        + ".openmrs-lib-cache");
			
			if (log.isDebugEnabled())
				log.debug("libraries cache folder is " + libCacheFolder);
			
			File lockFile = new File(libCacheFolder, "lock");
			if (lockFile.exists()) {
				log.error("can't initialize libraries cache folder " + libCacheFolder + " as lock file indicates that it"
				        + " is owned by another openmrs instance");
				return null;
			}
			
			if (libCacheFolder.exists()) {
				// clean up and empty the folder if it exists (and is not locked)
				try {
					OpenmrsUtil.deleteDirectory(libCacheFolder);
				}
				catch (IOException io) {
					log.warn("Unable to delete: " + libCacheFolder.getName());
				}
			} else {
				// otherwise just create the dir structure
				libCacheFolder.mkdirs();
			}
			
			// create the lock file in the lib cache folder to prevent other caches
			// from being created here
			try {
				if (!lockFile.createNewFile()) {
					log.error("can't create lock file in JPF libraries cache folder" + libCacheFolder);
					return null;
				}
			}
			catch (IOException ioe) {
				log.error("can't create lock file in JPF libraries cache folder " + libCacheFolder, ioe);
				return null;
			}
			
			// mark the lock and entire library cache to be deleted when the jvm exits
			lockFile.deleteOnExit();
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
		if (OpenmrsConstants.UNIX_BASED_OPERATING_SYSTEM)
			extForm = extForm.replaceFirst("jar:file:", "").replaceAll("%20", " ");
		else
			extForm = extForm.replaceFirst("jar:file:/", "").replaceAll("%20", " ");
		
		if (log.isDebugEnabled())
			log.debug("url external form: " + extForm);
		
		int i = extForm.indexOf("!");
		String jarPath = extForm.substring(0, i);
		String filePath = extForm.substring(i + 2); // skip over both the '!' and the '/'
		
		if (log.isDebugEnabled()) {
			log.debug("jarPath: " + jarPath);
			log.debug("filePath: " + filePath);
		}
		
		File file = new File(folder, filePath);
		
		if (log.isDebugEnabled())
			log.debug("absolute path: " + file.getAbsolutePath());
		
		try {
			// if the file has been expanded already, return that
			if (file.exists())
				return file.toURI().toURL();
			else {
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
