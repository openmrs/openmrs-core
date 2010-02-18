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
package org.openmrs.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.LogManager;
import org.openmrs.api.context.Context;
import org.openmrs.module.MandatoryModuleException;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleFactory;
import org.openmrs.module.ModuleMustStartException;
import org.openmrs.module.OpenmrsRequiredModuleException;
import org.openmrs.module.web.WebModuleUtil;
import org.openmrs.scheduler.SchedulerUtil;
import org.openmrs.util.DatabaseUpdateException;
import org.openmrs.util.DatabaseUpdater;
import org.openmrs.util.InputRequiredException;
import org.openmrs.util.OpenmrsClassLoader;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.filter.initialization.InitializationFilter;
import org.openmrs.web.filter.update.UpdateFilter;
import org.springframework.web.context.ContextLoaderListener;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Our Listener class performs the basic starting functions for our webapp. Basic needs for starting
 * the API: 1) Get the runtime properties 2) Start Spring 3) Start the OpenMRS APi (via
 * Context.startup) Basic startup needs specific to the web layer: 1) Do the web startup of the
 * modules 2) Copy the custom look/images/messages over into the web layer
 */
public final class Listener extends ContextLoaderListener {
	
	private static boolean runtimePropertiesFound = false;
	
	private static Throwable errorAtStartup = null;
	
	/**
	 * Boolean flag set on webapp startup marking whether there is a runtime properties file or not.
	 * If there is not, then the {@link InitializationFilter} takes over any openmrs url and
	 * redirects to the {@link #SETUP_PAGE_URL}
	 * 
	 * @return true/false whether an openmrs runtime properties file is defined
	 */
	public static boolean runtimePropertiesFound() {
		return runtimePropertiesFound;
	}
	
	/**
	 * Boolean flag set by the {@link #contextInitialized(ServletContextEvent)} method
	 * if an error occurred when trying to start up.  The StartupErrorFilter displays
	 * the error to the admin
	 * 
	 * @return true/false if an error occurred when starting up
	 */
	public static boolean errorOccurredAtStartup() {
		return errorAtStartup != null;
	}
	
	/**
	 * Get the error thrown at startup
	 * 
	 * @return get the error thrown at startup
	 */
	public static Throwable getErrorAtStartup() {
		return errorAtStartup;
	}
	
	/**
	 * This method is called when the servlet context is initialized(when the Web Application is
	 * deployed). You can initialize servlet context related data here.
	 * 
	 * @param event
	 */
	public void contextInitialized(ServletContextEvent event) {
		Log log = LogFactory.getLog(Listener.class);
		
		log.debug("Starting the OpenMRS webapp");
		
		try {
			ServletContext servletContext = event.getServletContext();
			
			// pulled from web.xml.
			loadConstants(servletContext);
			
			// erase things in the dwr file
			clearDWRFile(servletContext);
			
			// Try to get the runtime properties 
			Properties props = getRuntimeProperties();
			if (props != null) {
				// the user has defined a runtime properties file
				runtimePropertiesFound = true;
				// set props to the context so that they can be 
				// used during sessionFactory creation 
				Context.setRuntimeProperties(props);
			}
			
			Thread.currentThread().setContextClassLoader(OpenmrsClassLoader.getInstance());
			
			if (!setupNeeded()) {
				// must be done after the runtime properties are
				// found but before the database update is done
				copyCustomizationIntoWebapp(servletContext, props);
				
				super.contextInitialized(event);
				startOpenmrs(event.getServletContext());
			}
			
		}
		catch (Throwable t) {
			errorAtStartup = t;
			log.fatal("Got exception while starting up: ", t);
		}
		
	}
	
	/**
	 * This method knows about all the filters that openmrs uses for setup. Currently those are the
	 * {@link InitializationFilter} and the {@link UpdateFilter}. If either of these have to do
	 * something, openmrs won't start in this Listener.
	 * 
	 * @return true if one of the filters needs to take some action
	 */
	private boolean setupNeeded() throws ServletException {
		if (!runtimePropertiesFound)
			return true;
		
		try {
			return DatabaseUpdater.updatesRequired() && !DatabaseUpdater.allowAutoUpdate();
		}
		catch (Throwable t) {
			throw new ServletException(t);
		}
	}
	
