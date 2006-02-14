package org.openmrs.web.controller.analysis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.context.Context;
import org.openmrs.reporting.ObsListProducer;
import org.openmrs.reporting.PatientAnalysis;
import org.openmrs.reporting.PatientDataSet;
import org.openmrs.reporting.PatientDataSetFormatter;
import org.openmrs.reporting.PatientFilter;
import org.openmrs.reporting.PatientSet;
import org.openmrs.reporting.ReportService;
import org.openmrs.reporting.ShortDescriptionProducer;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.view.RedirectView;

public class OnTheFlyAnalysisController implements Controller {
	
	final static String ON_THE_FLY_ANALYSIS_ATTR = "__openmrs_on_the_fly_analysis";
	
	protected Log log = LogFactory.getLog(getClass());

	public ModelAndView addFilter(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context == null) {
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "auth.session.expired");
			response.sendRedirect(request.getContextPath() + "/logout");
			return null;
		}
		
		ReportService reportService = context.getReportService();
		
		PatientAnalysis analysis = (PatientAnalysis) httpSession.getAttribute(ON_THE_FLY_ANALYSIS_ATTR);
		if (analysis == null) {
			analysis = new PatientAnalysis();
			httpSession.setAttribute(ON_THE_FLY_ANALYSIS_ATTR, analysis);
		}
		String[] idsToAdd = request.getParameterValues("patient_filter_id");
		if (idsToAdd != null) {
			for (String patientFilterId : idsToAdd) {
				try {
					PatientFilter pf = reportService.getPatientFilterById(new Integer(patientFilterId));
					analysis.addFilter(pf);
				} catch (NumberFormatException ex) { }
			}
		}
		
		return new ModelAndView(new RedirectView("analysis.list"));
	}
	
	public ModelAndView removeFilter(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context == null) {
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "auth.session.expired");
			response.sendRedirect(request.getContextPath() + "/logout");
			return null;
		}
		
		PatientAnalysis analysis = (PatientAnalysis) httpSession.getAttribute(ON_THE_FLY_ANALYSIS_ATTR);
		if (analysis != null) {
			String indexToRemove = request.getParameter("patient_filter_index");
			if (indexToRemove != null) {
				try {
					analysis.removeFilter(Integer.parseInt(indexToRemove));
				} catch (NumberFormatException ex) { }
			}
		}
		
		return new ModelAndView(new RedirectView("analysis.list"));
	}
	
    public ModelAndView handleRequest(HttpServletRequest request,
    		HttpServletResponse response) throws ServletException, IOException {

		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		Locale locale = context.getLocale();
		
		if (context == null) {
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "auth.session.expired");
			response.sendRedirect(request.getContextPath() + "/logout");
			return null;
		}
		
		ReportService reportService = context.getReportService();
		PatientSetService patientSetService = context.getPatientSetService();
		
		PatientAnalysis analysis = (PatientAnalysis) httpSession.getAttribute(ON_THE_FLY_ANALYSIS_ATTR);
		if (analysis == null) {
			analysis = new PatientAnalysis();
		}
		
		String[] filterIds = request.getParameterValues("patient_filter_id");
		if (filterIds != null) {
			for (String filterId : filterIds) {
				try {
					analysis.addFilter(reportService.getPatientFilterById(new Integer(filterId.trim())));
				} catch (Exception ex) { }
			}
		}

		PatientSet everyone = patientSetService.getPatientsByCharacteristics(null, null, null);
		PatientSet result = analysis.runFilters(context, everyone);
		
		PatientDataSet pds = new PatientDataSet();
		ShortDescriptionProducer sdp = new ShortDescriptionProducer();
		pds.putDataSeries("description", sdp.produceData(context, result));
		
		Object resultsToDisplay = pds;
		Object xmlToDisplay = null;
		
		String viewMethod = request.getParameter("view");
		if ("cd4".equals(viewMethod)) {
			log.debug("preparing cd4 view");
			ObsListProducer olp = new ObsListProducer(context.getConceptService().getConcept(new Integer(5497)));
			pds.putDataSeries("cd4s", olp.produceData(context, result));
			PatientDataSetFormatter formatter = new ChronologicalObsFormatterHtml("cd4s");
			resultsToDisplay = formatter.format(pds, locale);
		} else if ("xml".equals(viewMethod)) {
			Set<Integer> temp = result.getPatientIds();
			if (temp.size() > 0) {
				Integer ptId = temp.iterator().next();
				log.debug("preparing xml view of patient " + ptId);
				xmlToDisplay = patientSetService.exportXml(ptId).replaceAll(">", ">\n");
			}
		}
		
		List<PatientFilter> filters = analysis.getPatientFilters();
		if (filters == null) {
			filters = new ArrayList<PatientFilter>();
		}
		
		List availableFilters = new ArrayList<PatientFilter>(reportService.getAllPatientFilters());
		for (PatientFilter pf : filters) {
			availableFilters.remove(pf);
		}		
		
		Map myModel = new HashMap();
		myModel.put("no_filters", new Boolean(filters.size() == 0));
		myModel.put("active_filters", filters);
		myModel.put("suggested_filters", availableFilters);
		myModel.put("number_of_results", new Integer(result.size()));
		myModel.put("analysis_results", resultsToDisplay);
		myModel.put("xml_debug", xmlToDisplay);

		return new ModelAndView("/analysis/on-the-fly-analysis", "model", myModel);
	}

}
