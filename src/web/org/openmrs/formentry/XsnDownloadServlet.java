package org.openmrs.formentry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.WebConstants;

/**
 * Provides a servlet through which an XSN is downloaded. This class differs
 * from org.openmrs.formentry.FormDownloadServlet in that this class /will not/
 * modify the template or schema files inside of the xsn. This class simply
 * writes the named schema to the response
 * 
 * @author Ben Wolfe
 * @version 1.0
 */
public class XsnDownloadServlet extends HttpServlet {

	public static final long serialVersionUID = 123424L;

	private Log log = LogFactory.getLog(this.getClass());

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException {

		HttpSession httpSession = request.getSession();

		Context context = (Context) httpSession
		    .getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context == null) {
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "auth.session.expired");
			response.sendRedirect(request.getContextPath() + "/logout");
			return;
		}

		response.setHeader("Content-Type", "text/plain; charset=utf-8");
		

		// since we've got a "/formentry/form/*" servlet-mapping,
		// getServletPath() will only return /formentry/form.
		String filename = request.getRequestURI();
		// get only the file name out of path
		filename = filename.substring(filename.lastIndexOf("/") + 1);
		
		// append xsn storage location for file look up
		String url = context.getAdministrationService().getGlobalProperty("formentry.infopath_output_dir");
		if (!url.endsWith(File.separator))
			url += File.separator;
		url = url + filename;
		log.debug("url = " + url);

		try {
			File file = new File(url);
			Date modified = new Date(file.lastModified());
			
			log.debug("testing modified date: " + modified.toString());
			log.debug("testing etag: " + modified.getTime());
			
			// InfoPath checks one or both of these values to determine if it needs to 
			// update its internal/local cache
			response.setHeader("Last-Modified", modified.toString());
			response.setHeader("ETag", "" + modified.getTime());
			
			FileInputStream formStream = new FileInputStream(file);
			OpenmrsUtil.copyFile(formStream, response.getOutputStream());
		} 
		catch (FileNotFoundException e) {
			log
			    .error(
			        "The request for '"
			        	+ url
			            + "' cannot be found.  More than likely the XSN has not been uploaded (via Upload XSN in form administration).",
			        e);
			response.sendError(404);
		}
	}

}
