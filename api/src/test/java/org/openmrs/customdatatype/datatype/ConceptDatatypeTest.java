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

/**
 * test class for the org.openmrs.customdatatype.datatype.ConceptDatatype
 */
public class ConceptDatatypeTest extends BaseContextSensitiveTest {
	
	ConceptDatatype datatype;
	
	@Before
	public void before() {
		datatype = new ConceptDatatype();
	}
	
	/**
	 * @see Concept#serialize(Concept)
	 * @verifies return a concept uuid during serialization
	 */
	public void serialize_shouldReturnAConceptUuidDuringSerialization() throws Exception {
		Concept concept = new Concept();
		Assert.assertEquals(concept.getUuid(), datatype.serialize(concept));
	}
	
	/**
	 * @see Concept#deserialize(String)
	 * @verifies reconstruct a concept serialized by this handler
	 */
	@Test
	public void deserialize_shouldReconstructAConceptSerializedByThisHandler() throws Exception {
		String uuid = "9bc5693a-f558-40c9-8177-145a4b119ca7";
		Concept concept = Context.getConceptService().getConceptByUuid(uuid);
		Assert.assertEquals(concept, datatype.deserialize(datatype.serialize(concept)));
	}
	
}
