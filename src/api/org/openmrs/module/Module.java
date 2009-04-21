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
package org.openmrs.module;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.GlobalProperty;
import org.openmrs.Privilege;
import org.w3c.dom.Document;

/**
 * Generic module class that openmrs manipulates
 * 
 * @version 1.0
 */
public final class Module {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	private String name;
	
	private String moduleId;
	
	private String packageName;
	
	private String description;
	
	private String author;
	
	private String version;
	
	private String updateURL; // should be a URL to an update.rdf file
	
	private String updateVersion = null; // version obtained from the remote update.rdf file
	
	private String downloadURL = null; // will only be populated when the remote file is newer than the current module
	
	private Activator activator;
	
	private String activatorName;
	
	private String requireOpenmrsVersion;
	
	private String requireDatabaseVersion;
	
	private Map<String, String> requiredModulesMap;
	
	private List<AdvicePoint> advicePoints = new Vector<AdvicePoint>();
	
	private IdentityHashMap<String, String> extensionNames = new IdentityHashMap<String, String>();
	
	private List<Extension> extensions = new Vector<Extension>();
	
	private Map<String, Properties> messages = new HashMap<String, Properties>();
	
	private List<Privilege> privileges = new Vector<Privilege>();
	
	private List<GlobalProperty> globalProperties = new Vector<GlobalProperty>();
	
	private List<String> mappingFiles = new Vector<String>();
	
	private Document config = null;
	
	private Document sqldiff = null;
	
	private Document log4j = null;
	
	// keep a reference to the file that we got this module from so we can delete
	// it if necessary
	private File file = null;
	
	private String startupErrorMessage = null;
	
	/**
	 * Simple constructor
	 * 
	 * @param name
	 */
	public Module(String name) {
		this.name = name;
	}
	
