package org.openmrs.formentry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

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

		response.setHeader("Content-Type", "application/ms-infopath.xml");

		// servletPath() returns everything matched by servlet-mapping in
		// web.xml
		// with mapping of "*.xsn" and request of ...org/amrs/asdf/bob.xsn will
		// return /asdf/bob.xsn
		String filename = request.getServletPath();
		// get only the file name out of path
		filename = filename.substring(filename.lastIndexOf("/"));

		// append xsn storage location for file look up
		String url = FormEntryConstants.FORMENTRY_INFOPATH_OUTPUT_DIR;
		if (!url.endsWith(File.separator))
			url += File.separator;
		url = url + filename;

		try {
			FileInputStream formStream = new FileInputStream(url);
			OpenmrsUtil.copyFile(formStream, response.getOutputStream());
		}
		catch (FileNotFoundException e) {
			log
			    .error(
			        "The request for '"
			        	+ request.getServletPath()
			            + "' cannot be found.  More than likely the XSN has not been uploaded (via Upload XSN in form administration).",
			        e);
		}
	}

}
