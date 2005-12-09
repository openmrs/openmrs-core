package test;

import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;

import org.openmrs.reporting.*;
import org.openmrs.Patient;
import java.util.*;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.util.DoubleRange;
import org.openmrs.web.Constants;

public class DariusController implements Controller {
	
	protected Log log = LogFactory.getLog(getClass());

    public ModelAndView handleRequest(HttpServletRequest request,
    		HttpServletResponse response) throws ServletException, IOException {

		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		if (context == null) {
			httpSession.setAttribute(Constants.OPENMRS_ERROR_ATTR, "auth.session.expired");
			response.sendRedirect(request.getContextPath() + "/logout");
			return null;
		}
		
		org.openmrs.api.db.PatientService patientService = context.getPatientService();
		if (patientService == null) {
			log.warn("context.getPatientService() returned null. (context is " + context);
		}

		String gender = request.getParameter("gender");
		if (gender == null) {
			gender = "M";
		}
		
		Integer minAgeInt = null;
		Integer maxAgeInt = null;
		{
			String minAge = request.getParameter("minage");
			if (minAge != null) {
				minAgeInt = Integer.parseInt(minAge);
			}
			String maxAge = request.getParameter("maxage");
			if (maxAge != null) {
				maxAgeInt = Integer.parseInt(maxAge);
			}
		}
		
		Set<Patient> everyone = patientService.getPatientsByName("");
		
		PatientCharacteristicFilter filter = new PatientCharacteristicFilter();
		filter.setContext(context);
		filter.setGender(gender);
		filter.setMinAge(minAgeInt);
		filter.setMaxAge(maxAgeInt);
		
		DataSet<Patient> temp = new SimpleDataSet<Patient>(everyone);
		DataSet<Patient> results = filter.filter(temp); 
		
		AgeDataProducer ageSelector = new AgeDataProducer();
		ageSelector.produceData(results);
		
		DataGrouper<Patient, DoubleRange> nrpg = new NumericRangeGrouper<Patient>("age_in_years", "0,10,20,30,40,50", true);
		Map<DoubleRange, DataSet<Patient>> ageGroups = nrpg.groupDataSet(results);
		
		DataSetAggregator<Patient, DoubleRange, Integer> aggregator = new CountAggregator<Patient, DoubleRange>();
		Map<DoubleRange, Integer> ageCounts = aggregator.aggregateDataSets(ageGroups);
		
		FrequencyDistributionFormatterHTML formatter = new FrequencyDistributionFormatterHTML();
		String ageFrequencyDistribution = (String) formatter.format(ageCounts);
		
		Map<String, Object> myModel = new HashMap<String, Object>();
		myModel.put("all_patients", everyone);
		myModel.put("filtered_patients", results);
		myModel.put("filter_description", gender + " patients between the ages of " + minAgeInt + " and " + maxAgeInt);
		myModel.put("age_table", ((SimpleDataSet) results).toHtmlTable());
		myModel.put("age_frequency", ageFrequencyDistribution);
		return new ModelAndView("WEB-INF/view/darius.jsp", "model", myModel);
	}

}
