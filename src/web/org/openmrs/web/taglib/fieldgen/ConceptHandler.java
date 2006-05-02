package org.openmrs.web.taglib.fieldgen;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.web.WebConstants;
import org.openmrs.web.taglib.FieldGenTag;

public class ConceptHandler extends AbstractFieldGenHandler implements FieldGenHandler {

	public String getOutput(String startingOutput) {
		String output = startingOutput;
		
		//System.out.println("\n\n\nNOW IN GETOUTPUT METHOD OF CONCEPTHANDLER\n\n\n");
		
		if ( fieldGenTag != null ) {
			String startVal = this.fieldGenTag.getStartVal();
			String fieldLength = this.fieldGenTag.getFieldLength();
			String formFieldName = this.fieldGenTag.getFormFieldName();
			
			String startId = "";
			if ( startVal != null ) {
				startId = startVal;
				HttpSession session = this.fieldGenTag.getPageContext().getSession();
				HttpServletRequest request = (HttpServletRequest)this.fieldGenTag.getPageContext().getRequest();
				Context context = (Context)session.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
				ConceptService cs = context.getConceptService();
				try { 
					Concept c = cs.getConcept(new Integer(startId));
					ConceptName cName = c.getName(request.getLocale());
					startVal = cName.getName();
				} catch (NumberFormatException nfe) {
					// TODO: should probably throw another exception here
					startId = "";
				}
				
			} else startVal = "";
			if ( fieldLength == null ) fieldLength = FieldGenTag.DEFAULT_CONCEPT_NAME_LENGTH;
			String formFieldTextName = "_" + formFieldName + "_text";
			output = "<input id=\"" + formFieldTextName + "\" type=\"text\" isContentEditable=\"false\" name=\"" + formFieldTextName + "\" value=\"" + startVal + "\" size=\"" + fieldLength + "\" onFocus=\"javascript:this.blur();popWindow('/openmrs/dictionary/popup/','conceptSearch',660,400,200,200);\" /> ";
			output += "<input id=\"" + formFieldName + "\" type=\"hidden\" name=\"" + formFieldName + "\" value=\"" + startId + "\" /> ";
			output += "<a href=\"javascript:popWindow('/openmrs/dictionary/popup/','conceptSearch',660,400,200,200);\"><img src=\"/openmrs/images/lookup.gif\" border=\"0\" /></a>";
		}
		
		return output;
	}
}
