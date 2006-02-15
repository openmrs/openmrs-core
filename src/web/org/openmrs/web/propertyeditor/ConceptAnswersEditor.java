package org.openmrs.web.propertyeditor;

import java.beans.PropertyEditorSupport;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
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
				List<String> requestConceptIds = new Vector<String>();
				Set<ConceptAnswer> newAnswers = new HashSet<ConceptAnswer>();
				//set up parameter answer Set for easier add/delete functions
				// and removal of duplicates
				for (String id : conceptIds) {
					id = id.trim();
					if (!id.equals("") && !requestConceptIds.contains(id)) //remove whitespace, blank lines, and duplicates
						requestConceptIds.add(id);
				}
				
				// Union the original and request (submitted) sets to get the 'clean' sets
				Collection<ConceptAnswer> originalConceptAnswers = (Collection<ConceptAnswer>)getValue();
				for (ConceptAnswer origConceptAnswer : originalConceptAnswers) {
					for (String conceptId : requestConceptIds) {
						Integer id = null;
						Integer drugId = null;
						if (conceptId.contains("^")) {
							id = Integer.valueOf(conceptId.substring(0, conceptId.indexOf("^")));
							drugId = Integer.valueOf(conceptId.substring(0, conceptId.indexOf("^")));
						}
						else {
							id = Integer.valueOf(conceptId);
						}
						if (conceptId.equals(origConceptAnswer.getAnswerConcept().getConceptId())) {
							if ((drugId == null && origConceptAnswer.getAnswerDrug() == null) ||
									drugId == origConceptAnswer.getAnswerDrug().getDrugId())
							newAnswers.add(origConceptAnswer);
							requestConceptIds.remove(conceptId); //erasing concept id to shorten next for loop
						}
					}
				}
				
				log.debug("originalConceptAnswers: ");
				for (ConceptAnswer a : originalConceptAnswers)
					log.debug("id: " + a.getAnswerConcept().getConceptId());
				
				
				log.debug("requestConceptIds: ");
				for (String i : requestConceptIds)
					log.debug("id: " + i);
				
				//add all remaining parameter answers
				for (String i : requestConceptIds) {
					Concept c = cs.getConcept(Integer.valueOf(i));
					ConceptAnswer ca = new ConceptAnswer(c);
					newAnswers.add(ca);
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
