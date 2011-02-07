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
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.Vector;
import java.util.WeakHashMap;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.aopalliance.aop.Advice;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.GlobalProperty;
import org.openmrs.Privilege;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.Daemon;
import org.openmrs.module.Extension.MEDIA_TYPE;
import org.openmrs.util.DatabaseUpdateException;
import org.openmrs.util.DatabaseUpdater;
import org.openmrs.util.InputRequiredException;
import org.openmrs.util.OpenmrsClassLoader;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.aop.Advisor;
import org.springframework.util.StringUtils;

/**
 * Methods for loading, starting, stopping, and storing OpenMRS modules
 */
public class ModuleFactory {
	
	private static Log log = LogFactory.getLog(ModuleFactory.class);
	
	protected static Map<String, Module> loadedModules = new WeakHashMap<String, Module>();
	
	protected static Map<String, Module> startedModules = new WeakHashMap<String, Module>();
	
	protected static Map<String, List<Extension>> extensionMap = new HashMap<String, List<Extension>>();
	
	// maps to keep track of the memory and objects to free/close
	protected static Map<Module, ModuleClassLoader> moduleClassLoaders = new WeakHashMap<Module, ModuleClassLoader>();
	
	// the name of the file within a module file
	private static final String MODULE_CHANGELOG_FILENAME = "liquibase.xml";
	
	/**
	 * Add a module (in the form of a jar file) to the list of openmrs modules Returns null if an
	 * error occurred and/or module was not successfully loaded
	 * 
	 * @param moduleFile
	 * @return Module
	 */
	public static Module loadModule(File moduleFile) throws ModuleException {
		
		return loadModule(moduleFile, true);
		
	}
	
	/**
	 * Add a module (in the form of a jar file) to the list of openmrs modules Returns null if an
	 * error occurred and/or module was not successfully loaded
	 * 
	 * @param moduleFile
	 * @param replaceIfExists unload a module that has the same moduleId if one is loaded already
	 * @return Module
	 */
	public static Module loadModule(File moduleFile, Boolean replaceIfExists) throws ModuleException {
		Module module = getModuleFromFile(moduleFile);
		
		if (module != null)
			loadModule(module, replaceIfExists);
		
		return module;
	}
	
	/**
	 * Add a module to the list of openmrs modules
	 * 
	 * @param module
	 * @param replaceIfExists unload a module that has the same moduleId if one is loaded already
	 * @return module the module that was loaded or if the module exists already with the same
	 *         version, the old module
	 */
	public static Module loadModule(Module module, Boolean replaceIfExists) throws ModuleException {
		
		if (log.isDebugEnabled())
			log.debug("Adding module " + module.getName() + " to the module queue");
		
		Module oldModule = getLoadedModulesMap().get(module.getModuleId());
		if (oldModule != null) {
			int versionComparison = ModuleUtil.compareVersion(oldModule.getVersion(), module.getVersion());
			if (versionComparison < 0) {
				// if oldModule version is lower, unload it and use the new
				unloadModule(oldModule);
			} else if (versionComparison == 0) {
				if (replaceIfExists) {
					// if the versions are the same and we're told to replaceIfExists, use the new
					unloadModule(oldModule);
				} else
					// if the versions are equal and we're not told to replaceIfExists, jump out of here in a bad way
					throw new ModuleException("A module with the same id and version already exists", module.getModuleId());
			} else {
				// if the older (already loaded) module is newer, keep that original one that was loaded. return that one.
				return oldModule;
			}
		}
		
		getLoadedModulesMap().put(module.getModuleId(), module);
		
		return module;
	}
	
	/**
	 * Load OpenMRS modules from <code>OpenmrsUtil.getModuleRepository()</code>
	 */
	public static void loadModules() {
		
		// load modules from the user's module repository directory
		File modulesFolder = ModuleUtil.getModuleRepository();
		
		if (log.isDebugEnabled())
			log.debug("Loading modules from: " + modulesFolder.getAbsolutePath());
		
		if (modulesFolder.isDirectory()) {
			loadModules(Arrays.asList(modulesFolder.listFiles()));
		} else
			log.error("modules folder: '" + modulesFolder.getAbsolutePath() + "' is not a valid directory");
	}
	
	/**
	 * Attempt to load the given files as OpenMRS modules
	 * 
	 * @param modulesToLoad the list of files to try and load
	 */
	public static void loadModules(List<File> modulesToLoad) {
		// loop over the modules and load all the modules that we can
		for (File f : modulesToLoad) {
			// ignore .svn folder and the like
			if (!f.getName().startsWith(".")) {
				try {
					Module mod = loadModule(f, true); // last module loaded wins
					log.debug("Loaded module: " + mod + " successfully");
				}
				catch (Throwable t) {
					log.debug("Unable to load file in module directory: " + f + ". Skipping file.", t);
				}
			}
		}
	}
	
