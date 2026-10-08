/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import java.util.Date;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the {@link BaseChangeableOpenmrsData} and {@link BaseChangeableOpenmrsMetadata} base
 * classes and verifies that change audit information is only available through the
 * {@link Changeable} contract.
 */
public class BaseChangeableOpenmrsTest {

	@Test
	public void baseChangeableOpenmrsData_shouldImplementChangeable() {
		assertTrue(Changeable.class.isAssignableFrom(BaseChangeableOpenmrsData.class));
	}

	@Test
	public void baseChangeableOpenmrsMetadata_shouldImplementChangeable() {
		assertTrue(Changeable.class.isAssignableFrom(BaseChangeableOpenmrsMetadata.class));
	}

	@Test
	public void baseOpenmrsData_shouldNotImplementChangeable() {
		assertFalse(Changeable.class.isAssignableFrom(BaseOpenmrsData.class));
	}

	@Test
	public void baseOpenmrsMetadata_shouldNotImplementChangeable() {
		assertFalse(Changeable.class.isAssignableFrom(BaseOpenmrsMetadata.class));
	}

	@Test
	public void baseChangeableOpenmrsData_shouldStoreChangedByAndDateChanged() {
		Person person = new Person();
		User changedBy = new User(1);
		Date dateChanged = new Date();

		person.setChangedBy(changedBy);
		person.setDateChanged(dateChanged);

		assertSame(changedBy, person.getChangedBy());
		assertSame(dateChanged, person.getDateChanged());
	}

	@Test
	public void baseChangeableOpenmrsMetadata_shouldStoreChangedByAndDateChanged() {
		Form form = new Form();
		User changedBy = new User(1);
		Date dateChanged = new Date();

		form.setChangedBy(changedBy);
		form.setDateChanged(dateChanged);

		assertSame(changedBy, form.getChangedBy());
		assertSame(dateChanged, form.getDateChanged());
	}

	@Test
	public void providerRole_shouldBeChangeable() {
		assertTrue(Changeable.class.isAssignableFrom(ProviderRole.class));
	}
}
