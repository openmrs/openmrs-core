package org.openmrs.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.support.XmlWebApplicationContext;

public class OpenmrsXmlApplicationContext_old extends XmlWebApplicationContext {
	private Log log = LogFactory.getLog(getClass());
	
	@Override
	public void setClassLoader(ClassLoader cl) {
		//if (getParent() != null)
		//	((DefaultResourceLoader)getParent()).setClassLoader(cl);
		super.setClassLoader(cl);
		//Thread.currentThread().setContextClassLoader(cl);
		log.error("Setting classLoader: " + cl.getClass().getName());
	}
	
}
