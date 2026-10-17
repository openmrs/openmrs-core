/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate;

import org.junit.jupiter.api.Test;
import org.openmrs.BaseOpenmrsMetadata;
import org.openmrs.OpenmrsObject;
import org.openmrs.api.db.SerializedObjectDAO;
import org.openmrs.serialization.OpenmrsSerializer;
import org.openmrs.serialization.SerializationException;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * This class tests the {@link SerializedObjectDAO} linked to from the Context. Currently, that file
 * is the {@link HibernateSerializedObjectDAO}.
 */
public class HibernateSerializedObjectDAOTest extends BaseContextSensitiveTest {

	@Autowired
	private HibernateSerializedObjectDAO dao;

	/**
	 * @see org.openmrs.api.db.SerializedObjectDAO#saveObject(OpenmrsObject, OpenmrsSerializer)
	 */
	@Test
	public void saveObject_shouldSetCreationAuditFieldsForCreatable() {
		TestMetadata metadata = new TestMetadata();
		metadata.setName("Test metadata");

		dao.registerSupportedType(TestMetadata.class);
		dao.saveObject(metadata, new StubSerializer());

		assertNotNull(metadata.getCreator());
		assertNotNull(metadata.getDateCreated());
	}

	/**
	 * A minimal metadata implementation that is Creatable through BaseOpenmrsMetadata but is not
	 * Changeable. This represents objects that only need creation audit information.
	 */
	private static class TestMetadata extends BaseOpenmrsMetadata {

		private Integer id;

		@Override
		public Integer getId() {
			return id;
		}

		@Override
		public void setId(Integer id) {
			this.id = id;
		}
	}

	/**
	 * A lightweight serializer stub that allows the DAO's saveObject overload accepting an
	 * OpenmrsSerializer to be tested without requiring the XStream serialization module.
	 */
	private static class StubSerializer implements OpenmrsSerializer {

		@Override
		public String serialize(Object object) throws SerializationException {
			return "serialized";
		}

		@Override
		public <T> T deserialize(String serializedObject, Class<? extends T> clazz) throws SerializationException {
			return null;
		}
	}
}
