package org.openmrs.module;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.Vector;
import java.util.WeakHashMap;

import org.aopalliance.aop.Advice;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.GlobalProperty;
import org.openmrs.Privilege;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsClassLoader;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.aop.Advisor;

/**
 * Methods for starting, stopping, and storing OpenMRS modules
 * 
 * @author Ben Wolfe
 * @version 1.0
 */
public class ModuleFactory {
	
	private static Log log = LogFactory.getLog(ModuleFactory.class);
	
	private static Map<String, Module> loadedModules = new WeakHashMap<String, Module>();
	private static Map<String, Module> startedModules = new WeakHashMap<String, Module>();
	
	private static Map<String, List<Extension>> extensionMap = new HashMap<String, List<Extension>>();
	
	// maps to keep track of the memory and objects to free/close
	private static Map<Module, ModuleClassLoader> moduleClassLoaders = new WeakHashMap<Module, ModuleClassLoader>();
	
	/**
	 * Add a module (in the form of a jar file) to the list of openmrs modules
	 * Returns null if an error occurred and/or module was not successfully
	 * loaded
	 * 
	 * @param moduleFile
	 * @return Module 
	 */
	public static Module loadModule(File moduleFile) throws ModuleException {
		
		return loadModule(moduleFile, true);
		
	}
	
	/**
	 * Add a module (in the form of a jar file) to the list of openmrs modules
	 * Returns null if an error occurred and/or module was not successfully
	 * loaded
	 * 
	 * @param moduleFile
	 * @param Boolean replaceIfExists unload a module that has the same moduleId if one is loaded already
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
	 * @param module
	 * @param Boolean replaceIfExists unload a module that has the same moduleId if one is loaded already
	 */
	public static Module loadModule(Module module, Boolean replaceIfExists) throws ModuleException {
		
		log.debug("Adding module " + module.getName() + " to the module queue");
		
		Module oldModule = getLoadedModulesMap().get(module.getModuleId());
		if (oldModule != null) {
			if (replaceIfExists == true) {
				// TODO need to stop the module in the web layer as well.
				unloadModule(oldModule);
			}
			else
				throw new ModuleException("A module with the same id already exists", module.getModuleId());
		}
		
		getLoadedModulesMap().put(module.getModuleId(), module);
		
		return module;
	}
	
	/**
	 * Load OpenMRS modules from <code>OpenmrsUtil.getModuleRepository()</code>
	 * If the global property <i>moduleId</i>.started is set to "true", try and start 
	 * the module. Otherwise, leave it as only "loaded"
	 */
	public static void loadAndStartModules() {
		
		// load modules from the user's module repository directory
		File modulesFolder = ModuleUtil.getModuleRepository();
		log.debug("Loading modules from: " + modulesFolder.getAbsolutePath());
		
		if (modulesFolder.isDirectory()) {
			// loop over the modules and load the modules that we can
			for (File f : modulesFolder.listFiles()) {
				if (!f.getName().startsWith(".")) { // ignore .svn folder and the like
					Module mod = loadModule(f);
					log.debug("Loaded module: " + mod + " successfully");
				}
			}
		}
		else
			log.error("modules folder: '" + modulesFolder.getAbsolutePath() + "' is not a valid directory");
		
		
		
		// loop over and try starting each of the loaded modules
		if (getLoadedModules().size() > 0) {
			Context.addProxyPrivilege("");
			AdministrationService as = Context.getAdministrationService();
			// try and start the modules that should be started
			List<Module> leftoverModules = new Vector<Module>();
			for (Module mod : getLoadedModules()) {
				String key = mod.getModuleId() + ".started";
				String prop = as.getGlobalProperty(key, "false");
				if (prop.equals("true")) {
					if (requiredModulesStarted(mod))
						try {
							log.debug("starting module: " + mod.getModuleId());
							
							startModule(mod);
						}
						catch (Exception e) {
							log.error("Error while loading module: " + mod.getName(), e);
						}
					else
						leftoverModules.add(mod);
				}
			}
			Context.removeProxyPrivilege("");
			
			// loop over the leftover modules until we can't load 
			// anymore or we've loaded them all
			boolean atLeastOneModuleLoaded = true;
			while (leftoverModules.size() > 0 && atLeastOneModuleLoaded) {
				atLeastOneModuleLoaded = false;
				List<Module> modulesJustLoaded = new Vector<Module>();
				for (Module leftoverModule : leftoverModules) {
					if (requiredModulesStarted(leftoverModule)) {
						try {
							// don't need to check globalproperty here because it would only
							// be on the leftovermodules list if it were set to true
							startModule(leftoverModule);
							atLeastOneModuleLoaded = true;
							modulesJustLoaded.add(leftoverModule);
						}
						catch (Exception e) {
							log.error("Error while loading leftover module: " + leftoverModule.getName(), e);
						}
					}
				}
				leftoverModules.removeAll(modulesJustLoaded);
			}
			
			if (leftoverModules.size() > 0)
				for (Module leftoverModule : leftoverModules)
					log.error("Unable to load module '" + leftoverModule.getName() + "'.  All required modules are not available");
		}
		
	}
	
