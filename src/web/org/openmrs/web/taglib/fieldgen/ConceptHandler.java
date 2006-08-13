package org.openmrs.web.taglib.fieldgen;

import org.openmrs.Concept;
import org.openmrs.ConceptName;

public class ConceptHandler extends AbstractFieldGenHandler implements FieldGenHandler {

	private String defaultUrl = "concept.field";
	
	public void run() {
		setUrl(defaultUrl);

		htmlInclude("/scripts/dojoConfig.js");
		htmlInclude("/scripts/dojoConfig.js");
		
		if ( fieldGenTag != null ) {
			Concept c = (Concept)this.fieldGenTag.getVal();
			if ( c != null ) {
				ConceptName cName = c.getName(this.fieldGenTag.getPageContext().getRequest().getLocale());
				setParameter("startName", cName);
			} else setParameter("startName", "");
		}
	}
}
