package org.openmrs.web;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.LogFactory;
import org.openmrs.api.db.hibernate.HibernateUtil;

public final class Listener implements ServletContextListener {
	public void contextInitialized(ServletContextEvent event) {

		/*
		 * This method is called when the servlet context is initialized(when
		 * the Web Application is deployed). You can initialize servlet context
		 * related data here.
		 */
		
		// TODO generify this call once we have an application context
		// I'd prefer this not be a Hibernate-specific call.  
		HibernateUtil.startup();

	}

	public void contextDestroyed(ServletContextEvent event) {

		// DriverManager cleanup code taken from 
		// http://opensource2.atlassian.com/confluence/spring/pages/viewpage.action?pageId=2669
		try {
			Enumeration drivers = DriverManager.getDrivers();
			while (drivers.hasMoreElements()) {
				Driver o = (Driver) drivers.nextElement();
				if (doesClassLoaderMatch(o)) {
					//log.debug("The current driver '" + o + "' is being deregistered.");
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
		//LogFactory.release(Thread.currentThread().getContextClassLoader());
		LogFactory.releaseAll();
		
		//Introspector.flushCaches();
		HibernateUtil.shutdown();
	}

	private boolean doesClassLoaderMatch(Driver o) {
		return o.getClass().getClassLoader() == this.getClass()
				.getClassLoader();
	}

}