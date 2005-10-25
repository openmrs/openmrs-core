package org.openmrs.web.dwr;

import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PatientService;
import org.openmrs.context.Context;
import org.openmrs.web.Constants;

import uk.ltd.getahead.dwr.ExecutionContext;

public class DWRConceptService {

	protected final Log log = LogFactory.getLog(getClass());
	
	public Vector findConcepts(String phrase) {

		Vector conceptList = new Vector();

		Context context = (Context) ExecutionContext.get().getSession()
				.getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		try {
			ConceptService cs = context.getConceptService();
			List<Concept> concepts;
			
			if (phrase != null && !phrase.equals("")) {
				concepts = cs.getConceptByName(phrase);
			}
			else {
				//TODO get all concepts?
				concepts = new Vector();
			}
			
			conceptList = new Vector(concepts.size());
			for (Concept c : concepts) {
				conceptList.add(new ConceptListItem(c));
			}
		} catch (Exception e) {
			log.error(e);
			e.printStackTrace();
		}
		return conceptList;
	}
	
	public ConceptListItem getConcept(Integer patientId) {
		Context context = (Context) ExecutionContext.get().getSession().getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		ConceptService cs = context.getConceptService();
		Concept c = cs.getConcept(patientId);
		
		return new ConceptListItem(c);
	}

}
