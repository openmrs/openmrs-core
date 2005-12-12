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
import org.openmrs.api.context.Context;
import org.openmrs.api.ConceptService;
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
				Set<ConceptAnswer> newSets = new HashSet<ConceptAnswer>();
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
							newSets.add(origConceptAnswer);
							requestConceptIds.remove(x); //erasing concept id to shorten next for loop
						}
					}
				}
				
				//add all remaining parameter synonyms
				for (int x = 0; x < requestConceptIds.size(); x++) {
					Integer answerId = requestConceptIds.get(x);
					newSets.add(new ConceptAnswer(cs.getConcept(answerId)));
				}
				
				setValue(newSets);
			}
			else {
				setValue(null);
			}
		}
	}

}