	/**
	 * Returns all modules found/loaded into the system (started and not started)
	 * 
	 * @return
	 */
	public static Collection<Module> getLoadedModules() {
		if (getLoadedModulesMap().size() > 0)
			return getLoadedModulesMap().values();

		return Collections.emptyList();
	}
	
	/**
	 * Returns all modules found/loaded into the system (started and not started) in the 
	 * form of a map<ModuleId, Module>
	 * 
	 * @return map<ModuleId, Module>
	 */
	public static synchronized Map<String, Module> getLoadedModulesMap() {
		if (loadedModules == null)
			loadedModules = new WeakHashMap<String, Module>();
		
		return loadedModules;
	}
	
	/**
	 * Returns the modules that have been successfully started
	 * 
	 * @return
	 */
	public static Collection<Module> getStartedModules() {
		if (getStartedModulesMap().size() > 0)
			return getStartedModulesMap().values();
		
		return Collections.emptyList();
	}
	
	/**
	 * Returns the modules that have been successfully started in the form of
	 * a map<ModuleId, Module>
	 * 
	 * @return map<ModuleId, Module>
	 */
	public static synchronized Map<String, Module> getStartedModulesMap() {
		if (startedModules == null)
			startedModules = new WeakHashMap<String, Module>();
		
		return startedModules;
	}
	
	/**
	 * Creates a Module object from the (jar)file pointed to by <code>moduleFile</code>
	 * 
	 * returns null if an error occurred during processing
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
	 * 
	 * @param module id
	 * @return Module matching module id or null if none
	 */
	public static Module getModuleById(String moduleId) {
		return getLoadedModulesMap().get(moduleId);
	}
	
