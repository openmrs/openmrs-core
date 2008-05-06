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

public class EncounterHandler extends AbstractFieldGenHandler implements FieldGenHandler {

	private String defaultUrl = "encounter.field";
	
	public void run() {
		setUrl(defaultUrl);
		/*
		String output = startingOutput;
		
		if ( fieldGenTag != null ) {
			String startVal = this.fieldGenTag.getStartVal();
			String formFieldName = this.fieldGenTag.getFormFieldName();
			
			output = "<input type=\"text\" size=\"12\" name=\"" + formFieldName + "\" value=\"" + startVal + "\" />";
			output += " (need a better widget than this - for now input Encounter id number)";
		}
		
		return output;
		*/
	}
}