	/**
	 * Try to start all of the loaded modules that have the global property <i>moduleId</i>.started
	 * is set to "true" or the property does not exist. Otherwise, leave it as only "loaded"<br/>
	 * <br/>
	 * Modules that are already started will be skipped.
	 */
	public static void startModules() {
		// loop over and try starting each of the loaded modules
		if (getLoadedModules().size() > 0) {
			List<Module> leftoverModules = new Vector<Module>();
			
			try {
				Context.addProxyPrivilege("");
				AdministrationService as = Context.getAdministrationService();
				// try and start the modules that should be started
				for (Module mod : getLoadedModulesCoreFirst()) {
					if (mod.isStarted())
						continue; // skip over modules that are already started
						
					String key = mod.getModuleId() + ".started";
					String startedProp = as.getGlobalProperty(key, null);
					String mandatoryProp = as.getGlobalProperty(mod.getModuleId() + ".mandatory", null);
					// if this is a core module and we're not ignoring core modules, this module should always start
					boolean isCoreToOpenmrs = ModuleConstants.CORE_MODULES.containsKey(mod.getModuleId())
					        && !ModuleUtil.ignoreCoreModules();
					
					// if a 'moduleid.started' property doesn't exist, start the module anyway
					// as this is probably the first time they are loading it
					if (startedProp == null || startedProp.equals("true") || "true".equalsIgnoreCase(mandatoryProp)
					        || mod.isMandatory() || isCoreToOpenmrs) {
						if (requiredModulesStarted(mod))
							try {
								if (log.isDebugEnabled())
									log.debug("starting module: " + mod.getModuleId());
								
								startModule(mod);
							}
							catch (Exception e) {
								log.error("Error while starting module: " + mod.getName(), e);
								mod.setStartupErrorMessage("Error while starting module", e);
								notifySuperUsersAboutModuleFailure(mod);
							}
						else {
							// if not all the modules required by this mod are loaded, save it for later
							leftoverModules.add(mod);
							if (log.isDebugEnabled())
								log.debug("cannot start because required modules are not started: " + mod.getModuleId());
						}
					}
				}
			}
			finally {
				Context.removeProxyPrivilege("");
			}
			
			// loop over the leftover modules until we can't load
			// anymore or we've loaded them all
			boolean atLeastOneModuleLoaded = true;
			while (leftoverModules.size() > 0 && atLeastOneModuleLoaded) {
				if (log.isDebugEnabled())
					log.debug("Trying to start leftover modules: " + leftoverModules);
				
				atLeastOneModuleLoaded = false;
				List<Module> modulesStartedInThisLoop = new Vector<Module>();
				
				for (Module leftoverModule : leftoverModules) {
					if (requiredModulesStarted(leftoverModule)) {
						if (log.isDebugEnabled())
							log.debug("starting leftover module: " + leftoverModule.getModuleId());
						
						try {
							// don't need to check globalproperty here because
							// it would only be on the leftover modules list if
							// it were set to true already
							startModule(leftoverModule);
							
							// set this boolean flag to true so we keep looping over the modules
							atLeastOneModuleLoaded = true;
							
							// save the module we just started
							modulesStartedInThisLoop.add(leftoverModule);
						}
						catch (Exception e) {
							log.error("Error while starting leftover module: " + leftoverModule.getName(), e);
						}
					} else {
						if (log.isDebugEnabled())
							log.debug("cannot start leftover module because required modules are not started: "
							        + leftoverModule.getModuleId());
					}
				}
				
				// remove the modules we started in this loop from the overall
				// leftover modules list
				leftoverModules.removeAll(modulesStartedInThisLoop);
			}
			
			// if we failed to start all the modules, error out
			if (leftoverModules.size() > 0)
				for (Module leftoverModule : leftoverModules) {
					String message = "Unable to start module '" + leftoverModule.getName()
					        + "'.  All required modules are not available: "
					        + OpenmrsUtil.join(getMissingRequiredModules(leftoverModule), ", ");
					log.error(message);
					leftoverModule.setStartupErrorMessage(message);
					notifySuperUsersAboutModuleFailure(leftoverModule);
				}
		}
		
	}
	
	/**
	 * Send an Alert to all super users that the given module did not start successfully.
	 * 
	 * @param mod The Module that failed
	 */
	private static void notifySuperUsersAboutModuleFailure(Module mod) {
		try {
			// Add the privileges necessary for notifySuperUsers
			Context.addProxyPrivilege(PrivilegeConstants.MANAGE_ALERTS);
			Context.addProxyPrivilege(PrivilegeConstants.VIEW_USERS);
			
			// Send an alert to all administrators
			Context.getAlertService().notifySuperUsers("Module.startupError.notification.message", null, mod.getName());
		}
		finally {
			// Remove added privileges
			Context.removeProxyPrivilege(PrivilegeConstants.VIEW_USERS);
			Context.removeProxyPrivilege(PrivilegeConstants.MANAGE_ALERTS);
		}
	}
	
	/**
	 * Returns all modules found/loaded into the system (started and not started), with the core
	 * modules at the start of that list
	 * 
	 * @return <code>List<Module></code> of the modules loaded into the system, with the core
	 *         modules first.
	 */
	public static List<Module> getLoadedModulesCoreFirst() {
		List<Module> list = new ArrayList<Module>(getLoadedModules());
		final Collection<String> coreModuleIds = ModuleConstants.CORE_MODULES.keySet();
		Collections.sort(list, new Comparator<Module>() {
			
			public int compare(Module left, Module right) {
				Integer leftVal = coreModuleIds.contains(left.getModuleId()) ? 0 : 1;
				Integer rightVal = coreModuleIds.contains(right.getModuleId()) ? 0 : 1;
				return leftVal.compareTo(rightVal);
			}
		});
		return list;
	}
	
