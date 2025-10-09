/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openmrs.api.AdministrationService;
import org.openmrs.serialization.OpenmrsSerializer;
import org.openmrs.serialization.SerializationException;

/**
 * Tests the {@link SerializationServiceImpl} class
 */
public class SerializationServiceImplTest {
	
	private static List<? extends OpenmrsSerializer> currentSerializers;
	
	/**
	 * Store the current serializers that are on the serialization service that were put there by
	 * other tests
	 * 
	 * @see #restoreSerializers()
	 */
	@BeforeAll
	public static void clearSerializers() {
		SerializationServiceImpl ssi = new SerializationServiceImpl(mock(AdministrationService.class));
		currentSerializers = ssi.getSerializers();
		ssi.setSerializers(null); // clear out the current serializers
	}
	
	/**
	 * @see SerializationServiceImpl#setSerializers(List<OpenmrsSerializer>)
	 */
	@Test
	public void setSerializers_shouldNotResetSerializersListWhenCalledMultipleTimes() {
		SerializationServiceImpl ssi = new SerializationServiceImpl(mock(AdministrationService.class));
		assertEquals(0, ssi.getSerializers().size());
		
		ssi.setSerializers(Collections.singletonList(new MockSerializer1()));
		assertEquals(1, ssi.getSerializers().size());
		
		ssi.setSerializers(Collections.singletonList(new MockSerializer2()));
		assertEquals(2, ssi.getSerializers().size());
	}
	
	class MockSerializer1 implements OpenmrsSerializer {
		
		public MockSerializer1() {
		}
		
		/**
		 * @see org.openmrs.serialization.OpenmrsSerializer#deserialize(java.lang.String,
		 *      java.lang.Class)
		 */
		@Override
		public <T> T deserialize(String serializedObject, Class<? extends T> clazz) throws SerializationException {
			return null;
		}
		
		/**
		 * @see org.openmrs.serialization.OpenmrsSerializer#serialize(java.lang.Object)
		 */
		@Override
		public String serialize(Object o) throws SerializationException {
			return null;
		}
		
	}
	
	class MockSerializer2 implements OpenmrsSerializer {
		
		public MockSerializer2() {
		}
		
		/**
		 * @see org.openmrs.serialization.OpenmrsSerializer#deserialize(java.lang.String,
		 *      java.lang.Class)
		 */
		@Override
		public <T> T deserialize(String serializedObject, Class<? extends T> clazz) throws SerializationException {
			return null;
		}
		
		/**
		 * @see org.openmrs.serialization.OpenmrsSerializer#serialize(java.lang.Object)
		 */
		@Override
		public String serialize(Object o) throws SerializationException {
			return null;
		}
		
	}
	
	/**
	 * Clear out what we did in this class and restore the serializers that were on the
	 * {@link SerializationServiceImpl} class before we started
	 */
	@AfterAll
	public static void restoreSerializers() {
		SerializationServiceImpl ssi = new SerializationServiceImpl(mock(AdministrationService.class));
		ssi.setSerializers(null); // clear out our serializers
		ssi.setSerializers(currentSerializers); // reset the serializers that were here before this class
	}
}
