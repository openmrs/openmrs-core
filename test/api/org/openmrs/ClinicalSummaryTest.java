package org.openmrs;

import java.io.File;
import java.io.FileWriter;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.logic.LogicException;
import org.openmrs.logic.LogicService;
import org.openmrs.logic.Result;
import org.openmrs.logic.rule.BirthdateOrAgeRule;
import org.openmrs.logic.rule.CD4ReminderRule;
import org.openmrs.logic.rule.CXREverRule;
import org.openmrs.logic.rule.ClinicalSummaryRule;
import org.openmrs.logic.rule.FirstEncounterDate;
import org.openmrs.logic.rule.HealthCenterRule;
import org.openmrs.logic.rule.HelloWorldRule;
import org.openmrs.logic.rule.HospitalizedWithinPastYearRule;
import org.openmrs.logic.rule.NameRule;
import org.openmrs.logic.rule.PatientAlternateIdentifiersRule;
import org.openmrs.logic.rule.PatientIdentifierRule;
import org.openmrs.logic.rule.PerfectAdherenceRule;
import org.openmrs.logic.rule.ProblemListRule;

public class ClinicalSummaryTest extends BaseTest {

	protected final Log log = LogFactory.getLog(getClass());

	String[] conceptList = new String[] { "CIVIL STATUS", "PREGNANCY STATUS",
			"TOTAL NUMBER OF CHILDREN SIRED",
			"TOTAL CHILDREN UNDER 5YO LIVING IN HOME", "CURRENT WHO HIV STAGE",
			"REFERRALS ORDERED", "PATIENT HOSPITALIZED",
			"HOSPITALIZED SINCE LAST VISIT", "HOSPITALIZED PREVIOUS YEAR",
			"WEIGHT (KG)", "PROBLEM ADDED", "PROBLEM RESOLVED",
			"OVERALL DRUG ADHERENCE IN LAST MONTH",
			"ANTIRETROVIRAL ADHERENCE IN PAST WEEK", "HEMOGLOBIN",
			"BLOOD OXYGEN SATURATION", "CD4, BY FACS", "SERUM CREATININE",
			"SERUM GLUTAMIC-PYRUVIC TRANSAMINASE", "X-RAY, CHEST" };

	public void testLogic() throws Exception {
		startup();

		// Setup
		Context.authenticate("admin", "test");
		
		Integer[] pidList = new Integer[] { 4 };
		
//		Integer[] pidList = new Integer[] { 4, 11271, 18049, 9013, 27189,
//				12987, 1044, 26269, 3563, 610, 12286, 7670, 19255, 25797, 6316,
//				33330, 7618, 7132, 9755, 35520, 19363, 37440, 10340, 25202,
//				17238, 16958, 31883, 12417, 13412, 12066, 13283, 24697, 17286,
//				24961, 17781, 19221, 4732, 19149, 6444, 7070, 19412, 27361,
//				20776, 17089, 2405, 2079, 7533, 18333, 18336, 38724, 25373,
//				30244, 18048, 17966, 12338, 11917, 31473, 21801, 28020, 36821,
//				4606, 4891, 3775, 4331, 7711, 33293, 31275, 14912, 38835, 620,
//				23436 };
		
		LogicService logic = Context.getLogicService();
		registerRules(logic);
		registerConcepts(logic);

		long startTime = new Date().getTime();

		StringBuffer s = new StringBuffer();
		for (Integer patientId : pidList) {
			Patient patient = Context.getPatientService().getPatient(patientId);
			Result summary = logic.eval(patient, "CLINICAL SUMMARY");
			s.append(summary);
		}
		try {
			File f = new File("c:\\JoeSummaries.xml");
			FileWriter out = new FileWriter(f);
			IOUtils.write(s, out);
			out.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		

		long endTime = new Date().getTime();

		// Output results

		shutdown();
	}

	private void registerRules(LogicService logic) throws LogicException {
		// Register rules
		logic.addToken("HELLO WORLD", HelloWorldRule.class);
		logic.addToken("NAME", NameRule.class);
		logic.addToken("BIRTHDATE OR AGE", BirthdateOrAgeRule.class);
		logic.addToken("CLINICAL SUMMARY", ClinicalSummaryRule.class);
		logic.addToken("PATIENT IDENTIFIER", PatientIdentifierRule.class);
		logic.addToken("ALTERNATE PATIENT IDENTIFIERS",
				PatientAlternateIdentifiersRule.class);
		logic.addToken("HOSPITALIZED WITHIN PAST YEAR",
				HospitalizedWithinPastYearRule.class);
		logic.addToken("PROBLEM LIST", ProblemListRule.class);
		logic.addToken("PERFECT ADHERENCE", PerfectAdherenceRule.class);
		logic.addToken("HEALTH CENTER", HealthCenterRule.class);
		logic.addToken("CD4 COUNT WITHIN SIX MONTHS", CD4ReminderRule.class);
		logic.addToken("CHEST X-RAY EVER", CXREverRule.class);
		logic.addToken("FIRST ENCOUNTER DATE", FirstEncounterDate.class);
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
