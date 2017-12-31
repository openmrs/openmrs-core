/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.openmrs.api.context.Context;
import org.openmrs.module.MandatoryModuleException;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleFactory;
import org.openmrs.module.ModuleMustStartException;
import org.openmrs.module.OpenmrsCoreModuleException;
import org.openmrs.module.web.WebModuleUtil;
import org.openmrs.scheduler.SchedulerUtil;
import org.openmrs.util.DatabaseUpdateException;
import org.openmrs.util.DatabaseUpdater;
import org.openmrs.util.InputRequiredException;
import org.openmrs.util.MemoryLeakUtil;
import org.openmrs.util.OpenmrsClassLoader;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.filter.initialization.InitializationFilter;
import org.openmrs.web.filter.update.UpdateFilter;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.util.StringUtils;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * Our Listener class performs the basic starting functions for our webapp. Basic needs for starting
 * the API: 1) Get the runtime properties 2) Start Spring 3) Start the OpenMRS APi (via
 * Context.startup) Basic startup needs specific to the web layer: 1) Do the web startup of the
 * modules 2) Copy the custom look/images/messages over into the web layer
 */
public final class Listener extends ContextLoader implements ServletContextListener {
	
	private static final org.slf4j.Logger log = LoggerFactory.getLogger(Listener.class);
	
	private static boolean runtimePropertiesFound = false;
	
	private static Throwable errorAtStartup = null;
	
	private static boolean setupNeeded = false;
	
	/**
	 * Boolean flag set on webapp startup marking whether there is a runtime properties file or not.
	 * If there is not, then the {@link InitializationFilter} takes over any openmrs url and
	 * redirects to the {@link WebConstants#SETUP_PAGE_URL}
	 *
	 * @return true/false whether an openmrs runtime properties file is defined
	 */
	public static boolean runtimePropertiesFound() {
		return runtimePropertiesFound;
	}
	
	/**
	 * Boolean flag set by the {@link #contextInitialized(ServletContextEvent)} method if an error
	 * occurred when trying to start up. The StartupErrorFilter displays the error to the admin
	 *
	 * @return true/false if an error occurred when starting up
	 */
	public static boolean errorOccurredAtStartup() {
		return errorAtStartup != null;
	}
	
	/**
	 * Boolean flag that tells if we need to run the database setup wizard.
	 *
	 * @return true if setup is needed, else false.
	 */
	public static boolean isSetupNeeded() {
		return setupNeeded;
	}
	
	/**
	 * Get the error thrown at startup
	 *
	 * @return get the error thrown at startup
	 */
	public static Throwable getErrorAtStartup() {
		return errorAtStartup;
	}
	
	public static void setRuntimePropertiesFound(boolean runtimePropertiesFound) {
		Listener.runtimePropertiesFound = runtimePropertiesFound;
	}
	
	public static void setErrorAtStartup(Throwable errorAtStartup) {
		Listener.errorAtStartup = errorAtStartup;
	}
	
