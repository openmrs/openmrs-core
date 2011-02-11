/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.WebConstants;

public class MRNGeneratorServlet extends HttpServlet {
	
	/**
	 * TODO Where to put this (mostly) AMRS-specific servlet ?
	 */
	
	public static final long serialVersionUID = 1231231L;
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String site = request.getParameter("site");
		String prefix = request.getParameter("mrn_prefix");
		String first = request.getParameter("mrn_first");
		String count = request.getParameter("mrn_count");
		HttpSession session = request.getSession();
		
		if (prefix == null)
			prefix = "";
		
		if (site == null || first == null || count == null || site.length() == 0 || first.length() == 0
		        || count.length() == 0) {
			session.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "MRNGenerator.all.required");
			response.sendRedirect("admin/maintenance/mrnGenerator.htm");
			return;
		}
		
		Integer mrnFirst = Integer.valueOf(first);
		Integer mrnCount = Integer.valueOf(count);
		
		AdministrationService as = Context.getAdministrationService();
		
		// log who generated this list
		as.mrnGeneratorLog(site, mrnFirst, mrnCount);
		
		String filename = site + "_" + mrnFirst + "-" + (mrnFirst + (mrnCount - 1)) + prefix + ".txt";
		
		response.setHeader("Content-Type", "text");
		response.setHeader("Content-Disposition", "attachment; filename=" + filename);
		
		Integer end = mrnCount + mrnFirst;
		while (mrnFirst < end) {
			
			String line = prefix + mrnFirst + site;
			int checkdigit;
			try {
				checkdigit = OpenmrsUtil.getCheckDigit(line);
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
