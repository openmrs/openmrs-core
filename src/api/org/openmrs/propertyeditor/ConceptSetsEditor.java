package org.openmrs.propertyeditor;

import java.beans.PropertyEditorSupport;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.ConceptSet;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

public class ConceptSetsEditor extends PropertyEditorSupport {

	private Log log = LogFactory.getLog(this.getClass());
	
	public ConceptSetsEditor() { }
	
	@SuppressWarnings("unchecked")
	public void setAsText(String text) throws IllegalArgumentException {
		log.debug("setting text: " + text);
		if (StringUtils.hasText(text)) {
			ConceptService cs = Context.getConceptService();
			String[] conceptIds = text.split(" ");
			List<Integer> requestConceptIds = new Vector<Integer>();
			Set<ConceptSet> newSets = new HashSet<ConceptSet>();
			//set up parameter Synonym Set for easier add/delete functions
			// and removal of duplicates
			for (String id : conceptIds) {
				id = id.trim();
				if (!id.equals("") && !requestConceptIds.contains(Integer.valueOf(id))) //remove whitespace, blank lines, and duplicate entries
					requestConceptIds.add(Integer.valueOf(id));
			}
			
			// Union the original and request (submitted) sets to get the 'clean' sets
			//   marks request as seen with Integer(-1) instead of removing to retain order
			Collection<ConceptSet> originalConceptSets = (Collection<ConceptSet>)getValue();
			for (ConceptSet origConceptSet : originalConceptSets) {
				for (int x = 0; x < requestConceptIds.size(); x++) {
					if (requestConceptIds.get(x).equals(origConceptSet.getConcept().getConceptId())) {
						origConceptSet.setSortWeight(Double.valueOf(x));
						newSets.add(origConceptSet);
						requestConceptIds.set(x, new Integer(-1)); //'erasing' concept id in order to keep list size/sort intact
					}
				}
			}
			
			//add all remaining parameter synonyms
			for (int x = 0; x < requestConceptIds.size(); x++) {
				Integer conceptId = requestConceptIds.get(x);
				if (!conceptId.equals(new Integer(-1)))
					newSets.add(new ConceptSet(cs.getConcept(conceptId), Double.valueOf(x)));
			}
			
			setValue(newSets);
		}
		else {
			setValue(null);
		}
	}

}
