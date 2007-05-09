package org.openmrs.web.taglib.fieldgen;

import org.openmrs.web.taglib.FieldGenTag;

public interface FieldGenHandler {
	public void run();
	public void setFieldGenTag(FieldGenTag fieldGenTag);
}
