/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.customdatatype.datatype;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Provider;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

/**
 * Unit Tests for {@link ProviderDatatype}
 * 
 */
public class ProviderDatatypeTest extends BaseContextSensitiveTest {

	ProviderDatatype datatype;

	private String uuid = "c2299800-cca9-11e0-9572-0800200c9a66";

	@Before
	public void before() {
		datatype = new ProviderDatatype();
	}

	/**
	 * @see ProviderDatatype#deserialize(String)
	 */
	@Test
	@Verifies(value = "reconstruct a Provider(org.openmrs.Provider) serialized by this handler", method = "deserialize(String)")
	public void deserialize_shouldReconstructAProviderSerializedByThisHandler()
			throws Exception {
		// getting provider by its uuid
		Provider provider = Context.getProviderService()
				.getProviderByUuid(uuid);
		Assert.assertNotNull(provider);

		Assert.assertEquals(provider, datatype.deserialize(uuid));
	}

	/**
	 * @see ProviderDatatype#serialize(Provider)
	 */
	@Test
	@Verifies(value = "return a provider uuid during serialization", method = "serialize(Concept)")
	public void serialize_shouldReturnAProviderUuidDuringSerialization()
			throws Exception {
		Provider provider = new Provider();
		provider.setUuid(uuid);

		Assert.assertEquals(provider.getUuid(), datatype.serialize(provider));
	}
}
