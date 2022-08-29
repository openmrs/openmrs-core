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
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.activation.Activator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.aopalliance.aop.Advice;
import org.openmrs.GlobalProperty;
import org.openmrs.Privilege;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.OpenmrsService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.Daemon;
import org.openmrs.module.Extension.MEDIA_TYPE;
import org.openmrs.util.CycleException;
import org.openmrs.util.DatabaseUpdater;
import org.openmrs.util.Graph;
import org.openmrs.util.InputRequiredException;
import org.openmrs.util.OpenmrsClassLoader;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.util.PrivilegeConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.Advisor;
import org.springframework.context.support.AbstractRefreshableApplicationContext;
import org.springframework.util.StringUtils;

import liquibase.Contexts;

/**
 * Methods for loading, starting, stopping, and storing OpenMRS modules
 */
public class ModuleFactory {
	
	private ModuleFactory() {
	}
	
	private static final Logger log = LoggerFactory.getLogger(ModuleFactory.class);
	
	protected static volatile Map<String, Module> loadedModules = new WeakHashMap<>();
	
	protected static volatile Map<String, Module> startedModules = new WeakHashMap<>();
	
	protected static volatile Map<String, List<Extension>> extensionMap = new HashMap<>();
	
	// maps to keep track of the memory and objects to free/close
	protected static volatile Map<Module, ModuleClassLoader> moduleClassLoaders = new WeakHashMap<>();
	
	private static Map<String, Set<ModuleClassLoader>> providedPackages = new ConcurrentHashMap<>();
	
	// the name of the file within a module file
	private static final String MODULE_CHANGELOG_FILENAME = "liquibase.xml";
	
	private static final Map<String, DaemonToken> daemonTokens = new WeakHashMap<>();
	
	private static volatile Set<String> actualStartupOrder;
	
	/**
	 * Add a module (in the form of a jar file) to the list of openmrs modules Returns null if an error
	 * occurred and/or module was not successfully loaded
	 * 
	 * @param moduleFile
	 * @return Module
	 */
	public static Module loadModule(File moduleFile) throws ModuleException {
		
		return loadModule(moduleFile, true);
		
	}
	
	/**
	 * Add a module (in the form of a jar file) to the list of openmrs modules Returns null if an error
	 * occurred and/or module was not successfully loaded
	 * 
	 * @param moduleFile
	 * @param replaceIfExists unload a module that has the same moduleId if one is loaded already
	 * @return Module
	 */
	public static Module loadModule(File moduleFile, Boolean replaceIfExists) throws ModuleException {
		Module module = new ModuleFileParser(Context.getMessageSourceService()).parse(moduleFile);
		
		if (module != null) {
			loadModule(module, replaceIfExists);
		}
		
		return module;
	}
	
	/**
	 * Add a module to the list of openmrs modules
	 * 
	 * @param module
	 * @param replaceIfExists unload a module that has the same moduleId if one is loaded already
	 *            <strong>Should</strong> load module if it is currently not loaded
	 *            <strong>Should</strong> not load module if already loaded <strong>Should</strong>
	 *            always load module if replacement is wanted <strong>Should</strong> not load an older
	 *            version of the same module <strong>Should</strong> load a newer version of the same
	 *            module
	 * @return module the module that was loaded or if the module exists already with the same version,
	 *         the old module
	 */
	public static Module loadModule(Module module, Boolean replaceIfExists) throws ModuleException {
		
		log.debug("Adding module {} to the module queue", module.getName());
		
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
				} else {
					// if the versions are equal and we're not told to replaceIfExists, jump out of here in a bad way
					throw new ModuleException("A module with the same id and version already exists", module.getModuleId());
				}
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
		
		log.debug("Loading modules from: {}", modulesFolder.getAbsolutePath());
		
