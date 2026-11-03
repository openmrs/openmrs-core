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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests methods on the {@link Relationship} class
 */
public class RelationshipTest {

	/**
	 * @see Relationship#copy()
	 */
	@Test
	public void copy_shouldCopyTheStartAndEndDates() {
		Relationship relationship = new Relationship(new Person(1), new Person(2), new RelationshipType(1));
		Date startDate = new Date(1000000000000L);
		Date endDate = new Date(1100000000000L);
		relationship.setStartDate(startDate);
		relationship.setEndDate(endDate);

		Relationship copy = relationship.copy();

		assertEquals(startDate, copy.getStartDate());
		assertEquals(endDate, copy.getEndDate());
	}

	/**
	 * @see Relationship#copy()
	 */
	@Test
	public void copy_shouldNotCopyTheRelationshipId() {
		Relationship relationship = new Relationship(1);

		assertNull(relationship.copy().getRelationshipId());
	}
}
