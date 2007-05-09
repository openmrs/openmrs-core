package org.openmrs.web;

import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleFactory;
import org.openmrs.module.web.WebModuleUtil;
import org.openmrs.util.OpenmrsClassLoader;
import org.springframework.beans.BeansException;

/**
 * This class is only used to get access to the DispatcherServlet. After
 * creation, this obj is saved to WebUtil for later use.
 *  
 * When Spring's webApplicationContext is refreshed, the dispatcherservlet 
 * needs to be refreshed too.
 * 
 * @author bwolfe
 */
public class DispatcherServlet extends
		org.springframework.web.servlet.DispatcherServlet {

	private static final long serialVersionUID = -6925172744402818729L;

	private Log log = LogFactory.getLog(this.getClass());
	
	@Override
	protected void initFrameworkServlet() throws ServletException, BeansException {
		// refresh the application context to look for module xml config files as well
		
		//XmlWebApplicationContext wac = ((XmlWebApplicationContext)getWebApplicationContext());
		//Thread.currentThread().setContextClassLoader(OpenmrsClassLoader.getInstance());
		//wac.refresh();
		
		log.debug("Framework being initialized");
		WebModuleUtil.setDispatcherServlet(this);
		
		super.initFrameworkServlet();

		// the spring context gets reset by the framework servlet, so we need to 
		// reload the advice points that were lost when refreshing Spring
		for (Module module : ModuleFactory.getStartedModules()) {
			ModuleFactory.loadAdvice(module);
		}
		
	}
	
	public void reInitFrameworkServlet() throws ServletException {
		Thread.currentThread().setContextClassLoader(OpenmrsClassLoader.getInstance());

		// reset bean info and framework servlet
		init();
	}

	
	
}
