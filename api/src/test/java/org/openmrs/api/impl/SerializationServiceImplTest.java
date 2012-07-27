/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.api.impl;

import java.util.Collections;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openmrs.serialization.OpenmrsSerializer;
import org.openmrs.serialization.SerializationException;
import org.openmrs.test.Verifies;

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
	@BeforeClass
	public static void clearSerializers() {
		SerializationServiceImpl ssi = new SerializationServiceImpl();
		currentSerializers = ssi.getSerializers();
		ssi.setSerializers(null); // clear out the current serializers
	}
	
	/**
	 * @see {@link SerializationServiceImpl#setSerializers(List<OpenmrsSerializer>)}
	 */
	@Test
	@Verifies(value = "should not reset serializers list when called multiple times", method = "setSerializers(List<+QOpenmrsSerializer;>)")
	public void setSerializers_shouldNotResetSerializersListWhenCalledMultipleTimes() throws Exception {
		SerializationServiceImpl ssi = new SerializationServiceImpl();
		Assert.assertEquals(0, ssi.getSerializers().size());
		
		ssi.setSerializers(Collections.singletonList(new MockSerializer1()));
		Assert.assertEquals(1, ssi.getSerializers().size());
		
		ssi.setSerializers(Collections.singletonList(new MockSerializer2()));
		Assert.assertEquals(2, ssi.getSerializers().size());
	}
	
	class MockSerializer1 implements OpenmrsSerializer {
		
		public MockSerializer1() {
		}
		
		/**
		 * @see org.openmrs.serialization.OpenmrsSerializer#deserialize(java.lang.String,
		 *      java.lang.Class)
		 */
		public <T> T deserialize(String serializedObject, Class<? extends T> clazz) throws SerializationException {
			return null;
		}
		
		/**
		 * @see org.openmrs.serialization.OpenmrsSerializer#serialize(java.lang.Object)
		 */
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
		public <T> T deserialize(String serializedObject, Class<? extends T> clazz) throws SerializationException {
			return null;
		}
		
		/**
		 * @see org.openmrs.serialization.OpenmrsSerializer#serialize(java.lang.Object)
		 */
		public String serialize(Object o) throws SerializationException {
			return null;
		}
		
	}
	
	/**
	 * Clear out what we did in this class and restore the serializers that were on the
	 * {@link SerializationServiceImpl} class before we started
	 */
	@AfterClass
	public static void restoreSerializers() {
		SerializationServiceImpl ssi = new SerializationServiceImpl();
		ssi.setSerializers(null); // clear out our serializers
		ssi.setSerializers(currentSerializers); // reset the serializers that were here before this class
	}
}
