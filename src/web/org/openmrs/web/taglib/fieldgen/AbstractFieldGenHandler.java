package org.openmrs.web.taglib.fieldgen;

import org.openmrs.web.taglib.FieldGenTag;

public abstract class AbstractFieldGenHandler {
	protected FieldGenTag fieldGenTag;
	
	public void setFieldGenTag(FieldGenTag fieldGenTag) {
		this.fieldGenTag = fieldGenTag;
	}
}