	/**
	 * Convenience method to return a List of Strings containing a description of which modules the
	 * passed module requires but which are not started. The returned description of each module is
	 * the moduleId followed by the required version if one is specified
	 * 
	 * @param module the module to check required modules for
	 * @return List<String> of module names + optional required versions:
	 *         "org.openmrs.formentry 1.8, org.rg.patientmatching"
	 */
	private static List<String> getMissingRequiredModules(Module module) {
		List<String> ret = new ArrayList<String>();
		for (String moduleName : module.getRequiredModules()) {
			String moduleVersion = module.getRequiredModuleVersion(moduleName);
			ret.add(moduleName + (moduleVersion != null ? " " + moduleVersion : ""));
		}
		return ret;
	}
	
	/**
	 * Returns all modules found/loaded into the system (started and not started)
	 * 
	 * @return <code>Collection<Module></code> of the modules loaded into the system
	 */
	public static Collection<Module> getLoadedModules() {
		if (getLoadedModulesMap().size() > 0)
			return getLoadedModulesMap().values();
		
		return Collections.emptyList();
	}
	
	/**
	 * Returns all modules found/loaded into the system (started and not started) in the form of a
	 * map<ModuleId, Module>
	 * 
	 * @return map<ModuleId, Module>
	 */
	public static Map<String, Module> getLoadedModulesMap() {
		if (loadedModules == null)
			loadedModules = new WeakHashMap<String, Module>();
		
		return loadedModules;
	}
	
	/**
	 * Returns the modules that have been successfully started
	 * 
	 * @return <code>Collection<Module></code> of the started modules
	 */
	public static Collection<Module> getStartedModules() {
		if (getStartedModulesMap().size() > 0)
			return getStartedModulesMap().values();
		
		return Collections.emptyList();
	}
	
	/**
	 * Returns the modules that have been successfully started in the form of a map&lt;ModuleId,
	 * Module&gt;
	 * 
	 * @return Map&lt;ModuleId, Module&gt;
	 */
	public static Map<String, Module> getStartedModulesMap() {
		if (startedModules == null)
			startedModules = new WeakHashMap<String, Module>();
		
		return startedModules;
	}
	
	/**
	 * Creates a Module object from the (jar)file pointed to by <code>moduleFile</code> returns null
	 * if an error occurred during processing
	 * 
	 * @param moduleFile
	 * @return module Module
	 */
	private static Module getModuleFromFile(File moduleFile) throws ModuleException {
		
		Module module = null;
		try {
			module = new ModuleFileParser(moduleFile).parse();
		}
		catch (ModuleException e) {
			log.error("Error getting module object from file", e);
			throw e;
		}
		
		return module;
	}
	
	/**
	 * @param moduleId
	 * @return Module matching module id or null if none
	 */
	public static Module getModuleById(String moduleId) {
		return getLoadedModulesMap().get(moduleId);
	}
	
	/**
	 * @param moduleId
	 * @return Module matching moduleId, if it is started or null otherwise
	 */
	public static Module getStartedModuleById(String moduleId) {
		return getStartedModulesMap().get(moduleId);
	}
	
	/**
	 * @param modulePackage
	 * @return Module matching module package or null if none
	 */
	public static Module getModuleByPackage(String modulePackage) {
		for (Module mod : getLoadedModulesMap().values()) {
			if (mod.getPackageName().equals(modulePackage))
				return mod;
		}
		return null;
	}
	
	/**
	 * Runs through extensionPoints and then calls {@link BaseModuleActivator#willStart()} on the
	 * Module's activator. This method is run in a new thread and is authenticated as the Daemon
	 * user
	 * 
	 * @param module Module to start
	 * @throws ModuleException if the module throws any kind of error at startup or in an activator
	 * @see #startModuleInternal(Module)
	 * @see Daemon#startModule(Module)
	 */
	public static Module startModule(Module module) throws ModuleException {
		return Daemon.startModule(module);
	}
	
