package org.openmrs.module;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AdvicePoint {

	private Log log = LogFactory.getLog(this.getClass());
	
	private String point;
	private Object classInstance;
	private Module module;
	private String className;

	public AdvicePoint() { }
	
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
		if (classInstance != null)
			return classInstance;
		
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

}
