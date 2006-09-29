package org.openmrs.web.taglib.fieldgen;

import org.openmrs.User;

public class ConceptHandler extends AbstractFieldGenHandler implements FieldGenHandler {

	private String defaultUrl = "concept.field";
	
	public void run() {
		htmlInclude("/scripts/dojoConfig.js");
		htmlInclude("/scripts/dojo/dojo.js");
		htmlInclude("/scripts/dojoUserSearchIncludes.js");
		
		setUrl(defaultUrl);
		checkEmptyVal((User)null);
		if (fieldGenTag != null) {
			Object initialValue = this.fieldGenTag.getVal();
			setParameter("initialValue", initialValue == null ? "" : initialValue.toString());
		}
	}
}
