package org.openmrs.web.taglib.fieldgen;

import java.util.ArrayList;
import java.util.List;

public class DrugOrderDiscontinuedReasonHandler extends AbstractFieldGenHandler implements FieldGenHandler {

	private String defaultUrl = "reason.field";
	
	public void run() {
		setUrl(defaultUrl);

		if ( fieldGenTag != null ) {
			String initialValue = "";
			checkEmptyVal("");
			String optionHeader = "";
			if ( this.fieldGenTag.getParameterMap() != null ) {
				optionHeader = (String) this.fieldGenTag.getParameterMap().get("optionHeader");
			}
			if ( optionHeader == null ) optionHeader = "";

			List<String> reasons = new ArrayList<String>();
			reasons.add("DrugOrder.discontinue.reason.toxicity");
			reasons.add("DrugOrder.discontinue.reason.failure");
			reasons.add("DrugOrder.discontinue.reason.pregnancy");
			reasons.add("DrugOrder.discontinue.reason.interactionTB");
			reasons.add("DrugOrder.discontinue.reason.outOfStock");
			reasons.add("DrugOrder.discontinue.reason.patientRefusal");
			reasons.add("DrugOrder.discontinue.reason.other");
			
			setParameter("initialValue", initialValue);
			setParameter("optionHeader", optionHeader);
			setParameter("reasons", reasons);
		}
	}
}
