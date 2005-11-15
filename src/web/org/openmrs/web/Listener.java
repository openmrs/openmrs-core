package org.openmrs.web;

import java.beans.Introspector;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
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
        	try {
        		Enumeration drivers = DriverManager.getDrivers();
                while (drivers.hasMoreElements()) {
                    Driver o = (Driver) drivers.nextElement();
                    if(doesClassLoaderMatch(o)){
                        log.info("The current driver '" + o + "' is being deregistered.");
                        try {
                            DriverManager.deregisterDriver(o);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }else{
                        log.info("Driver '" + o + "' wasn't loaded by this webapp, so not touching it.");
                    }
                }
                
        		log.debug("flushing caches");
            	Introspector.flushCaches(); 

        	} catch (Throwable e) { 
        		log.error("Failed to cleanup ClassLoader for webapp"); 
        		e.printStackTrace(); 
        	} 
        	
        	log.debug("undeploying application : LogFactory() being destroyed");
        	//LogFactory.release(Thread.currentThread().getContextClassLoader());
        	LogFactory.releaseAll();
        }
        private boolean doesClassLoaderMatch(Driver o) {
        	return o.getClass().getClassLoader() == this.getClass().getClassLoader();
        }
        	
	}
    

