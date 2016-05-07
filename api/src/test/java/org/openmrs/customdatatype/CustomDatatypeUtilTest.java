/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.customdatatype;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;

public class CustomDatatypeUtilTest extends BaseContextSensitiveTest {
	
	/**
	 * @see CustomDatatypeUtil#deserializeSimpleConfiguration(String)
	 * @verifies deserialize a configuration serialized by the corresponding serialize method
	 */
	@Test
	public void deserializeSimpleConfiguration_shouldDeserializeAConfigurationSerializedByTheCorrespondingSerializeMethod()
	        throws Exception {
		Map<String, String> config = new HashMap<String, String>();
		config.put("one property", "one value");
		config.put("another property", "another value < with > strange&nbsp;characters");
		
		String serialized = CustomDatatypeUtil.serializeSimpleConfiguration(config);
		Map<String, String> deserialized = CustomDatatypeUtil.deserializeSimpleConfiguration(serialized);
		Assert.assertEquals(2, deserialized.size());
		Assert.assertEquals("one value", deserialized.get("one property"));
		Assert.assertEquals("another value < with > strange&nbsp;characters", deserialized.get("another property"));
	}
	/**
	 * @see CustomDatatypeUtil#serializeSimpleConfiguration(String)
	 * @verifies serialize a deserialized null
	 */

	@Test
	public void serializeSimpleConfiguration_shouldSerializeaDeserializedNullWithoutException() 
		throws Exception {
		String serialized = CustomDatatypeUtil.serializeSimpleConfiguration(null);
		Map<String, String> deserialized = CustomDatatypeUtil.deserializeSimpleConfiguration(serialized);
		Assert.assertEquals(0, deserialized.size());
                Assert.assertEquals(null, deserialized.get(""));
	}
        /**
         * @see CustomDatatypeUtil#getDatatypeClassnames()
         * @see CustomDatatypeUtil#getHandlerClassnames()
         * @verifies length of datatype and handlers, tests get methods
         */

	@Test
        public void getDatatypeHandlerClassnames_shouldGetClassnames()
                throws Exception {
		Assert.assertEquals(10,CustomDatatypeUtil.getDatatypeClassnames().size());
		Assert.assertEquals(2,CustomDatatypeUtil.getHandlerClassnames().size());
        }
        /**
         * @see CustomDatatypeUtil#getDatatype(String,String)
         * @verifies null strings for getDatatype
         */

	@Test(expected = CustomDatatypeException.class)
        public void getDatatype_shouldThrowCustomDatatypeException()
                throws Exception {
		CustomDatatypeUtil.getDatatype(null,null);
	}
        /**
         * @see CustomDatatypeUtil#getDatatypeOrDefault(CustomValueDescriptor)
         * not sure if this is a bug or a feature
         * throws null pointer exception
         */

	@Test(expected = NullPointerException.class)
        public void getDatatypeOrDefault_shouldThrowNullPointer()
                throws Exception {
                CustomDatatypeUtil.getDatatypeOrDefault(null);
        }

	/**
         * @see CustomDatatypeUtil#getDatatype(CustomValueDescriptor)
         * not sure if this is a bug or a feature
         * throws null pointer exception
         */

        @Test(expected = NullPointerException.class)
        public void getDatatype_shouldThrowNullPointer()
                throws Exception {
                CustomDatatypeUtil.getDatatype(null);
        }

	/**
         * @see CustomDatatypeUtil#getHandler(CustomValueDescriptor)
         * not sure if this is a bug or a feature
         * throws null pointer exception
         */

        @Test(expected = NullPointerException.class)
        public void getHandler_shouldThrowNullPointer()
                throws Exception {
                CustomDatatypeUtil.getHandler(null);
        }
        
}
