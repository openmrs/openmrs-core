package org.openmrs.logic;

import java.util.Collection;
import java.util.Iterator;

import org.openmrs.module.ModuleClassLoader;
import org.openmrs.module.ModuleFactory;
import org.openmrs.util.OpenmrsClassLoader;

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
		// check openmrs class loader
		if (clas == null) {
			try {
				clas = OpenmrsClassLoader.getInstance().loadClass(name);
			} catch (Exception e) {
			}
		}

		return clas;
	}
}
