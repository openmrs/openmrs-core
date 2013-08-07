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
package org.openmrs.customdatatype.datatype;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Provider;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

/**
 * test class for the org.openmrs.customdatatype.datatype.ProviderDatatype
 */
public class ProviderDatatypeTest extends BaseContextSensitiveTest {
	
	ProviderDatatype datatype;
	
	private String uuid = "c2299800-cca9-11e0-9572-0800200c9a66";
	
	@Before
	public void before() {
		datatype = new ProviderDatatype();
	}
	
	/**
	 * @see Provider#deserialize(String)
	 */
	@Test
	@Verifies(value = "reconstruct a provider serialized by this handler", method = "deserialize(String)")
	public void deserialize_shouldReconstructAProviderSerializedByThisHandler() throws Exception {
		//getting provider by its uuid
		Provider provider = Context.getProviderService().getProviderByUuid(uuid);
		Assert.assertNotNull(provider);
		
		Assert.assertEquals(provider, datatype.deserialize(uuid));
	}
	
	/**
	 * @see Provider#serialize(provider)
	 */
	@Test
	@Verifies(value = "return a concept uuid during serialization", method = "serialize(Concept)")
	public void serialize_shouldReturnAProviderUuidDuringSerialization() throws Exception {
		Provider provider = new Provider();
		provider.setUuid(uuid);
		
		Assert.assertEquals(provider.getUuid(), datatype.serialize(provider));
	}
}
