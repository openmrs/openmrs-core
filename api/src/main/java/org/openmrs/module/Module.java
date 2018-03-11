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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.openmrs.GlobalProperty;
import org.openmrs.Privilege;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * Generic module class that openmrs manipulates
 *
 * @version 1.0
 */
public final class Module {
	
	private static final Logger log = LoggerFactory.getLogger(Module.class);
	
	private String name;
	
	private String moduleId;
	
	private String packageName;
	
	private String description;
	
	private String author;
	
	private String version;
	
	private String updateURL; // should be a URL to an update.rdf file
	
	private String updateVersion = null; // version obtained from the remote update.rdf file
	
	private String downloadURL = null; // will only be populated when the remote file is newer than the current module
	
	private ModuleActivator moduleActivator;
	
	private String activatorName;
	
	private String requireOpenmrsVersion;
	
	private String requireDatabaseVersion;
	
	private Map<String, String> requiredModulesMap;
	
	private Map<String, String> awareOfModulesMap;
	
	private Map<String, String> startBeforeModulesMap;
	
	private List<AdvicePoint> advicePoints = new ArrayList<>();
	
	private Map<String, String> extensionNames = new IdentityHashMap<>();
	
	private List<Extension> extensions = new ArrayList<>();
	
	private Map<String, Properties> messages = new HashMap<>();
	
	private List<Privilege> privileges = new ArrayList<>();
	
	private List<GlobalProperty> globalProperties = new ArrayList<>();
	
	private List<String> mappingFiles = new ArrayList<>();
	
	private Set<String> packagesWithMappedClasses = new HashSet<>();
	
	private Document config = null;
	
	private Document sqldiff = null;
	
	private boolean mandatory = Boolean.FALSE;
	
