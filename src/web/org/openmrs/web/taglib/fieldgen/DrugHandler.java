package org.openmrs.web.taglib.fieldgen;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.Drug;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;

public class DrugHandler extends AbstractFieldGenHandler implements FieldGenHandler {

	private String defaultUrl = "drug.field";
	
	public void run() {
		setUrl(defaultUrl);

		if ( fieldGenTag != null ) {
			String initialValue = "";
			checkEmptyVal((Drug)null);
			Drug d = (Drug)this.fieldGenTag.getVal();
			if ( d != null ) if ( d.getDrugId() != null ) initialValue = d.getDrugId().toString();
			String optionHeader = "";
			if ( this.fieldGenTag.getParameterMap() != null ) {
				optionHeader = (String) this.fieldGenTag.getParameterMap().get("optionHeader");
			}
			if ( optionHeader == null ) optionHeader = "";
			
			//HttpServletRequest request = (HttpServletRequest)this.fieldGenTag.getPageContext().getRequest();
			

			ConceptService cs = Context.getConceptService();
			List<Drug> drugs = cs.getDrugs();
			if ( drugs == null ) drugs = new ArrayList<Drug>();
			
			setParameter("initialValue", initialValue);
			setParameter("optionHeader", optionHeader);
			setParameter("drugs", drugs);
		}
	}
}
