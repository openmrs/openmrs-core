package org.openmrs.web;

import java.beans.Introspector;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.hibernate.HibernateUtil;

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
        	log.error("undeploying application : LogFactory() being destroyed");
        	LogFactory.release(Thread.currentThread().getContextClassLoader());
        	log.error("undeploying application : closing session");
        	HibernateUtil.shutdown();
        	log.error("undeploying application : done");
        	
        	Introspector.flushCaches();
        }
    }