	/**
	 * This method should not be called directly.<br/>
	 * <br/>
	 * The {@link #startModule(Module)} (and hence {@link Daemon#startModule(Module)}) calls this
	 * method in a new Thread and is authenticated as the {@link Daemon} user<br/>
	 * <br/>
	 * Runs through extensionPoints and then calls {@link BaseModuleActivator#willStart()} on the
	 * Module's activator.
	 * 
	 * @param module Module to start
	 */
	public static Module startModuleInternal(Module module) throws ModuleException {
		
		if (module != null) {
			
			String moduleId = module.getModuleId();
			try {
				
				// check to be sure this module can run with our current version
				// of OpenMRS code
				String requireVersion = module.getRequireOpenmrsVersion();
				ModuleUtil.checkRequiredVersion(OpenmrsConstants.OPENMRS_VERSION_SHORT, requireVersion);
				
				// check for required modules
				if (!requiredModulesStarted(module)) {
					throw new ModuleException("Not all required modules are started: "
					        + OpenmrsUtil.join(getMissingRequiredModules(module), ", ") + ". ", module.getName());
				}
				
				// fire up the classloader for this module
				ModuleClassLoader moduleClassLoader = new ModuleClassLoader(module, ModuleFactory.class.getClassLoader());
				getModuleClassLoaderMap().put(module, moduleClassLoader);
				
				// don't load the advice objects into the Context
				// At startup, the spring context isn't refreshed until all modules
				// have been loaded.  This causes errors if called here during a
				// module's startup if one of these advice points is on another
				// module because that other module's service won't have been loaded
				// into spring yet.  All advice for all modules must be reloaded
				// a spring context refresh anyway, so skip the advice loading here
				// loadAdvice(module);
				
				// add all of this module's extensions to the extension map
				for (Extension ext : module.getExtensions()) {
					
					String extId = ext.getExtensionId();
					List<Extension> tmpExtensions = getExtensions(extId);
					if (tmpExtensions == null)
						tmpExtensions = new Vector<Extension>();
					
					log.debug("Adding to mapping ext: " + ext.getExtensionId() + " ext.class: " + ext.getClass());
					
					tmpExtensions.add(ext);
					getExtensionMap().put(extId, tmpExtensions);
				}
				
				// run the module's sql update script
				// This and the property updates are the only things that can't
				// be undone at startup, so put these calls after any other
				// calls that might hinder startup
				SortedMap<String, String> diffs = SqlDiffFileParser.getSqlDiffs(module);
				
				try {
					// this method must check and run queries against the database.
					// to do this, it must be "authenticated".  Give the current
					// "user" the proxy privilege so this can be done. ("user" might
					// be nobody because this is being run at startup)
					Context.addProxyPrivilege("");
					
					for (String version : diffs.keySet()) {
						String sql = diffs.get(version);
						if (StringUtils.hasText(sql))
							runDiff(module, version, sql);
					}
				}
				finally {
					// take the "authenticated" privilege away from the current "user"
					Context.removeProxyPrivilege("");
				}
				
				// run module's optional liquibase.xml immediately after sqldiff.xml
				runLiquibase(module);
				
				// effectively mark this module as started successfully
				getStartedModulesMap().put(moduleId, module);
				
				try {
					// save the state of this module for future restarts
					saveGlobalProperty(moduleId + ".started", "true", getGlobalPropertyStartedDescription(moduleId));
					
					// save the mandatory status
					saveGlobalProperty(moduleId + ".mandatory", String.valueOf(module.isMandatory()),
					    getGlobalPropertyMandatoryModuleDescription(moduleId));
				}
				catch (Exception e) {
					// pass over errors because this doesn't really concern startup
					// passing over this also allows for multiple of the same-named modules
					// to be loaded in junit tests that are run within one session
					log.debug("Got an error when trying to set the global property on module startup", e);
				}
				
				// (this must be done after putting the module in the started
				// list)
				// if this module defined any privileges or global properties,
				// make sure they are added to the database
				// (Unfortunately, placing the call here will duplicate work
				// done at initial app startup)
				if (module.getPrivileges().size() > 0 || module.getGlobalProperties().size() > 0) {
					log.debug("Updating core dataset");
					Context.checkCoreDataset();
					// checkCoreDataset() currently doesn't throw an error. If
					// it did, it needs to be
					// caught and the module needs to be stopped and given a
					// startup error
				}
				
				// should be near the bottom so the module has all of its stuff
				// set up for it already.
				try {
					if (module.getModuleActivator() != null)// if extends BaseModuleActivator
						module.getModuleActivator().willStart();
					else
						module.getActivator().startup();//implements old Activator interface
				}
				catch (ModuleException e) {
					// just rethrow module exceptions. This should be used for a
					// module marking that it had trouble starting
					throw e;
				}
				catch (Exception e) {
					throw new ModuleException("Error while calling module's Activator.startup()/willStart() method", e);
				}
				
				// erase any previous startup error
				module.clearStartupError();
			}
			catch (Exception e) {
				log.warn("Error while trying to start module: " + moduleId, e);
				module.setStartupErrorMessage("Error while trying to start module", e);
				notifySuperUsersAboutModuleFailure(module);
				
				// undo all of the actions in startup
				try {
					boolean skipOverStartedProperty = false;
					
					if (e instanceof ModuleMustStartException)
						skipOverStartedProperty = true;
					
					stopModule(module, skipOverStartedProperty, true);
				}
				catch (Exception e2) {
					// this will probably occur about the same place as the
					// error in startup
					log.debug("Error while stopping module: " + moduleId, e2);
				}
			}
			
		}
		
		// refresh spring service context?
		
		return module;
	}
	
	/**
	 * Loop over the given module's advice objects and load them into the Context This needs to be
	 * called for all started modules after every restart of the Spring Application Context
	 * 
	 * @param module
	 */
	@SuppressWarnings("unchecked")
	public static void loadAdvice(Module module) {
		for (AdvicePoint advice : module.getAdvicePoints()) {
			Class cls = null;
			try {
				cls = Context.loadClass(advice.getPoint());
				Object aopObject = advice.getClassInstance();
				if (Advisor.class.isInstance(aopObject)) {
					log.debug("adding advisor: " + aopObject.getClass());
					Context.addAdvisor(cls, (Advisor) aopObject);
				} else {
					log.debug("Adding advice: " + aopObject.getClass());
					Context.addAdvice(cls, (Advice) aopObject);
				}
			}
			catch (ClassNotFoundException e) {
				log.warn("Could not load advice point: " + advice.getPoint(), e);
				//throw new ModuleException("Could not load advice point: " + advice.getPoint(), e);
			}
		}
	}
	
