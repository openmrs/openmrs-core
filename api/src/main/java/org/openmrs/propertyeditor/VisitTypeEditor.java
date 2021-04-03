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

import java.beans.PropertyEditor;

import org.openmrs.VisitType;
import org.openmrs.api.context.Context;

/**
 * {@link PropertyEditor} for {@link VisitType}
 * 
 * @since 1.9
 */
public class VisitTypeEditor extends OpenmrsPropertyEditor<VisitType> {
	
	public VisitTypeEditor() {
	}
	
	@Override
	protected VisitType getObjectById(Integer id) {
		return Context.getVisitService().getVisitType(id);
	}
	
	@Override
	protected VisitType getObjectByUuid(String uuid) {
		return Context.getVisitService().getVisitTypeByUuid(uuid);
	}
}