	/**
	 * Do the work of starting openmrs.
	 * 
	 * @param servletContext
	 * @throws ServletException
	 */
	public static void startOpenmrs(ServletContext servletContext) throws ServletException {
		// start openmrs
		try {
			Context.openSession();
			
			// load core modules that are packaged into the webapp
			Listener.loadCoreModules(servletContext);
			
			Context.startup(getRuntimeProperties());
		}
		catch (DatabaseUpdateException updateEx) {
			throw new ServletException("Should not be here because updates were run previously", updateEx);
		}
		catch (InputRequiredException inputRequiredEx) {
			throw new ServletException("Should not be here because updates were run previously", inputRequiredEx);
		}
		catch (MandatoryModuleException mandatoryModEx) {
			throw new ServletException(mandatoryModEx);
		}
		catch (OpenmrsRequiredModuleException reqModEx) {
			// don't wrap this error in a ServletException because we want to deal with it differently
			// in the StartupErrorFilter class
			throw reqModEx;
		}
		
		
		// TODO catch openmrs errors here and drop the user back out to the setup screen
		
		try {
			
			// web load modules
			Listener.performWebStartOfModules(servletContext);
			
			// start the scheduled tasks
			SchedulerUtil.startup(getRuntimeProperties());
		}
		catch (Throwable t) {
			Context.shutdown();
			WebModuleUtil.shutdownModules(servletContext);
			throw new ServletException(t);
		}
		finally {
			Context.closeSession();
		}
	}
	
	/**
	 * Load the openmrs constants with values from web.xml init parameters
	 * 
	 * @param servletContext startup context (web.xml)
	 */
	private void loadConstants(ServletContext servletContext) {
		WebConstants.BUILD_TIMESTAMP = servletContext.getInitParameter("build.timestamp");
		WebConstants.WEBAPP_NAME = getContextPath(servletContext);
	}
	
