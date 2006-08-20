package org.openmrs.web.controller.analysis;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openmrs.Program;
import org.openmrs.api.context.Context;
import org.openmrs.reporting.CompoundClassifier;
import org.openmrs.reporting.CountAggregator;
import org.openmrs.reporting.DataTable;
import org.openmrs.reporting.DataTransformer;
import org.openmrs.reporting.DateColumnClassifier;
import org.openmrs.reporting.NumericRangeColumnClassifier;
import org.openmrs.reporting.PatientAnalysis;
import org.openmrs.reporting.PatientAttributeDataProducer;
import org.openmrs.reporting.PatientDataProducer;
import org.openmrs.reporting.PatientProgramDataProducer;
import org.openmrs.reporting.PatientSet;
import org.openmrs.reporting.ReportElement;
import org.openmrs.reporting.SimpleColumnClassifier;
import org.openmrs.reporting.TableGroupAndAggregate;
import org.openmrs.util.Helper;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public class CohortSummaryController implements Controller {

	public CohortSummaryController() { }
	
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		if (context == null || !context.isAuthenticated()) {
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "auth.session.expired");
			return null;
		}
		
		PatientAnalysis analysis = context.getPatientSetService().getMyPatientAnalysis();
		
		PatientSet ps = analysis.runFilters(context, context.getPatientSetService().getAllPatients());

		ReportElement ageGenderGraph = new ReportElement();
		
		PatientDataProducer p = new PatientAttributeDataProducer("Patient", "gender");
		ageGenderGraph.addProducer("gender", p);
		p = new PatientAttributeDataProducer("Patient", "birthdate", new DataTransformer() {
				public Object transform(Object o) {
					return Helper.ageFromBirthdate((Date) o);
				}
			});
		ageGenderGraph.addProducer("age", p);
		
		NumericRangeColumnClassifier ageClassifier = new NumericRangeColumnClassifier("age", "general.unknown");
		ageClassifier.addCutoff(15, "child");
		ageClassifier.addLastLabel("adult");
		CompoundClassifier classifier = new CompoundClassifier(" + ");
		classifier.addClassifiers(new SimpleColumnClassifier("gender", "general.unknown"), ageClassifier);
		TableGroupAndAggregate tga = new TableGroupAndAggregate(
				classifier,
				new CountAggregator(),
				"gender + age",
				"Cohort.count");
		ageGenderGraph.addGroupAndAggregate(tga);
		
		DataTable ageGenderTable = ageGenderGraph.run(context, ps);
		
		ReportElement enrollmentGraph = new ReportElement();
		
		Program program = context.getProgramWorkflowService().getProgram("IMB HIV PROGRAM");
		p = new PatientProgramDataProducer(program, PatientProgramDataProducer.WhichField.ENROLLMENT_DATE);
		enrollmentGraph.addProducer("hiv_enrollment_date", p);
		DateColumnClassifier dateClassifier = new DateColumnClassifier("hiv_enrollment_date", DateColumnClassifier.CombineMethod.MONTH, "unknown hiv enrollment date");
		tga = new TableGroupAndAggregate(dateClassifier, new CountAggregator(), "hiv_enrollment_date", "count");
		enrollmentGraph.addGroupAndAggregate(tga);
		DataTable enrollmentTable = enrollmentGraph.run(context, ps);
		enrollmentTable.sortByColumn("hiv_enrollment_date");
		
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("patientSet", ps);
		model.put("ageGenderTable", ageGenderTable);
		model.put("hivEnrollmentTable", enrollmentTable);
		return new ModelAndView("/analysis/cohortSummary", "model", model);
	}

}
