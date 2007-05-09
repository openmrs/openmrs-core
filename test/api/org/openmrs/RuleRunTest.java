package org.openmrs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.logic.LogicException;
import org.openmrs.logic.LogicService;
import org.openmrs.logic.Result;
import org.openmrs.logic.rule.*;

public class RuleRunTest extends BaseTest {
	
	protected final Log log = LogFactory.getLog(getClass());
	String[] conceptList = new String[] { "VENOUS BLOOD LEAD - QUALITATIVE"
			
	};
	public void testClass() throws Exception {
		String [] ruleList = {
			"NAME_RULE",
			"LEAD RULE",
			"HI RISK LEAD RULE",
			"DIRECT TB CONTACT",
			"INDIRECT TB CONTACT",
			"DIRECT EXP HI RISK COUNTRY",
			"INDIRECT EXP HI RISK COUNTRY",
			
		};
		
		startup();
		Context.authenticate("vibha", "chicachica");
		LogicService logic = Context.getLogicService();
		registerRules(logic);
		registerConcepts(logic);

		Integer[] pidList = new Integer[] { 1 } ;
		try {
			for (Integer patientId : pidList) {
				Patient patient = Context.getPatientService().getPatient(patientId);
				for (String rule: ruleList) {
					Result result = logic.eval(patient, rule);
					System.out.println("\n" + result.toString());
				}
		//		Result result = logic.eval(patient, "NAME RULE");
		//		System.out.println("\n" + result.toString());
		//		result = logic.eval(patient, "LEAD RULE");
		//		System.out.println("\n\n" + result.toString());
			
		//		result = logic.eval(patient, "HI RISK LEAD RULE");
		//		System.out.println(result.toString());
			}
	
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
		
		
		shutdown();
	}
	private void registerRules(LogicService logic) throws LogicException {
		// Register rules
		logic.addToken("NAME RULE",NameRule.class);
		logic.addToken("LEAD RULE", leadspecpws.class);
		logic.addToken("HI RISK LEAD RULE", HiRiskLeadScreen.class);
		logic.addToken("DIRECT TB CONTACT", directtbcontact.class);
		logic.addToken("INDIRECT TB CONTACT", indirecttbcontact.class);
		logic.addToken("DIRECT EXP HI RISK COUNTRY", directexphiriskcountry.class);
		logic.addToken("DIRECT TB CONTACT", indirectexphiriskcountry.class);
		
 	
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