	/**
	 * Hacky way to get the current contextPath. This will usually be "openmrs". This method will be
	 * obsolete when servlet api ~2.6 comes out...at which point a call like
	 * servletContext.getContextRoot() would be sufficient
	 * 
	 * @return current contextPath of this webapp without initial slash
	 */
	private String getContextPath(ServletContext servletContext) {
		// Get the context path without the request.
		String contextPath = "";
		try {
			String path = servletContext.getResource("/").getPath();
			contextPath = path.substring(0, path.lastIndexOf("/"));
			contextPath = contextPath.substring(contextPath.lastIndexOf("/"));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		// trim off initial slash if it exists
		if (contextPath.indexOf("/") != -1)
			contextPath = contextPath.substring(1);
		
		return contextPath;
	}
	
	/**
	 * Convenience method to empty out the dwr-modules.xml file to fix any errors that might have
	 * occurred in it when loading or unloading modules.
	 * 
	 * @param servletContext
	 */
	private void clearDWRFile(ServletContext servletContext) {
		Log log = LogFactory.getLog(Listener.class);
		
		String realPath = servletContext.getRealPath("");
		String absPath = realPath + "/WEB-INF/dwr-modules.xml";
		File dwrFile = new File(absPath.replace("/", File.separator));
		if (dwrFile.exists()) {
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
				Document doc = db.parse(dwrFile);
				Element elem = doc.getDocumentElement();
				elem.setTextContent("");
				OpenmrsUtil.saveDocument(doc, dwrFile);
			}
			catch (Throwable t) {
				// got here because the dwr-modules.xml file is empty for some reason.  This might 
				// happen because the servlet container (i.e. tomcat) crashes when first loading this file
				log.debug("Error clearing dwr-modules.xml", t);
				dwrFile.delete();
				try {
					FileWriter writer = new FileWriter(dwrFile);
					writer
					        .write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!DOCTYPE dwr PUBLIC \"-//GetAhead Limited//DTD Direct Web Remoting 2.0//EN\" \"http://directwebremoting.org/schema/dwr20.dtd\">\n<dwr></dwr>");
					writer.close();
				}
				catch (IOException io) {
					log.error("Unable to clear out the " + dwrFile.getAbsolutePath()
					        + " file.  Please redeploy the openmrs war file", io);
				}
				
			}
		}
	}
	
	/**
	 * Copy the customization scripts over into the webapp
	 * 
	 * @param servletContext
	 */
	private void copyCustomizationIntoWebapp(ServletContext servletContext, Properties props) {
		Log log = LogFactory.getLog(Listener.class);
		
		String realPath = servletContext.getRealPath("");
		// TODO centralize map to WebConstants?
		Map<String, String> custom = new HashMap<String, String>();
		custom.put("custom.template.dir", "/WEB-INF/template");
		custom.put("custom.index.jsp.file", "/WEB-INF/view/index.jsp");
		custom.put("custom.login.jsp.file", "/WEB-INF/view/login.jsp");
		custom.put("custom.patientDashboardForm.jsp.file", "/WEB-INF/view/patientDashboardForm.jsp");
		custom.put("custom.images.dir", "/images");
		custom.put("custom.style.css.file", "/style.css");
		custom.put("custom.messages", "/WEB-INF/custom_messages.properties");
		custom.put("custom.messages_fr", "/WEB-INF/custom_messages_fr.properties");
		custom.put("custom.messages_es", "/WEB-INF/custom_messages_es.properties");
		custom.put("custom.messages_de", "/WEB-INF/custom_messages_de.properties");
		
		for (String prop : custom.keySet()) {
			String webappPath = custom.get(prop);
			String userOverridePath = props.getProperty(prop);
			// if they defined the variable
			if (userOverridePath != null) {
				String absolutePath = realPath + webappPath;
				File file = new File(userOverridePath);
				
				// if they got the path correct
				// also, if file does not start with a "." (hidden files, like SVN files) 
				if (file.exists() && !userOverridePath.startsWith(".")) {
					log.debug("Overriding file: " + absolutePath);
					log.debug("Overriding file with: " + userOverridePath);
					if (file.isDirectory()) {
						for (File f : file.listFiles()) {
							userOverridePath = f.getAbsolutePath();
							if (!f.getName().startsWith(".")) {
								String tmpAbsolutePath = absolutePath + "/" + f.getName();
								if (!copyFile(userOverridePath, tmpAbsolutePath)) {
									log.warn("Unable to copy file in folder defined by runtime property: " + prop);
									log.warn("Your source directory (or a file in it) '" + userOverridePath
									        + " cannot be loaded or destination '" + tmpAbsolutePath + "' cannot be found");
								}
							}
						}
					} else {
						// file is not a directory
						if (!copyFile(userOverridePath, absolutePath)) {
							log.warn("Unable to copy file defined by runtime property: " + prop);
							log.warn("Your source file '" + userOverridePath + " cannot be loaded or destination '"
							        + absolutePath + "' cannot be found");
						}
					}
				}
			}
			
		}
	}
	
	/**
	 * Copies file pointed to by <code>fromPath</code> to <code>toPath</code>
	 * 
	 * @param fromPath
	 * @param toPath
	 * @return true/false whether the copy was a success
	 */
	private boolean copyFile(String fromPath, String toPath) {
		Log log = LogFactory.getLog(Listener.class);
		
		FileInputStream inputStream = null;
		FileOutputStream outputStream = null;
		try {
			inputStream = new FileInputStream(fromPath);
			outputStream = new FileOutputStream(toPath);
			OpenmrsUtil.copyFile(inputStream, outputStream);
		}
		catch (IOException io) {
			return false;
		}
		finally {
			try {
				if (inputStream != null)
					inputStream.close();
			}
			catch (IOException io) {
				log.warn("Unable to close input stream", io);
			}
			try {
				if (outputStream != null)
					outputStream.close();
			}
			catch (IOException io) {
				log.warn("Unable to close input stream", io);
			}
		}
		return true;
	}
	
	/**
	 * Load the core modules from web/WEB-INF/coreModules. <br/>
	 * <br/>
	 * This method assumes that the api startup() and WebModuleUtil.startup() will be called later
	 * for modules that loaded here
	 * 
	 * @param servletContext the current servlet context for the webapp
	 */
	public static void loadCoreModules(ServletContext servletContext) {
		Log log = LogFactory.getLog(Listener.class);
		
		String path = servletContext.getRealPath("");
		path += File.separator + "WEB-INF" + File.separator + "coreModules";
		File folder = new File(path);
		
		if (!folder.exists()) {
			log.warn("Core module repository doesn't exist: " + folder.getAbsolutePath());
			return;
		}
		if (!folder.isDirectory()) {
			log.warn("Core module repository isn't a directory: " + folder.getAbsolutePath());
			return;
		}
		
		// loop over the modules and load the modules that we can
		for (File f : folder.listFiles()) {
			if (!f.getName().startsWith(".")) { // ignore .svn folder and the like
				try {
					Module mod = ModuleFactory.loadModule(f);
					log.debug("Loaded module: " + mod + " successfully");
				}
				catch (Throwable t) {
					log.warn("Error while trying to load module " + f.getName() + "", t);
				}
			}
		}
	}
	
	/**
	 * Called when the webapp is shut down properly Must call Context.shutdown() and then shutdown
	 * all the web layers of the modules
	 * 
	 * @see org.springframework.web.context.ContextLoaderListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
	public void contextDestroyed(ServletContextEvent event) {
		
		try {
			Context.openSession();
			
			Context.shutdown();
			
			WebModuleUtil.shutdownModules(event.getServletContext());
			
		}
		catch (Throwable t) {
			// don't print the unhelpful "contextDAO is null" message
			if (!t.getMessage().equals("contextDAO is null")) {
				// not using log.error here so it can be garbage collected 
				System.out.println("Listener.contextDestroyed: Error while shutting down openmrs: ");
				t.printStackTrace();
			}
		}
		finally {
			// remove the user context that we set earlier
			Context.closeSession();
		}
		
		super.contextDestroyed(event);
		
		try {
			for (Enumeration<Driver> e = DriverManager.getDrivers(); e.hasMoreElements();) {
				Driver driver = e.nextElement();
				ClassLoader classLoader = driver.getClass().getClassLoader();
				// only unload drivers for this webapp
				if (classLoader == null || classLoader == getClass().getClassLoader()) {
					DriverManager.deregisterDriver(driver);
				} else {
					System.err.println("Didn't remove driver class: " + driver.getClass() + " with classloader of: "
					        + driver.getClass().getClassLoader());
				}
			}
		}
		catch (Throwable e) {
			System.err.println("Listener.contextDestroyed: Failed to cleanup drivers in webapp");
			e.printStackTrace();
		}
		
		OpenmrsClassLoader.onShutdown();
		
		LogManager.shutdown();
		
		// just to make things nice and clean.
		System.gc();
		System.gc();
	}
	
	/**
	 * Looks for and loads in the runtime properties. Searches for an the file in this order: 1)
	 * environment variable called "OPENMRS_RUNTIME_PROPERTIES_FILE" 2)
	 * {user_home}/WEBAPPNAME_runtime.properties 3) ./WEBAPPNAME_runtime.properties Returns null if
	 * no runtime properties file was found
	 * 
	 * @return Properties
	 */
	public static Properties getRuntimeProperties() {
		Log log = LogFactory.getLog(Listener.class);
		
		Properties props = new Properties();
		
		try {
			FileInputStream propertyStream = null;
			
			// Look for environment variable {WEBAPP.NAME}_RUNTIME_PROPERTIES_FILE
			String webapp = WebConstants.WEBAPP_NAME;
			String env = webapp.toUpperCase() + "_RUNTIME_PROPERTIES_FILE";
			
			String filepath = System.getenv(env);
			
			if (filepath != null) {
				log.debug("Atempting to load runtime properties from: " + filepath + " ");
				try {
					propertyStream = new FileInputStream(filepath);
				}
				catch (IOException e) {
					log.warn("Unable to load properties file with path: " + filepath
					        + ". (derived from environment variable " + env + ")", e);
				}
			} else {
				log.info("Couldn't find an environment variable named " + env);
				log.debug("Available environment variables are named: " + System.getenv().keySet());
			}
			
			// env is the name of the file to look for in the directories
			String filename = webapp + "-runtime.properties";
			
			if (propertyStream == null) {
				filepath = OpenmrsUtil.getApplicationDataDirectory() + filename;
				log.debug("Attempting to load property file from: " + filepath);
				try {
					propertyStream = new FileInputStream(filepath);
				}
				catch (FileNotFoundException e) {
					log.warn("Unable to find properties file: " + filepath);
				}
			}
			
			// look in current directory last
			if (propertyStream == null) {
				filepath = filename;
				log.debug("Attempting to load properties file in directory: " + filepath);
				try {
					propertyStream = new FileInputStream(filepath);
				}
				catch (FileNotFoundException e) {
					log.warn("Also unable to find a runtime properties file named " + new File(filepath).getAbsolutePath());
				}
			}
			
			if (propertyStream == null)
				throw new IOException("Could not open '" + filename + "' in user or local directory.");
			OpenmrsUtil.loadProperties(props, propertyStream);
			propertyStream.close();
			log.info("Using runtime properties file: " + filepath);
			
		}
		catch (Throwable t) {
			log.warn("Unable to find a runtime properties file. Initial setup is needed. View the webapp to run the setup wizard.");
			return null;
		}
		return props;
	}
	
	/**
	 * Call WebModuleUtil.startModule on each started module
	 * 
	 * @param servletContext
	 * @throws ModuleMustStartException if the context cannot restart due to a
	 *             {@link MandatoryModuleException} or {@link OpenmrsRequiredModuleException}
	 */
	public static void performWebStartOfModules(ServletContext servletContext) throws ModuleMustStartException {
		Log log = LogFactory.getLog(Listener.class);
		
		List<Module> startedModules = new ArrayList<Module>();
		startedModules.addAll(ModuleFactory.getStartedModules());
		boolean someModuleNeedsARefresh = false;
		for (Module mod : startedModules) {
			try {
				boolean thisModuleCausesRefresh = WebModuleUtil.startModule(mod, servletContext,
				/* delayContextRefresh */true);
				someModuleNeedsARefresh = someModuleNeedsARefresh || thisModuleCausesRefresh;
			}
			catch (Throwable t) {
				mod.setStartupErrorMessage("Unable to start module", t);
			}
		}
		
		if (someModuleNeedsARefresh) {
			try {
				WebModuleUtil.refreshWAC(servletContext);
			}
			catch (ModuleMustStartException ex) {
				// pass this up to the calling method so that openmrs loading stops
				throw ex;
			}
			catch (Throwable t) {
				log.fatal("Unable to refresh the spring application context. Unloading all modules,  Error was:", t);
				try {
					WebModuleUtil.shutdownModules(servletContext);
					for (Module mod : ModuleFactory.getLoadedModules()) {// use loadedModules to avoid a concurrentmodificationexception
						ModuleFactory.stopModule(mod, true, true); // pass in one true value here so that the global properties aren't written to and the second true so core modules can be stopped 
					}
					WebModuleUtil.refreshWAC(servletContext);
				}
				catch (ModuleMustStartException ex) {
					// pass this up to the calling method so that openmrs loading stops
					throw ex;
				}
				catch (Throwable t2) {
					log.warn("caught another error: ", t2);
				}
			}
		}
	}
	
}