	/**
	 * This method is called when the servlet context is initialized(when the Web Application is
	 * deployed). You can initialize servlet context related data here.
	 *
	 * @param event
	 */
	@Override
	public void contextInitialized(ServletContextEvent event) {
		log.debug("Starting the OpenMRS webapp");
		
		try {
			// validate the current JVM version
			OpenmrsUtil.validateJavaVersion();
			
			ServletContext servletContext = event.getServletContext();
			
			// pulled from web.xml.
			loadConstants(servletContext);
			
			// erase things in the dwr file
			clearDWRFile(servletContext);
			
			setApplicationDataDirectory(servletContext);
			
			// Try to get the runtime properties
			Properties props = getRuntimeProperties();
			if (props != null) {
				// the user has defined a runtime properties file
				setRuntimePropertiesFound(true);
				// set props to the context so that they can be
				// used during sessionFactory creation
				Context.setRuntimeProperties(props);
				
				String appDataRuntimeProperty = props
				        .getProperty(OpenmrsConstants.APPLICATION_DATA_DIRECTORY_RUNTIME_PROPERTY, null);
				if (StringUtils.hasLength(appDataRuntimeProperty)) {
					OpenmrsUtil.setApplicationDataDirectory(null);
				}
				
				//ensure that we always log the runtime properties file that we are using
				//since openmrs is just booting, the log levels are not yet set. TRUNK-4835
				Logger contextLog = Logger.getLogger(getClass());
				contextLog.setLevel(Level.INFO);
				contextLog.info("Using runtime properties file: "
				        + OpenmrsUtil.getRuntimePropertiesFilePathName(WebConstants.WEBAPP_NAME));
			}
			
			Thread.currentThread().setContextClassLoader(OpenmrsClassLoader.getInstance());
			
			if (!setupNeeded()) {
				// must be done after the runtime properties are
				// found but before the database update is done
				copyCustomizationIntoWebapp(servletContext, props);
				
				/**
				 * This logic is from ContextLoader.initWebApplicationContext. Copied here instead
				 * of calling that so that the context is not cached and hence not garbage collected
				 */
				XmlWebApplicationContext context = (XmlWebApplicationContext) createWebApplicationContext(servletContext);
				configureAndRefreshWebApplicationContext(context, servletContext);
				servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, context);
				
				WebDaemon.startOpenmrs(event.getServletContext());
			} else {
				setupNeeded = true;
			}
			
		}
		catch (Exception e) {
			setErrorAtStartup(e);
			log.error(MarkerFactory.getMarker("FATAL"), "Failed to obtain JDBC connection", e);
		}
		
	}
	
	/**
	 * This method knows about all the filters that openmrs uses for setup. Currently those are the
	 * {@link InitializationFilter} and the {@link UpdateFilter}. If either of these have to do
	 * something, openmrs won't start in this Listener.
	 *
	 * @return true if one of the filters needs to take some action
	 */
	private boolean setupNeeded() throws Exception {
		if (!runtimePropertiesFound) {
			return true;
		}
		
		return DatabaseUpdater.updatesRequired() && !DatabaseUpdater.allowAutoUpdate();
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
			// load bundled modules that are packaged into the webapp
			Listener.loadBundledModules(servletContext);
			
			Context.startup(getRuntimeProperties());
		}
		catch (DatabaseUpdateException | InputRequiredException updateEx) {
			throw new ServletException("Should not be here because updates were run previously", updateEx);
		}
		catch (MandatoryModuleException mandatoryModEx) {
			throw new ServletException(mandatoryModEx);
		}
		catch (OpenmrsCoreModuleException coreModEx) {
			// don't wrap this error in a ServletException because we want to deal with it differently
			// in the StartupErrorFilter class
			throw coreModEx;
		}
		
		// TODO catch openmrs errors here and drop the user back out to the setup screen
		
		try {
			
			// web load modules
			Listener.performWebStartOfModules(servletContext);
			
			// start the scheduled tasks
			SchedulerUtil.startup(getRuntimeProperties());
		}
		catch (Exception t) {
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
		WebConstants.MODULE_REPOSITORY_URL = servletContext.getInitParameter("module.repository.url");
		
		if (!"openmrs".equalsIgnoreCase(WebConstants.WEBAPP_NAME)) {
			OpenmrsConstants.KEY_OPENMRS_APPLICATION_DATA_DIRECTORY = WebConstants.WEBAPP_NAME
			        + "_APPLICATION_DATA_DIRECTORY";
		}
	}
	
	private void setApplicationDataDirectory(ServletContext servletContext) {
		// note: the below value will be overridden after reading the runtime properties if the
		// "application_data_directory" runtime property is set
		String appDataDir = servletContext.getInitParameter("application.data.directory");
		if (StringUtils.hasLength(appDataDir)) {
			OpenmrsUtil.setApplicationDataDirectory(appDataDir);
		} else if (!"openmrs".equalsIgnoreCase(WebConstants.WEBAPP_NAME)) {
			OpenmrsUtil.setApplicationDataDirectory(
			    OpenmrsUtil.getApplicationDataDirectory() + File.separator + WebConstants.WEBAPP_NAME);
		}
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
			contextPath = servletContext.getContextPath();
		}
		catch (NoSuchMethodError ex) {
			// ServletContext.getContextPath() was added in version 2.5 of the Servlet API. Tomcat 5.5
			// has version 2.4 of the servlet API, so we fall back to the hacky code we were previously
			// using
			try {
				String path = servletContext.getResource("/").getPath();
				contextPath = path.substring(0, path.lastIndexOf("/"));
				contextPath = contextPath.substring(contextPath.lastIndexOf("/"));
			}
			catch (Exception e) {
				log.error("Failed to get construct context path", e);
			}
		}
		catch (Exception e) {
			log.error("Failed to get context path", e);
		}
		
		// trim off initial slash if it exists
		if (contextPath.contains("/")) {
			contextPath = contextPath.substring(1);
		}
		
		return contextPath;
	}
	
	/**
	 * Convenience method to empty out the dwr-modules.xml file to fix any errors that might have
	 * occurred in it when loading or unloading modules.
	 *
	 * @param servletContext
	 */
	private void clearDWRFile(ServletContext servletContext) {
		String realPath = servletContext.getRealPath("");
		String absPath = realPath + "/WEB-INF/dwr-modules.xml";
		File dwrFile = new File(absPath.replace("/", File.separator));
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			db.setEntityResolver((publicId, systemId) -> {
				// When asked to resolve external entities (such as a DTD) we return an InputSource
				// with no data at the end, causing the parser to ignore the DTD.
				return new InputSource(new StringReader(""));
			});
			Document doc = db.parse(dwrFile);
			Element elem = doc.getDocumentElement();
			elem.setTextContent("");
			OpenmrsUtil.saveDocument(doc, dwrFile);
		}
		catch (Exception e) {
			// got here because the dwr-modules.xml file is empty for some reason.  This might
			// happen because the servlet container (i.e. tomcat) crashes when first loading this file
			log.debug("Error clearing dwr-modules.xml", e);
			dwrFile.delete();
			OutputStreamWriter writer = null;
			try {
				writer = new OutputStreamWriter(new FileOutputStream(dwrFile), StandardCharsets.UTF_8);
				writer.write(
				    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!DOCTYPE dwr PUBLIC \"-//GetAhead Limited//DTD Direct Web Remoting 2.0//EN\" \"http://directwebremoting.org/schema/dwr20.dtd\">\n<dwr></dwr>");
			}
			catch (IOException io) {
				log.error(
				    "Unable to clear out the " + dwrFile.getAbsolutePath() + " file.  Please redeploy the openmrs war file",
				    io);
			}
			finally {
				if (writer != null) {
					try {
						writer.close();
					}
					catch (IOException io) {
						log.warn("Couldn't close Writer: " + io);
					}
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
		String realPath = servletContext.getRealPath("");
		// TODO centralize map to WebConstants?
		Map<String, String> custom = new HashMap<>();
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
		
		for (Map.Entry<String, String> entry : custom.entrySet()) {
			String prop = entry.getKey();
			String webappPath = entry.getValue();
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
						File[] files = file.listFiles();
						if (files != null) {
							for (File f : files) {
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
				if (inputStream != null) {
					inputStream.close();
				}
			}
			catch (IOException io) {
				log.warn("Unable to close input stream", io);
			}
			try {
				if (outputStream != null) {
					outputStream.close();
				}
			}
			catch (IOException io) {
				log.warn("Unable to close input stream", io);
			}
		}
		return true;
	}
	
	/**
	 * Load the pre-packaged modules from web/WEB-INF/bundledModules. <br>
	 * <br>
	 * This method assumes that the api startup() and WebModuleUtil.startup() will be called later
	 * for modules that loaded here
	 *
	 * @param servletContext the current servlet context for the webapp
	 */
	public static void loadBundledModules(ServletContext servletContext) {
		String path = servletContext.getRealPath("");
		path += File.separator + "WEB-INF" + File.separator + "bundledModules";
		File folder = new File(path);
		
		if (!folder.exists()) {
			log.warn("Bundled module folder doesn't exist: " + folder.getAbsolutePath());
			return;
		}
		if (!folder.isDirectory()) {
			log.warn("Bundled module folder isn't really a directory: " + folder.getAbsolutePath());
			return;
		}
		
		// loop over the modules and load the modules that we can
		File[] files = folder.listFiles();
		if (files != null) {
			for (File f : files) {
				if (!f.getName().startsWith(".")) { // ignore .svn folder and the like
					try {
						Module mod = ModuleFactory.loadModule(f);
						log.debug("Loaded bundled module: " + mod + " successfully");
					}
					catch (Exception e) {
						log.warn("Error while trying to load bundled module " + f.getName() + "", e);
					}
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
	@SuppressWarnings("squid:S1215")
	@Override
	public void contextDestroyed(ServletContextEvent event) {
		
		try {
			Context.openSession();
			
			Context.shutdown();
			
			WebModuleUtil.shutdownModules(event.getServletContext());
			
		}
		catch (Exception e) {
			// don't print the unhelpful "contextDAO is null" message
			if (!"contextDAO is null".equals(e.getMessage())) {
				// not using log.error here so it can be garbage collected
				System.out.println("Listener.contextDestroyed: Error while shutting down openmrs: ");
				log.error("Listener.contextDestroyed: Error while shutting down openmrs: ", e);
			}
		}
		finally {
			if ("true".equalsIgnoreCase(System.getProperty("FUNCTIONAL_TEST_MODE"))) {
				//Delete the temporary file created for functional testing and shutdown the mysql daemon
				String filename = WebConstants.WEBAPP_NAME + "-test-runtime.properties";
				File file = new File(OpenmrsUtil.getApplicationDataDirectory(), filename);
				System.out.println(filename + " delete=" + file.delete());
				//new com.mysql.management.MysqldResource(new File("../openmrs/target/database")).shutdown();
			}
			// remove the user context that we set earlier
			Context.closeSession();
		}
		
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
		catch (Exception e) {
			System.err.println("Listener.contextDestroyed: Failed to cleanup drivers in webapp");
			log.error("Listener.contextDestroyed: Failed to cleanup drivers in webapp", e);
		}
		
		MemoryLeakUtil.shutdownMysqlCancellationTimer();
		
		OpenmrsClassLoader.onShutdown();
		
		LogManager.shutdown();
		
		// just to make things nice and clean.
		// Suppressing sonar issue squid:S1215
		System.gc();
		System.gc();
	}
	
	/**
	 * Finds and loads the runtime properties
	 *
	 * @return Properties
	 * @see OpenmrsUtil#getRuntimeProperties(String)
	 */
	public static Properties getRuntimeProperties() {
		return OpenmrsUtil.getRuntimeProperties(WebConstants.WEBAPP_NAME);
	}
	
	/**
	 * Call WebModuleUtil.startModule on each started module
	 *
	 * @param servletContext
	 * @throws ModuleMustStartException if the context cannot restart due to a
	 *             {@link MandatoryModuleException} or {@link OpenmrsCoreModuleException}
	 */
	public static void performWebStartOfModules(ServletContext servletContext) throws ModuleMustStartException, Exception {
		List<Module> startedModules = new ArrayList<>(ModuleFactory.getStartedModules());
		performWebStartOfModules(startedModules, servletContext);
	}
	
	public static void performWebStartOfModules(Collection<Module> startedModules, ServletContext servletContext)
	        throws ModuleMustStartException, Exception {
		
		boolean someModuleNeedsARefresh = false;
		for (Module mod : startedModules) {
			try {
				boolean thisModuleCausesRefresh = WebModuleUtil.startModule(mod, servletContext,
				    /* delayContextRefresh */true);
				someModuleNeedsARefresh = someModuleNeedsARefresh || thisModuleCausesRefresh;
			}
			catch (Exception e) {
				mod.setStartupErrorMessage("Unable to start module", e);
			}
		}
		
		if (someModuleNeedsARefresh) {
			try {
				WebModuleUtil.refreshWAC(servletContext, true, null);
			}
			catch (ModuleMustStartException | BeanCreationException ex) {
				// pass this up to the calling method so that openmrs loading stops
				throw ex;
			}
			catch (Exception e) {
				Throwable rootCause = getActualRootCause(e, true);
				if (rootCause != null) {
					log.error(MarkerFactory.getMarker("FATAL"),
					    "Unable to refresh the spring application context.  Root Cause was:", rootCause);
				} else {
					log.error(MarkerFactory.getMarker("FATAL"),
					    "nable to refresh the spring application context. Unloading all modules,  Error was:", e);
				}
				
				try {
					WebModuleUtil.shutdownModules(servletContext);
					for (Module mod : ModuleFactory.getLoadedModules()) {// use loadedModules to avoid a concurrentmodificationexception
						if (!mod.isCoreModule() && !mod.isMandatory()) {
							try {
								ModuleFactory.stopModule(mod, true, true);
							}
							catch (Exception t3) {
								// just keep going if we get an error shutting down.  was probably caused by the module
								// that actually got us to this point!
								log.trace("Unable to shutdown module:" + mod, t3);
							}
						}
					}
					WebModuleUtil.refreshWAC(servletContext, true, null);
				}
				catch (MandatoryModuleException ex) {
					// pass this up to the calling method so that openmrs loading stops
					throw new MandatoryModuleException(ex.getModuleId(), "Got an error while starting a mandatory module: "
					        + e.getMessage() + ". Check the server logs for more information");
				}
				catch (Exception t2) {
					// a mandatory or core module is causing spring to fail to start up.  We don't want those
					// stopped so we must report this error to the higher authorities
					log.warn("caught another error: ", t2);
					throw t2;
				}
			}
		}
		
		// because we delayed the refresh, we need to load+start all servlets and filters now
		// (this is to protect servlets/filters that depend on their module's spring xml config being available)
		for (Module mod : ModuleFactory.getStartedModules()) {
			WebModuleUtil.loadServlets(mod, servletContext);
			WebModuleUtil.loadFilters(mod, servletContext);
		}
	}
	
	/**
	 * Convenience method that recursively attempts to pull the root case from a Throwable
	 *
	 * @param t the Throwable object
	 * @param isOriginalError specifies if the passed in Throwable is the original Exception that
	 *            was thrown
	 * @return the root cause if any was found
	 */
	private static Throwable getActualRootCause(Throwable t, boolean isOriginalError) {
		if (t.getCause() != null) {
			return getActualRootCause(t.getCause(), false);
		}
		
		if (!isOriginalError) {
			return t;
		}
		
		return null;
	}
	
}
