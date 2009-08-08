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
package org.openmrs.reporting;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;

/**
 *
 */
public class ReportObjectFactory {
	
	private static ReportObjectFactory singleton;
	
	private static Log log = LogFactory.getLog(ReportObjectFactory.class);
	
	private String defaultValidator;
	
	private List<ReportObjectFactoryModule> modules;
	
	/**
	 * 
	 */
	public ReportObjectFactory() {
		if (singleton == null)
			singleton = this;
	}
	
	/**
	 * Auto generated method comment
	 * 
	 * @return
	 */
	public static ReportObjectFactory getInstance() {
		if (singleton == null)
			throw new RuntimeException("Not Yet Instantiated");
		else
			return singleton;
	}
	
	/**
	 * Auto generated method comment
	 * 
	 * @return
	 */
	public List<String> getReportObjectTypes() {
		if (modules != null) {
			List<String> uniqueTypes = new Vector<String>();
			for (ReportObjectFactoryModule mod : modules) {
				if (!uniqueTypes.contains(mod.getType()))
					uniqueTypes.add(mod.getType());
			}
			
			return uniqueTypes;
		} else {
			return Collections.emptyList();
		}
	}
	
	/**
	 * Auto generated method comment
	 * 
	 * @param type
	 * @return
	 */
	public List<String> getReportObjectSubTypes(String type) {
		if (modules != null && type != null) {
			List<String> uniqueTypes = new Vector<String>();
			for (ReportObjectFactoryModule mod : modules) {
				if (type.equals(mod.getType()) && !uniqueTypes.contains(mod.getDisplayName())) {
					uniqueTypes.add(mod.getDisplayName());
				}
			}
			
			return uniqueTypes;
		} else {
			return Collections.emptyList();
		}
	}
	
	/**
	 * Auto generated method comment
	 * 
	 * @param type
	 * @param subType
	 * @return
	 */
	public boolean isSubTypeOfType(String type, String subType) {
		boolean retVal = false;
		
		List<String> availableTypes = getReportObjectTypes();
		if (availableTypes.contains(type)) {
			List<String> availableSubTypes = getReportObjectSubTypes(type);
			if (availableSubTypes.contains(subType)) {
				retVal = true;
			}
		}
		return retVal;
	}
	
	/**
	 * Auto generated method comment
	 * 
	 * @param subType
	 * @return
	 */
	public String getReportObjectClassBySubType(String subType) {
		if (modules != null && subType != null) {
			String className = "";
			for (ReportObjectFactoryModule mod : modules) {
				if (subType.equals(mod.getDisplayName())) {
					className = mod.getClassName();
				}
			}
			
			return className;
		} else {
			return "";
		}
	}
	
	/**
	 * Auto generated method comment
	 * 
	 * @param name
	 * @return
	 */
	public String getReportObjectClassByName(String name) {
		if (modules != null && name != null) {
			String className = "";
			for (ReportObjectFactoryModule mod : modules) {
				if (name.equals(mod.getName())) {
					className = mod.getClassName();
				}
			}
			
			return className;
		} else {
			return "";
		}
	}
	
	/**
	 * Auto generated method comment
	 * 
	 * @return
	 */
	public List<String> getAllReportObjectClasses() {
		if (modules != null) {
			List<String> uniqueClasses = new Vector<String>();
			for (ReportObjectFactoryModule mod : modules) {
				if (!uniqueClasses.contains(mod.getClassName()))
					uniqueClasses.add(mod.getClassName());
			}
			
			return uniqueClasses;
		} else {
			return Collections.emptyList();
		}
	}
	
	/**
	 * Auto generated method comment
	 * 
	 * @param currentClassName
	 * @return
	 */
	public String getReportObjectValidatorByClass(String currentClassName) {
		if (modules != null && currentClassName != null) {
			String validator = "";
			for (int i = 0; i < modules.size(); i++) {
				ReportObjectFactoryModule mod = modules.get(i);
				if (currentClassName.equals(mod.getClassName())) {
					validator = mod.getValidatorClass();
				}
			}
			
			return validator;
		} else {
			return "";
		}
	}
	
