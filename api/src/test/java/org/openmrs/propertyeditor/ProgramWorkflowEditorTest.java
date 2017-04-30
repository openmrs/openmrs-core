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

import org.openmrs.ProgramWorkflow;
import org.openmrs.api.ProgramWorkflowService;
import org.springframework.beans.factory.annotation.Autowired;

public class ProgramWorkflowEditorTest extends BasePropertyEditorTest<ProgramWorkflow, ProgramWorkflowEditor> {
	
	private static final String EXISTING_UUID = "84f0effa-dd73-46cb-b931-7cd6be6c5f81";
	
	@Autowired
	ProgramWorkflowService programWorkflowService;
	
	@Override
	protected ProgramWorkflowEditor getNewEditor() {
		return new ProgramWorkflowEditor();
	}
	
	@Override
	protected ProgramWorkflow getExistingObject() {
		return programWorkflowService.getWorkflowByUuid(EXISTING_UUID);
	}
}
