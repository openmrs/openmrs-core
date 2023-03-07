/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs1_8;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;

import org.openmrs.FieldType;
import org.openmrs.api.context.Context;
import org.openmrs.api.FormService;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.apache.commons.beanutils.PropertyUtils;

/**
 * Tests functionality of {@link FieldTypeController}.
 */
public class FieldTypeController1_8Test extends MainResourceControllerTest {

	private FormService service;

	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getURI()
	 */
	@Override
	public String getURI() {
		return "fieldtype";
	}

	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getUuid()
	 */
	@Override
	public String getUuid() {
		return RestTestConstants1_8.FIELD_TYPE_UUID;
	}

	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getAllCount()
	 */
	@Override
	public long getAllCount() {
		return 2;
	}

	@Before
	public void before() {
		this.service = Context.getFormService();
	}

	@Test
	public void shouldUnRetireAFieldType() throws Exception {
		FieldType fieldType = service.getFieldTypeByUuid(getUuid());
		fieldType.setRetired(true);
		fieldType.setRetireReason("random reason");
		service.saveFieldType(fieldType);
		fieldType = service.getFieldTypeByUuid(getUuid());
		assertTrue(fieldType.isRetired());

		String json = "{\"deleted\": \"false\"}";
		SimpleObject response = deserialize(handle(newPostRequest(getURI() + "/" + getUuid(), json)));

		fieldType = service.getFieldTypeByUuid(getUuid());
		assertFalse(fieldType.isRetired());
		assertEquals("false", PropertyUtils.getProperty(response, "retired").toString());

	}

	@Test
	public void shouldCreateNewFieldType() throws Exception {
		int countBefore = service.getAllFieldTypes().size();

		String json = "{\"name\": \"test11\",\"description\": \"test\",\"isSet\": false}";
		SimpleObject response = deserialize(handle(newPostRequest(getURI(), json)));

		String uuid = response.get("uuid");
		FieldType createdFieldType = service.getFieldTypeByUuid(uuid);

		assertNotNull(createdFieldType);
		assertEquals(countBefore + 1, service.getAllFieldTypes().size());
		assertEquals("test11", createdFieldType.getName());
		assertEquals("test", createdFieldType.getDescription());
		assertEquals(false, createdFieldType.getIsSet());
	}

	@Test
	public void shouldUpdateFieldType() throws Exception {
		FieldType existingFieldType = new FieldType();
		existingFieldType.setName("field type");
		existingFieldType.setDescription("desc");
		existingFieldType.setIsSet(false);
		service.saveFieldType(existingFieldType);

		String json = "{\"name\": \"field type\",\"description\": \"desc\",\"isSet\": true}";
		handle(newPostRequest(getURI() + "/" + existingFieldType.getUuid(), json));
		FieldType updatedFieldType = service.getFieldTypeByUuid(existingFieldType.getUuid());

		assertNotNull(updatedFieldType);
		assertEquals("field type", updatedFieldType.getName());
		assertEquals("desc", updatedFieldType.getDescription());
		assertEquals(true, updatedFieldType.getIsSet());
	}

	@Test
	public void shouldPurgeFieldType() throws Exception {
		FieldType existingFieldType = new FieldType();
		existingFieldType.setName("field type");
		existingFieldType.setDescription("desc");
		existingFieldType.setIsSet(false);
		service.saveFieldType(existingFieldType);

		int countBefore = service.getAllFieldTypes().size();
		handle(newDeleteRequest(getURI() + "/" + existingFieldType.getUuid(), new Parameter("purge", "true")));

		FieldType deletedFieldType = service.getFieldTypeByUuid(existingFieldType.getUuid());
		assertNull(deletedFieldType);
		assertEquals(countBefore - 1, service.getAllFieldTypes().size());
	}
}
