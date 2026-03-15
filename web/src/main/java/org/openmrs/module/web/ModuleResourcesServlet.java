package org.openmrs.module.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.openmrs.module.Module;
import org.openmrs.module.ModuleUtil;
import org.openmrs.util.OpenmrsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModuleResourcesServlet extends HttpServlet {
	
	private static final String MODULE_PATH = "/WEB-INF/view/module/";
	
	private static final long serialVersionUID = 1239820102030344L;
	
	private static final Logger log = LoggerFactory.getLogger(ModuleResourcesServlet.class);
	
	@Override
	protected long getLastModified(HttpServletRequest req) {
		
		File f = getFile(req);
		
		if (f == null) {
			return super.getLastModified(req);
		}
		
		return f.lastModified();
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		
		log.debug("In service method for module servlet: {}", request.getPathInfo());
		
		File f = getFile(request);
		
		if (f == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		response.setDateHeader("Last-Modified", f.lastModified());
		response.setContentLengthLong(f.length());
		
		String mimeType = getServletContext().getMimeType(f.getName());
		response.setContentType(mimeType);
		
		try (FileInputStream is = new FileInputStream(f)) {
			OpenmrsUtil.copyFile(is, response.getOutputStream());
		}
	}
	
	protected File getFile(HttpServletRequest request) {
		
		String path = request.getPathInfo();
		
		Module module = ModuleUtil.getModuleForPath(path);
		
		if (module == null) {
			log.warn("No module handles the path: {}", path);
			return null;
		}
		
		String relativePath = ModuleUtil.getPathForResource(module, path);
		
		String basePath = getServletContext().getRealPath("")
			+ MODULE_PATH
			+ module.getModuleIdAsPath()
			+ "/resources";
		
		File devDir = ModuleUtil.getDevelopmentDirectory(module.getModuleId());
		
		if (devDir != null) {
			basePath = devDir.getAbsolutePath()
				+ "/omod/target/classes/web/module/resources";
		}
		
		try {
			
			Path baseDir = Paths.get(basePath).normalize();
			
			Path resolvedPath = baseDir.resolve(relativePath).normalize();
			
			if (!resolvedPath.startsWith(baseDir)) {
				log.warn("Possible path traversal attempt detected: {}", resolvedPath);
				return null;
			}
			
			File file = resolvedPath.toFile();
			
			if (!file.exists()) {
				log.warn("No file with path '{}' exists for module '{}'",
					resolvedPath, module.getModuleId());
				return null;
			}
			
			return file;
			
		} catch (Exception e) {
			
			log.error("Error resolving module resource path", e);
			return null;
			
		}
	}
}
