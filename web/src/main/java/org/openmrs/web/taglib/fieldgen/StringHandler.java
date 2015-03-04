/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.taglib.fieldgen;

import org.openmrs.web.taglib.FieldGenTag;

public class StringHandler extends AbstractFieldGenHandler implements FieldGenHandler {
	
	private String defaultUrl = "string.field";
	
	public static final String DEFAULT_TEXTAREA_ROWS = "1";
	
	public static final String DEFAULT_TEXTAREA_COLS = "40";
	
	public void run() {
		setUrl(defaultUrl);
		checkEmptyVal((String) null);
		if (fieldGenTag != null) {
			Object initialValue = this.fieldGenTag.getVal();
			log.debug("Initialvalue: '" + initialValue + "'");
			setParameter("initialValue", initialValue == null ? "" : initialValue);
			
			String fieldLength = this.fieldGenTag.getParameterMap() != null ? (String) this.fieldGenTag.getParameterMap()
			        .get("fieldLength") : null;
			fieldLength = (fieldLength == null) ? FieldGenTag.DEFAULT_INPUT_TEXT_LENGTH : fieldLength;
			setParameter("fieldLength", fieldLength);
			
			String answerSet = this.fieldGenTag.getParameterMap() != null ? (String) this.fieldGenTag.getParameterMap().get(
			    "answerSet") : null;
			setParameter("answerSet", answerSet);
			
			String inputType = this.fieldGenTag.getParameterMap() != null ? (String) this.fieldGenTag.getParameterMap().get(
			    "inputType") : null;
			inputType = (inputType == null) ? "text" : inputType;
			
			setParameter("inputType", inputType);
			
			String rows = this.fieldGenTag.getParameterMap() != null ? (String) this.fieldGenTag.getParameterMap().get(
			    "rows") : null;
			rows = (rows == null) ? DEFAULT_TEXTAREA_ROWS : rows;
			
			setParameter("rows", rows);
			
			String cols = this.fieldGenTag.getParameterMap() != null ? (String) this.fieldGenTag.getParameterMap().get(
			    "cols") : null;
			cols = (cols == null) ? DEFAULT_TEXTAREA_COLS : cols;
			
			setParameter("cols", cols);
		}
	}
}
