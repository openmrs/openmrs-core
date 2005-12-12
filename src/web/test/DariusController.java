package test;

import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;

import org.openmrs.reporting.*;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.reporting.Analysis;
import org.openmrs.reporting.ColumnSorter;
import org.openmrs.reporting.CountAggregator;
import org.openmrs.reporting.DataTable;
import org.openmrs.reporting.DataTableGrouper;
import org.openmrs.reporting.FrequencyDistributionFormatterHTML;
import org.openmrs.reporting.NumericPatientObservationFilter;
import org.openmrs.reporting.NumericRangeClassifier;
import org.openmrs.reporting.PatientCharacteristicFilter;
import org.openmrs.reporting.PatientListFormatterHTML;
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

		if (false) {
			ByteArrayOutputStream arr = new ByteArrayOutputStream();
			XMLEncoder enc = new XMLEncoder(new BufferedOutputStream(arr));
			enc.writeObject(ageAnalysis);
			enc.close();
			log.warn("ageAnalysisXml = " + arr.toString());
			XMLDecoder dec = new XMLDecoder(new BufferedInputStream(new ByteArrayInputStream(arr.toByteArray())));
			ageAnalysis = (Analysis) dec.readObject();
			dec.close();
		}
		
		Set<Patient> everyone = patientService.getPatientsByName("");
		DataTable ageTable = ageAnalysis.run(everyone);
		DataTable patientTable = patientsAnalysis.run(everyone);
		
		String ageFrequencyDistribution = new FrequencyDistributionFormatterHTML("total number").format(ageTable);
		String patientList = new PatientListFormatterHTML().format(patientTable);
		
		Map<String, Object> myModel = new HashMap<String, Object>();
		myModel.put("all_patients", everyone);
		myModel.put("filtered_patients", patientList);
		myModel.put("filter_description", gender + " patients between the ages of " + minAgeInt + " and " + maxAgeInt);
		myModel.put("age_frequency", ageFrequencyDistribution);
		return new ModelAndView("WEB-INF/view/darius.jsp", "model", myModel);
	}

}
