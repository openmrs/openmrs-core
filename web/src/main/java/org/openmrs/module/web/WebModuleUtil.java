/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.openmrs.api.context.Context;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleException;
import org.openmrs.module.ModuleFactory;
import org.openmrs.module.ModuleUtil;
import org.openmrs.module.web.filter.ModuleFilterConfig;
import org.openmrs.module.web.filter.ModuleFilterDefinition;
import org.openmrs.module.web.filter.ModuleFilterMapping;
import org.openmrs.scheduler.SchedulerException;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.scheduler.TaskDefinition;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.util.PrivilegeConstants;
import org.openmrs.web.DispatcherServlet;
import org.openmrs.web.StaticDispatcherServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class WebModuleUtil {

	private WebModuleUtil() {
	}
	
	private static final Logger log = LoggerFactory.getLogger(WebModuleUtil.class);
	
	private static DispatcherServlet dispatcherServlet = null;
	
	private static StaticDispatcherServlet staticDispatcherServlet = null;
	
	// caches all of the modules' mapped servlets
	private static Map<String, HttpServlet> moduleServlets = Collections.synchronizedMap(new HashMap<String, HttpServlet>());
	
	// caches all of the module loaded filters and filter-mappings
	private static Map<Module, Collection<Filter>> moduleFilters = Collections
	        .synchronizedMap(new HashMap<Module, Collection<Filter>>());
	
	private static Map<String, Filter> moduleFiltersByName = Collections.synchronizedMap(new HashMap<String, Filter>());
	
	private static List<ModuleFilterMapping> moduleFilterMappings = Collections
	        .synchronizedList(new Vector<ModuleFilterMapping>());
	
	/**
	 * Performs the webapp specific startup needs for modules Normal startup is done in
	 * {@link ModuleFactory#startModule(Module)} If delayContextRefresh is true, the spring context
	 * is not rerun. This will save a lot of time, but it also means that the calling method is
	 * responsible for restarting the context if necessary (the calling method will also have to
	 * call {@link #loadServlets(Module, ServletContext)} and
	 * {@link #loadFilters(Module, ServletContext)}).<br>
	 * <br>
	 * If delayContextRefresh is true and this module should have caused a context refresh, a true
	 * value is returned. Otherwise, false is returned
	 *
	 * @param mod Module to start
	 * @param servletContext the current ServletContext
	 * @param delayContextRefresh true/false whether or not to do the context refresh
	 * @return boolean whether or not the spring context need to be refreshed
	 */
	public static boolean startModule(Module mod, ServletContext servletContext, boolean delayContextRefresh) {
		
		if (log.isDebugEnabled()) {
			log.debug("trying to start module " + mod);
		}
		
		// only try and start this module if the api started it without a
		// problem.
		if (ModuleFactory.isModuleStarted(mod) && !mod.hasStartupError()) {
			
			String realPath = getRealPath(servletContext);
			
			if (realPath == null) {
				realPath = System.getProperty("user.dir");
			}
			
			File webInf = new File(realPath + "/WEB-INF".replace("/", File.separator));
			if (!webInf.exists()) {
				webInf.mkdir();
			}
			
			// flag to tell whether we added any xml/dwr/etc changes that necessitate a refresh
			// of the web application context
			boolean moduleNeedsContextRefresh = false;
			
			// copy the html files into the webapp (from /web/module/ in the module)
			// also looks for a spring context file. If found, schedules spring to be restarted
			JarFile jarFile = null;
			OutputStream outStream = null;
			InputStream inStream = null;
			try {
				File modFile = mod.getFile();
				jarFile = new JarFile(modFile);
				Enumeration<JarEntry> entries = jarFile.entries();
				
				while (entries.hasMoreElements()) {
					JarEntry entry = entries.nextElement();
					String name = entry.getName();
					log.debug("Entry name: " + name);
					if (name.startsWith("web/module/")) {
						// trim out the starting path of "web/module/"
						String filepath = name.substring(11);
						
						StringBuilder absPath = new StringBuilder(realPath + "/WEB-INF");
						
						// If this is within the tag file directory, copy it into /WEB-INF/tags/module/moduleId/...
						if (filepath.startsWith("tags/")) {
							filepath = filepath.substring(5);
							absPath.append("/tags/module/");
						}
						// Otherwise, copy it into /WEB-INF/view/module/moduleId/...
						else {
							absPath.append("/view/module/");
						}
						
						// if a module id has a . in it, we should treat that as a /, i.e. files in the module
						// ui.springmvc should go in folder names like .../ui/springmvc/...
						absPath.append(mod.getModuleIdAsPath()).append("/").append(filepath);
						if (log.isDebugEnabled()) {
							log.debug("Moving file from: " + name + " to " + absPath);
						}
						
						// get the output file
						File outFile = new File(absPath.toString().replace("/", File.separator));
						if (entry.isDirectory()) {
							if (!outFile.exists()) {
								outFile.mkdirs();
							}
						} else {
							// make the parent directories in case it doesn't exist
							File parentDir = outFile.getParentFile();
							if (!parentDir.exists()) {
								parentDir.mkdirs();
							}
							
							// copy the contents over to the webapp for non directories
							outStream = new FileOutputStream(outFile, false);
							inStream = jarFile.getInputStream(entry);
							OpenmrsUtil.copyFile(inStream, outStream);
						}
					} else if ("moduleApplicationContext.xml".equals(name) || "webModuleApplicationContext.xml".equals(name)) {
						moduleNeedsContextRefresh = true;
					} else if (name.equals(mod.getModuleId() + "Context.xml")) {
						String msg = "DEPRECATED: '" + name
						        + "' should be named 'moduleApplicationContext.xml' now. Please update/upgrade. ";
						throw new ModuleException(msg, mod.getModuleId());
					}
				}
			}
			catch (IOException io) {
				log.warn("Unable to copy files from module " + mod.getModuleId() + " to the web layer", io);
			}
			finally {
				if (jarFile != null) {
					try {
						jarFile.close();
					}
					catch (IOException io) {
						log.warn("Couldn't close jar file: " + jarFile.getName(), io);
					}
				}
				if (inStream != null) {
					try {
						inStream.close();
					}
					catch (IOException io) {
						log.warn("Couldn't close InputStream: " + io);
					}
				}
				if (outStream != null) {
					try {
						outStream.close();
					}
					catch (IOException io) {
						log.warn("Couldn't close OutputStream: " + io);
					}
				}
			}
			
			// find and add the dwr code to the dwr-modules.xml file (if defined)
			InputStream inputStream = null;
			try {
				Document config = mod.getConfig();
				Element root = config.getDocumentElement();
				if (root.getElementsByTagName("dwr").getLength() > 0) {
					
					// get the dwr-module.xml file that we're appending our code to
					File f = new File(realPath + "/WEB-INF/dwr-modules.xml".replace("/", File.separator));
					
					// testing if file exists
					if (!f.exists()) {
						// if it does not -> needs to be created
						createDwrModulesXml(realPath);
					}
					
					inputStream = new FileInputStream(f);
					Document dwrmodulexml = getDWRModuleXML(inputStream, realPath);
					Element outputRoot = dwrmodulexml.getDocumentElement();
					
					// loop over all of the children of the "dwr" tag
					Node node = root.getElementsByTagName("dwr").item(0);
					Node current = node.getFirstChild();
					
					while (current != null) {
						if ("allow".equals(current.getNodeName()) || "signatures".equals(current.getNodeName())
						        || "init".equals(current.getNodeName())) {
							((Element) current).setAttribute("moduleId", mod.getModuleId());
							outputRoot.appendChild(dwrmodulexml.importNode(current, true));
						}
						
						current = current.getNextSibling();
					}
					
					moduleNeedsContextRefresh = true;
					
					// save the dwr-modules.xml file.
					OpenmrsUtil.saveDocument(dwrmodulexml, f);
				}
			}
			catch (FileNotFoundException e) {
				throw new ModuleException(realPath + "/WEB-INF/dwr-modules.xml file doesn't exist.", e);
			}
			finally {
				if (inputStream != null) {
					try {
						inputStream.close();
					}
					catch (IOException io) {
						log.error("Error while closing input stream", io);
					}
				}
			}
			
			// mark to delete the entire module web directory on exit
			// this will usually only be used when an improper shutdown has occurred.
			String folderPath = realPath + "/WEB-INF/view/module/" + mod.getModuleIdAsPath();
			File outFile = new File(folderPath.replace("/", File.separator));
			outFile.deleteOnExit();
			
			// additional checks on module needing a context refresh
			if (!moduleNeedsContextRefresh && mod.getAdvicePoints() != null && !mod.getAdvicePoints().isEmpty()) {
				
				// AOP advice points are only loaded during the context refresh now.
				// if the context hasn't been marked to be refreshed yet, mark it
				// now if this module defines some advice
				moduleNeedsContextRefresh = true;
				
			}
			
			// refresh the spring web context to get the just-created xml
			// files into it (if we copied an xml file)
			if (moduleNeedsContextRefresh && !delayContextRefresh) {
				if (log.isDebugEnabled()) {
					log.debug("Refreshing context for module" + mod);
				}
				
				try {
					refreshWAC(servletContext, false, mod);
					log.debug("Done Refreshing WAC");
				}
				catch (Exception e) {
					String msg = "Unable to refresh the WebApplicationContext";
					mod.setStartupErrorMessage(msg, e);
					
					if (log.isWarnEnabled()) {
						log.warn(msg + " for module: " + mod.getModuleId(), e);
					}
					
					try {
						stopModule(mod, servletContext, true);
						ModuleFactory.stopModule(mod, true, true); //remove jar from classloader play
					}
					catch (Exception e2) {
						// exception expected with most modules here
						if (log.isWarnEnabled()) {
							log.warn("Error while stopping a module that had an error on refreshWAC", e2);
						}
					}
					
					// try starting the application context again
					refreshWAC(servletContext, false, mod);
					
					notifySuperUsersAboutModuleFailure(mod);
				}
				
			}
			
			if (!delayContextRefresh && ModuleFactory.isModuleStarted(mod)) {
				// only loading the servlets/filters if spring is refreshed because one
				// might depend on files being available in spring
				// if the caller wanted to delay the refresh then they are responsible for
				// calling these two methods on the module
				
				// find and cache the module's servlets
				//(only if the module started successfully previously)
				log.debug("Loading servlets and filters for module: " + mod);
				loadServlets(mod, servletContext);
				loadFilters(mod, servletContext);
			}
			
			// return true if the module needs a context refresh and we didn't do it here
			return (moduleNeedsContextRefresh && delayContextRefresh);
			
		}
		
		// we aren't processing this module, so a context refresh is not necessary
		return false;
	}
	
	/** Stops all tasks started by given module
	 * @param mod
	 */
	private static void stopTasks(Module mod) {
		
		SchedulerService schedulerService = Context.getSchedulerService();
		
		String modulePackageName = mod.getPackageName();
		for (TaskDefinition task : schedulerService.getRegisteredTasks()) {
			
			String taskClass = task.getTaskClass();
			if (isModulePackageNameInTaskClass(modulePackageName, taskClass)) {
				try {
					schedulerService.shutdownTask(task);
				}
				catch (SchedulerException e) {
					log.error("Couldn't stop task:" + task + " for module: " + mod);
				}
			}
		}
	}
	
	/**
	 * Checks if module package name is in task class name
	 * @param modulePackageName the package name of module
	 * @param taskClass the class of given task
	 * @return true if task and module are in the same package
	 * @should return false for different package names
	 * @should return false if module has longer package name
	 * @should properly match subpackages
	 * @should return false for empty package names
	 */
	public static boolean isModulePackageNameInTaskClass(String modulePackageName, String taskClass) {
		return modulePackageName.length() <= taskClass.length()
		        && taskClass.matches(Pattern.quote(modulePackageName) + "(\\..*)+");
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
			Context.addProxyPrivilege(PrivilegeConstants.GET_USERS);
			
			// Send an alert to all administrators
			Context.getAlertService().notifySuperUsers("Module.startupError.notification.message", null, mod.getName());
		}
		finally {
			// Remove added privileges
			Context.removeProxyPrivilege(PrivilegeConstants.GET_USERS);
			Context.removeProxyPrivilege(PrivilegeConstants.MANAGE_ALERTS);
		}
	}
	
	/**
	 * This method will find and cache this module's servlets (so that it doesn't have to look them
	 * up every time)
	 *
	 * @param mod
	 * @param servletContext the servlet context
	 */
	public static void loadServlets(Module mod, ServletContext servletContext) {
		Element rootNode = mod.getConfig().getDocumentElement();
		NodeList servletTags = rootNode.getElementsByTagName("servlet");
		
		for (int i = 0; i < servletTags.getLength(); i++) {
			Node node = servletTags.item(i);
			NodeList childNodes = node.getChildNodes();
			String name = "", className = "";
			for (int j = 0; j < childNodes.getLength(); j++) {
				Node childNode = childNodes.item(j);
				if ("servlet-name".equals(childNode.getNodeName())) {
					if (childNode.getTextContent() != null) {
						name = childNode.getTextContent().trim();
					}
				} else if ("servlet-class".equals(childNode.getNodeName()) && childNode.getTextContent() != null) {
					className = childNode.getTextContent().trim();
				}
			}
			if (name.length() == 0 || className.length() == 0) {
				log.warn("both 'servlet-name' and 'servlet-class' are required for the 'servlet' tag. Given '" + name
				        + "' and '" + className + "' for module " + mod.getName());
				continue;
			}
			
			HttpServlet httpServlet;
			try {
				httpServlet = (HttpServlet) ModuleFactory.getModuleClassLoader(mod).loadClass(className).newInstance();
			}
			catch (ClassNotFoundException e) {
				log.warn("Class not found for servlet " + name + " for module " + mod.getName(), e);
				continue;
			}
			catch (IllegalAccessException e) {
				log.warn("Class cannot be accessed for servlet " + name + " for module " + mod.getName(), e);
				continue;
			}
			catch (InstantiationException e) {
				log.warn("Class cannot be instantiated for servlet " + name + " for module " + mod.getName(), e);
				continue;
			}
			
			try {
				log.debug("Initializing " + name + " servlet. - " + httpServlet + ".");
				ServletConfig servletConfig = new ModuleServlet.SimpleServletConfig(name, servletContext);
				httpServlet.init(servletConfig);
			}
			catch (Exception e) {
				log.warn("Unable to initialize servlet: ", e);
				throw new ModuleException("Unable to initialize servlet: " + httpServlet, mod.getModuleId(), e);
			}
			
			// don't allow modules to overwrite servlets of other modules.
			HttpServlet otherServletUsingSameName = moduleServlets.get(name);
			if (otherServletUsingSameName != null) {
				String otherServletName = otherServletUsingSameName.getClass().getPackage() + "."
				        + otherServletUsingSameName.getClass().getName();
				throw new ModuleException("A servlet mapping with name " + name + " is already in use and pointing at: "
				        + otherServletName + " from another installed module and this module is trying"
				        + " to use that same name.  Either the module attempting to be installed (" + mod.getModuleId()
				        + ") will not work or the other one will not.  Please consult the developers of these two"
				        + " modules to sort this out.");
			}
			
			log.debug("Caching the " + name + " servlet.");
			moduleServlets.put(name, httpServlet);
		}
	}
	
	/**
	 * Remove all of the servlets defined for this module
	 *
	 * @param mod the module that is being stopped that needs its servlets removed
	 */
	public static void unloadServlets(Module mod) {
		Element rootNode = mod.getConfig().getDocumentElement();
		NodeList servletTags = rootNode.getElementsByTagName("servlet");
		
		for (int i = 0; i < servletTags.getLength(); i++) {
			Node node = servletTags.item(i);
			NodeList childNodes = node.getChildNodes();
			String name;
			for (int j = 0; j < childNodes.getLength(); j++) {
				Node childNode = childNodes.item(j);
				if ("servlet-name".equals(childNode.getNodeName()) && childNode.getTextContent() != null) {
					name = childNode.getTextContent().trim();
					HttpServlet servlet = moduleServlets.get(name);
					if (servlet != null) {
						servlet.destroy(); // shut down the servlet
						moduleServlets.remove(name);
					}
				}
			}
		}
	}
	
	/**
	 * This method will initialize and store this module's filters
	 *
	 * @param module - The Module to load and register Filters
	 * @param servletContext - The servletContext within which this method is called
	 */
	public static void loadFilters(Module module, ServletContext servletContext) {
		
		// Load Filters
		Map<String, Filter> filters = new HashMap<>();
		try {
			for (ModuleFilterDefinition def : ModuleFilterDefinition.retrieveFilterDefinitions(module)) {
				if (moduleFiltersByName.containsKey(def.getFilterName())) {
					throw new ModuleException("A filter with name <" + def.getFilterName()
					        + "> has already been registered.");
				}
				ModuleFilterConfig config = ModuleFilterConfig.getInstance(def, servletContext);
				Filter f = (Filter) ModuleFactory.getModuleClassLoader(module).loadClass(def.getFilterClass()).newInstance();
				f.init(config);
				filters.put(def.getFilterName(), f);
			}
		}
		catch (ModuleException e) {
			throw e;
		}
		catch (Exception e) {
			throw new ModuleException("An error occurred initializing Filters for module: " + module.getModuleId(), e);
		}
		moduleFilters.put(module, filters.values());
		moduleFiltersByName.putAll(filters);
		log.debug("Module: " + module.getModuleId() + " successfully loaded " + filters.size() + " filters.");
		
		// Load Filter Mappings
		List<ModuleFilterMapping> modMappings = ModuleFilterMapping.retrieveFilterMappings(module);
		moduleFilterMappings.addAll(modMappings);
		log.debug("Module: " + module.getModuleId() + " successfully loaded " + modMappings.size() + " filter mappings.");
	}
	
	/**
	 * This method will destroy and remove all filters that were registered by the passed
	 * {@link Module}
	 *
	 * @param module - The Module for which you want to remove and destroy filters.
	 */
	public static void unloadFilters(Module module) {
		
		// Unload Filter Mappings
		for (Iterator<ModuleFilterMapping> mapIter = moduleFilterMappings.iterator(); mapIter.hasNext();) {
			ModuleFilterMapping mapping = mapIter.next();
			if (module.equals(mapping.getModule())) {
				mapIter.remove();
				log.debug("Removed ModuleFilterMapping: " + mapping);
			}
		}
		
		// unload Filters
		Collection<Filter> filters = moduleFilters.get(module);
		if (filters != null) {
			try {
				for (Filter f : filters) {
					f.destroy();
				}
			}
			catch (Exception e) {
				log.warn("An error occurred while trying to destroy and remove module Filter.", e);
			}
			log.debug("Module: " + module.getModuleId() + " successfully unloaded " + filters.size() + " filters.");
			moduleFilters.remove(module);

			moduleFiltersByName.values().removeIf(filters::contains);
		}
	}
	
	/**
	 * This method will return all Filters that have been registered a module
	 *
	 * @return A Collection of {@link Filter}s that have been registered by a module
	 */
	public static Collection<Filter> getFilters() {
		return moduleFiltersByName.values();
	}
	
	/**
	 * This method will return all Filter Mappings that have been registered by a module
	 *
	 * @return A Collection of all {@link ModuleFilterMapping}s that have been registered by a
	 *         Module
	 */
	public static Collection<ModuleFilterMapping> getFilterMappings() {
		return moduleFilterMappings;
	}
	
	/**
	 * Return List of Filters that have been loaded through Modules that have mappings that pass for
	 * the passed request
	 *
	 * @param request - The request to check for matching {@link Filter}s
	 * @return List of all {@link Filter}s that have filter mappings that match the passed request
	 */
	public static List<Filter> getFiltersForRequest(ServletRequest request) {
		
		List<Filter> filters = new ArrayList<>();
		if (request != null) {
			HttpServletRequest httpRequest = (HttpServletRequest) request;
			String requestPath = httpRequest.getRequestURI();
			
			if (requestPath != null) {
				if (requestPath.startsWith(httpRequest.getContextPath())) {
					requestPath = requestPath.substring(httpRequest.getContextPath().length());
				}
				for (ModuleFilterMapping filterMapping : WebModuleUtil.getFilterMappings()) {
					if (ModuleFilterMapping.filterMappingPasses(filterMapping, requestPath)) {
						Filter passedFilter = moduleFiltersByName.get(filterMapping.getFilterName());
						if (passedFilter != null) {
							filters.add(passedFilter);
						} else {
							log.warn("Unable to retrieve filter that has a name of " + filterMapping.getFilterName()
							        + " in filter mapping.");
						}
					}
				}
			}
		}
		return filters;
	}
	
	/**
	 * @param inputStream
	 * @param realPath
	 * @return
	 */
	private static Document getDWRModuleXML(InputStream inputStream, String realPath) {
		Document dwrmodulexml;
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			db.setEntityResolver((publicId, systemId) -> {
				// When asked to resolve external entities (such as a DTD) we return an InputSource
				// with no data at the end, causing the parser to ignore the DTD.
				return new InputSource(new StringReader(""));
			});
			
			dwrmodulexml = db.parse(inputStream);
		}
		catch (Exception e) {
			throw new ModuleException("Error parsing dwr-modules.xml file", e);
		}
		
		return dwrmodulexml;
	}
	
	/**
	 * Reverses all activities done by startModule(org.openmrs.module.Module) Normal stop/shutdown
	 * is done by ModuleFactory
	 */
	public static void shutdownModules(ServletContext servletContext) {
		
		String realPath = getRealPath(servletContext);
		
		// clear the module messages
		String messagesPath = realPath + "/WEB-INF/";
		File folder = new File(messagesPath.replace("/", File.separator));
		
		File[] files = folder.listFiles();
		if (folder.exists() && files != null) {
			Properties emptyProperties = new Properties();
			for (File f : files) {
				if (f.getName().startsWith("module_messages")) {
					OpenmrsUtil.storeProperties(emptyProperties, f, "");
				}
			}
		}
		
		// call web shutdown for each module
		for (Module mod : ModuleFactory.getLoadedModules()) {
			stopModule(mod, servletContext, true);
		}
		
	}
	
	/**
	 * Reverses all visible activities done by startModule(org.openmrs.module.Module)
	 *
	 * @param mod
	 * @param servletContext
	 */
	public static void stopModule(Module mod, ServletContext servletContext) {
		stopModule(mod, servletContext, false);
	}
	
	/**
	 * Reverses all visible activities done by startModule(org.openmrs.module.Module)
	 *
	 * @param mod
	 * @param servletContext
	 * @param skipRefresh
	 */
	public static void stopModule(Module mod, ServletContext servletContext, boolean skipRefresh) {
		
		String moduleId = mod.getModuleId();
		String modulePackage = mod.getPackageName();
		
		// stop all dependent modules
		for (Module dependentModule : ModuleFactory.getStartedModules()) {
			if (!dependentModule.equals(mod) && dependentModule.getRequiredModules().contains(modulePackage)) {
				stopModule(dependentModule, servletContext, skipRefresh);
			}
		}
		
		String realPath = getRealPath(servletContext);
		
		// delete the web files from the webapp
		String absPath = realPath + "/WEB-INF/view/module/" + moduleId;
		File moduleWebFolder = new File(absPath.replace("/", File.separator));
		if (moduleWebFolder.exists()) {
			try {
				OpenmrsUtil.deleteDirectory(moduleWebFolder);
			}
			catch (IOException io) {
				log.warn("Couldn't delete: " + moduleWebFolder.getAbsolutePath(), io);
			}
		}
		
		// (not) deleting module message properties
		
		// remove the module's servlets
		unloadServlets(mod);
		
		// remove the module's filters and filter mappings
		unloadFilters(mod);
		
		// stop all tasks associated with mod
		stopTasks(mod);
		
		// remove this module's entries in the dwr xml file
		InputStream inputStream = null;
		try {
			Document config = mod.getConfig();
			Element root = config.getDocumentElement();
			// if they defined any xml element
			if (root.getElementsByTagName("dwr").getLength() > 0) {
				
				// get the dwr-module.xml file that we're appending our code to
				File f = new File(realPath + "/WEB-INF/dwr-modules.xml".replace("/", File.separator));
				
				// testing if file exists
				if (!f.exists()) {
					// if it does not -> needs to be created
					createDwrModulesXml(realPath);
				}
				
				inputStream = new FileInputStream(f);
				Document dwrmodulexml = getDWRModuleXML(inputStream, realPath);
				Element outputRoot = dwrmodulexml.getDocumentElement();
				
				// loop over all of the children of the "dwr" tag
				// and remove all "allow" and "signature" tags that have the
				// same moduleId attr as the module being stopped
				NodeList nodeList = outputRoot.getChildNodes();
				int i = 0;
				while (i < nodeList.getLength()) {
					Node current = nodeList.item(i);
					if ("allow".equals(current.getNodeName()) || "signatures".equals(current.getNodeName())) {
						NamedNodeMap attrs = current.getAttributes();
						Node attr = attrs.getNamedItem("moduleId");
						if (attr != null && moduleId.equals(attr.getNodeValue())) {
							outputRoot.removeChild(current);
						} else {
							i++;
						}
					} else {
						i++;
					}
				}
				
				// save the dwr-modules.xml file.
				OpenmrsUtil.saveDocument(dwrmodulexml, f);
			}
		}
		catch (FileNotFoundException e) {
			throw new ModuleException(realPath + "/WEB-INF/dwr-modules.xml file doesn't exist.", e);
		}
		finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				}
				catch (IOException io) {
					log.error("Error while closing input stream", io);
				}
			}
		}
		
		if (!skipRefresh) {	
			refreshWAC(servletContext, false, null);
		}
		
	}
	
	/**
	 * Stops, closes, and refreshes the Spring context for the given <code>servletContext</code>
	 *
	 * @param servletContext
	 * @param isOpenmrsStartup if this refresh is being done at application startup
	 * @param startedModule the module that was just started and waiting on the context refresh
	 * @return The newly refreshed webApplicationContext
	 */
	public static XmlWebApplicationContext refreshWAC(ServletContext servletContext, boolean isOpenmrsStartup,
	        Module startedModule) {
		XmlWebApplicationContext wac = (XmlWebApplicationContext) WebApplicationContextUtils
		        .getWebApplicationContext(servletContext);
		if (log.isDebugEnabled()) {
			log.debug("Refreshing web application Context of class: " + wac.getClass().getName());
		}
		
		if (dispatcherServlet != null) {
			dispatcherServlet.stopAndCloseApplicationContext();
		}
		
		if (staticDispatcherServlet != null) {
			staticDispatcherServlet.stopAndCloseApplicationContext();
		}
		
		XmlWebApplicationContext newAppContext = (XmlWebApplicationContext) ModuleUtil.refreshApplicationContext(wac,
		    isOpenmrsStartup, startedModule);
		
		try {
			// must "refresh" the spring dispatcherservlet as well to add in
			//the new handlerMappings
			if (dispatcherServlet != null) {
				dispatcherServlet.reInitFrameworkServlet();
			}
			
			if (staticDispatcherServlet != null) {
				staticDispatcherServlet.refreshApplicationContext();
			}
		}
		catch (ServletException se) {
			log.warn("Caught a servlet exception while refreshing the dispatcher servlet", se);
		}
		
		return newAppContext;
	}
	
	/**
	 * Save the dispatcher servlet for use later (reinitializing things)
	 *
	 * @param ds
	 */
	public static void setDispatcherServlet(DispatcherServlet ds) {
		log.debug("Setting dispatcher servlet: " + ds);
		dispatcherServlet = ds;
	}
	
	/**
	 * Save the static content dispatcher servlet for use later when refreshing spring
	 *
	 * @param ds
	 */
	public static void setStaticDispatcherServlet(StaticDispatcherServlet ds) {
		log.debug("Setting dispatcher servlet for static content: " + ds);
		staticDispatcherServlet = ds;
	}
	
	/**
	 * Finds the servlet defined by the servlet name
	 *
	 * @param servletName the name of the servlet out of the path
	 * @return the current servlet or null if none defined
	 */
	public static HttpServlet getServlet(String servletName) {
		return moduleServlets.get(servletName);
	}
	
	/**
	 * Retrieves a path to a folder that stores web files of a module. <br>
	 * (path-to-openmrs/WEB-INF/view/module/moduleid)
	 *
	 * @param moduleId module id (e.g., "basicmodule")
	 * @return a path to a folder that stores web files or null if not in a web environment
	 * @should return the correct module folder
	 * @should return null if the dispatcher servlet is not yet set
	 * @should return the correct module folder if real path has a trailing slash
	 */
	public static String getModuleWebFolder(String moduleId) {
		if (dispatcherServlet == null) {
			throw new ModuleException("Dispatcher servlet must be present in the web environment");
		}
		
		String moduleFolder = "WEB-INF/view/module/";
		String realPath = dispatcherServlet.getServletContext().getRealPath("");
		String moduleWebFolder;
		
		//RealPath may contain '/' on Windows when running tests with the mocked servlet context
		if (realPath.endsWith(File.separator) || realPath.endsWith("/")) {
			moduleWebFolder = realPath + moduleFolder;
		} else {
			moduleWebFolder = realPath + "/" + moduleFolder;
		}
		
		moduleWebFolder += moduleId;
		
		return moduleWebFolder.replace("/", File.separator);
	}
	
	public static void createDwrModulesXml(String realPath) {
		
		try {
			
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			
			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("dwr");
			doc.appendChild(rootElement);
			
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(realPath
			        + "/WEB-INF/dwr-modules.xml".replace("/", File.separator)));
			
			transformer.transform(source, result);
			
		}
		catch (ParserConfigurationException pce) {
			log.error("Failed to parse document", pce);
		}
		catch (TransformerException tfe) {
			log.error("Failed to transorm xml source", tfe);
		}
	}
	
	public static String getRealPath(ServletContext servletContext) {
		return servletContext.getRealPath("");
	}
	
}
