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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AdvicePoint {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	private String point;
	
	private Object classInstance;
	
	private Module module;
	
	private String className;
	
	public AdvicePoint() {
	}
	
	public AdvicePoint(String point, Class<?> clazz) {
		this.point = point;
		try {
			this.classInstance = clazz.newInstance();
		}
		catch (Exception e) {
			log.error("Unable to get instance of: " + clazz.getName(), e);
		}
	}
	
	public AdvicePoint(Module mod, String point, String className) {
		this.point = point;
		this.module = mod;
		this.className = className;
	}
	
	public String getPoint() {
		return point;
	}
	
	/**
	 * @return the classInstance
	 */
	public Object getClassInstance() {
		if (classInstance != null) {
			return classInstance;
		}
		
		Object o = null;
		try {
			Class<?> c = ModuleFactory.getModuleClassLoader(getModule()).loadClass(getClassName());
			o = c.newInstance();
		}
		catch (Exception e) {
			log.warn("Could not get instance for advice point: " + point, e);
		}
		classInstance = o;
		return o;
	}
	
	/**
	 * @return the className
	 */
	public String getClassName() {
		return className;
	}
	
	/**
	 * @return the module
	 */
	public Module getModule() {
		return module;
	}
	
	public void disposeClassInstance() {
		classInstance = null;
	}
}
