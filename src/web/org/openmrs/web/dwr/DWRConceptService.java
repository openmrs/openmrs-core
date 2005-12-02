package org.openmrs.web.dwr;

import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.ConceptService;
import org.openmrs.web.Constants;

import uk.ltd.getahead.dwr.ExecutionContext;

public class DWRConceptService {

	protected final Log log = LogFactory.getLog(getClass());
	
	public Vector findConcepts(String phrase, List<String> classNames) {

		Vector objectList = new Vector();

		Context context = (Context) ExecutionContext.get().getSession()
				.getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context == null) {
			objectList.add("Your session has expired.");
			objectList.add("Please log in again.");
		}
		else {
			try {
				ConceptService cs = context.getConceptService();
				List<Concept> concepts = new Vector();
				
				if (phrase.matches("\\d+")) {
					// user searched on a number.  Insert concept with corresponding conceptId 
					Concept c = cs.getConcept(Integer.valueOf(phrase));
					if (c != null)
						concepts.add(c);
				}
				
				if (phrase == null || phrase.equals("")) {
					//TODO get all concepts for testing purposes?
				}
				else {
					//TODO change this search to concept word
					concepts.addAll(cs.getConceptByName(phrase));
				}

				if (concepts.size() == 0) {
					objectList.add("No matches found");
				}
				else {
					objectList = new Vector(concepts.size());
					int maxCount = 30;
					int curCount = 0;
					if (classNames.size() > 0) {
						outer: for (Concept c : concepts) {
							inner: for (String o : classNames)
								if (o.equals(c.getConceptClass().getName())) {
									if ( ++curCount > maxCount ) {
										break outer;
									}
									objectList.add(new ConceptListItem(c));
								}
						}
					}
					else {
						for (Concept c : concepts) {
							if ( ++curCount > maxCount ) {
								break;
							}
							objectList.add(new ConceptListItem(c));
						}
					}
				}
			} catch (Exception e) {
				log.error(e);
				e.printStackTrace();
				objectList.add("Error while attempting to find concepts");
			}
		}
		return objectList;
	}
	
	public ConceptListItem getConcept(Integer conceptId) {
		Context context = (Context) ExecutionContext.get().getSession().getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		ConceptService cs = context.getConceptService();
		Concept c = cs.getConcept(conceptId);
		
		if (c == null)
			return null;
		else
			return new ConceptListItem(c);
	}

}
