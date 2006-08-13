package org.openmrs.web.taglib.fieldgen;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Drug;
import org.openmrs.OrderType;
import org.openmrs.User;
import org.openmrs.api.ConceptService;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.web.WebConstants;
import org.openmrs.web.taglib.FieldGenTag;

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
			
			HttpSession session = this.fieldGenTag.getPageContext().getSession();
			//HttpServletRequest request = (HttpServletRequest)this.fieldGenTag.getPageContext().getRequest();
			Context context = (Context)session.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);

			ConceptService cs = context.getConceptService();
			List<Drug> drugs = cs.getDrugs();
			if ( drugs == null ) drugs = new ArrayList<Drug>();
			
			setParameter("initialValue", initialValue);
			setParameter("optionHeader", optionHeader);
			setParameter("drugs", drugs);
		}
	}
}
