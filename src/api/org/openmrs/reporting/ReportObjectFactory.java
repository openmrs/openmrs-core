package org.openmrs.reporting;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openmrs.api.context.Context;

public class ReportObjectFactory {
	private String defaultValidator;
	private List<ReportObjectFactoryModule> modules;
	
	public Set<String> getReportObjectTypes() {
		if ( modules != null ) {
			Set<String> uniqueTypes = new HashSet<String>();
			for ( int i = 0; i < modules.size(); i++ ) {
				ReportObjectFactoryModule mod = modules.get(i);
				boolean isAdded = uniqueTypes.add(mod.getType());
			}

			return uniqueTypes;
		} else {
			return new HashSet<String>();
		}
	}
	
	public Set<String> getReportObjectSubTypes(String type) {
		if ( modules != null && type != null ) {
			Set<String> uniqueTypes = new HashSet<String>();
			for ( int i = 0; i < modules.size(); i++ ) {
				ReportObjectFactoryModule mod = modules.get(i);
				if ( type.equals(mod.getType()) ) {
					boolean isAdded = uniqueTypes.add(mod.getDisplayName());
				}
			}

			return uniqueTypes;
		} else {
			return new HashSet<String>();
		}
	}
	
	public boolean isSubTypeOfType(String type, String subType) {
		boolean retVal = false;
		
		
		Set availableTypes = getReportObjectTypes();
		if ( availableTypes.contains(type) )  {
			Set availableSubTypes = getReportObjectSubTypes(type);
			if ( availableSubTypes.contains(subType) ) {
				retVal = true;
			}
		}
		return retVal;
	}

	public String getReportObjectClassBySubType(String subType) {
		if ( modules != null && subType != null ) {
			String className = "";
			for ( int i = 0; i < modules.size(); i++ ) {
				ReportObjectFactoryModule mod = modules.get(i);
				if ( subType.equals(mod.getDisplayName()) ) {
					className = mod.getClassName();
				}
			}

			return className;
		} else {
			return "";
		}
	}

	public String getReportObjectClassByName(String name) {
		if ( modules != null && name != null ) {
			String className = "";
			for ( int i = 0; i < modules.size(); i++ ) {
				ReportObjectFactoryModule mod = modules.get(i);
				if ( name.equals(mod.getName()) ) {
					className = mod.getClassName();
				}
			}

			return className;
		} else {
			return "";
		}
	}

	public Set<String> getAllReportObjectClasses() {
		if ( modules != null ) {
			Set<String> uniqueClasses = new HashSet<String>();
			for ( int i = 0; i < modules.size(); i++ ) {
				ReportObjectFactoryModule mod = modules.get(i);
				boolean isAdded = uniqueClasses.add(mod.getClassName());
			}

			return uniqueClasses;
		} else {
			return new HashSet<String>();
		}
	}

	public String getReportObjectValidatorByClass(String currentClassName) {
		if ( modules != null && currentClassName != null ) {
			String validator = "";
			for ( int i = 0; i < modules.size(); i++ ) {
				ReportObjectFactoryModule mod = modules.get(i);
				if ( currentClassName.equals(mod.getClassName()) ) {
					validator = mod.getValidatorClass();
				}
			}

			return validator;
		} else {
			return "";
		}
	}
	
	public static AbstractReportObject getInstance(String reportObjectName, Map<String, Object> initialValues, Context context) {
		ReportObjectFactory rof = context.getReportService().getReportObjectFactory();
		String className = rof.getReportObjectClassByName(reportObjectName);
		AbstractReportObject reportObj = null;
		
		if ( className != null ) {
			try {
				Class cls = Class.forName(className);
				reportObj = ReportObjectFactory.getInstance(cls, initialValues);
				// attempt to populate setters with initialValues Map

			} catch ( Throwable t ) {
				//System.out.println("Could not create class: " + className + " when trying to get report object from the factory");
			}
		}
		
		return reportObj;
	}
	
	public static AbstractReportObject getInstance(Class reportObjectClass, Map<String, Object> initialValues) {
		AbstractReportObject reportObj = null;
		
		if ( reportObjectClass != null ) {
			try {
				Constructor ct = reportObjectClass.getConstructor();
				reportObj = (AbstractReportObject)ct.newInstance();
				reportObj = ReportObjectFactory.initInstance(reportObj, initialValues);
			} catch ( Throwable t ) {
				//System.out.println("Could not instantiate class: " + reportObjectClass.getName() + " when trying to get report object from the factory");
			}
		}
		
		return reportObj;
	}

	private static AbstractReportObject initInstance(AbstractReportObject reportObj, Map<String, Object> initialValues) {
		if ( reportObj != null && initialValues != null ) {
			for ( Iterator<String> i = initialValues.keySet().iterator(); i.hasNext(); ) {
				String key = i.next();
				Object val = initialValues.get(key);
				Class valClass = val.getClass();
				String methodName = "set" + key.substring(0,1).toUpperCase() + key.substring(1);
				Class[] setterParamClasses = new Class[1];
				setterParamClasses[0] = valClass;
				Object[] setterParams = new Object[1];
				setterParams[0] = val;
				Method m = null;
				try {
					m = reportObj.getClass().getMethod(methodName, setterParamClasses);
				} catch ( NoSuchMethodException nsme ) {
					Class[] setterParamSupers = new Class[1];
					setterParamSupers[0] = valClass.getSuperclass();
					try {
						m = reportObj.getClass().getMethod(methodName, setterParamSupers);
					} catch ( Exception e ) {
						m = null;
						//System.out.println("Could not instantiate setter method [" + methodName + "()] for field [" + key + "] in class [" + reportObj.getClass().getName() + "] while initializing report object fields.");
						//System.out.println(e.toString());
					}
				} catch ( Exception e ) {
					m = null;
					//System.out.println("Could not instantiate setter method [" + methodName + "()] for field [" + key + "] in class [" + reportObj.getClass().getName() + "] while initializing report object fields.");
					//System.out.println(e.toString());
				}

				if ( m != null ) {
					try {
						Object fieldObj = m.invoke(reportObj, setterParams);
					} catch ( Exception e ) {
						//System.out.println("Could not invoke setter method [" + methodName + "()] for field [" + key + "] in class [" + reportObj.getClass().getName() + "] while initializing report object fields.");
						//System.out.println(e.toString());
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
