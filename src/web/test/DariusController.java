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
import org.openmrs.api.db.ConceptService;
import org.openmrs.api.db.PatientService;
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
		
		PatientService patientService = context.getPatientService();
		if (patientService == null) {
			log.warn("context.getPatientService() returned null. (context is " + context);
		}

		ConceptService conceptService = context.getConceptService();
		if (conceptService == null) {
			log.warn("context.getConceptService() returned null. (context is " + context);
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

		Analysis patientsAnalysis = new Analysis();
		Analysis ageAnalysis = new Analysis();

		{
			PatientCharacteristicFilter filter = new PatientCharacteristicFilter();
			filter.setContext(context);
			filter.setGender(gender);
			filter.setMinAge(minAgeInt);
			filter.setMaxAge(maxAgeInt);
			ageAnalysis.addFilter(filter);
			patientsAnalysis.addFilter(filter);
		}
		if (false) {
			NumericPatientObservationFilter filter2 = new NumericPatientObservationFilter(conceptService.getConcept(5497), NumericPatientObservationFilter.Modifier.LESS, new Integer(200), NumericPatientObservationFilter.Method.LAST);
			filter2.setContext(context);
			patientsAnalysis.addFilter(filter2);
		}
		ageAnalysis.addProducer(new AgeDataProducer());
		ageAnalysis.addGrouper(new DataTableGrouper(new NumericRangeClassifier("age_in_years", "0,10,20,30,40,50", true), "age range", new CountAggregator(), "total number"));
		ageAnalysis.setSorter(new ColumnSorter("age range"));
		
		Set<Patient> everyone = patientService.getPatientsByName("");
		DataTable ageTable = ageAnalysis.run(everyone);
		DataTable patientTable = patientsAnalysis.run(everyone);
		
		String ageFrequencyDistribution = new FrequencyDistributionFormatterHTML("total number").format(ageTable);
		//String ageFrequencyDistribution = "Not Yet Implemented";
		String patientList = new PatientListFormatterHTML().format(patientTable);
		
		Map<String, Object> myModel = new HashMap<String, Object>();
		myModel.put("all_patients", everyone);
		myModel.put("filtered_patients", patientList);
		myModel.put("filter_description", gender + " patients between the ages of " + minAgeInt + " and " + maxAgeInt);
		myModel.put("age_frequency", ageFrequencyDistribution);
		return new ModelAndView("WEB-INF/view/darius.jsp", "model", myModel);
	}

}