	/**
	 * Execute the given sql diff section for the given module
	 * 
	 * @param module the module being executed on
	 * @param version the version of this sql diff
	 * @param sql the actual sql statements to run (separated by semi colons)
	 */
	private static void runDiff(Module module, String version, String sql) {
		AdministrationService as = Context.getAdministrationService();
		
		String key = module.getModuleId() + ".database_version";
		GlobalProperty gp = as.getGlobalPropertyObject(key);
		
		boolean executeSQL = false;
		
		// check given version against current version
		if (gp != null && StringUtils.hasLength(gp.getPropertyValue())) {
			String currentDbVersion = gp.getPropertyValue();
			if (log.isDebugEnabled()) {
				log.debug("version:column " + version + ":" + currentDbVersion);
				log.debug("compare: " + ModuleUtil.compareVersion(version, currentDbVersion));
			}
			if (ModuleUtil.compareVersion(version, currentDbVersion) > 0)
				executeSQL = true;
		} else {
			executeSQL = true;
		}
		
		// version is greater than the currently installed version. execute this update.
		if (executeSQL) {
			try {
				Context.addProxyPrivilege(PrivilegeConstants.SQL_LEVEL_ACCESS);
				log.debug("Executing sql: " + sql);
				String[] sqlStatements = sql.split(";");
				for (String sqlStatement : sqlStatements) {
					if (sqlStatement.trim().length() > 0)
						as.executeSQL(sqlStatement, false);
				}
			}
			finally {
				Context.removeProxyPrivilege(PrivilegeConstants.SQL_LEVEL_ACCESS);
			}
			
			// save the global property
			try {
				Context.addProxyPrivilege(PrivilegeConstants.MANAGE_GLOBAL_PROPERTIES);
				
				String description = "DO NOT MODIFY.  Current database version number for the " + module.getModuleId()
				        + " module.";
				
				if (gp == null) {
					log.info("Global property " + key + " was not found. Creating one now.");
					gp = new GlobalProperty(key, version, description);
					as.saveGlobalProperty(gp);
				} else if (gp.getPropertyValue().equals(version) == false) {
					log.info("Updating global property " + key + " to version: " + version);
					gp.setDescription(description);
					gp.setPropertyValue(version);
					as.saveGlobalProperty(gp);
				} else {
					log.error("Should not be here. GP property value and sqldiff version should not be equal");
				}
				
			}
			finally {
				Context.removeProxyPrivilege(PrivilegeConstants.MANAGE_GLOBAL_PROPERTIES);
			}
			
		}
		
	}
	
	/**
	 * Execute all unrun changeSets in liquibase.xml for the given module
	 * 
	 * @param module the module being executed on
	 */
	private static void runLiquibase(Module module) {
		JarFile jarFile = null;
		boolean liquibaseFileExists = false;
		
		try {
			try {
				jarFile = new JarFile(module.getFile());
			}
			catch (IOException e) {
				throw new ModuleException("Unable to get jar file", module.getName(), e);
			}
			ZipEntry liquiEntry = jarFile.getEntry(MODULE_CHANGELOG_FILENAME);
			
			//check whether module has a moduleid-liquibase.xml
			liquibaseFileExists = liquiEntry != null;
		}
		finally {
			try {
				if (jarFile != null)
					jarFile.close();
			}
			catch (IOException e) {
				log.warn("Unable to close jarfile: " + jarFile.getName());
			}
		}
		
		if (liquibaseFileExists) {
			try {
				// run liquibase.xml by Liquibase API
				DatabaseUpdater.executeChangelog(MODULE_CHANGELOG_FILENAME, null, null, null, getModuleClassLoader(module));
			}
			catch (InputRequiredException ire) {
				// the user would be stepped through the questions returned here.
				throw new ModuleException("Input during database updates is not yet implemented.", module.getName(), ire);
			}
			catch (DatabaseUpdateException e) {
				throw new ModuleException("Unable to update data model using liquibase.xml.", module.getName(), e);
			}
			catch (Exception e) {
				throw new ModuleException("Unable to update data model using liquibase.xml.", module.getName(), e);
			}
		}
	}
	
	/**
	 * Runs through the advice and extension points and removes from api. <br/>
	 * Also calls mod.Activator.shutdown()
	 * 
	 * @param mod module to stop
	 * @see ModuleFactory#stopModule(Module, boolean, boolean)
	 */
	public static void stopModule(Module mod) {
		stopModule(mod, false, false);
	}
	
	/**
	 * Runs through the advice and extension points and removes from api.<br/>
	 * Also calls mod.Activator.shutdown()
	 * 
	 * @param mod the module to stop
	 * @param isShuttingDown true if this is called during the process of shutting down openmrs
	 * @see #stopModule(Module, boolean, boolean)
	 */
	public static void stopModule(Module mod, boolean isShuttingDown) {
		stopModule(mod, isShuttingDown, false);
	}
	