	/**
	 * Auto generated method comment
	 * 
	 * @param reportObjectName
	 * @param initialValues
	 * @param context
	 * @return
	 */
	public static AbstractReportObject getInstance(String reportObjectName, Map<String, Object> initialValues,
	                                               Context context) {
		ReportObjectFactory rof = ReportObjectFactory.singleton;
		String className = rof.getReportObjectClassByName(reportObjectName);
		AbstractReportObject reportObj = null;
		
		if (className != null) {
			try {
				Class cls = Class.forName(className);
				reportObj = ReportObjectFactory.getInstance(cls, initialValues);
				// attempt to populate setters with initialValues Map
				
			}
			catch (Throwable t) {
				log.error("Could not create class: " + className + " when trying to get report object from the factory");
			}
		}
		
		return reportObj;
	}
	
	/**
	 * Auto generated method comment
	 * 
	 * @param reportObjectClass
	 * @param initialValues
	 * @return
	 */
	public static AbstractReportObject getInstance(Class reportObjectClass, Map<String, Object> initialValues) {
		AbstractReportObject reportObj = null;
		
		if (reportObjectClass != null) {
			try {
				Constructor ct = reportObjectClass.getConstructor();
				reportObj = (AbstractReportObject) ct.newInstance();
				reportObj = ReportObjectFactory.initInstance(reportObj, initialValues);
			}
			catch (Throwable t) {
				log.error("Could not instantiate class: " + reportObjectClass.getName()
				        + " when trying to get report object from the factory");
			}
		}
		
		return reportObj;
	}
	
	/**
	 * Auto generated method comment
	 * 
	 * @param reportObj
	 * @param initialValues
	 * @return
	 */
	private static AbstractReportObject initInstance(AbstractReportObject reportObj, Map<String, Object> initialValues) {
		if (reportObj != null && initialValues != null) {
			for (Iterator<String> i = initialValues.keySet().iterator(); i.hasNext();) {
				String key = i.next();
				Object val = initialValues.get(key);
				Class valClass = val.getClass();
				String methodName = "set" + key.substring(0, 1).toUpperCase() + key.substring(1);
				Class[] setterParamClasses = new Class[1];
				setterParamClasses[0] = valClass;
				Object[] setterParams = new Object[1];
				setterParams[0] = val;
				Method m = null;
				try {
					m = reportObj.getClass().getMethod(methodName, setterParamClasses);
				}
				catch (NoSuchMethodException nsme) {
					Class[] setterParamSupers = new Class[1];
					setterParamSupers[0] = valClass.getSuperclass();
					try {
						m = reportObj.getClass().getMethod(methodName, setterParamSupers);
					}
					catch (Exception e) {
						m = null;
						log.error("Could not instantiate setter method [" + methodName + "()] for field [" + key
						        + "] in class [" + reportObj.getClass().getName()
						        + "] while initializing report object fields.");
					}
				}
				catch (Exception e) {
					m = null;
					log
					        .error("Could not instantiate setter method [" + methodName + "()] for field [" + key
					                + "] in class [" + reportObj.getClass().getName()
					                + "] while initializing report object fields.");
				}
				
				if (m != null) {
					try {
						Object fieldObj = m.invoke(reportObj, setterParams);
					}
					catch (Exception e) {
						log.error("Could not invoke setter method [" + methodName + "()] for field [" + key + "] in class ["
						        + reportObj.getClass().getName() + "] while initializing report object fields.");
					}
				}
			}
		}
		
		return reportObj;
	}
	
	/**
	 * @return Returns the defaultValidator.
	 */
	public String getDefaultValidator() {
		return defaultValidator;
	}
	
	/**
	 * @param defaultValidator The defaultValidator to set.
	 */
	public void setDefaultValidator(String defaultValidator) {
		this.defaultValidator = defaultValidator;
	}
	
	/**
	 * @return Returns the modules.
	 */
	public List<ReportObjectFactoryModule> getModules() {
		return modules;
	}
	
	/**
	 * @param modules The modules to set.
	 */
	public void setModules(List<ReportObjectFactoryModule> modules) {
		this.modules = modules;
	}
}
