package org.openmrs;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.logic.Aggregation;
import org.openmrs.logic.DateConstraint;
import org.openmrs.logic.Duration;
import org.openmrs.logic.LogicException;
import org.openmrs.logic.LogicService;
import org.openmrs.logic.Result;
import org.openmrs.logic.rule.BirthdateRule;
import org.openmrs.logic.rule.ClinicalSummaryRule;
import org.openmrs.logic.rule.HealthCenterRule;
import org.openmrs.logic.rule.HelloWorldRule;
import org.openmrs.logic.rule.HospitalizedWithinPastYearRule;
import org.openmrs.logic.rule.NameRule;
import org.openmrs.logic.rule.PatientAlternateIdentifiersRule;
import org.openmrs.logic.rule.PatientIdentifierRule;
import org.openmrs.logic.rule.PerfectAdherenceRule;
import org.openmrs.logic.rule.ProblemListRule;

public class LogicTest extends BaseTest {

	protected final Log log = LogFactory.getLog(getClass());

	String[] conceptList = new String[] { "CIVIL STATUS",
			"PREGNANCY STATUS", "TOTAL NUMBER OF CHILDREN SIRED",
			"TOTAL CHILDREN UNDER 5YO LIVING IN HOME",
			"CURRENT WHO HIV STAGE", "REFERRALS ORDERED",
			"PATIENT HOSPITALIZED", "HOSPITALIZED SINCE LAST VISIT",
			"HOSPITALIZED PREVIOUS YEAR", "WEIGHT (KG)", "PROBLEM ADDED",
			"PROBLEM RESOLVED", "OVERALL DRUG ADHERENCE IN LAST MONTH",
			"ANTIRETROVIRAL ADHERENCE IN PAST WEEK",
			"HEMOGLOBIN", "BLOOD OXYGEN SATURATION",
			"CD4, BY FACS", "SERUM CREATININE", "SERUM GLUTAMIC-PYRUVIC TRANSAMINASE",
			"X-RAY, CHEST"};

	public void testLogic() throws Exception {
		startup();

		// Setup
		Context.authenticate("paul", "xxxxxxxxx");
//		Patient patient = Context.getPatientService().getPatient(8637);
		Patient patient = Context.getPatientService().getPatient(21342);
		LogicService logic = Context.getLogicService();
		registerRules(logic);
		registerConcepts(logic);
		
		long startTime = new Date().getTime();

		// Use rules
		Result weight = logic.eval(patient, Aggregation.count(), "WEIGHT (KG)",
				DateConstraint.withinPreceding(Duration.days(60.0)), null);
		Result hello = logic.eval(patient, "HELLO WORLD");
		Result name = logic.eval(patient, "NAME");

		Result summary = logic.eval(patient, "CLINICAL SUMMARY");

		long endTime = new Date().getTime();

		// Output results
		System.out.println("Elapsed time: " + (endTime - startTime) + " ms");
		System.out.println(summary);

		shutdown();
	}

	private void registerRules(LogicService logic) throws LogicException {
		// Register rules
		logic.addToken("HELLO WORLD", HelloWorldRule.class);
		logic.addToken("NAME", NameRule.class);
		logic.addToken("BIRTHDATE", BirthdateRule.class);
		logic.addToken("CLINICAL SUMMARY", ClinicalSummaryRule.class);
		logic.addToken("PATIENT IDENTIFIER", PatientIdentifierRule.class);
		logic.addToken("ALTERNATE PATIENT IDENTIFIERS",
				PatientAlternateIdentifiersRule.class);
		logic.addToken("HOSPITALIZED WITHIN PAST YEAR", HospitalizedWithinPastYearRule.class);
		logic.addToken("PROBLEM LIST", ProblemListRule.class);
		logic.addToken("PERFECT ADHERENCE", PerfectAdherenceRule.class);
		logic.addToken("HEALTH CENTER", HealthCenterRule.class);
	}

	private void registerConcepts(LogicService logic) throws LogicException {
		// Register concepts
		ConceptService cs = Context.getConceptService();
		for (String conceptName : conceptList) {
			Concept concept = cs.getConceptByName(conceptName);
			if (concept != null)
				logic.addToken(conceptName, concept);
			else
				log.error("Missing concept: " + conceptName);
		}
	}

}
