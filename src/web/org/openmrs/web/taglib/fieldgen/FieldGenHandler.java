package org.openmrs.web.taglib.fieldgen;

import org.openmrs.web.taglib.FieldGenTag;

public interface FieldGenHandler {
	public String getOutput(String startingOutput);
	public void setFieldGenTag(FieldGenTag fieldGenTag);
}
