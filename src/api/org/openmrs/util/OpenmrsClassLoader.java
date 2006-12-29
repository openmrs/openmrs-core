package org.openmrs.util;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.apache.catalina.loader.WebappClassLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ModuleClassLoader;
import org.openmrs.module.ModuleFactory;

public class OpenmrsClassLoader extends WebappClassLoader {
	private static Log log = LogFactory.getLog(OpenmrsClassLoader.class);
    
	// parent class loader for all modules
	private static OpenmrsClassLoader instance = null;
	
	/**
	 * Creates the instance for the moduleclassloader
	 */
	public OpenmrsClassLoader(ClassLoader parent) {
		super(parent);
		instance = this;
		log.debug("Creating new OpenmrsClassLoader instance with parent: " + parent);
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
			log.debug("Creating new OpenmrsClassLoader instance");
			instance = new OpenmrsClassLoader();
			//Thread.currentThread().setContextClassLoader(getInstance());
		}
		return instance;
	}
	
	public Class<?> loadClass(String name, final boolean resolve) throws ClassNotFoundException {
		//log.debug("loading class " + name + " with " + pluginClassLoader);
		
		for (ModuleClassLoader classLoader : ModuleFactory.getModuleClassLoaders()) {
			if (name.contains("Hello"))
				log.debug(name + " classLoader: " + classLoader + " uid: " + classLoader.hashCode());
			try {
				return classLoader.loadClass(name);
			}
			catch (ClassNotFoundException e) {
				//log.debug("Didn't find entry for: " + name);
			}
		}
		
		if (name.contains("Hello"))
			log.debug("'Hello' class " + name + " not found. will try parent: " + getParent() + " uid: " + getParent().hashCode());
		
		return getParent().loadClass(name);
	}
	
	public URL findResource(final String name) {
		if (name.contains("Hello"))
			log.debug("Finding 'Hello' resource: " + name);
		
		URL result;
		for (ModuleClassLoader classLoader : ModuleFactory.getModuleClassLoaders()) {
			result = classLoader.getResource(name);
			if (result != null)
				return result;
		}
		
		return super.findResource(name);
	}
	
	public Enumeration<URL> findResources(final String name) throws IOException {
		if (name.contains("Hello"))
			log.debug("Finding 'Hello' resources: " + name);
		
		Set<URL> results = new HashSet<URL>();
		for (ModuleClassLoader classLoader : ModuleFactory.getModuleClassLoaders()) {
			Enumeration<URL> urls = classLoader.getResources(name);
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
	
	public static void destroyInstance() {
		instance = null;
	}
}
