/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.propertyeditor;

import org.openmrs.ConceptClass;
import org.openmrs.api.ConceptService;
import org.springframework.beans.factory.annotation.Autowired;

public class ConceptClassEditorTest extends BasePropertyEditorTest<ConceptClass, ConceptClassEditor> {
	
	private static final Integer EXISTING_ID = 1;
	
	@Autowired
	private ConceptService conceptService;
	
	@Override
	protected ConceptClassEditor getNewEditor() {
		return new ConceptClassEditor();
	}
	
	@Override
	protected ConceptClass getExistingObject() {
		return conceptService.getConceptClass(EXISTING_ID);
	}
}