		File[] files = modulesFolder.listFiles();
		if (modulesFolder.isDirectory() && files != null) {
			loadModules(Arrays.asList(files));
		} else {
			log.error("modules folder: '" + modulesFolder.getAbsolutePath() + "' is not a directory or IO error occurred");
		}
	}
	
	/**
	 * Attempt to load the given files as OpenMRS modules
	 * 
	 * @param modulesToLoad the list of files to try and load <strong>Should</strong> not crash when
	 *            file is not found or broken <strong>Should</strong> setup requirement mappings for
	 *            every module <strong>Should</strong> not start the loaded modules
	 */
	public static void loadModules(List<File> modulesToLoad) {
		// loop over the modules and load all the modules that we can
		for (File f : modulesToLoad) {
			if (f.exists()) {
				// ignore .svn folder and the like
				if (!f.getName().startsWith(".")) {
					try {
						// last module loaded wins
						Module mod = loadModule(f, true);
						log.debug("Loaded module: " + mod + " successfully");
					}
					catch (Exception e) {
						log.error("Unable to load file in module directory: " + f + ". Skipping file.", e);
					}
				}
			} else {
				log.error("Could not find file in module directory: " + f);
			}
		}
		
		//inform modules, that they can't start before other modules
		
		Map<String, Module> loadedModulesMap = getLoadedModulesMapPackage();
		for (Module m : loadedModulesMap.values()) {
			Map<String, String> startBeforeModules = m.getStartBeforeModulesMap();
			if (startBeforeModules.size() > 0) {
				for (String s : startBeforeModules.keySet()) {
					Module mod = loadedModulesMap.get(s);
					if (mod != null) {
						mod.addRequiredModule(m.getPackageName(), m.getVersion());
					}
				}
			}
		}
	}
	
	/**
	 * Try to start all of the loaded modules that have the global property <i>moduleId</i>.started is
	 * set to "true" or the property does not exist. Otherwise, leave it as only "loaded"<br>
	 * <br>
	 * Modules that are already started will be skipped.
	 */
	public static void startModules() {
		
		// loop over and try starting each of the loaded modules
		if (!getLoadedModules().isEmpty()) {
			
			List<Module> modules = getModulesThatShouldStart();
			
			try {
				modules = getModulesInStartupOrder(modules);
			}
			catch (CycleException ex) {
				String message = getCyclicDependenciesMessage(ex.getMessage());
				log.error(message, ex);
				notifySuperUsersAboutCyclicDependencies(ex);
				modules = (List<Module>) ex.getExtraData();
			}
			
			// try and start the modules that should be started
			for (Module mod : modules) {
				
				if (mod.isStarted()) {
					// skip over modules that are already started
					continue;
				}
				
				// Skip module if required ones are not started
				if (!requiredModulesStarted(mod)) {
					String message = getFailedToStartModuleMessage(mod);
					log.error(message);
					mod.setStartupErrorMessage(message);
					notifySuperUsersAboutModuleFailure(mod);
					continue;
				}
				
				try {
					log.debug("starting module: {}", mod.getModuleId());
					startModule(mod);
				}
				catch (Exception e) {
					log.error("Error while starting module: " + mod.getName(), e);
					mod.setStartupErrorMessage("Error while starting module", e);
					notifySuperUsersAboutModuleFailure(mod);
				}
			}
		}
	}
	
	/**
	 * Obtain the list of modules that should be started
	 * 
	 * @return list of modules
	 */
	private static List<Module> getModulesThatShouldStart() {
		List<Module> modules = new ArrayList<>();
		
		AdministrationService adminService = Context.getAdministrationService();
		
		for (Module mod : getLoadedModulesCoreFirst()) {
			
			String key = mod.getModuleId() + ".started";
			String startedProp = adminService.getGlobalProperty(key, null);
			String mandatoryProp = adminService.getGlobalProperty(mod.getModuleId() + ".mandatory", null);
			
			boolean isCoreToOpenmrs = mod.isCore() && !ModuleUtil.ignoreCoreModules();
			
			// if a 'moduleid.started' property doesn't exist, start the module anyway
			// as this is probably the first time they are loading it
			if (startedProp == null || "true".equals(startedProp) || "true".equalsIgnoreCase(mandatoryProp)
			        || mod.isMandatory() || isCoreToOpenmrs) {
				modules.add(mod);
			}
		}
		return modules;
	}
	
	/**
	 * Sort modules in startup order based on required and aware-of dependencies
	 * 
	 * @param modules list of modules to sort
	 * @return list of modules sorted by dependencies
	 * @throws CycleException
	 */
	public static List<Module> getModulesInStartupOrder(Collection<Module> modules) throws CycleException {
		Graph<Module> graph = new Graph<>();
		
		for (Module mod : modules) {
			
			graph.addNode(mod);
			
			// Required dependencies
			for (String key : mod.getRequiredModules()) {
				Module module = getModuleByPackage(key);
				Module fromNode = graph.getNode(module);
				if (fromNode == null) {
					fromNode = module;
				}
				
				if (fromNode != null) {
					graph.addEdge(graph.new Edge(
					                             fromNode,
					                             mod));
				}
			}
			
			// Aware-of dependencies
			for (String key : mod.getAwareOfModules()) {
				Module module = getModuleByPackage(key);
				Module fromNode = graph.getNode(module);
				if (fromNode == null) {
					fromNode = module;
				}
				
				if (fromNode != null) {
					graph.addEdge(graph.new Edge(
					                             fromNode,
					                             mod));
				}
			}
		}
		
		return graph.topologicalSort();
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
			
			// Send an alert to all administrators
			Context.getAlertService().notifySuperUsers("Module.startupError.notification.message", null, mod.getName());
		}
		catch (Exception e) {
			log.error("Unable to send an alert to the super users", e);
		}
		finally {
			// Remove added privileges
			Context.removeProxyPrivilege(PrivilegeConstants.MANAGE_ALERTS);
		}
	}
	
	/**
	 * Send an Alert to all super users that modules did not start due to cyclic dependencies
	 */
	private static void notifySuperUsersAboutCyclicDependencies(Exception ex) {
		try {
			Context.addProxyPrivilege(PrivilegeConstants.MANAGE_ALERTS);
			Context.getAlertService().notifySuperUsers("Module.error.cyclicDependencies", ex, ex.getMessage());
		}
		catch (Exception e) {
			log.error("Unable to send an alert to the super users", e);
		}
		finally {
			Context.removeProxyPrivilege(PrivilegeConstants.MANAGE_ALERTS);
		}
	}
	
	/**
	 * Returns all modules found/loaded into the system (started and not started), with the core modules
	 * at the start of that list
	 * 
	 * @return <code>List&lt;Module&gt;</code> of the modules loaded into the system, with the core
	 *         modules first.
	 */
	public static List<Module> getLoadedModulesCoreFirst() {
		List<Module> list = new ArrayList<>(getLoadedModules());
		final Collection<String> coreModuleIds = ModuleConstants.CORE_MODULES.keySet();
		list.sort((left, right) -> {
			Integer leftVal = coreModuleIds.contains(left.getModuleId()) ? 0 : 1;
			Integer rightVal = coreModuleIds.contains(right.getModuleId()) ? 0 : 1;
			return leftVal.compareTo(rightVal);
		});
		return list;
	}
	
	/**
	 * Convenience method to return a List of Strings containing a description of which modules the
	 * passed module requires but which are not started. The returned description of each module is the
	 * moduleId followed by the required version if one is specified
	 * 
	 * @param module the module to check required modules for
	 * @return List&lt;String&gt; of module names + optional required versions: "org.openmrs.formentry
	 *         1.8, org.rg.patientmatching"
	 */
	private static List<String> getMissingRequiredModules(Module module) {
		List<String> ret = new ArrayList<>();
		for (String moduleName : module.getRequiredModules()) {
			boolean started = false;
			for (Module mod : getStartedModules()) {
				if (mod.getPackageName().equals(moduleName)) {
					String reqVersion = module.getRequiredModuleVersion(moduleName);
					if (reqVersion == null || ModuleUtil.compareVersion(mod.getVersion(), reqVersion) >= 0) {
						started = true;
					}
					break;
				}
			}
			
			if (!started) {
				String moduleVersion = module.getRequiredModuleVersion(moduleName);
				moduleName = moduleName.replace("org.openmrs.module.", "").replace("org.openmrs.", "");
				ret.add(moduleName + (moduleVersion != null ? " " + moduleVersion : ""));
			}
		}
		return ret;
	}
	
	/**
	 * Returns all modules found/loaded into the system (started and not started)
	 * 
	 * @return <code>Collection&lt;Module&gt;</code> of the modules loaded into the system
	 */
	public static Collection<Module> getLoadedModules() {
		if (getLoadedModulesMap().size() > 0) {
			return getLoadedModulesMap().values();
		}
		
		return Collections.emptyList();
	}
	
	/**
	 * Returns all modules found/loaded into the system (started and not started) in the form of a
	 * map&lt;ModuleId, Module&gt;
	 * 
	 * @return map&lt;ModuleId, Module&gt;
	 */
	public static Map<String, Module> getLoadedModulesMap() {
		if (loadedModules == null) {
			loadedModules = new WeakHashMap<>();
		}
		
		return loadedModules;
	}
	
	/**
	 * Returns all modules found/loaded into the system (started and not started) in the form of a
	 * map&lt;PackageName, Module&gt;
	 * 
	 * @return map&lt;PackageName, Module&gt;
	 */
	public static Map<String, Module> getLoadedModulesMapPackage() {
		if (loadedModules == null) {
			loadedModules = new WeakHashMap<>();
			return loadedModules;
		}
		
		Map<String, Module> map = new WeakHashMap<>();
		for (Module loadedModule : loadedModules.values()) {
			map.put(loadedModule.getPackageName(), loadedModule);
		}
		return map;
	}
	
	/**
	 * Returns the modules that have been successfully started
	 * 
	 * @return <code>Collection&lt;Module&gt;</code> of the started modules
	 */
	public static Collection<Module> getStartedModules() {
		if (getStartedModulesMap().size() > 0) {
			return getStartedModulesMap().values();
		}
		
		return Collections.emptyList();
	}
	
	public static List<Module> getStartedModulesInOrder() {
		List<Module> modules = new ArrayList<>();
		if (actualStartupOrder != null) {
			for (String moduleId : actualStartupOrder) {
				modules.add(getStartedModulesMap().get(moduleId));
			}
		} else {
			modules.addAll(getStartedModules());
		}
		return modules;
	}
	
	/**
	 * Returns the modules that have been successfully started in the form of a map&lt;ModuleId,
	 * Module&gt;
	 * 
	 * @return Map&lt;ModuleId, Module&gt;
	 */
	public static Map<String, Module> getStartedModulesMap() {
		if (startedModules == null) {
			startedModules = new WeakHashMap<>();
		}
		
		return startedModules;
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
			if (mod.getPackageName().equals(modulePackage)) {
				return mod;
			}
		}
		return null;
	}
	
	/**
	 * @see #startModule(Module, boolean, AbstractRefreshableApplicationContext)
	 * @see #startModuleInternal(Module)
	 * @see Daemon#startModule(Module)
	 */
	public static Module startModule(Module module) throws ModuleException {
		return startModule(module, false, null);
	}
	
	/**
	 * Runs through extensionPoints and then calls {@link BaseModuleActivator#willStart()} on the
	 * Module's activator. This method is run in a new thread and is authenticated as the Daemon user.
	 * If a non null application context is passed in, it gets refreshed to make the module's services
	 * available
	 * 
	 * @param module Module to start
	 * @param isOpenmrsStartup Specifies whether this module is being started at application startup or
	 *            not, this argument is ignored if a null application context is passed in
	 * @param applicationContext the spring application context instance to refresh
	 * @throws ModuleException if the module throws any kind of error at startup or in an activator
	 * @see #startModuleInternal(Module, boolean, AbstractRefreshableApplicationContext)
	 * @see Daemon#startModule(Module, boolean, AbstractRefreshableApplicationContext)
	 */
	public static Module startModule(Module module, boolean isOpenmrsStartup,
	        AbstractRefreshableApplicationContext applicationContext) throws ModuleException {
		
		if (!requiredModulesStarted(module)) {
			int missingModules = 0;
			
			for (String packageName : module.getRequiredModulesMap().keySet()) {
				Module mod = getModuleByPackage(packageName);
				
				// mod not installed
				if (mod == null) {
					missingModules++;
					continue;
				}
				
				if (!mod.isStarted()) {
					startModule(mod);
				}
			}
			
			if (missingModules > 0) {
				String message = getFailedToStartModuleMessage(module);
				log.error(message);
				module.setStartupErrorMessage(message);
				notifySuperUsersAboutModuleFailure(module);
				// instead of return null, i realized that Daemon.startModule() always returns a Module
				// object,irrespective of whether the startup succeeded
				return module;
			}
		}
		return Daemon.startModule(module, isOpenmrsStartup, applicationContext);
	}
	
	/**
	 * This method should not be called directly.<br>
	 * <br>
	 * The {@link #startModule(Module)} (and hence {@link Daemon#startModule(Module)}) calls this method
	 * in a new Thread and is authenticated as the {@link Daemon} user<br>
	 * <br>
	 * Runs through extensionPoints and then calls {@link BaseModuleActivator#willStart()} on the
	 * Module's activator.
	 * 
	 * @param module Module to start
	 */
	public static Module startModuleInternal(Module module) throws ModuleException {
		return startModuleInternal(module, false, null);
	}
	
	/**
	 * This method should not be called directly.<br>
	 * <br>
	 * The {@link #startModule(Module)} (and hence {@link Daemon#startModule(Module)}) calls this method
	 * in a new Thread and is authenticated as the {@link Daemon} user<br>
	 * <br>
	 * Runs through extensionPoints and then calls {@link BaseModuleActivator#willStart()} on the
	 * Module's activator. <br>
	 * <br>
	 * If a non null application context is passed in, it gets refreshed to make the module's services
	 * available
	 * 
	 * @param module Module to start
	 * @param isOpenmrsStartup Specifies whether this module is being started at application startup or
	 *            not, this argument is ignored if a null application context is passed in
	 * @param applicationContext the spring application context instance to refresh
	 */
	public static Module startModuleInternal(Module module, boolean isOpenmrsStartup,
	        AbstractRefreshableApplicationContext applicationContext) throws ModuleException {
		
		if (module != null) {
			String moduleId = module.getModuleId();
			
			try {
				
				// check to be sure this module can run with our current version
				// of OpenMRS code
				String requireVersion = module.getRequireOpenmrsVersion();
				ModuleUtil.checkRequiredVersion(OpenmrsConstants.OPENMRS_VERSION_SHORT, requireVersion);
				
				// check for required modules
				if (!requiredModulesStarted(module)) {
					throw new ModuleException(getFailedToStartModuleMessage(module));
				}
				
				// fire up the classloader for this module
				ModuleClassLoader moduleClassLoader = new ModuleClassLoader(module, ModuleFactory.class.getClassLoader());
				getModuleClassLoaderMap().put(module, moduleClassLoader);
				registerProvidedPackages(moduleClassLoader);
				
				// don't load the advice objects into the Context
				// At startup, the spring context isn't refreshed until all modules
				// have been loaded.  This causes errors if called here during a
				// module's startup if one of these advice points is on another
				// module because that other module's service won't have been loaded
				// into spring yet.  All advice for all modules must be reloaded
				// a spring context refresh anyway
				
				// map extension point to a list of extensions for this module only
				Map<String, List<Extension>> moduleExtensionMap = new HashMap<>();
				for (Extension ext : module.getExtensions()) {
					
					String extId = ext.getExtensionId();
					List<Extension> tmpExtensions = moduleExtensionMap.computeIfAbsent(extId, k -> new ArrayList<>());
					
					tmpExtensions.add(ext);
				}
				
				// Sort this module's extensions, and merge them into the full extensions map
				Comparator<Extension> sortOrder = (e1, e2) -> Integer.valueOf(e1.getOrder()).compareTo(e2.getOrder());
				for (Map.Entry<String, List<Extension>> moduleExtensionEntry : moduleExtensionMap.entrySet()) {
					// Sort this module's extensions for current extension point
					List<Extension> sortedModuleExtensions = moduleExtensionEntry.getValue();
					sortedModuleExtensions.sort(sortOrder);
					
					// Get existing extensions, and append the ones from the new module
					List<Extension> extensions = getExtensionMap().computeIfAbsent(moduleExtensionEntry.getKey(),
					    k -> new ArrayList<>());
					for (Extension ext : sortedModuleExtensions) {
						log.debug("Adding to mapping ext: " + ext.getExtensionId() + " ext.class: " + ext.getClass());
						extensions.add(ext);
					}
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
					
					for (Map.Entry<String, String> entry : diffs.entrySet()) {
						String version = entry.getKey();
						String sql = entry.getValue();
						if (StringUtils.hasText(sql)) {
							runDiff(module, version, sql);
						}
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
				if (actualStartupOrder == null) {
					actualStartupOrder = new LinkedHashSet<>();
				}
				actualStartupOrder.add(moduleId);
				
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
				if (!module.getPrivileges().isEmpty() || !module.getGlobalProperties().isEmpty()) {
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
					if (module.getModuleActivator() != null) {
						// if extends BaseModuleActivator
						module.getModuleActivator().willStart();
					}
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
					
					if (e instanceof ModuleMustStartException) {
						skipOverStartedProperty = true;
					}
					
					stopModule(module, skipOverStartedProperty, true);
				}
				catch (Exception e2) {
					// this will probably occur about the same place as the
					// error in startup
					log.debug("Error while stopping module: " + moduleId, e2);
				}
			}
			
		}
		
		if (applicationContext != null) {
			ModuleUtil.refreshApplicationContext(applicationContext, isOpenmrsStartup, module);
		}
		
		return module;
	}
	
	private static void registerProvidedPackages(ModuleClassLoader moduleClassLoader) {
		for (String providedPackage : moduleClassLoader.getProvidedPackages()) {
			Set<ModuleClassLoader> newSet = new HashSet<>();
			
			Set<ModuleClassLoader> set = providedPackages.get(providedPackage);
			if (set != null) {
				newSet.addAll(set);
			}
			
			newSet.add(moduleClassLoader);
			providedPackages.put(providedPackage, newSet);
		}
	}
	
	private static void unregisterProvidedPackages(ModuleClassLoader moduleClassLoader) {
		for (String providedPackage : moduleClassLoader.getProvidedPackages()) {
			Set<ModuleClassLoader> newSet = new HashSet<>();
			
			Set<ModuleClassLoader> set = providedPackages.get(providedPackage);
			if (set != null) {
				newSet.addAll(set);
			}
			newSet.remove(moduleClassLoader);
			
			providedPackages.put(providedPackage, newSet);
		}
	}
	
	public static Set<ModuleClassLoader> getModuleClassLoadersForPackage(String packageName) {
		Set<ModuleClassLoader> set = providedPackages.get(packageName);
		if (set == null) {
			return Collections.emptySet();
		} else {
			return new HashSet<>(set);
		}
	}
	
	/**
	 * Gets the error message of a module which fails to start.
	 * 
	 * @param module the module that has failed to start.
	 * @return the message text.
	 */
	private static String getFailedToStartModuleMessage(Module module) {
		String[] params = { module.getName(), String.join(",", getMissingRequiredModules(module)) };
		return Context.getMessageSourceService().getMessage("Module.error.moduleCannotBeStarted", params,
		    Context.getLocale());
	}
	
	/**
	 * Gets the error message of cyclic dependencies between modules
	 * 
	 * @return the message text.
	 */
	private static String getCyclicDependenciesMessage(String message) {
		return Context.getMessageSourceService().getMessage("Module.error.cyclicDependencies", new Object[] { message },
		    Context.getLocale());
	}
	
	/**
	 * Loop over the given module's advice objects and load them into the Context This needs to be
	 * called for all started modules after every restart of the Spring Application Context
	 * 
	 * @param module
	 */
	public static void loadAdvice(Module module) {
		for (AdvicePoint advice : module.getAdvicePoints()) {
			Class<?> cls;
			try {
				cls = Context.loadClass(advice.getPoint());
				Object aopObject = advice.getClassInstance();
				if (aopObject instanceof Advisor) {
					log.debug("adding advisor [{}]", aopObject.getClass());
					Context.addAdvisor(cls, (Advisor) aopObject);
				} else if (aopObject != null) {
					log.debug("adding advice [{}]", aopObject.getClass());
					Context.addAdvice(cls, (Advice) aopObject);
				} else {
					log.debug("Could not load advice class for {} [{}]", advice.getPoint(), advice.getClassName());
				}
			}
			catch (ClassNotFoundException | NoClassDefFoundError e) {
				log.warn("Could not load advice point [{}]", advice.getPoint(), e);
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
				log.debug("version:column {}:{}", version, currentDbVersion);
				log.debug("compare: {}", ModuleUtil.compareVersion(version, currentDbVersion));
			}
			if (ModuleUtil.compareVersion(version, currentDbVersion) > 0) {
				executeSQL = true;
			}
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
					if (sqlStatement.trim().length() > 0) {
						as.executeSQL(sqlStatement, false);
					}
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
				} else if (!gp.getPropertyValue().equals(version)) {
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
	 * Execute all not run changeSets in liquibase.xml for the given module
	 * 
	 * @param module the module being executed on
	 */
	private static void runLiquibase(Module module) {
		ModuleClassLoader moduleClassLoader = getModuleClassLoader(module);
		boolean liquibaseFileExists = false;
		
		if (moduleClassLoader != null) {
			try (InputStream inStream = moduleClassLoader.getResourceAsStream(MODULE_CHANGELOG_FILENAME)) {
				liquibaseFileExists = (inStream != null);
			}
			catch (IOException ignored) {
				
			}
		}
		
		if (liquibaseFileExists) {
			try {
				// run liquibase.xml by Liquibase API
				DatabaseUpdater.executeChangelog(MODULE_CHANGELOG_FILENAME, new Contexts(), null, moduleClassLoader);
			}
			catch (InputRequiredException e) {
				// the user would be stepped through the questions returned here.
				throw new ModuleException("Input during database updates is not yet implemented.", module.getName(), e);
			}
			catch (Exception e) {
				throw new ModuleException("Unable to update data model using " + MODULE_CHANGELOG_FILENAME + ".",
					module.getName(), e);
			}
		}
	}
	
	/**
	 * Runs through the advice and extension points and removes from api. <br>
	 * Also calls mod.Activator.shutdown()
	 * 
	 * @param mod module to stop
	 * @see ModuleFactory#stopModule(Module, boolean, boolean)
	 */
	public static void stopModule(Module mod) {
		stopModule(mod, false, false);
	}
	
	/**
	 * Runs through the advice and extension points and removes from api.<br>
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
	 * Runs through the advice and extension points and removes from api.<br>
	 * <code>skipOverStartedProperty</code> should only be true when openmrs is stopping modules because
	 * it is shutting down. When normally stopping a module, use {@link #stopModule(Module)} (or leave
	 * value as false). This property controls whether the globalproperty is set for startup/shutdown.
	 * <br>
	 * Also calls module's {@link Activator#shutdown()}
	 * 
	 * @param mod module to stop
	 * @param skipOverStartedProperty true if we don't want to set &lt;moduleid&gt;.started to false
	 * @param isFailedStartup true if this is being called as a cleanup because of a failed module
	 *            startup
	 * @return list of dependent modules that were stopped because this module was stopped. This will
	 *         never be null.
	 */
	public static List<Module> stopModule(Module mod, boolean skipOverStartedProperty, boolean isFailedStartup)
	        throws ModuleMustStartException {
		
		List<Module> dependentModulesStopped = new ArrayList<>();
		
		if (mod != null) {
			
			if (!ModuleFactory.isModuleStarted(mod)) {
				return dependentModulesStopped;
			}
			
			try {
				// if extends BaseModuleActivator
				if (mod.getModuleActivator() != null) {
					mod.getModuleActivator().willStop();
				}
			}
			catch (Exception t) {
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
			List<Module> startedModulesCopy = new ArrayList<>(getStartedModules());
			for (Module dependentModule : startedModulesCopy) {
				if (dependentModule != null && !dependentModule.equals(mod)
				        && isModuleRequiredByAnother(dependentModule, modulePackage)) {
					dependentModulesStopped.add(dependentModule);
					dependentModulesStopped.addAll(stopModule(dependentModule, skipOverStartedProperty, isFailedStartup));
				}
			}
			
			getStartedModulesMap().remove(moduleId);
			if (actualStartupOrder != null) {
				actualStartupOrder.remove(moduleId);
				for (Module depModule : dependentModulesStopped) {
					actualStartupOrder.remove(depModule.getModuleId());
				}
			}
			
			if (!skipOverStartedProperty && !Context.isRefreshingContext()) {
				saveGlobalProperty(moduleId + ".started", "false", getGlobalPropertyStartedDescription(moduleId));
			}
			
			ModuleClassLoader moduleClassLoader = getModuleClassLoaderMap().get(mod);
			if (moduleClassLoader != null) {
				unregisterProvidedPackages(moduleClassLoader);
				
				log.debug("Mod was in classloader map.  Removing advice and extensions.");
				// remove all advice by this module
				try {
					for (AdvicePoint advice : mod.getAdvicePoints()) {
						Class cls;
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
						catch (Exception t) {
							log.warn("Could not remove advice point: " + advice.getPoint(), t);
						}
					}
				}
				catch (Exception t) {
					log.warn("Error while getting advicePoints from module: " + moduleId, t);
				}
				
				// remove all extensions by this module
				try {
					for (Extension ext : mod.getExtensions()) {
						String extId = ext.getExtensionId();
						try {
							List<Extension> tmpExtensions = getExtensions(extId);
							tmpExtensions.remove(ext);
							getExtensionMap().put(extId, tmpExtensions);
						}
						catch (Exception exterror) {
							log.warn("Error while getting extension: " + ext, exterror);
						}
					}
				}
				catch (Exception t) {
					log.warn("Error while getting extensions from module: " + moduleId, t);
				}
			}
			
			//Run the onShutdown() method for openmrs services in this module.
			List<OpenmrsService> services = Context.getModuleOpenmrsServices(modulePackage);
			if (services != null) {
				for (OpenmrsService service : services) {
					service.onShutdown();
				}
			}
			
			try {
				if (mod.getModuleActivator() != null) {// extends BaseModuleActivator
					mod.getModuleActivator().stopped();
				}
			}
			catch (Exception t) {
				log.warn("Unable to call module's Activator.shutdown() method", t);
			}
			
			//Since extensions are loaded by the module class loader which is about to be disposed,
			//we need to clear them, else we shall never be able to unload the class loader until
			//when we unload the module, hence resulting into two problems:
			// 1) Memory leakage for start/stop module.
			// 2) Calls to Context.getService(Service.class) which are made within these extensions 
			//	  will throw APIException("Service not found: ") because their calls to Service.class
			//    will pass in a Class from the old module class loader (which loaded them) yet the
			//    ServiceContext will have new services from a new module class loader.
			//
			//Same thing applies to activator, moduleActivator and AdvicePoint classInstance.
			mod.getExtensions().clear();
			mod.setModuleActivator(null);
			mod.disposeAdvicePointsClassInstance();
			
			ModuleClassLoader cl = removeClassLoader(mod);
			if (cl != null) {
				cl.dispose();
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
	
	/**
	 * Checks if a module is required by another
	 *
	 * @param dependentModule the module whose required modules are to be checked
	 * @param modulePackage the package of the module to check if required by another
	 * @return true if the module is required, else false
	 */
	private static boolean isModuleRequiredByAnother(Module dependentModule, String modulePackage) {
		return dependentModule.getRequiredModules() != null && dependentModule.getRequiredModules().contains(modulePackage);
	}
	
	private static ModuleClassLoader removeClassLoader(Module mod) {
		// create map if it is null
		getModuleClassLoaderMap();
		if (!moduleClassLoaders.containsKey(mod)) {
			log.warn("Module: " + mod.getModuleId() + " does not exist");
		}
		
		return moduleClassLoaders.remove(mod);
	}
	
	/**
	 * Removes module from module repository
	 * 
	 * @param mod module to unload
	 */
	public static void unloadModule(Module mod) {
		
		// remove this module's advice and extensions
		if (isModuleStarted(mod)) {
			stopModule(mod, true);
		}
		
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
		List<Extension> extensions;
		Map<String, List<Extension>> extensionMap = getExtensionMap();
		
		// get all extensions for this exact pointId
		extensions = extensionMap.get(pointId);
		if (extensions == null) {
			extensions = new ArrayList<>();
		}
		
		// if this pointId doesn't contain the separator character, search
		// for this point prepended with each MEDIA TYPE
		if (!pointId.contains(Extension.EXTENSION_ID_SEPARATOR)) {
			for (MEDIA_TYPE mediaType : Extension.MEDIA_TYPE.values()) {
				
				// get all extensions for this type and point id
				List<Extension> tmpExtensions = extensionMap.get(Extension.toExtensionId(pointId, mediaType));
				
				// 'extensions' should be a unique list
				if (tmpExtensions != null) {
					for (Extension ext : tmpExtensions) {
						if (!extensions.contains(ext)) {
							extensions.add(ext);
						}
					}
				}
			}
		}
		
		log.debug("Getting extensions defined by : " + pointId);
		return extensions;
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
	 * @return <code>List&lt;Privilege&gt;</code> of the required privileges
	 */
	public static List<Privilege> getPrivileges() {
		
		List<Privilege> privileges = new ArrayList<>();
		
		for (Module mod : getStartedModules()) {
			privileges.addAll(mod.getPrivileges());
		}
		
		log.debug(privileges.size() + " new privileges");
		
		return privileges;
	}
	
	/**
	 * Get a list of required GlobalProperties defined by the modules
	 * 
	 * @return <code>List&lt;GlobalProperty&gt;</code> object of the module's global properties
	 */
	public static List<GlobalProperty> getGlobalProperties() {
		
		List<GlobalProperty> globalProperties = new ArrayList<>();
		
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
	 * @return ModuleClassLoader pertaining to this module. Returns null if the module is not started
	 * @throws ModuleException if the module does not have a registered classloader
	 */
	public static ModuleClassLoader getModuleClassLoader(Module mod) throws ModuleException {
		ModuleClassLoader mcl = getModuleClassLoaderMap().get(mod);
		
		if (mcl == null) {
			log.debug("Module classloader not found for module with id: " + mod.getModuleId());
		}
		
		return mcl;
	}
	
	/**
	 * Get a module's classloader via the module id
	 * 
	 * @param moduleId <code>String</code> id of the module
	 * @return ModuleClassLoader pertaining to this module. Returns null if the module is not started
	 * @throws ModuleException if this module isn't started or doesn't have a classloader
	 * @see #getModuleClassLoader(Module)
	 */
	public static ModuleClassLoader getModuleClassLoader(String moduleId) throws ModuleException {
		Module mod = getStartedModulesMap().get(moduleId);
		if (mod == null) {
			log.debug("Module id not found in list of started modules: " + moduleId);
		}
		
		return getModuleClassLoader(mod);
	}
	
	/**
	 * Returns all module classloaders This method will not return null
	 * 
	 * @return Collection&lt;ModuleClassLoader&gt; all known module classloaders or empty list.
	 */
	public static Collection<ModuleClassLoader> getModuleClassLoaders() {
		Map<Module, ModuleClassLoader> classLoaders = getModuleClassLoaderMap();
		if (classLoaders.size() > 0) {
			return classLoaders.values();
		}
		
		return Collections.emptyList();
	}
	
	/**
	 * Return all current classloaders keyed on module object
	 * 
	 * @return Map&lt;Module, ModuleClassLoader&gt;
	 */
	public static Map<Module, ModuleClassLoader> getModuleClassLoaderMap() {
		if (moduleClassLoaders == null) {
			moduleClassLoaders = new WeakHashMap<>();
		}
		
		return moduleClassLoaders;
	}
	
	/**
	 * Return the current extension map keyed on extension point id
	 * 
	 * @return Map&lt;String, List&lt;Extension&gt;&gt;
	 */
	public static Map<String, List<Extension>> getExtensionMap() {
		if (extensionMap == null) {
			extensionMap = new WeakHashMap<>();
		}
		
		return extensionMap;
	}
	
	/**
	 * Tests whether all modules mentioned in module.requiredModules are loaded and started already (by
	 * being in the startedModules list)
	 * 
	 * @param module
	 * @return true/false boolean whether this module's required modules are all started
	 */
	private static boolean requiredModulesStarted(Module module) {
		//required
		for (String reqModPackage : module.getRequiredModules()) {
			boolean started = false;
			for (Module mod : getStartedModules()) {
				if (mod.getPackageName().equals(reqModPackage)) {
					String reqVersion = module.getRequiredModuleVersion(reqModPackage);
					if (reqVersion == null || ModuleUtil.compareVersion(mod.getVersion(), reqVersion) >= 0) {
						started = true;
					}
					break;
				}
			}
			
			if (!started) {
				return false;
			}
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
		
		URL url;
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
	 * Validates the given token.
	 * <p>
	 * It is thread safe.
	 * 
	 * @param token
	 * @since 1.9.2
	 */
	public static boolean isTokenValid(DaemonToken token) {
		if (token == null) {
			return false;
		} else {
			//We need to synchronize to guarantee that the last passed token is valid.
			synchronized (daemonTokens) {
				DaemonToken validToken = daemonTokens.get(token.getId());
				//Compare by reference to defend from overridden equals.
				return validToken == token;
			}
		}
	}
	
	/**
	 * Passes a daemon token to the given module.
	 * <p>
	 * The token is passed to that module's {@link ModuleActivator} if it implements
	 * {@link DaemonTokenAware}.
	 * <p>
	 * This method is called automatically before {@link ModuleActivator#contextRefreshed()} or
	 * {@link ModuleActivator#started()}. Note that it may be called multiple times and there is no
	 * guarantee that it will always pass the same token. The last passed token is valid, whereas
	 * previously passed tokens may be invalidated.
	 * <p>
	 * It is thread safe.
	 * 
	 * @param module
	 * @since 1.9.2
	 */
	static void passDaemonToken(Module module) {
		ModuleActivator moduleActivator = module.getModuleActivator();
		if (moduleActivator instanceof DaemonTokenAware) {
			DaemonToken daemonToken = getDaemonToken(module);
			((DaemonTokenAware) module.getModuleActivator()).setDaemonToken(daemonToken);
		}
	}
	
	/**
	 * Gets a new or existing token. Uses weak references for tokens so that they are garbage collected
	 * when not needed.
	 * <p>
	 * It is thread safe.
	 * 
	 * @param module
	 * @return the token
	 */
	private static DaemonToken getDaemonToken(Module module) {
		synchronized (daemonTokens) {
			DaemonToken token = daemonTokens.get(module.getModuleId());
			if (token != null) {
				return token;
			}
			
			token = new DaemonToken(module.getModuleId());
			daemonTokens.put(module.getModuleId(), token);
			
			return token;
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
	 * Convenience method to save a global property with the given value. Proxy privileges are added so
	 * that this can occur at startup.
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
			if (gp == null) {
				gp = new GlobalProperty(key, value, desc);
			} else {
				gp.setPropertyValue(value);
			}
			
			as.saveGlobalProperty(gp);
		}
		catch (Exception e) {
			log.warn("Unable to save the global property", e);
		}
	}
	
	/**
	 * Convenience method used to identify module interdependencies and alert the user before modules
	 * are shut down.
	 * 
	 * @param moduleId the moduleId used to identify the module being validated
	 * @return List&lt;dependentModules&gt; the list of moduleId's which depend on the module about to
	 *         be shutdown.
	 * @since 1.10
	 */
	public static List<String> getDependencies(String moduleId) {
		List<String> dependentModules = null;
		Module module = getModuleById(moduleId);
		
		Map<String, Module> startedModules = getStartedModulesMap();
		String modulePackage = module.getPackageName();
		
		for (Entry<String, Module> entry : startedModules.entrySet()) {
			if (!moduleId.equals(entry.getKey()) && entry.getValue().getRequiredModules().contains(modulePackage)) {
				if (dependentModules == null) {
					dependentModules = new ArrayList<>();
				}
				dependentModules.add(entry.getKey() + " " + entry.getValue().getVersion());
			}
		}
		return dependentModules;
	}
}
