package org.openmrs.module.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleFactory;
import org.openmrs.util.OpenmrsUtil;

public class ModuleResourcesServlet extends HttpServlet {

	private final String MODULE_PATH = "/WEB-INF/view/module/";
	
	private static final long serialVersionUID = 1239820102030344L;
	
	private Log log = LogFactory.getLog(this.getClass());
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.debug("In service method for module servlet: " + request.getPathInfo());
		
		String path = request.getPathInfo();
		Integer i = path.indexOf("/", 1);
		String moduleId = path.substring(1, i);
		
		log.debug("ModuleId: " + moduleId);
		
		Module mod = ModuleFactory.getModuleById(moduleId);
		
		if (mod == null) {
			log.warn("No module with id '" + moduleId + "' exists");
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		log.debug("nextslash: " + i + 1);
		String otherPath = path.substring(i);
		
		String realPath = getServletContext().getRealPath("");
		realPath = realPath + MODULE_PATH + moduleId + "/resources/" + otherPath;
		
		realPath = realPath.replace("/", File.separator);
		
		log.debug("Real path: " + realPath);
		
		File f = new File(realPath);
		if (!f.exists()) {
			log.warn("No object with path '" + realPath + "' exists for module with id '" + moduleId + "'");
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		InputStream is = new FileInputStream(f);
		OpenmrsUtil.copyFile(is, response.getOutputStream());
		
	}
	
}