package org.openmrs.api.impl;


import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.serialization.OpenmrsSerializer;
import org.openmrs.serialization.SerializationException;
import org.openmrs.test.Verifies;

public class SerializationServiceImplTest {

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
		
		public MockSerializer1() { }

		/**
         * @see org.openmrs.serialization.OpenmrsSerializer#deserialize(java.lang.String, java.lang.Class)
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
		
		public MockSerializer2() { }

		/**
         * @see org.openmrs.serialization.OpenmrsSerializer#deserialize(java.lang.String, java.lang.Class)
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
}