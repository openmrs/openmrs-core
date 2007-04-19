package org.openmrs.web.taglib.fieldgen;

import org.openmrs.web.taglib.FieldGenTag;



public class StringHandler extends AbstractFieldGenHandler implements FieldGenHandler {

	private String defaultUrl = "string.field";
	
	public void run() {
		setUrl(defaultUrl);
		checkEmptyVal((String)null);
		if (fieldGenTag != null) {
			Object initialValue = this.fieldGenTag.getVal();
			log.debug("Initialvalue: '" + initialValue + "'");
			setParameter("initialValue", initialValue == null ? "" : initialValue);

			String fieldLength = this.fieldGenTag.getParameterMap() != null ? (String)this.fieldGenTag.getParameterMap().get("fieldLength") : null;
			fieldLength = (fieldLength == null) ? FieldGenTag.DEFAULT_INPUT_TEXT_LENGTH : fieldLength;
			setParameter("fieldLength", fieldLength);
			
			String answerSet = this.fieldGenTag.getParameterMap() != null ? (String)this.fieldGenTag.getParameterMap().get("answerSet") : null;
			setParameter("answerSet", answerSet);
		}
	}
}
