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
import org.openmrs.Concept;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

/**
 * test class for the org.openmrs.customdatatype.datatype.ConceptDatatype
 */
public class ConceptDatatypeTest extends BaseContextSensitiveTest {
	
	ConceptDatatype datatype;
	
	private String uuid = "32d3611a-6699-4d52-823f-b4b788bac3e3";
	
	@Before
	public void before() {
		datatype = new ConceptDatatype();
	}
	
	/**
	 * @see Concept#serialize(Concept)
	 */
	@Test
	@Verifies(value = "return a concept uuid during serialization", method = "serialize(Concept)")
	public void serialize_shouldReturnAConceptUuidDuringSerialization() throws Exception {
		Concept concept = new Concept();
		concept.setUuid(uuid);
		
		Assert.assertEquals(concept.getUuid(), datatype.serialize(concept));
	}
	
	/**
	 * @see Concept#deserialize(String)
	 */
	@Test
	@Verifies(value = "reconstruct a concept serialized by this handler", method = "deserialize(String)")
	public void deserialize_shouldReconstructAConceptSerializedByThisHandler() throws Exception {
		Concept concept = Context.getConceptService().getConceptByUuid(uuid);
		
		Assert.assertEquals(concept, datatype.deserialize(uuid));
		Assert.assertNotNull(concept);
	}
}