	/**
	 * Runs through extensionPoints and then calls 
	 * mod.Activator.startup()
	 * 
	 * @param module Module to start
	 */
	public static Module startModule(Module module) throws ModuleException {
		
		if (module != null) {
			
			try {
			
				// check to be sure this module can run with our current version of OpenMRS code
				String requireVersion = module.getRequireOpenmrsVersion();
				if (requireVersion != null && !requireVersion.equals(""))
					if (ModuleUtil.compareVersion(OpenmrsConstants.OPENMRS_VERSION_SHORT, requireVersion) < 0)
						throw new ModuleException("Module requires at least version '" + requireVersion + "'.  Current code version is only '" + OpenmrsConstants.OPENMRS_VERSION_SHORT + "'", module.getName());
					
				// check to be sure this module can run with our current version of the OpenMRS database
				String requireDBVersion = module.getRequireDatabaseVersion();
				if (requireDBVersion != null && !requireDBVersion.equals(""))
					if (ModuleUtil.compareVersion(OpenmrsConstants.DATABASE_VERSION, requireDBVersion) < 0)
						throw new ModuleException("Module requires at least database version '" + requireDBVersion + "'. Current database version is only '" + OpenmrsConstants.DATABASE_VERSION + "'", module.getName());
				
				// check for required modules
				if (!requiredModulesStarted(module)) {
					throw new ModuleException("Not all required modules are started: (" + OpenmrsUtil.join(module.getRequiredModules(), ", ") + "). ", module.getName());
				}
				
				// fire up the classloader for this module
				ModuleClassLoader moduleClassLoader = new ModuleClassLoader(module, ModuleFactory.class.getClassLoader());
				getModuleClassLoaderMap().put(module, moduleClassLoader);
				
				// load the advice objects into the Context
				loadAdvice(module);
				
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
				// be undone at startup, so put these calls after any other calls
				// that might hinder startup
				SortedMap<String, String> diffs = SqlDiffFileParser.getSqlDiffs(module);
				for (String version : diffs.keySet()) {
					String sql = diffs.get(version);
					runDiff(module, version, sql);
				}
				
				// effectively mark this module as started successfully
				getStartedModulesMap().put(module.getModuleId(), module);
				
				// save the state of this module for future restarts
				Context.addProxyPrivilege("");
				AdministrationService as = Context.getAdministrationService();
				GlobalProperty gp = new GlobalProperty(module.getModuleId() + ".started", "true", getGlobalPropertyStartedDescription(module.getModuleId()));
				as.setGlobalProperty(gp);
				Context.removeProxyPrivilege("");
				
				// (this must be done after putting the module in the started list)
				// if this module defined any privileges or global properties, make 
				// sure they are added to the database
				// (Unfortunately, placing the call here will duplicate work done 
				//    at initial app startup)
				if (module.getPrivileges().size() > 0 || module.getGlobalProperties().size() > 0) {
					log.debug("Updating core dataset");
					Context.checkCoreDataset(); 
					// checkCoreDataset() currently doesn't throw an error.  If it did, it needs to be
					// caught and the module needs to be stopped and given a startup error
				}
				
				// should be near the bottom so the module has all of its stuff
				// set up for it already.
				try {
					module.getActivator().startup();
				}
				catch (ModuleException e) {
					// just rethrow module exceptions.  This should be used for a module
					// marking that it had trouble starting
					throw e;
				}
				catch (Exception e) {
					throw new ModuleException("Error while calling module's Activator.startup() method", e);
				}
				
				// erase any previous startup error
				module.clearStartupError();
				
			}
			catch (Exception e) {
				log.warn("Error while trying to start module: " + module.getModuleId(), e);
				module.setStartupErrorMessage(e.getMessage());
				
				// undo all of the actions in startup
				try {
					stopModule(module);
				}
				catch (Exception e2) {
					// this will probably occur about the same place as the error in startup
					log.debug("Error while stopping module: " + module.getModuleId(), e2);
				}
			}
			
		}
		
		return module;
	}
	
	/**
	 * Loop over the given module's advice objects and load them into the Context
	 * This needs to be called for all started modules after every restart of the
	 * Spring Application Context
	 * 
	 * @param module
	 */
	public static void loadAdvice(Module module) {
		ModuleClassLoader moduleClassLoader = getModuleClassLoader(module);
		
		for (AdvicePoint advice : module.getAdvicePoints()) {
			Class cls = null;
			try {
				cls = moduleClassLoader.loadClass(advice.getPoint());
				Object aopObject = advice.getClassInstance();
				if (Advisor.class.isInstance(aopObject)) {
					log.debug("adding advisor: " + aopObject.getClass());
					Context.addAdvisor(cls, (Advisor)aopObject);
				}
				else {
					log.debug("Adding advice: " + aopObject.getClass());
					Context.addAdvice(cls, (Advice)aopObject);
				}
			}
			catch (ClassNotFoundException e) {
				throw new ModuleException("Could not load advice point: " + advice.getPoint(), e);
			}
		}
	}
	
	/**
	 * Execute the given sql diff for the given module
	 * 
	 * @param module
	 * @param version
	 * @param sql
	 */
	private static void runDiff(Module module, String version, String sql) {
		AdministrationService as = Context.getAdministrationService();
		
		String key = module.getModuleId() + ".database_version";
		String select = "select property_value from global_property where property = '" + key + "'";
		
		List<List<Object>> results = as.executeSQL(select, true);
		
		boolean executeSQL = false;
		
		// check given version against current version
		if (results.size() > 0) {
			for (List<Object> row : results) {
				String column = (String)row.get(0);
				log.debug("version:column " + version + ":" + column);
				log.debug("compare: " + ModuleUtil.compareVersion(version, column));
				if (ModuleUtil.compareVersion(version, column) > 0)
					executeSQL = true;
			}
		}
		else {
			String insert = "insert into global_property (property, property_value) values ('" + key + "', '0')";
			as.executeSQL(insert, false);
			executeSQL = true;
		}
		
		// version is greater than the currently installed version. execute this update. 
		if (executeSQL) {
			log.debug("Executing sql: " + sql);
			String[] sqlStatements = sql.split(";");
			for (String sqlStatement : sqlStatements) {
				if (sqlStatement.trim().length() > 0)
					as.executeSQL(sqlStatement, false);
			}
			String description = "DO NOT MODIFY.  Current database version number for the " + module.getModuleId() + " module.";
			String update = "update global_property set property_value = '" + version + "', description = '" + description + "' where property = '" + key + "'";
			as.executeSQL(update, false);
		}
		
	}
	
