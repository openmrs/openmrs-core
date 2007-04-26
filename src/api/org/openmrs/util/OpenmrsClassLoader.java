package org.openmrs.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
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
import org.openmrs.scheduler.SchedulerService;

public class OpenmrsClassLoader extends URLClassLoader {
	private static Log log = LogFactory.getLog(OpenmrsClassLoader.class);
	
	private static File libCacheFolder;
	private static boolean libCacheFolderInitialized = false;
	
	// parent class loader for all modules
	private static OpenmrsClassLoader instance = null;
	
	// placeholder to hold mementos to restore
	private static Map<String, OpenmrsMemento> mementos = new WeakHashMap<String, OpenmrsMemento>();
	
	/**
	 * Creates the instance for the OpenmrsClassLoader
	 */
	public OpenmrsClassLoader(ClassLoader parent) {
		super(new URL[0], parent);
		instance = this;
		log.debug("Creating new OpenmrsClassLoader instance with parent: " + parent);
		
		//disable caching so the jars aren't locked
		// if performace is effected, this can be disabled in favor of
		//  copying all opened jars to a temp location 
		//  (ala org.apache.catalina.loader.WebappClassLoader antijarlocking)
		URLConnection urlConnection = new OpenmrsURLConnection();
		urlConnection.setDefaultUseCaches(false);
		
	}
	
	public OpenmrsClassLoader() {
		this(OpenmrsClassLoader.class.getClassLoader());
	}
	
	
	/**
	 * Get the static/singular instance of the module class loader
	 * @return
	 */
	public static OpenmrsClassLoader getInstance() {
		if (instance == null) {
			log.trace("Creating new OpenmrsClassLoader instance");
			instance = new OpenmrsClassLoader();
		}
		return instance;
	}
	
	public Class<?> loadClass(String name, final boolean resolve) throws ClassNotFoundException {
		for (ModuleClassLoader classLoader : ModuleFactory.getModuleClassLoaders()) {
			try {
				return classLoader.loadClass(name);
			}
			catch (ClassNotFoundException e) {
				//log.debug("Didn't find entry for: " + name);
			}
		}
		
		return getParent().loadClass(name);
	}
	
	public URL findResource(final String name) {
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

	public String toString() {
		return "Openmrs" + super.toString();
	}
	
	
	/**
	 * Destroy the current instance of the classloader.  
	 * 
	 * Note**:  After calling this and after the new service is set up,
	 * All classes using this instance should be flushed.  This would allow all java classes 
	 * that were loaded by the old instance variable to be gc'd and 
	 * modules to load in new java classes
	 * @see flushInstance()
	 *
	 */
	public static void destroyInstance() {
		instance = null;
	}
	
	/**
	 * This method should be called before destroying the instance
	 * 
	 * @see destroyInstance()
	 */
	public static void saveState() {
		
		// TODO our services should implement a common 
		// OpenmrsService so this can be generalized
		try {
			String key = SchedulerService.class.getName();
			mementos.put(key, Context.getSchedulerService().saveToMemento());
		}
		catch (APIException e) {
			// pass
		}
		
	}

	/**
	 * This method should be called after restoring the instance
	 * 
	 * @see destroyInstance()
	 * @see saveState()
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
	 * All objects depending on the old classloader should be restarted here
	 * 
	 * Should be called after destoryInstance() and after the service is restarted
	 * 
	 * @see destroyInstance()
	 */
	public static void flushInstance() {
		try {
			SchedulerService service = null;
			try {
				Context.getSchedulerService();
			}
			catch (APIException e2) {
				// if there isn't a scheduler service yet, ignore error
				log.warn("Unable to get scheduler service", e2);
			}
			if (service != null)
				service.restartTasks();
		}
		catch (Exception e) {
			log.error("Unable to restart scheduler tasks", e);
		}
	}
	
	/**
	 * Get the temporary "work" directory for expanded jar files
	 * 
	 * @return
	 */
	public static File getLibCacheFolder() {
		if (libCacheFolder != null) {
			return libCacheFolderInitialized ? libCacheFolder : null;
		}
		synchronized (ModuleClassLoader.class) {
			libCacheFolder = new File(System.getProperty("java.io.tmpdir"),
					System.currentTimeMillis() + ".openmrs-lib-cache");
			log.debug("libraries cache folder is " + libCacheFolder);
			File lockFile = new File(libCacheFolder, "lock");
			if (lockFile.exists()) {
				log.error("can't initialize libraries cache folder "
						+ libCacheFolder + " as lock file indicates that it"
						+ " is owned by another openmrs instance");
				return null;
			}
			if (libCacheFolder.exists()) {
				// clean up folder
				try {
					OpenmrsUtil.deleteDirectory(libCacheFolder);
				}
				catch (IOException io) {
					log.warn("Unable to delete: " + libCacheFolder.getName());
				}
			} else {
				libCacheFolder.mkdirs();
			}
			try {
				if (!lockFile.createNewFile()) {
					log.error("can\'t create lock file in JPF libraries cache"
							+ " folder " + libCacheFolder);
					return null;
				}
			} catch (IOException ioe) {
				log.error("can\'t create lock file in JPF libraries cache"
						+ " folder " + libCacheFolder, ioe);
				return null;
			}
			lockFile.deleteOnExit();
			libCacheFolder.deleteOnExit();
			libCacheFolderInitialized = true;
		}
		return libCacheFolder;
	}
	
	/**
	 * Expand the given URL into the given folder
	 * 
	 * @param result
	 * @param folder
	 * @return
	 */
	public static URL expandURL(URL result, File folder) {
		String extForm = result.toExternalForm();
		// trim out "jar:file:/ and ascii spaces"
		if (OpenmrsConstants.OPERATING_SYSTEM_FREEBSD.equalsIgnoreCase(OpenmrsConstants.OPERATING_SYSTEM) || 
			OpenmrsConstants.OPERATING_SYSTEM_LINUX.equalsIgnoreCase(OpenmrsConstants.OPERATING_SYSTEM))
			extForm = extForm.replaceFirst("jar:file:", "").replaceAll("%20", " ");
		else
			extForm = extForm.replaceFirst("jar:file:/", "").replaceAll("%20", " ");
		
		log.debug("url external form: " + extForm);
		
		int i = extForm.indexOf("!");
		String jarPath = extForm.substring(0, i);
		String filePath = extForm.substring(i+2); // skip over both the '!' and the '/'
		
		log.debug("jarPath: " + jarPath);
		log.debug("filePath: " + filePath);
		
		File file = new File(folder, filePath);
		
		log.debug("absolute path: " + file.getAbsolutePath());
		
		try {
			// if the file has been expanded already, return that
			if (file.exists())
				return file.toURL();
			else {
				// expand the url and return a url to the temp file
				File jarFile = new File(jarPath);
				if (!jarFile.exists()) {
					log.warn("Cannot find jar at: " + jarFile + " for url: " + result);
					return null;
				}
				
				ModuleUtil.expandJar(jarFile, folder, filePath, true);
				return file.toURL();
			}
		}
		catch (IOException io) {
			log.warn("Unable to expand url: " + result, io);
			return null;
		}
	}
	
	/**
	 * This class exists solely so OpenmrsClassLoader can call the (should be static)
	 * method <code>URLConnection.setDefaultUseCaches(Boolean)</code>.  This causes jars opened to not be 
	 * locked (and allows for the webapp to be reloadable.
	 *  
	 * @author Ben Wolfe
	 */
	private class OpenmrsURLConnection extends URLConnection {

		public OpenmrsURLConnection() {
			super(null);
		}

		public void connect() throws IOException {
			
		}

	}
}
