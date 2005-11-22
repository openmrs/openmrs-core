package org.openmrs.web.dwr;

import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.api.db.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.web.Constants;

import uk.ltd.getahead.dwr.ExecutionContext;

public class DWRConceptService {

	protected final Log log = LogFactory.getLog(getClass());
	
	public Vector findConcepts(String phrase, List<String> classNames) {

		Vector conceptList = new Vector();

		Context context = (Context) ExecutionContext.get().getSession()
				.getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		try {
			ConceptService cs = context.getConceptService();
			List<Concept> concepts;
			
			if (phrase == null || phrase.equals("")) {
				//TODO get all concepts?
				concepts = new Vector();
			}
			else {
				concepts = cs.getConceptByName(phrase);
			}
			
			conceptList = new Vector(concepts.size());
			int maxCount = 10;
			int curCount = 0;
			if (classNames.size() > 0) {
				outer: for (Concept c : concepts) {
					inner: for (String o : classNames)
						if (o.equals(c.getConceptClass().getName())) {
							if ( curCount++ > maxCount ) {
								break outer;
							}
							conceptList.add(new ConceptListItem(c));
						}
				}
			}
			else {
				outer: for (Concept c : concepts) {
					if ( curCount++ > maxCount ) {
						break outer;
					}
					conceptList.add(new ConceptListItem(c));
				}
			}
			
		} catch (Exception e) {
			log.error(e);
			e.printStackTrace();
		}
		return conceptList;
	}
	
	public ConceptListItem getConcept(Integer conceptId) {
		Context context = (Context) ExecutionContext.get().getSession().getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		ConceptService cs = context.getConceptService();
		Concept c = cs.getConcept(conceptId);
		
		return new ConceptListItem(c);
	}

}
