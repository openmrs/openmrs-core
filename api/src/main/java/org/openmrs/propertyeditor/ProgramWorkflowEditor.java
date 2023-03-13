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
import org.openmrs.api.context.Context;

/**
 * Allows for serializing/deserializing an object to a string so that Spring knows how to pass an
 * object back and forth through an html form or other medium. <br>
 * <br>
 * In version 1.9, added ability for this to also retrieve objects by uuid
 * 
 * @see ProgramWorkflow
 */
public class ProgramWorkflowEditor extends OpenmrsPropertyEditor<ProgramWorkflow> {
	
	public ProgramWorkflowEditor() {
	}
	
	@Override
	protected ProgramWorkflow getObjectById(Integer id) {
		return Context.getProgramWorkflowService().getWorkflow(id);
	}
	
	@Override
	protected ProgramWorkflow getObjectByUuid(String uuid) {
		return Context.getProgramWorkflowService().getWorkflowByUuid(uuid);
	}
}
