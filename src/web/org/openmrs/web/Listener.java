package org.openmrs.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.db.hibernate.HibernateUtil;
import org.openmrs.formentry.FormEntryUtil;
import org.openmrs.scheduler.SchedulerUtil;
import org.openmrs.util.Helper;

public final class Listener implements ServletContextListener {

	/**
	 * 
	 * This method is called when the servlet context is initialized(when the
	 * Web Application is deployed). You can initialize servlet context related
	 * data here.
	 * 
	 * @param event
	 */
	public void contextInitialized(ServletContextEvent event) {
		Log log = LogFactory.getLog(this.getClass());

		try {
			FileInputStream propertyStream = null;

			// Look for environment variable {WEBAPP.NAME}_RUNTIME_PROPERTIES_FILE
			String webapp = WebConstants.OPENMRS_WEBAPP_NAME;
			webapp = webapp.toUpperCase();
			String env = webapp + "_RUNTIME_PROPERTIES_FILE";
			
			String filepath = System.getenv(env);

			if (filepath == null)
				throw new IOException("Env " + env + " not found");
			
			log.debug("Loading runtime properties from: " + filepath);
			propertyStream = new FileInputStream(filepath);

			Properties props = new Properties();
			props.load(propertyStream);

			propertyStream.close();

			// TODO Generify
			HibernateUtil.startup(props);

			// Loop over each "module" and startup each with the custom
			// properties
			Helper.startup(props);
			FormEntryUtil.startup(props);
			SchedulerUtil.startup(props);

			// Copy the customization scripts over into the webapp
			// TODO centralize map to OpenmrsConstants?
			Map<String, String> custom = new HashMap<String, String>();
			custom.put("custom.template.dir", "/WEB-INF/template");
			custom.put("custom.style.css.file", "/style.css");
			custom.put("custom.index.jsp.file", "/WEB-INF/view/index.jsp");
			custom.put("custom.images.dir", "/images");

			String realPath = event.getServletContext().getRealPath("");
			for (String prop : custom.keySet()) {
				String webappPath = custom.get(prop);
				String userOverridePath = props.getProperty(prop);
				// if they defined the variable
				if (userOverridePath != null) {
					String absolutePath = realPath + webappPath;
					File file = new File(userOverridePath);
					// if they got the path correct
					if (file.exists()) {
						log.debug("Overriding file: " + absolutePath);
						log.debug("Overriding file with: " + userOverridePath);
						if (file.isDirectory()) {
							for (File f : file.listFiles()) {
								userOverridePath = f.getAbsolutePath();
								String tmpAbsolutePath = absolutePath + "/"
										+ f.getName();
								FileInputStream inputStream = new FileInputStream(
										userOverridePath);
								FileOutputStream outputStream = new FileOutputStream(
										tmpAbsolutePath);
								Helper.copyFile(inputStream, outputStream);
							}
						} else {
							// file is not a directory
							FileInputStream inputStream = new FileInputStream(
									userOverridePath);
							FileOutputStream outputStream = new FileOutputStream(
									absolutePath);
							Helper.copyFile(inputStream, outputStream);
						}
					}
				}
			}

		} catch (IOException e) {
			log.warn("Unable to load properties file", e);

			// TODO generify this call once we have an application context
			// I'd prefer this not be a Hibernate-specific call.
			HibernateUtil.startup();

			// Loop over each "module" and startup each with default properties
			FormEntryUtil.startup();
			SchedulerUtil.startup();
		}

	}

	public void contextDestroyed(ServletContextEvent event) {

		// DriverManager cleanup code taken from
		// http://opensource2.atlassian.com/confluence/spring/pages/viewpage.action?pageId=2669
		try {
			Enumeration drivers = DriverManager.getDrivers();
			while (drivers.hasMoreElements()) {
				Driver o = (Driver) drivers.nextElement();
				if (doesClassLoaderMatch(o)) {
					// log.debug("The current driver '" + o + "' is being
					// deregistered.");
					try {
						DriverManager.deregisterDriver(o);
					} catch (SQLException e) {
						throw new RuntimeException(e);
					}
				} else {
					// log.info("Driver '" + o + "' wasn't loaded by this
					// webapp, so not touching it.");
				}
			}

			// log.debug("flushing caches");

		} catch (Throwable e) {
			// log.error("Failed to cleanup ClassLoader for webapp");
		}

		// log.debug("undeploying application : LogFactory() being destroyed");
		// LogFactory.release(Thread.currentThread().getContextClassLoader());
		LogFactory.releaseAll();

		// Needs to be shutdown before Hibernate
		SchedulerUtil.shutdown();
		
		// Introspector.flushCaches();
		HibernateUtil.shutdown();
	}

	private boolean doesClassLoaderMatch(Driver o) {
		return o.getClass().getClassLoader() == this.getClass()
				.getClassLoader();
	}

}