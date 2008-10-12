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
package org.openmrs.test;

import java.util.Locale;

import org.openmrs.ConceptName;

/**
 * Behavior-driven tests of the ConceptName class.
 *
 */
public class ConceptNameTest {

	/**
	 * Convenient factory method to create a populated Concept.
     * 
     * @param i
     */
    public static ConceptName createMockConceptName(int conceptNameId, Locale locale) {
    	ConceptName mockConceptName = new ConceptName();
    	
    	mockConceptName.setConceptNameId(conceptNameId);
    	mockConceptName.setLocale(locale);
    	
    	return mockConceptName;
    }

}
