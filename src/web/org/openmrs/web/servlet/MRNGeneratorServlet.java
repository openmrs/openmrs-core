package org.openmrs.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.util.Helper;
import org.openmrs.web.WebConstants;

public class MRNGeneratorServlet extends HttpServlet {

	/**
	 * TODO Where to put this (mostly) AMRS-specific servlet ?
	 */
	
	
	public static final long serialVersionUID = 1231231L;
	
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		String site = request.getParameter("site");
		String first = request.getParameter("mrn_first");
		String count = request.getParameter("mrn_count");
		HttpSession session = request.getSession();
		Context context = (Context)session.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		if (site == null || first == null || count == null || context == null ||
				site.length()==0 || first.length()==0 || count.length()==0) {
			session.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "MRNGenerator.all.required");
			response.sendRedirect("admin/maintenance/mrnGenerator.htm");
			return;
		}
		
		Integer mrnFirst = Integer.valueOf(first);
		Integer mrnCount = Integer.valueOf(count);
		
		AdministrationService as = context.getAdministrationService();
		
		// log who generated this list
		as.mrnGeneratorLog(site, mrnFirst, mrnCount);
		
		String filename = site + "_" + mrnFirst + "-" + (mrnFirst + (mrnCount - 1)) + ".txt"; 
		
		response.setHeader("Content-Type", "text");
		response.setHeader("Content-Disposition", "attachment; filename=" + filename);
		
		Integer end = mrnCount + mrnFirst;
		while (mrnFirst < end) {
			
			String line = mrnFirst + site;
			int checkdigit;
			try {
				checkdigit = Helper.getCheckDigit(line);
			}
			catch (Exception e) {
				throw new ServletException(e);
			}
			line = line + "-" + checkdigit;  
			
			response.getOutputStream().println(line);
			
			mrnFirst++;
		}
	}
}