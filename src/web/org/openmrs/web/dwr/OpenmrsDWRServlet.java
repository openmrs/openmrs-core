package org.openmrs.web.dwr;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.openmrs.module.web.WebModuleUtil;
import org.openmrs.util.OpenmrsClassLoader;

import uk.ltd.getahead.dwr.DWRServlet;

/**
 * Simply used so that we have a way we can restart the DWR HttpServlet
 * @author bwolfe
 */
public class OpenmrsDWRServlet extends DWRServlet {

	private static final long serialVersionUID = 121212111335789L;

	/**
	 * Overriding the init(ServletConfig) method to save the dwr servlet to the 
	 * ModuleWebUtil class
	 */
	public void init(ServletConfig config) throws ServletException {
		Thread.currentThread().setContextClassLoader(OpenmrsClassLoader.getInstance());
		super.init(config);
		WebModuleUtil.setDWRServlet(this);
	}
	
	/**
	 * This method is called to remake all of the dwr methods
	 * @throws ServletException
	 */
	public void reInitServlet() throws ServletException {
		init(this.getServletConfig());
	}
	
}
