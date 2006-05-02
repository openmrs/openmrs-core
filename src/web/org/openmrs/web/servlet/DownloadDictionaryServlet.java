package org.openmrs.web.servlet;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.ConceptSynonym;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.web.WebConstants;

public class DownloadDictionaryServlet extends HttpServlet {

	public static final long serialVersionUID = 1231231L;

	/**
	 * 
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		HttpSession httpSession = request.getSession();
		Context context = (Context)httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context == null) {
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "auth.session.expired");
			response.sendRedirect(request.getContextPath() + "/logout");
			return;
		}
		
		Locale locale = context.getLocale();
		
		ConceptService cs = context.getConceptService();
		String s = new SimpleDateFormat("dMy_Hm").format(new Date());

		response.setHeader("Content-Type", "text/csv");
		response.setHeader("Content-Disposition", "attachment; filename=conceptDictionary" + s + ".csv");
		
		for (Concept c : cs.getConcepts("conceptId", "asc")){
			
			String line = c.getConceptId()+ ",";
			String name, description;
			ConceptName cn = c.getName(locale);
			if (cn == null)	
				name = description = "";
			else {
				name = cn.getName();
				description = cn.getDescription();
			}
			line = line + '"' + name.replace("\"", "\"\"") + "\",";
			
			if (description == null) description = "";
			line = line + '"' + description.replace("\"", "\"\"") + "\",";
			
			String tmp = "";
			for (ConceptSynonym syn : c.getSynonyms()) {
				tmp += syn + "\n";
			}
			line = line + '"' + tmp.trim() + "\",";
			
			line = line + c.getRetired().toString();
			
			response.getOutputStream().println(line);
		}
	}
	
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}