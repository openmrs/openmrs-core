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
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public class CohortSummaryController implements Controller {

	public CohortSummaryController() { }
	
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		HttpSession httpSession = request.getSession();
		
		if (!Context.isAuthenticated()) {
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "auth.session.expired");
			return null;
		}
		
		PatientAnalysis analysis = Context.getPatientSetService().getMyPatientAnalysis();
		
		PatientSet ps = analysis.runFilters(Context.getPatientSetService().getAllPatients());

		ReportElement ageGenderGraph = new ReportElement();
		
		PatientDataProducer p = new PatientAttributeDataProducer("Patient", "gender");
		ageGenderGraph.addProducer("gender", p);
		p = new PatientAttributeDataProducer("Patient", "birthdate", new DataTransformer() {
				public Object transform(Object o) {
					return OpenmrsUtil.ageFromBirthdate((Date) o);
				}
			});
		ageGenderGraph.addProducer("age", p);
		
		NumericRangeColumnClassifier ageClassifier = new NumericRangeColumnClassifier("age", "Unknown age");
		ageClassifier.addCutoff(15, "Child");
		ageClassifier.addLastLabel("Adult");
		CompoundClassifier classifier = new CompoundClassifier(" + ");
		classifier.addClassifiers(new SimpleColumnClassifier("gender", "Unknown gender"), ageClassifier);
		TableGroupAndAggregate tga = new TableGroupAndAggregate(
				classifier,
				new CountAggregator(),
				"gender + age",
				"Cohort.count");
		ageGenderGraph.addGroupAndAggregate(tga);
		
		DataTable ageGenderTable = ageGenderGraph.run(ps);
		httpSession.setAttribute("ageGenderDataTable", ageGenderTable);
		
		ReportElement enrollmentGraph = new ReportElement();
		
		Program program = Context.getProgramWorkflowService().getProgram("HIV PROGRAM");
		p = new PatientProgramDataProducer(program, PatientProgramDataProducer.WhichField.ENROLLMENT_DATE);
		enrollmentGraph.addProducer("hiv_enrollment_date", p);
		DateColumnClassifier dateClassifier = new DateColumnClassifier("hiv_enrollment_date", DateColumnClassifier.CombineMethod.MONTH, "unknown hiv enrollment date");
		tga = new TableGroupAndAggregate(dateClassifier, new CountAggregator(), "hiv_enrollment_date", "count");
		enrollmentGraph.addGroupAndAggregate(tga);
		DataTable enrollmentTable = enrollmentGraph.run(ps);
		enrollmentTable.sortByColumn("hiv_enrollment_date");
		httpSession.setAttribute("hivEnrollmentDataTable", enrollmentTable);
		
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("patientSet", ps);
		model.put("ageGenderTable", ageGenderTable);
		model.put("hivEnrollmentTable", enrollmentTable);
		return new ModelAndView("/analysis/cohortSummary", "model", model);
	}

}