	private List<ModuleConditionalResource> conditionalResources = new ArrayList<>();
	
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
	
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof Module) {
			Module mod = (Module) obj;
			return getModuleId().equals(mod.getModuleId());
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(getModuleId()).toHashCode();
	}
	
	/**
	 * @return the moduleActivator
	 */
	public ModuleActivator getModuleActivator() {
		try {
			if (moduleActivator == null) {
				ModuleClassLoader classLoader = ModuleFactory.getModuleClassLoader(this);
				if (classLoader == null) {
					throw new ModuleException("The classloader is null", getModuleId());
				}
				
				Class<?> c = classLoader.loadClass(getActivatorName());
				Object o = c.newInstance();
				if (ModuleActivator.class.isAssignableFrom(o.getClass())) {
					setModuleActivator((ModuleActivator) o);
				}
			}
			
		}
		catch (ClassNotFoundException | NoClassDefFoundError e) {
			
			throw new ModuleException("Unable to load/find moduleActivator: '" + getActivatorName() + "'", name, e);
		}
		catch (IllegalAccessException e) {
			throw new ModuleException("Unable to load/access moduleActivator: '" + getActivatorName() + "'", name, e);
		}
		catch (InstantiationException e) {
			throw new ModuleException("Unable to load/instantiate moduleActivator: '" + getActivatorName() + "'", name, e);
		}

		return moduleActivator;
	}
	
	/**
	 * @param moduleActivator the moduleActivator to set
	 */
	public void setModuleActivator(ModuleActivator moduleActivator) {
		this.moduleActivator = moduleActivator;
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
		return requiredModulesMap == null ? null : new ArrayList<>(requiredModulesMap.keySet());
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
		if (requiredModulesMap == null) {
			requiredModulesMap = new HashMap<>();
		}
		
		for (String module : requiredModules) {
			requiredModulesMap.put(module, null);
		}
	}
	
	/**
	 * @param requiredModule the requiredModule to add for this module
	 * @param version version requiredModule
	 * @should add module to required modules map
	 */
	public void addRequiredModule(String requiredModule, String version) {
		if (requiredModulesMap != null) {
			requiredModulesMap.put(requiredModule, version);
		}
	}
	
	/**
	 * @param requiredModulesMap <code>Map&lt;String,String&gt;</code> of the <code>requiredModule</code>s
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
	public Map<String, String> getRequiredModulesMap() {
		return requiredModulesMap;
	}
	
	/**
	 * Sets modules that must start after this module
	 * @param startBeforeModulesMap the startedBefore modules to set
	 */
	public void setStartBeforeModulesMap(Map<String, String> startBeforeModulesMap) {
		this.startBeforeModulesMap = startBeforeModulesMap;
	}
	
	/**
	 * Gets modules which should start after this
	 * @return map where key is module name and value is module version
	 */
	public Map<String, String> getStartBeforeModulesMap() {
		return this.startBeforeModulesMap;
	}
	
	/**
	 * Gets names of modules which should start after this
	 * @since 1.11
	 * @return list of module names or null
	 */
	public List<String> getStartBeforeModules() {
		return this.startBeforeModulesMap == null ? null : new ArrayList<>(this.startBeforeModulesMap.keySet());
	}
	
	/**
	 * Sets the modules that this module is aware of.
	 *
	 * @param awareOfModulesMap <code>Map&lt;String,String&gt;</code> of the
	 *            <code>awareOfModulesMap</code>s to set
	 * @since 1.9
	 */
	public void setAwareOfModulesMap(Map<String, String> awareOfModulesMap) {
		this.awareOfModulesMap = awareOfModulesMap;
	}
	
	/**
	 * This list of strings is just what is included in the config.xml file, the full package names:
	 * e.g. org.openmrs.module.formentry, for the modules that this module is aware of.
	 *
	 * @since 1.9
	 * @return the list of awareOfModules
	 */
	public List<String> getAwareOfModules() {
		return awareOfModulesMap == null ? null : new ArrayList<>(awareOfModulesMap.keySet());
	}
	
	public String getAwareOfModuleVersion(String awareOfModule) {
		return awareOfModulesMap == null ? null : awareOfModulesMap.get(awareOfModule);
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
	 * @return the module id, with all . replaced with /
	 */
	public String getModuleIdAsPath() {
		return moduleId == null ? null : moduleId.replace('.', '/');
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
	 * Expands (i.e. creates instances of) {@code Extension}s defined by their class name in {@link #setExtensionNames(Map)}.
	 * 
	 * @return the extensions
	 *
	 * @should not expand extensionNames if extensionNames is null
	 * @should not expand extensionNames if extensionNames is empty
	 * @should not expand extensionNames if extensions matches extensionNames
	 * @should expand extensionNames if extensions does not match extensionNames 
	 */
	public List<Extension> getExtensions() {
		if (isNoNeedToExpand()) {
			return extensions;
		}
		return expandExtensionNames();
	}
	
	/**
	 * @param extensions the extensions to set
	 */
	public void setExtensions(List<Extension> extensions) {
		this.extensions = extensions;
	}
	
	/**
	 * A map of pointId to classname. The classname is expected to be a class that extends the
	 * {@link Extension} object.
	 * <br>
	 * This map will be expanded into full Extension objects the first time {@link #getExtensions()}
	 * is called.
	 * <p>
	 * The map is a direct representation of {@code extension} tags in a module's config.xml. For example
	 * <pre>{@code
	 * <extension>
	 *     <point>org.openmrs.admin.list</point>
	 *     <class>org.openmrs.module.reporting.web.extension.ManageAdminListExt</class>
	 * </extension>
	 * }
	 * </pre>
	 * </p>
	 *
	 * @param map from pointid to classname of an extension
	 * @see ModuleFileParser
	 */
	public void setExtensionNames(Map<String, String> map) {
		if (log.isDebugEnabled()) {
			for (Map.Entry<String, String> entry : extensionNames.entrySet()) {
				log.debug("Setting extension names: " + entry.getKey() + " : " + entry.getValue());
			}
		}
		this.extensionNames = map;
	}

	private boolean isNoNeedToExpand() {
		if (extensionNames == null || extensionNames.isEmpty()) {
			return true;
		}
		
		for (Extension ext : extensions) {
			if (extensionNames.get(ext.getPointId()) != ext.getClass().getName()) {
				return false;
			}
		}
		return extensions.size() == extensionNames.size();
	}
	
	/**
	 * Expand the temporary extensionNames map of pointid-classname to full pointid-classobject. <br>
	 * This has to be done after the fact because when the pointid-classnames are parsed, the
	 * module's objects aren't fully realized yet and so not all classes can be loaded. <br>
	 * <br>
	 *
	 * @return a list of full Extension objects
	 */
	private List<Extension> expandExtensionNames() {
		ModuleClassLoader moduleClsLoader = ModuleFactory.getModuleClassLoader(this);
		if (moduleClsLoader == null) {
			log.debug("Module class loader is not available, maybe the module {} is stopped/stopping", getName());
			return extensions;
		}
		
		extensions.clear();
		for (Map.Entry<String, String> entry : extensionNames.entrySet()) {
			String point = entry.getKey();
			String className = entry.getValue();
			log.debug(getModuleId() + ": Expanding extension name (point|class): {}|{}", point, className);
			try {
				Class<?> cls = moduleClsLoader.loadClass(className);
				Extension ext = (Extension) cls.newInstance();
				ext.setPointId(point);
				ext.setModuleId(this.getModuleId());
				extensions.add(ext);
				log.debug(getModuleId() + ": Added extension: {}|{}", ext.getExtensionId(), ext.getClass());
			}
			catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoClassDefFoundError e) {
				log.warn(getModuleId() + ": Unable to create instance of class defined for extension point: " + point, e);
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
	 * @deprecated as of 2.0 because messages are automatically loaded from the classpath
	 */
	@Deprecated
	public Map<String, Properties> getMessages() {
		return messages;
	}
	
	/**
	 * Sets the map from locale to properties used by this module.
	 *
	 * @param messages map of locale to properties for that locale
	 * @deprecated as of 2.0 because messages are automatically loaded from the classpath
	 */
	@Deprecated
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
	
	/**
	 * Packages to scan for classes with JPA annotated classes.
	 * @return the set of packages to scan
	 * @since 1.9.2, 1.10
	 */
	public Set<String> getPackagesWithMappedClasses() {
		return packagesWithMappedClasses;
	}
	
	/**
	 * @param packagesToScan
	 * @see #getPackagesWithMappedClasses()
	 * @since 1.9.2, 1.10
	 */
	public void setPackagesWithMappedClasses(Set<String> packagesToScan) {
		this.packagesWithMappedClasses = packagesToScan;
	}
	
	/**
	 * This property is set by the module owner to tell OpenMRS that once it is installed, it must
	 * always startup. This is intended for modules with system-critical monitoring or security
	 * checks that should always be in place.
	 *
	 * @return true if this module has said that it should always start up
	 */
	public boolean isMandatory() {
		return mandatory;
	}
	
	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}
	
	/**
	 * This is a convenience method to know whether this module is core to OpenMRS. A module is
	 * 'core' when this module is essentially part of the core code and must exist at all times
	 *
	 * @return true if this is an OpenMRS core module
	 * @see ModuleConstants#CORE_MODULES
	 */
	public boolean isCoreModule() {
		return !ModuleUtil.ignoreCoreModules() && ModuleConstants.CORE_MODULES.containsKey(moduleId);
	}
	
	public boolean isStarted() {
		return ModuleFactory.isModuleStarted(this);
	}
	
	/**
	 * @param e string to set as startup error message
	 * @should throw exception when message is null
	 */
	public void setStartupErrorMessage(String e) {
		if (e == null) {
			throw new ModuleException("Startup error message cannot be null", this.getModuleId());
		}
		
		this.startupErrorMessage = e;
	}
	
	/**
	 * Add the given exceptionMessage and throwable as the startup error for this module. This
	 * method loops over the stacktrace and adds the detailed message
	 *
	 * @param exceptionMessage optional. the default message to show on the first line of the error
	 *            message
	 * @param t throwable stacktrace to include in the error message
	 *
	 * @should throw exception when throwable is null
	 * @should set StartupErrorMessage when exceptionMessage is null
	 * @should append throwable's message to exceptionMessage
	 */
	public void setStartupErrorMessage(String exceptionMessage, Throwable t) {
		if (t == null) {
			throw new ModuleException("Startup error value cannot be null", this.getModuleId());
		}
		
		StringBuilder sb = new StringBuilder();
		
		// if exceptionMessage is not null, append it
		if (exceptionMessage != null) {
			sb.append(exceptionMessage);
			sb.append("\n");
		}
		
		sb.append(t.getMessage());
		sb.append("\n");
		
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
	
	@Override
	public String toString() {
		if (moduleId == null) {
			return super.toString();
		}
		
		return moduleId;
	}

	/*
	 * @should dispose all classInstances, not AdvicePoints
	 */	
	public void disposeAdvicePointsClassInstance() {
		if (advicePoints == null) {
			return;
		}
		
		for (AdvicePoint advicePoint : advicePoints) {
			advicePoint.disposeClassInstance();
		}
	}
	
	public List<ModuleConditionalResource> getConditionalResources() {
		return conditionalResources;
	}
	
	public void setConditionalResources(List<ModuleConditionalResource> conditionalResources) {
		this.conditionalResources = conditionalResources;
	}
	
	public boolean isCore() {
		return ModuleConstants.CORE_MODULES.containsKey(getModuleId());
	}
}