	/**
	 * Runs through the advice and extension points and removes from api.<br/>
	 * <code>skipOverStartedProperty</code> should only be true when openmrs is stopping modules
	 * because it is shutting down. When normally stopping a module, use {@link #stopModule(Module)}
	 * (or leave value as false). This property controls whether the globalproperty is set for
	 * startup/shutdown. <br/>
	 * Also calls module's {@link Activator#shutdown()}
	 * 
	 * @param mod module to stop
	 * @param skipOverStartedProperty true if we don't want to set &lt;moduleid&gt;.started to false
	 * @param isFailedStartup true if this is being called as a cleanup because of a failed module
	 *            startup
	 * @return list of dependent modules that were stopped because this module was stopped. This
	 *         will never be null.
	 */
	@SuppressWarnings("unchecked")
	public static List<Module> stopModule(Module mod, boolean skipOverStartedProperty, boolean isFailedStartup)
	                                                                                                           throws ModuleMustStartException {
		
		List<Module> dependentModulesStopped = new Vector<Module>();
		
		if (mod != null) {
			try {
				if (mod.getModuleActivator() != null)// if extends BaseModuleActivator
					mod.getModuleActivator().willStop();
			}
			catch (Throwable t) {
				log.warn("Unable to call module's Activator.willStop() method", t);
			}
			
			String moduleId = mod.getModuleId();
			
			// don't allow mandatory modules to be stopped
			// don't use database checks here because spring might be in a bad state
			if (!isFailedStartup && mod.isMandatory()) {
				throw new MandatoryModuleException(moduleId);
			}
			
			if (!isFailedStartup && ModuleConstants.CORE_MODULES.containsKey(moduleId)) {
				throw new OpenmrsCoreModuleException(moduleId);
			}
			
			String modulePackage = mod.getPackageName();
			
			// stop all dependent modules
			// copy modules to new list to avoid "concurrent modification exception"
			List<Module> startedModulesCopy = new ArrayList<Module>();
			startedModulesCopy.addAll(getStartedModules());
			for (Module dependentModule : startedModulesCopy) {
				if (!dependentModule.equals(mod) && dependentModule.getRequiredModules().contains(modulePackage)) {
					dependentModulesStopped.add(dependentModule);
					dependentModulesStopped.addAll(stopModule(dependentModule, skipOverStartedProperty, isFailedStartup));
				}
			}
			
			getStartedModulesMap().remove(moduleId);
			
			if (skipOverStartedProperty == false && !Context.isRefreshingContext()) {
				saveGlobalProperty(moduleId + ".started", "false", getGlobalPropertyStartedDescription(moduleId));
			}
			
			if (getModuleClassLoaderMap().containsKey(mod)) {
				log.debug("Mod was in classloader map.  Removing advice and extensions.");
				// remove all advice by this module
				try {
					for (AdvicePoint advice : mod.getAdvicePoints()) {
						Class cls = null;
						try {
							cls = Context.loadClass(advice.getPoint());
							Object aopObject = advice.getClassInstance();
							if (Advisor.class.isInstance(aopObject)) {
								log.debug("adding advisor: " + aopObject.getClass());
								Context.removeAdvisor(cls, (Advisor) aopObject);
							} else {
								log.debug("Adding advice: " + aopObject.getClass());
								Context.removeAdvice(cls, (Advice) aopObject);
							}
						}
						catch (Throwable t) {
							log.warn("Could not remove advice point: " + advice.getPoint(), t);
						}
					}
				}
				catch (Throwable t) {
					log.warn("Error while getting advicePoints from module: " + moduleId, t);
				}
				
				// remove all extensions by this module
				try {
					for (Extension ext : mod.getExtensions()) {
						String extId = ext.getExtensionId();
						try {
							List<Extension> tmpExtensions = getExtensions(extId);
							if (tmpExtensions == null)
								tmpExtensions = new Vector<Extension>();
							
							tmpExtensions.remove(ext);
							getExtensionMap().put(extId, tmpExtensions);
						}
						catch (Exception exterror) {
							log.warn("Error while getting extension: " + ext, exterror);
						}
					}
				}
				catch (Throwable t) {
					log.warn("Error while getting extensions from module: " + moduleId, t);
				}
			}
			
			try {
				if (mod.getModuleActivator() != null)//extends BaseModuleActivator
					mod.getModuleActivator().stopped();
				else
					mod.getActivator().shutdown();//implements old  Activator interface
			}
			catch (Throwable t) {
				log.warn("Unable to call module's Activator.shutdown() method", t);
			}
			
			ModuleClassLoader cl = removeClassLoader(mod);
			if (cl != null) {
				cl.dispose();
				cl = null;
				// remove files from lib cache
				File folder = OpenmrsClassLoader.getLibCacheFolder();
				File tmpModuleDir = new File(folder, moduleId);
				try {
					OpenmrsUtil.deleteDirectory(tmpModuleDir);
				}
				catch (IOException e) {
					log.warn("Unable to delete libcachefolder for " + moduleId);
				}
			}
		}
		
		return dependentModulesStopped;
	}
	