	/**
	 * Runs through the advice and extension points and removes from api
	 * Also calls mod.Activator.shutdown()
	 * 
	 * @param mod module to stop
	 */public static void stopModule(Module mod) {
		stopModule(mod, false);
	}
	
	/**
	 * Runs through the advice and extension points and removes from api
	 * <code>isShuttingDown</code> should only be true when openmrs is stopping modules because
	 * it is shutting down.  When normally stopping a module, use stopModule(Module)
	 * (or leave value as false).  This property controls whether the globalproperty is set for 
	 * startup/shutdown.
	 *  
	 * Also calls mod.Activator.shutdown()
	 * 
	 * @param mod module to stop
	 * @param isShuttingDown
	 */
	public static void stopModule(Module mod, boolean isShuttingDown) {
		
		if (mod != null) {
			String moduleId = mod.getModuleId();
			
			getStartedModulesMap().remove(moduleId);
			
			if (isShuttingDown == false) {
				Context.addProxyPrivilege("");
				AdministrationService as = Context.getAdministrationService();
				GlobalProperty gp = new GlobalProperty(moduleId + ".started", "false", getGlobalPropertyStartedDescription(moduleId));
				as.setGlobalProperty(gp);
				Context.removeProxyPrivilege("");
			}
			
			if (getModuleClassLoaderMap().containsKey(mod)) {
				log.debug("Mod was in classloader map.  Removing advice and extensions.");
				// remove all advice by this module
				try {
					for (AdvicePoint advice : mod.getAdvicePoints()) {
						Class cls = null;
						try {
							cls = Class.forName(advice.getPoint());
							Object aopObject = advice.getClassInstance();
							if (Advisor.class.isInstance(aopObject)) {
								log.debug("adding advisor: " + aopObject.getClass());
								Context.removeAdvisor(cls, (Advisor)aopObject);
							}
							else {
								log.debug("Adding advice: " + aopObject.getClass());
								Context.removeAdvice(cls, (Advice)aopObject);
							}
						}
						catch (ClassNotFoundException e) {
							log.warn("Could not remove advice point: " + advice.getPoint(), e);
						}
					}
				}
				catch (Exception e) {
					log.warn("Error while getting advicePoints from module: " + moduleId, e);
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
				catch (Exception e) {
					log.warn("Error while getting extensions from module: " + moduleId, e);
				}
			}
			
			try {
				mod.getActivator().shutdown();
			}
			catch (ModuleException me) {
				// essentially ignore thrown ModuleExceptions.
				log.debug("Exception encountered while calling module's activator.shutdown()", me);
			}
			catch (Exception e) {
				log.warn("Unable to call module's Activator.shutdown() method", e);
			}
			
			ModuleClassLoader cl = removeClassLoader(mod);
			if (cl != null) {
				cl.dispose();
				cl = null;
				// remove files from lib cache
				File folder = OpenmrsClassLoader.getLibCacheFolder();
				File tmpModuleDir = new File(folder, moduleId);
				try {
					System.gc();
					OpenmrsUtil.deleteDirectory(tmpModuleDir);
				} catch (IOException e) {
					log.warn("Unable to delete libcachefolder for " + moduleId);
				}
			}
			System.gc();
		}
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
			// run the garbage collector before deleting in case a stream hasn't
			// been cleaned up yet
			System.gc();
			System.gc();

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
	 * Return all of the extensions associated with the given <code>pointId</code>
	 * 
	 * Returns empty extension list if no modules extend this pointId
	 * 
	 * @param pointId
	 * @return List of extensions
	 */
	public static List<Extension> getExtensions(String pointId) {
		List<Extension> extensions = getExtensionMap().get(pointId);
		if (extensions != null) {
			log.debug("Getting extensions defined by : " + pointId);
			return extensions;
		}
		else {
			return new Vector<Extension>();
		}	
	}
	
	/**
	 * Return all of the extensions associated with the given <code>pointId</code>
	 * 
	 * Returns getExtension(pointId) if no modules extend this pointId for given media type
	 * 
	 * @param pointId
	 * @param Extension.MEDIA_TYPE
	 * @return List of extensions
	 */
	public static List<Extension> getExtensions(String pointId, Extension.MEDIA_TYPE type) {
		String key = pointId + "|" + type;
		List<Extension> extensions = getExtensionMap().get(key);
		if (extensions != null) {
			log.debug("Getting extensions defined by : " + key);
			return extensions;
		}
		else {
			return getExtensions(pointId);
		}	
	}
	
	/**
	 * Get a list of required Privileges defined by the modules
	 * 
	 * @return
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
	 * @return
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
	 * Returns true/false whether the Module <code>mod</code> is activated or not
	 * 
	 * @param mod
	 * @return started status
	 */
	public static boolean isModuleStarted(Module mod) {
		return getStartedModulesMap().containsValue(mod);
	}
	
	/**
	 * Get a module's classloader
	 * 
	 * @param mod
	 * @return ModuleClassLoader
	 * @throws ModuleException
	 */
	public static ModuleClassLoader getModuleClassLoader(Module mod) throws ModuleException {
		if (!getModuleClassLoaderMap().containsKey(mod))
			throw new ModuleException("Module not found", mod.getName());
		
		return getModuleClassLoaderMap().get(mod); 
	}
	
	/**
	 * Get a module's classloader via the module id
	 * 
	 * @param moduleId
	 * @return ModuleClassLoader
	 */
	public static ModuleClassLoader getModuleClassLoader(String moduleId) {
		Module mod = getStartedModulesMap().get(moduleId);
		if (mod == null)
			throw new ModuleException("Module id not found in list of started modules: ", moduleId);
		
		return getModuleClassLoader(mod);
	}

	/**
	 * Returns all module classloaders 
	 * 
	 * @return
	 */
	public static Collection<ModuleClassLoader> getModuleClassLoaders() {
		Map<Module, ModuleClassLoader> classLoaders = getModuleClassLoaderMap();
		if (classLoaders.size() > 0)
			return classLoaders.values();
		
		return Collections.emptyList();
	}
	
	/**
	 * Return all current classloaders in the form of a map<ModuleId, ModuleClassLoader>
	 * 
	 * @return
	 */
	public static synchronized Map<Module, ModuleClassLoader> getModuleClassLoaderMap() {
		if (moduleClassLoaders == null)
			moduleClassLoaders = new WeakHashMap<Module, ModuleClassLoader>();

		return moduleClassLoaders;
	}
	
	/**
	 * Return the current extension map
	 * 
	 * @return
	 */
	public static synchronized Map<String, List<Extension>> getExtensionMap() {
		if (extensionMap == null)
			extensionMap = new WeakHashMap<String, List<Extension>>();
		
		return extensionMap;
	}
	
	/**
	 * Tests whether all modules mentioned in module.requiredModules are loaded and started 
	 * already (by being in the startedModules list)
	 * 
	 * @param module
	 * @return true/false boolean
	 */
	private static boolean requiredModulesStarted(Module module) {
		for (String reqModPackage : module.getRequiredModules()) {
			boolean found = false;
			for (Module mod : getStartedModules()) {
				if (mod.getPackageName().equals(reqModPackage)) {
					found = true;
					break;
				}
			}
			
			if (!found)
				return false;
		}
		
		return true;
	}

	
	/**
	 * Update the module:
	 * 
	 * 1) Download the new module
	 * 2) Unload the old module
	 * 3) Load/start the new module
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
	 * @param moduleId
	 * @return
	 */
	private static String getGlobalPropertyStartedDescription(String moduleId) {
		String ret = "DO NOT MODIFY. true/false whether or not the " + moduleId;
		ret += " module has been started.  This is used to make sure modules that were running ";
		ret += " prior to a restart are started again";
		
		return ret;
	}

	
	
}
