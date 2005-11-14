package org.openmrs.web;

import java.beans.Introspector;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Enumeration;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

    public final class Listener implements
       ServletContextListener {
        public void contextInitialized(ServletContextEvent event) {

          /* This method is called when the servlet context is
             initialized(when the Web Application is deployed). 
             You can initialize servlet context related data here.
          */ 

        }

        public void contextDestroyed(ServletContextEvent event) {

        	Log log = LogFactory.getLog(getClass());
        	//log.error("undeploying application : closing session");
        	//HibernateUtil.shutdown();
        	log.error("flushing caches");
        	try { 
        		Introspector.flushCaches(); 
        		for (Enumeration e = DriverManager.getDrivers(); e.hasMoreElements();) { 
        			Driver driver = (Driver) e.nextElement(); 
        			if (driver.getClass().getClassLoader() == getClass().getClassLoader()) { 
        				DriverManager.deregisterDriver(driver);          
        			} 
        		} 
        	} catch (Throwable e) { 
        		System.err.println("Failed to cleanup ClassLoader for webapp"); 
        		e.printStackTrace(); 
        	} 
        	
        	log.error("undeploying application : LogFactory() being destroyed");
        	//LogFactory.release(Thread.currentThread().getContextClassLoader());
        	LogFactory.releaseAll();
        	
        	
        }
    }