	private static ModuleClassLoader removeClassLoader(Module mod) {
		getModuleClassLoaderMap(); // create map if it is null
		if (!moduleClassLoaders.containsKey(mod))
			log.warn("Module: " + mod.getModuleId() + " does not exist");
		
		return moduleClassLoaders.remove(mod);
	}
	
	/**
	 * Removes module from module repository
	 * 
	 * @param mod module to unload
	 */
	public static void unloadModule(Module mod) {
		
		// remove this module's advice and extensions
		if (isModuleStarted(mod))
			stopModule(mod, true);
		
		// remove from list of loaded modules
		getLoadedModules().remove(mod);
		
		if (mod != null) {
			// remove the file from the module repository
			File file = mod.getFile();
			
			boolean deleted = file.delete();
			if (!deleted) {
				file.deleteOnExit();
				log.warn("Could not delete " + file.getAbsolutePath());
			}
			
			file = null;
			mod = null;
		}
	}
	
	/**
	 * Return all of the extensions associated with the given <code>pointId</code> Returns empty
	 * extension list if no modules extend this pointId
	 * 
	 * @param pointId
	 * @return List of extensions
	 */
	public static List<Extension> getExtensions(String pointId) {
		List<Extension> extensions = null;
		Map<String, List<Extension>> extensionMap = getExtensionMap();
		
		// get all extensions for this exact pointId
		extensions = extensionMap.get(pointId);
		if (extensions == null)
			extensions = new ArrayList<Extension>();
		
		// if this pointId doesn't contain the separator character, search
		// for this point prepended with each MEDIA TYPE
		if (pointId.contains(Extension.extensionIdSeparator) == false) {
			for (MEDIA_TYPE mediaType : Extension.MEDIA_TYPE.values()) {
				
				// get all extensions for this type and point id
				List<Extension> tmpExtensions = extensionMap.get(Extension.toExtensionId(pointId, mediaType));
				
				// 'extensions' should be a unique list
				if (tmpExtensions != null)
					for (Extension ext : tmpExtensions) {
						if (extensions.contains(ext) == false) {
							extensions.add(ext);
						}
					}
			}
		}
		
		if (extensions != null) {
			log.debug("Getting extensions defined by : " + pointId);
			return extensions;
		} else {
			return new Vector<Extension>();
		}
	}
	
	/**
	 * Return all of the extensions associated with the given <code>pointId</code> Returns
	 * getExtension(pointId) if no modules extend this pointId for given media type
	 * 
	 * @param pointId
	 * @param type Extension.MEDIA_TYPE
	 * @return List of extensions
	 */
	public static List<Extension> getExtensions(String pointId, Extension.MEDIA_TYPE type) {
		String key = Extension.toExtensionId(pointId, type);
		List<Extension> extensions = getExtensionMap().get(key);
		if (extensions != null) {
			log.debug("Getting extensions defined by : " + key);
			return extensions;
		} else {
			return getExtensions(pointId);
		}
	}
	
	/**
	 * Get a list of required Privileges defined by the modules
	 * 
	 * @return <code>List<Privilege></code> of the required privileges
	 */
	public static List<Privilege> getPrivileges() {
		
		List<Privilege> privileges = new Vector<Privilege>();
		
		for (Module mod : getStartedModules()) {
			privileges.addAll(mod.getPrivileges());
		}
		
		log.debug(privileges.size() + " new privileges");
		
		return privileges;
	}
	
	/**
	 * Get a list of required GlobalProperties defined by the modules
	 * 
	 * @return <code>List<GlobalProperty></code> object of the module's global properties
	 */
	public static List<GlobalProperty> getGlobalProperties() {
		
		List<GlobalProperty> globalProperties = new Vector<GlobalProperty>();
		
		for (Module mod : getStartedModules()) {
			globalProperties.addAll(mod.getGlobalProperties());
		}
		
		log.debug(globalProperties.size() + " new global properties");
		
		return globalProperties;
	}
	
	/**
	 * Checks whether the given module is activated
	 * 
	 * @param mod Module to check
	 * @return true if the module is started, false otherwise
	 */
	public static boolean isModuleStarted(Module mod) {
		return getStartedModulesMap().containsValue(mod);
	}
	
	/**
	 * Checks whether the given module, identified by its id, is started.
	 * 
	 * @param moduleId module id. e.g formentry, logic
	 * @since 1.9
	 * @return true if the module is started, false otherwise
	 */
	public static boolean isModuleStarted(String moduleId) {
		return getStartedModulesMap().containsKey(moduleId);
	}
	
	/**
	 * Get a module's classloader
	 * 
	 * @param mod Module to fetch the class loader for
	 * @return ModuleClassLoader pertaining to this module. Returns null if the module is not
	 *         started
	 * @throws ModuleException if the module does not have a registered classloader
	 */
	public static ModuleClassLoader getModuleClassLoader(Module mod) throws ModuleException {
		ModuleClassLoader mcl = getModuleClassLoaderMap().get(mod);
		
		if (mcl == null)
			log.debug("Module classloader not found for module with id: " + mod.getModuleId());
		
		return mcl;
	}
	
