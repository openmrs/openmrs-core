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
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.servlet.Filter;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.xml.DOMConfigurator;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleException;
import org.openmrs.module.ModuleFactory;
import org.openmrs.module.ModuleUtil;
import org.openmrs.module.web.filter.ModuleFilterConfig;
import org.openmrs.module.web.filter.ModuleFilterDefinition;
import org.openmrs.module.web.filter.ModuleFilterMapping;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.DispatcherServlet;
import org.openmrs.web.dwr.OpenmrsDWRServlet;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class WebModuleUtil {
	
	private static Log log = LogFactory.getLog(WebModuleUtil.class);
	
	private static DispatcherServlet dispatcherServlet = null;
	
	private static OpenmrsDWRServlet dwrServlet = null;
	
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
	 * responsible for restarting the context if necessary. If delayContextRefresh is true and this
	 * module should have caused a context refresh, a true value is returned. Otherwise, false is
	 * returned
	 * 
	 * @param mod Module to start
	 * @param ServletContext the current ServletContext
	 * @param delayContextRefresh true/false whether or not to do the context refresh
	 * @return boolean whether or not the spring context need to be refreshed
	 */
	public static boolean startModule(Module mod, ServletContext servletContext, boolean delayContextRefresh) {
		//register the module loggers
		if (mod.getLog4j() != null) {
			DOMConfigurator.configure(mod.getLog4j().getDocumentElement());
		}
		
		if (log.isDebugEnabled())
			log.debug("trying to start module " + mod);
		
		// only try and start this module if the api started it without a
		// problem.
		if (ModuleFactory.isModuleStarted(mod) && !mod.hasStartupError()) {
			
			String realPath = servletContext.getRealPath("");
			
			// copy the messages into the webapp
			String path = "/WEB-INF/module_messages@LANG@.properties";
			
			for (Entry<String, Properties> entry : mod.getMessages().entrySet()) {
				if (log.isDebugEnabled())
					log.debug("Copying message property file: " + entry.getKey());
				
				String lang = "_" + entry.getKey();
				if (lang.equals("_en") || lang.equals("_"))
					lang = "";
				
				String currentPath = path.replace("@LANG@", lang);
				
				String absolutePath = realPath + currentPath;
				File file = new File(absolutePath);
				try {
					if (!file.exists())
						file.createNewFile();
				}
				catch (IOException ioe) {
					log.error("Unable to create new file " + file.getAbsolutePath() + " " + ioe);
				}
				
				Properties props = entry.getValue();
				
				// set all properties to start with 'moduleName.' if not already
				List<Object> keys = new Vector<Object>();
				keys.addAll(props.keySet());
				for (Object obj : keys) {
					String key = (String) obj;
					if (!key.startsWith(mod.getModuleId())) {
						props.put(mod.getModuleId() + "." + key, props.get(key));
						props.remove(key);
					}
				}
				
				// append the properties to the appropriate messages file
				OpenmrsUtil.storeProperties(props, file, "Module: " + mod.getName() + " v" + mod.getVersion());
			}
			log.debug("Done copying messages");
			
			// flag to tell whether we added any xml/dwr/etc changes that necessitate a refresh
			// of the web application context
			boolean moduleNeedsContextRefresh = false;
			
			// copy the html files into the webapp (from /web/module/ in the module)
			// also looks for a spring context file. If found, schedules spring to be restarted
			JarFile jarFile = null;
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
						
						StringBuffer absPath = new StringBuffer(realPath + "/WEB-INF");
						
						// If this is within the tag file directory, copy it into /WEB-INF/tags/module/moduleId/...
						if (filepath.startsWith("tags/")) {
							filepath = filepath.substring(5);
							absPath.append("/tags/module/");
						}
						// Otherwise, copy it into /WEB-INF/view/module/moduleId/...
						else {
							absPath.append("/view/module/");
						}
						absPath.append(mod.getModuleId() + "/" + filepath);
						if (log.isDebugEnabled())
							log.debug("Moving file from: " + name + " to " + absPath);
						
						// get the output file
						File outFile = new File(absPath.toString().replace("/", File.separator));
						if (entry.isDirectory()) {
							if (!outFile.exists()) {
								outFile.mkdirs();
							}
						} else {
							//if (outFile.getName().endsWith(".jsp") == false)
							//	outFile = new File(absPath.replace("/", File.separator) + MODULE_NON_JSP_EXTENSION);
							
							// copy the contents over to the webpp for non directories
							OutputStream outStream = new FileOutputStream(outFile, false);
							InputStream inStream = jarFile.getInputStream(entry);
							OpenmrsUtil.copyFile(inStream, outStream);
							inStream.close();
							outStream.close();
						}
					} else if (name.equals("moduleApplicationContext.xml") || name.equals("webModuleApplicationContext.xml")) {
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
			}
			
			// find and add the dwr code to the dwr-modules.xml file (if defined)
			InputStream inputStream = null;
			try {
				Document config = mod.getConfig();
				Element root = config.getDocumentElement();
				if (root.getElementsByTagName("dwr").getLength() > 0) {
					
					// get the dwr-module.xml file that we're appending our code to
					File f = new File(realPath + "/WEB-INF/dwr-modules.xml".replace("/", File.separator));
					inputStream = new FileInputStream(f);
					Document dwrmodulexml = getDWRModuleXML(inputStream, realPath);
					Element outputRoot = dwrmodulexml.getDocumentElement();
					
					// loop over all of the children of the "dwr" tag
					Node node = root.getElementsByTagName("dwr").item(0);
					Node current = node.getFirstChild();
					while (current != null) {
						if ("allow".equals(current.getNodeName()) || "signatures".equals(current.getNodeName())) {
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
			String folderPath = realPath + "/WEB-INF/view/module/" + mod.getModuleId();
			File outFile = new File(folderPath.replace("/", File.separator));
			outFile.deleteOnExit();
			
			// additional checks on module needing a context refresh
			if (moduleNeedsContextRefresh == false) {
				
				// AOP advice points are only loaded during the context refresh now.
				// if the context hasn't been marked to be refreshed yet, mark it
				// now if this module defines some advice
				if (mod.getAdvicePoints() != null && mod.getAdvicePoints().size() > 0) {
					moduleNeedsContextRefresh = true;
				}
				
			}
			
			// refresh the spring web context to get the just-created xml 
			// files into it (if we copied an xml file)
			if (moduleNeedsContextRefresh && delayContextRefresh == false) {
				if (log.isDebugEnabled())
					log.debug("Refreshing context for module" + mod);
				
				try {
					refreshWAC(servletContext);
					log.debug("Done Refreshing WAC");
				}
				catch (Exception e) {
					String msg = "Unable to refresh the WebApplicationContext";
					mod.setStartupErrorMessage(msg, e);
					
					if (log.isWarnEnabled())
						log.warn(msg + " for module: " + mod.getModuleId(), e);
					
					try {
						ModuleFactory.stopModule(mod, true, true); //remove jar from classloader play 
						stopModule(mod, servletContext, true);
					}
					catch (Exception e2) {
						// exception expected with most modules here
						if (log.isWarnEnabled())
							log.warn("Error while stopping a module that had an error on refreshWAC", e2);
					}
					
					// try starting the application context again
					refreshWAC(servletContext);
				}
			}
			
			// find and cache the module's servlets 
			//(only if the module started successfully previously)
			if (ModuleFactory.isModuleStarted(mod)) {
				log.debug("Loading servlets and filters for module: " + mod);
				loadServlets(mod, servletContext);
				loadFilters(mod, servletContext);
			}
			
			// return true if the module needs a context refresh and we didn't do it here
			return (moduleNeedsContextRefresh && delayContextRefresh == true);
			
		}
		
		// we aren't processing this module, so a context refresh is not necessary
		return false;
	}
	
	/**
	 * This method will find and cache this module's servlets (so that it doesn't have to look them
	 * up every time)
	 * 
	 * @param mod
	 * @param servletContext the servlet context
	 * @return this module's servlet map
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
					if (childNode.getTextContent() != null)
						name = childNode.getTextContent().trim();
				} else if ("servlet-class".equals(childNode.getNodeName())) {
					if (childNode.getTextContent() != null)
						className = childNode.getTextContent().trim();
				}
			}
			if (name.length() == 0 || className.length() == 0) {
				log.warn("both 'servlet-name' and 'servlet-class' are required for the 'servlet' tag. Given '" + name
				        + "' and '" + className + "' for module " + mod.getName());
				continue;
			}
			
			HttpServlet httpServlet = null;
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
			catch (Throwable t) {
				log.warn("Unable to initialize servlet: ", t);
				throw new ModuleException("Unable to initialize servlet: " + httpServlet, mod.getModuleId(), t);
			}
			
			// don't allow modules to overwrite servlets of other modules.
			if (moduleServlets.containsKey(name)) {
				//log.debug("A servlet mapping with name " + name + " already exists. " + mod.getModuleId() + "'s servlet is overwriting it");
				throw new ModuleException("A servlet mapping with name " + name + " already exists. " + mod.getModuleId()
				        + "'s servlet is trying to overwrite it");
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
			String name = "";
			for (int j = 0; j < childNodes.getLength(); j++) {
				Node childNode = childNodes.item(j);
				if ("servlet-name".equals(childNode.getNodeName())) {
					if (childNode.getTextContent() != null) {
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
	}
	
	/**
	 * This method will initialize and store this module's filters
	 * 
	 * @param module - The Module to load and register Filters
	 * @param servletContext - The servletContext within which this method is called
	 */
	public static void loadFilters(Module module, ServletContext servletContext) {
		
		// Load Filters
		Map<String, Filter> filters = new HashMap<String, Filter>();
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
		for (java.util.Iterator<ModuleFilterMapping> mapIter = moduleFilterMappings.iterator(); mapIter.hasNext();) {
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
			
			for (Iterator<String> i = moduleFiltersByName.keySet().iterator(); i.hasNext();) {
				String filterName = i.next();
				Filter filterVal = moduleFiltersByName.get(filterName);
				if (filters.contains(filterVal)) {
					i.remove();
				}
			}
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
		
		List<Filter> filters = new Vector<Filter>();
		if (request != null) {
			HttpServletRequest httpRequest = (HttpServletRequest) request;
			String requestPath = httpRequest.getRequestURI();
			
			if (requestPath != null) {
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
		Document dwrmodulexml = null;
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			db.setEntityResolver(new EntityResolver() {
				
				public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
					// When asked to resolve external entities (such as a DTD) we return an InputSource
					// with no data at the end, causing the parser to ignore the DTD.
					return new InputSource(new StringReader(""));
				}
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
		
		String realPath = servletContext.getRealPath("");
		
		// clear the module messages
		String messagesPath = realPath + "/WEB-INF/";
		File folder = new File(messagesPath.replace("/", File.separator));
		
		if (folder.exists()) {
			Properties emptyProperties = new Properties();
			for (File f : folder.listFiles()) {
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
	private static void stopModule(Module mod, ServletContext servletContext, boolean skipRefresh) {
		
		String moduleId = mod.getModuleId();
		String modulePackage = mod.getPackageName();
		
		// stop all dependent modules
		for (Module dependentModule : ModuleFactory.getStartedModules()) {
			if (!dependentModule.equals(mod) && dependentModule.getRequiredModules().contains(modulePackage))
				stopModule(dependentModule, servletContext, skipRefresh);
		}
		
		String realPath = servletContext.getRealPath("");
		
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
		
		// remove this module's entries in the dwr xml file
		InputStream inputStream = null;
		try {
			Document config = mod.getConfig();
			Element root = config.getDocumentElement();
			// if they defined any xml element
			if (root.getElementsByTagName("dwr").getLength() > 0) {
				
				// get the dwr-module.xml file that we're appending our code to
				File f = new File(realPath + "/WEB-INF/dwr-modules.xml".replace("/", File.separator));
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
						} else
							i++;
					} else
						i++;
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
		
		if (skipRefresh == false) {
			//try {
			//	if (dispatcherServlet != null)
			//		dispatcherServlet.reInitFrameworkServlet();
			//	if (dwrServlet != null)
			//		dwrServlet.reInitServlet();
			//}
			//catch (ServletException se) {
			//	log.warn("Unable to reinitialize webapplicationcontext for dispatcherservlet for module: " + mod.getName(), se);
			//}
			
			refreshWAC(servletContext);
		}
		
	}
	
	/**
	 * Stops, closes, and refreshes the Spring context for the given <code>servletContext</code>
	 * 
	 * @param servletContext
	 * @return
	 */
	public static XmlWebApplicationContext refreshWAC(ServletContext servletContext) {
		XmlWebApplicationContext wac = (XmlWebApplicationContext) WebApplicationContextUtils
		        .getWebApplicationContext(servletContext);
		if (log.isDebugEnabled())
			log.debug("Refreshing web applciation Context of class: " + wac.getClass().getName());
		
		XmlWebApplicationContext newAppContext = (XmlWebApplicationContext) ModuleUtil.refreshApplicationContext(wac);
		
		try {
			// must "refresh" the spring dispatcherservlet as well to add in 
			//the new handlerMappings
			if (dispatcherServlet != null)
				dispatcherServlet.reInitFrameworkServlet();
		}
		catch (ServletException se) {
			log.warn("Caught a servlet exception while refreshing the dispatcher servlet", se);
		}
		
		try {
			if (dwrServlet != null)
				dwrServlet.reInitServlet();
		}
		catch (ServletException se) {
			log.warn("Cause a servlet exception while refreshing the dwr servlet", se);
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
	 * Save the dwr servlet for use later (reinitializing things)
	 * 
	 * @param ds
	 */
	public static void setDWRServlet(OpenmrsDWRServlet ds) {
		log.debug("Setting dwr servlet: " + ds);
		dwrServlet = ds;
		//new NewCreator();
		//SessionFactoryUtils.processDeferredClose(null);
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
	
}
