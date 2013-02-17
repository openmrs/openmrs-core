/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.web.taglib.fieldgen;

import org.openmrs.web.taglib.FieldGenTag;

public class StringHandler extends AbstractFieldGenHandler implements FieldGenHandler {

    public static final String DEFAULT_TEXTAREA_ROWS = "1";
	
	public static final String DEFAULT_TEXTAREA_COLS = "40";
	
	@Override
    public void run() {
        String defaultUrl = "string.field";
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