	/**
	 * Get a module's classloader via the module id
	 * 
	 * @param moduleId <code>String</code> id of the module
	 * @return ModuleClassLoader pertaining to this module. Returns null if the module is not
	 *         started
	 * @throws ModuleException if this module isn't started or doesn't have a classloader
	 * @see #getModuleClassLoader(Module)
	 */
	public static ModuleClassLoader getModuleClassLoader(String moduleId) throws ModuleException {
		Module mod = getStartedModulesMap().get(moduleId);
		if (mod == null)
			log.debug("Module id not found in list of started modules: " + moduleId);
		
		return getModuleClassLoader(mod);
	}
	
	/**
	 * Returns all module classloaders This method will not return null
	 * 
	 * @return Collection<ModuleClassLoader> all known module classloaders or empty list.
	 */
	public static Collection<ModuleClassLoader> getModuleClassLoaders() {
		Map<Module, ModuleClassLoader> classLoaders = getModuleClassLoaderMap();
		if (classLoaders.size() > 0)
			return classLoaders.values();
		
		return Collections.emptyList();
	}
	
	/**
	 * Return all current classloaders keyed on module object
	 * 
	 * @return Map<Module, ModuleClassLoader>
	 */
	public static Map<Module, ModuleClassLoader> getModuleClassLoaderMap() {
		if (moduleClassLoaders == null)
			moduleClassLoaders = new WeakHashMap<Module, ModuleClassLoader>();
		
		return moduleClassLoaders;
	}
	
	/**
	 * Return the current extension map keyed on extension point id
	 * 
	 * @return Map<String, List<Extension>>
	 */
	public static Map<String, List<Extension>> getExtensionMap() {
		if (extensionMap == null)
			extensionMap = new WeakHashMap<String, List<Extension>>();
		
		return extensionMap;
	}
	
	/**
	 * Tests whether all modules mentioned in module.requiredModules are loaded and started already
	 * (by being in the startedModules list)
	 * 
	 * @param module
	 * @return true/false boolean whether this module's required modules are all started
	 */
	private static boolean requiredModulesStarted(Module module) {
		for (String reqModPackage : module.getRequiredModules()) {
			boolean started = false;
			for (Module mod : getStartedModules()) {
				if (mod.getPackageName().equals(reqModPackage)) {
					String reqVersion = module.getRequiredModuleVersion(reqModPackage);
					if (reqVersion == null || ModuleUtil.compareVersion(mod.getVersion(), reqVersion) >= 0)
						started = true;
					break;
				}
			}
			
			if (!started)
				return false;
		}
		
		return true;
	}
	
	/**
	 * Update the module: 1) Download the new module 2) Unload the old module 3) Load/start the new
	 * module
	 * 
	 * @param mod
	 */
	public static Module updateModule(Module mod) throws ModuleException {
		if (mod.getDownloadURL() == null) {
			return mod;
		}
		
		URL url = null;
		try {
			url = new URL(mod.getDownloadURL());
		}
		catch (MalformedURLException e) {
			throw new ModuleException("Unable to download module update", e);
		}
		
		unloadModule(mod);
		
		// copy content to a temporary file
		InputStream inputStream = ModuleUtil.getURLStream(url);
		log.warn("url pathname: " + url.getPath());
		String filename = url.getPath().substring(url.getPath().lastIndexOf("/"));
		File moduleFile = ModuleUtil.insertModuleFile(inputStream, filename);
		
		try {
			// load, and start the new module
			Module newModule = loadModule(moduleFile);
			startModule(newModule);
			return newModule;
		}
		catch (Exception e) {
			log.warn("Error while unloading old module and loading in new module");
			moduleFile.delete();
			return mod;
		}
		
	}
	
	/**
	 * Returns the description for the [moduleId].started global property
	 * 
	 * @param moduleId
	 * @return description to use for the .started property
	 */
	private static String getGlobalPropertyStartedDescription(String moduleId) {
		String ret = "DO NOT MODIFY. true/false whether or not the " + moduleId;
		ret += " module has been started.  This is used to make sure modules that were running ";
		ret += " prior to a restart are started again";
		
		return ret;
	}
	
	/**
	 * Returns the description for the [moduleId].mandatory global property
	 * 
	 * @param moduleId
	 * @return description to use for .mandatory property
	 */
	private static String getGlobalPropertyMandatoryModuleDescription(String moduleId) {
		String ret = "true/false whether or not the " + moduleId;
		ret += " module MUST start when openmrs starts.  This is used to make sure that mission critical";
		ret += " modules are always running if openmrs is running.";
		
		return ret;
	}
	
	/**
	 * Convenience method to save a global property with the given value. Proxy privileges are added
	 * so that this can occur at startup.
	 * 
	 * @param key the property for this global property
	 * @param value the value for this global property
	 * @param desc the description
	 * @see AdministrationService#saveGlobalProperty(GlobalProperty)
	 */
	private static void saveGlobalProperty(String key, String value, String desc) {
		try {
			AdministrationService as = Context.getAdministrationService();
			GlobalProperty gp = as.getGlobalPropertyObject(key);
			if (gp == null)
				gp = new GlobalProperty(key, value, desc);
			else
				gp.setPropertyValue(value);
			
			as.saveGlobalProperty(gp);
		}
		catch (Throwable t) {
			log.warn("Unable to save the global property", t);
		}
	}
}
