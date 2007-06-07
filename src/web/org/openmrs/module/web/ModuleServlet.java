package org.openmrs.module.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleFactory;

public class ModuleServlet extends HttpServlet {

	private static final long serialVersionUID = 1239820102030303L;
	private Log log = LogFactory.getLog(this.getClass());

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.debug("In service method for module servlet: " + request.getPathInfo());
		String moduleId = request.getPathInfo();
		int end = moduleId.indexOf("/", 1);
		if (end > 0)
			moduleId = moduleId.substring(1, end);
		
		log.debug("ModuleId: " + moduleId);
		
		Module mod = ModuleFactory.getModuleById(moduleId);
		
		if (mod == null) {
			log.warn("No module with id " + moduleId + " exists");
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		String servletName = request.getPathInfo();
		int start = moduleId.length() + 2;
		end = servletName.indexOf("/", start);
		if (end == -1 || end > servletName.length())
			end = servletName.length();
		servletName = servletName.substring(start, end);
		
		log.debug("Servlet name: " + servletName);
		
		HttpServlet servlet = WebModuleUtil.getServlet(mod, servletName);
		
		if (servlet == null) {
			log.warn("No servlet with name: " + servletName + " was found in module: " + moduleId);
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		servlet.service(request, response);
	}
	
}
