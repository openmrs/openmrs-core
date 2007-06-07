package org.openmrs.web.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.reporting.ReportService;
import org.openmrs.reporting.export.DataExportReportObject;
import org.openmrs.reporting.export.DataExportUtil;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.WebConstants;

public class DataExportServlet extends HttpServlet {

	public static final long serialVersionUID = 1231222L;
	private static Log log = LogFactory.getLog(DataExportServlet.class);
	
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		String reportId = request.getParameter("dataExportId");
		HttpSession session = request.getSession();
		
		if (reportId == null || reportId.length()==0 ) {
			session.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "error.null");
			return;
		}
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS)) {
			session.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Privilege required: " + OpenmrsConstants.PRIV_VIEW_PATIENTS);
			session.setAttribute(WebConstants.OPENMRS_LOGIN_REDIRECT_HTTPSESSION_ATTR, request.getRequestURI() + "?" + request.getQueryString());
			response.sendRedirect(request.getContextPath() + "/login.htm");
			return;
		}
		
		ReportService rs = Context.getReportService();
		DataExportReportObject dataExport = (DataExportReportObject)rs.getReportObject(Integer.valueOf(reportId));
		
		File file = DataExportUtil.getGeneratedFile(dataExport);
		
		if (!file.exists())
			throw new ServletException("The data export: " + dataExport + " has not been generated yet");
		
		response.setContentType("application/vnd.ms-excel");
		String s = new SimpleDateFormat("yyyyMMdd_Hm").format(new Date(file.lastModified()));
		String filename = dataExport.getName().replace(" ", "_") + "-" + s + ".xls";
		response.setHeader("Content-Disposition", "attachment; filename=" + filename);
		response.setHeader("Pragma", "no-cache");
		
		InputStream inStream = null;
		try { 
			inStream = new FileInputStream(file);
			OpenmrsUtil.copyFile(inStream, response.getOutputStream());
		}
		finally {
			if (inStream != null)
				try { inStream.close(); } catch (Exception e) {}
		}
		
	}
	
}