	/**
	 * Main constructor
	 * 
	 * @param name
	 * @param moduleId
	 * @param packageName
	 * @param author
	 * @param description
	 * @param version
	 */
	public Module(String name, String moduleId, String packageName, String author, String description, String version) {
		this.name = name;
		this.moduleId = moduleId;
		this.packageName = packageName;
		this.author = author;
		this.description = description;
		this.version = version;
		log.debug("Creating module " + name);
	}
	
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof Module) {
			Module mod = (Module) obj;
			return getModuleId().equals(mod.getModuleId());
		}
		return false;
	}
	
	/**
	 * @return the activator
	 */
	public Activator getActivator() {
		try {
			ModuleClassLoader classLoader = ModuleFactory.getModuleClassLoader(this);
			if (classLoader == null)
				throw new ModuleException("The classloader is null", getModuleId());
			
			Class<?> c = classLoader.loadClass(getActivatorName());
			setActivator((Activator) c.newInstance());
		}
		catch (ClassNotFoundException e) {
			throw new ModuleException("Unable to load/find activator: '" + getActivatorName() + "'", name, e);
		}
		catch (IllegalAccessException e) {
			throw new ModuleException("Unable to load/access activator: '" + getActivatorName() + "'", name, e);
		}
		catch (InstantiationException e) {
			throw new ModuleException("Unable to load/instantiate activator: '" + getActivatorName() + "'", name, e);
		}
		
		return activator;
	}
	
	/**
	 * @param activator the activator to set
	 */
	public void setActivator(Activator activator) {
		this.activator = activator;
	}
	
	/**
	 * @return the activatorName
	 */
	public String getActivatorName() {
		return activatorName;
	}
	
	/**
	 * @param activatorName the activatorName to set
	 */
	public void setActivatorName(String activatorName) {
		this.activatorName = activatorName;
	}
	
	/**
	 * @return the author
	 */
	public String getAuthor() {
		return author;
	}
	
	/**
	 * @param author the author to set
	 */
	public void setAuthor(String author) {
		this.author = author;
	}
	
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return the requireDatabaseVersion
	 */
	public String getRequireDatabaseVersion() {
		return requireDatabaseVersion;
	}
	
	/**
	 * @param requireDatabaseVersion the requireDatabaseVersion to set
	 */
	public void setRequireDatabaseVersion(String requireDatabaseVersion) {
		this.requireDatabaseVersion = requireDatabaseVersion;
	}
	
	/**
	 * This list of strings is just what is included in the config.xml file, the full package names:
	 * e.g. org.openmrs.module.formentry
	 * 
	 * @return the list of requiredModules
	 */
	public List<String> getRequiredModules() {
		return requiredModulesMap == null ? null : new ArrayList<String>(requiredModulesMap.keySet());
	}
	
	/**
	 * Convenience method to get the version of this given module that is required
	 * 
	 * @return the version of the given required module, or null if there are no version constraints
	 * @since 1.5
	 * @should return null if no required modules exist
	 * @should return null if no required module by given name exists
	 */
	public String getRequiredModuleVersion(String moduleName) {
		return requiredModulesMap == null ? null : requiredModulesMap.get(moduleName);
	}
	
	/**
	 * This is a convenience method to set all the required modules without any version requirements
	 * 
	 * @param requiredModules the requiredModules to set for this module
	 * @should set modules when there is a null required modules map
	 */
	public void setRequiredModules(List<String> requiredModules) {
		if (requiredModulesMap == null)
			requiredModulesMap = new HashMap<String, String>();
		
		for (String module : requiredModules) {
			requiredModulesMap.put(module, null);
		}
	}
	
	/**
	 * @param requiredModulesMap <code>Map<String,String></code> of the <code>requiredModule</code>s
	 *            to set
	 * @since 1.5
	 */
	public void setRequiredModulesMap(Map<String, String> requiredModulesMap) {
		this.requiredModulesMap = requiredModulesMap;
	}
	
	/**
	 * Get the modules that are required for this module. The keys in this map are the module
	 * package names. The values in the map are the required version. If no specific version is
	 * required, it will be null.
	 * 
	 * @return a map from required module to the version that is required
	 */
	public Map<String, String> setRequiredModulesMap() {
		return requiredModulesMap;
	}
	
	/**
	 * @return the requireOpenmrsVersion
	 */
	public String getRequireOpenmrsVersion() {
		return requireOpenmrsVersion;
	}
	
	/**
	 * @param requireOpenmrsVersion the requireOpenmrsVersion to set
	 */
	public void setRequireOpenmrsVersion(String requireOpenmrsVersion) {
		this.requireOpenmrsVersion = requireOpenmrsVersion;
	}
	
	/**
	 * @return the module id
	 */
	public String getModuleId() {
		return moduleId;
	}
	
	/**
	 * @param moduleId the module id to set
	 */
	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}
	
	/**
	 * @return the packageName
	 */
	public String getPackageName() {
		return packageName;
	}
	
	/**
	 * @param packageName the packageName to set
	 */
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	
	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}
	
	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}
	
	/**
	 * @return the updateURL
	 */
	public String getUpdateURL() {
		return updateURL;
	}
	
	/**
	 * @param updateURL the updateURL to set
	 */
	public void setUpdateURL(String updateURL) {
		this.updateURL = updateURL;
	}
	
	/**
	 * @return the downloadURL
	 */
	public String getDownloadURL() {
		return downloadURL;
	}
	
	/**
	 * @param downloadURL the downloadURL to set
	 */
	public void setDownloadURL(String downloadURL) {
		this.downloadURL = downloadURL;
	}
	
	/**
	 * @return the updateVersion
	 */
	public String getUpdateVersion() {
		return updateVersion;
	}
	
	/**
	 * @param updateVersion the updateVersion to set
	 */
	public void setUpdateVersion(String updateVersion) {
		this.updateVersion = updateVersion;
	}
	
	/**
	 * @return the extensions
	 */
	public List<Extension> getExtensions() {
		if (extensions.size() == extensionNames.size())
			return extensions;
		
		return expandExtensionNames();
	}
	
	/**
	 * @param extensions the extensions to set
	 */
	public void setExtensions(List<Extension> extensions) {
		this.extensions = extensions;
	}
	
	/**
	 * A map of pointid to classname. The classname is expected to be a class that extends the
	 * {@link Extension} object. <br/>
	 * <br/>
	 * This map will be expanded into full Extension objects the first time {@link #getExtensions()}
	 * is called
	 * 
	 * @param map from pointid to classname
	 * @see ModuleFileParser
	 */
	public void setExtensionNames(IdentityHashMap<String, String> map) {
		if (log.isDebugEnabled())
			for (Map.Entry<String, String> entry : extensionNames.entrySet()) {
				log.debug("Setting extension names: " + entry.getKey() + " : " + entry.getValue());
			}
		this.extensionNames = map;
	}
	
	/**
	 * Expand the temporary extensionNames map of pointid-classname to full pointid-classobject. <br>
	 * This has to be done after the fact because when the pointid-classnames are parsed, the
	 * module's objects aren't fully realized yet and so not all classes can be loaded. <br/>
	 * <br/>
	 * 
	 * @return a list of full Extension objects
	 */
	private List<Extension> expandExtensionNames() {
		if (extensions.size() != extensionNames.size()) {
			for (Map.Entry<String, String> entry : extensionNames.entrySet()) {
				String point = entry.getKey();
				String className = entry.getValue();
				log.debug("expanding extension names: " + point + " : " + className);
				try {
					Class<?> cls = ModuleFactory.getModuleClassLoader(this).loadClass(className);
					Extension ext = (Extension) cls.newInstance();
					ext.setPointId(point);
					ext.setModuleId(this.getModuleId());
					extensions.add(ext);
					log.debug("Added extension: " + ext.getExtensionId() + " : " + ext.getClass());
				}
				catch (NoClassDefFoundError e) {
					log.warn("Unable to find class definition for extension: " + point, e);
				}
				catch (ClassNotFoundException e) {
					log.warn("Unable to load class for extension: " + point, e);
				}
				catch (IllegalAccessException e) {
					log.warn("Unable to load class for extension: " + point, e);
				}
				catch (InstantiationException e) {
					log.warn("Unable to load class for extension: " + point, e);
				}
			}
		}
		
		return extensions;
	}
	
	/**
	 * @return the advicePoints
	 */
	public List<AdvicePoint> getAdvicePoints() {
		return advicePoints;
	}
	
	/**
	 * @param advicePoints the advicePoints to set
	 */
	public void setAdvicePoints(List<AdvicePoint> advicePoints) {
		this.advicePoints = advicePoints;
	}
	
	public File getFile() {
		return file;
	}
	
	public void setFile(File file) {
		this.file = file;
	}
	
	/**
	 * Gets a mapping from locale to properties used by this module. The locales are represented as
	 * a string containing language and country codes.
	 * 
	 * @return mapping from locales to properties
	 */
	public Map<String, Properties> getMessages() {
		return messages;
	}
	
	/**
	 * Sets the map from locale to properties used by this module.
	 * 
	 * @param messages map of locale to properties for that locale
	 */
	public void setMessages(Map<String, Properties> messages) {
		this.messages = messages;
	}
	
	public List<GlobalProperty> getGlobalProperties() {
		return globalProperties;
	}
	
	public void setGlobalProperties(List<GlobalProperty> globalProperties) {
		this.globalProperties = globalProperties;
	}
	
	public List<Privilege> getPrivileges() {
		return privileges;
	}
	
	public void setPrivileges(List<Privilege> privileges) {
		this.privileges = privileges;
	}
	
	public Document getConfig() {
		return config;
	}
	
	public void setConfig(Document config) {
		this.config = config;
	}
	
	public Document getLog4j() {
		return log4j;
	}
	
	public void setLog4j(Document log4j) {
		this.log4j = log4j;
	}
	
	public Document getSqldiff() {
		return sqldiff;
	}
	
	public void setSqldiff(Document sqldiff) {
		this.sqldiff = sqldiff;
	}
	
	public List<String> getMappingFiles() {
		return mappingFiles;
	}
	
	public void setMappingFiles(List<String> mappingFiles) {
		this.mappingFiles = mappingFiles;
	}
	
	public boolean isStarted() {
		return ModuleFactory.isModuleStarted(this);
	}
	
	public void setStartupErrorMessage(String e) {
		if (e == null)
			throw new ModuleException("Startup error message cannot be null", this.getModuleId());
		
		this.startupErrorMessage = e;
	}
	
	/**
	 * Add the given exceptionMessage and throwable as the startup error for this module. This
	 * method loops over the stacktrace and adds the detailed message
	 * 
	 * @param exceptionMessage optional. the default message to show on the first line of the error
	 *            message
	 * @param t throwable stacktrace to include in the error message
	 */
	public void setStartupErrorMessage(String exceptionMessage, Throwable t) {
		if (t == null)
			throw new ModuleException("Startup error value cannot be null", this.getModuleId());
		
		StringBuffer sb = new StringBuffer();
		
		// if exceptionMessage is not null, append it
		if (exceptionMessage != null) {
			sb.append(exceptionMessage);
			sb.append("\n");
		}
		
		sb.append(t.getMessage());
		sb.append("\n");
		
		// loop over and append all stacktrace elements marking the "openmrs" ones 
		for (StackTraceElement traceElement : t.getStackTrace()) {
			if (traceElement.getClassName().contains("openmrs"))
				sb.append(" ** ");
			sb.append(traceElement);
			sb.append("\n");
		}
		
		this.startupErrorMessage = sb.toString();
	}
	
	public String getStartupErrorMessage() {
		return startupErrorMessage;
	}
	
	public Boolean hasStartupError() {
		return (this.startupErrorMessage != null);
	}
	
	public void clearStartupError() {
		this.startupErrorMessage = null;
	}
	
	public String toString() {
		if (moduleId == null)
			return super.toString();
		
		return moduleId;
	}
	
}
