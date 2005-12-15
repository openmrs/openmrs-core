package org.openmrs.web.propertyeditor;

import java.beans.PropertyEditorSupport;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.ConceptAnswer;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

public class ConceptAnswersEditor extends PropertyEditorSupport {

	private Log log = LogFactory.getLog(this.getClass());
	
	private Context context;
	
	public ConceptAnswersEditor(Context c) {
		this.context = c;
	}
	
	public void setAsText(String text) throws IllegalArgumentException {
		
		if (context != null) {
			if (StringUtils.hasText(text)) {
				ConceptService cs = context.getConceptService();
				String[] conceptIds = text.split(" ");
				List<Integer> requestConceptIds = new Vector<Integer>();
				Set<ConceptAnswer> newAnswers = new HashSet<ConceptAnswer>();
				//set up parameter Synonym Set for easier add/delete functions
				// and removal of duplicates
				for (String id : conceptIds) {
					id = id.trim();
					if (!id.equals("") && !requestConceptIds.contains(Integer.valueOf(id))) //remove whitespace, blank lines, and duplicates
						requestConceptIds.add(Integer.valueOf(id));
				}
				
				// Union the original and request (submitted) sets to get the 'clean' sets
				Collection<ConceptAnswer> originalConceptAnswers = (Collection<ConceptAnswer>)getValue();
				for (ConceptAnswer origConceptAnswer : originalConceptAnswers) {
					for (int x = 0; x < requestConceptIds.size(); x++) {
						if (requestConceptIds.get(x).equals(origConceptAnswer.getAnswerConcept().getConceptId())) {
							newAnswers.add(origConceptAnswer);
							requestConceptIds.remove(x); //erasing concept id to shorten next for loop
						}
					}
				}
				
				log.debug("originalConceptAnswers: ");
				for (ConceptAnswer a : originalConceptAnswers)
					log.debug("id: " + a.getAnswerConcept().getConceptId());
				
				
				log.debug("requestConceptIds: ");
				for (Integer i : requestConceptIds)
					log.debug("id: " + i.toString());
				
				//add all remaining parameter answers
				for (int x = 0; x < requestConceptIds.size(); x++) {
					Integer answerId = requestConceptIds.get(x);
					newAnswers.add(new ConceptAnswer(cs.getConcept(answerId)));
				}
				
				log.debug("newAnswers: ");
				for (ConceptAnswer i : newAnswers)
					log.debug("id: " + i.getAnswerConcept().getConceptId());
				
				setValue(newAnswers);
			}
			else {
				setValue(null);
			}
		}
	}

}
