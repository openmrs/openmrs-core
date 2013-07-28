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

/**
 * test class for the org.openmrs.customdatatype.datatype.ProviderDatatype
 */
public class ProviderDatatypeTest extends BaseContextSensitiveTest {
	
	ProviderDatatype datatype;
	
	@Before
	public void before() {
		datatype = new ProviderDatatype();
	}
	
	/**
	 * @see Provider#deserialize(String)
	 * @verifies reconstruct a provider serialized by this handler
	 */
	@Test
	public void deserialize_shouldReconstructAProviderSerializedByThisHandler() throws Exception {
		Provider provider = Context.getProviderService().getProviderByUuid("9bc5693a-f558-40c9-8177-145a4b119ca7");
		Assert.assertEquals(provider, datatype.deserialize(datatype.serialize(provider)));
	}
	
	/**
	 * @see Provider#serialize(provider)
	 * @verifies return a Provider uuid during serialization
	 */
	public void serialize_shouldReturnAProviderUuidDuringSerialization() throws Exception {
		Provider provider = new Provider();
		Assert.assertEquals(provider.getUuid(), datatype.serialize(provider));
	}
	
}
