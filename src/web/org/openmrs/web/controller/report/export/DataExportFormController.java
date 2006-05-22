package org.openmrs.web.controller.report.export;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.reporting.ReportService;
import org.openmrs.reporting.export.DataExportReportObject;
import org.openmrs.reporting.export.ExportColumn;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.WebConstants;
import org.openmrs.web.propertyeditor.LocationEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.RequestUtils;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class DataExportFormController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());
    
    SimpleDateFormat dateFormat;
    
    
	/**
	 * 
	 * Allows for Integers to be used as values in input tags.
	 *   Normally, only strings and lists are expected 
	 * 
	 * @see org.springframework.web.servlet.mvc.BaseCommandController#initBinder(javax.servlet.http.HttpServletRequest, org.springframework.web.bind.ServletRequestDataBinder)
	 */
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		super.initBinder(request, binder);
        Context context = (Context) request.getSession().getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		dateFormat = new SimpleDateFormat(OpenmrsConstants.OPENMRS_LOCALE_DATE_PATTERNS().get(context.getLocale().toString().toLowerCase()), context.getLocale());
        binder.registerCustomEditor(java.lang.Integer.class,
                new CustomNumberEditor(java.lang.Integer.class, true));
        binder.registerCustomEditor(org.openmrs.Location.class,
        		new LocationEditor(context));
	}

	/** 
	 * 
	 * The onSubmit function receives the form/command object that was modified
	 *   by the input form and saves it to the db
	 * 
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj, BindException errors) throws Exception {
		
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		String view = getFormView();
		
		if (context != null && context.isAuthenticated()) {
			DataExportReportObject report = (DataExportReportObject)obj;
			
			// create PatientSet from selected values in report
			String[] patientIds = request.getParameterValues("patientId");
			report.setPatientIds(new Vector<Integer>());
			if (patientIds != null)
				for (String patientId : patientIds)
					if (patientId != null && !patientId.equals("")) 
						report.addPatientId(Integer.valueOf(patientId));
			
			Integer location = RequestUtils.getIntParameter(request, "location", 0);
			if (location > 0)
				report.setLocation(context.getPatientService().getLocation(location));
			
			//String startDate = RequestUtils.getStringParameter(request, "startDate", "");
			//String endDate = RequestUtils.getStringParameter(request, "endDate", "");
			//if (!startDate.equals(""))
			//	report.setStartDate(dateFormat.parse(startDate));
			//if (!endDate.equals(""))
			//	report.setEndDate(dateFormat.parse(endDate));
		
			
			// define columns for report object
			String[] columnIds = request.getParameterValues("columnId");
			report.setColumns(new Vector<ExportColumn>());
			if (columnIds != null) {
				for (String columnId : columnIds) {
					String columnName = request.getParameter("simpleName_" + columnId);
					if (columnName != null)
						// simple column
						report.addSimpleColumn(columnName, request.getParameter("simpleValue_" + columnId));
					else {
						columnName = request.getParameter("conceptColumnName_" + columnId);
						if (columnName != null) {
							// concept column
							String modifier = request.getParameter("conceptModifier_" + columnId);
							String conceptName = request.getParameter("conceptName_" + columnId);
							String[] extras = request.getParameterValues("conceptExtra_" + columnId);
							report.addConceptColumn(columnName, modifier, conceptName, extras);
						}
						else {
							columnName = request.getParameter("calculatedName_" + columnId);
							if (columnName != null) {
								// calculated column
								String columnValue = request.getParameter("calculatedValue_" + columnId);
								report.addCalculatedColumn(columnName, columnValue);
							}
							else
								log.warn("Cannot determine column type for column: " + columnId);
						}
					}
				}
			}
			
			String saveAsNew = RequestUtils.getStringParameter(request, "saveAsNew", "");
			if (!saveAsNew.equals(""))
				report.setReportObjectId(null);
			
			context.getReportService().updateReportObject(report);
			
			String action = RequestUtils.getRequiredStringParameter(request, "action");
			MessageSourceAccessor msa = getMessageSourceAccessor();
			if (action.equals(msa.getMessage("DataExport.save"))) {
				view = getSuccessView();
				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "DataExport.saved");
			}
			else {
				view = request.getContextPath() + "/dataExportServlet?dataExportId=" + report.getReportObjectId();
			}
		}
		
		return new ModelAndView(new RedirectView(view));
	}

	/**
	 * 
	 * This is called prior to displaying a form for the first time.  It tells Spring
	 *   the form/command object to load into the request
	 * 
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
    protected Object formBackingObject(HttpServletRequest request) throws ServletException {

		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		DataExportReportObject report = null;
		
		if (context != null && context.isAuthenticated()) {
			ReportService rs = context.getReportService();
			String reportId = request.getParameter("dataExportId");
	    	if (reportId != null)
	    		report = (DataExportReportObject)rs.getReportObject(Integer.valueOf(reportId));	
		}
		
		if (report == null)
			report = new DataExportReportObject();
    	
        return report;
    }
    
	protected Map referenceData(HttpServletRequest request, Object obj, Errors errs) throws Exception {
		
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);

		Map<String, Object> map = new HashMap<String, Object>();
		String defaultVerbose = "false";
		
		if (context != null && context.isAuthenticated()) {
			defaultVerbose = context.getAuthenticatedUser().getProperty(OpenmrsConstants.USER_PROPERTY_SHOW_VERBOSE);
		}
		map.put("datePattern", dateFormat.toLocalizedPattern().toLowerCase());

		map.put("defaultVerbose", defaultVerbose.equals("true") ? true : false);
		
		return map;
	}
    
}