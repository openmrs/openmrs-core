/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8;

import org.openmrs.Field;
import org.openmrs.FieldAnswer;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;

public class FieldAnswerResource1_8Test extends BaseDelegatingResourceTest<FieldAnswerResource1_8, FieldAnswer> {
	
	private String fieldAnswerUUID;
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest#newObject()
	 */
	@Override
	public FieldAnswer newObject() {
		Field field = Context.getFormService().getFieldByUuid(RestTestConstants1_8.FIELD_UUID);
		FieldAnswer fieldAnswer = new FieldAnswer();
		fieldAnswer.setConcept(Context.getConceptService().getConceptByUuid(RestTestConstants1_8.CONCEPT_UUID));
		field.addAnswer(fieldAnswer);
		
		fieldAnswerUUID = fieldAnswer.getUuid();
		
		return fieldAnswer;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest#getDisplayProperty()
	 */
	@Override
	public String getDisplayProperty() {
		return "Null Field - YES";
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest#getUuidProperty()
	 */
	@Override
	public String getUuidProperty() {
		return fieldAnswerUUID;
	}
	
}
