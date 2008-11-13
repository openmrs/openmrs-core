package org.openmrs.logic;

import java.util.Collection;
import java.util.Iterator;

import org.openmrs.module.ModuleClassLoader;
import org.openmrs.module.ModuleFactory;

/**
 * Class Loader that loads rule classes into ruleMap
 * 
 * Adapted from ibm.com/developerWorks
 */
public class RuleClassLoader extends ClassLoader {
	//private static Log log = LogFactory.getLog(RuleClassLoader.class);

	// The heart of the ClassLoader -- automatically compile
	// source as necessary when looking for class files
	@Override
	public Class<?> findClass(String name) throws ClassNotFoundException {

		// Our goal is to get a Class object
		Class<?> clas = null;

		// look for loaded classes
		try {
			clas = super.findLoadedClass(name);
		} catch (Exception e) {
		}

		// look for system classes
		if (clas == null) {
			try {
				clas = super.findSystemClass(name);
			} catch (Exception e) {
			}
		}

		// check context class loader
		if (clas == null) {
			try {
				ClassLoader parent = Thread.currentThread()
				                           .getContextClassLoader();
				clas = parent.loadClass(name);
			} catch (Exception e) {
			}
		}

		// check module class loaders
		if (clas == null) {
			Collection<ModuleClassLoader> moduleClassLoaders = ModuleFactory.getModuleClassLoaders();

			Iterator<ModuleClassLoader> iter = moduleClassLoaders.iterator();

			while (iter.hasNext() && clas == null) {
				ModuleClassLoader currClassLoader = iter.next();
				try {
					clas = currClassLoader.loadClass(name);
				} catch (Exception e) {
				}
			}
		}

		return clas;
	}
}
