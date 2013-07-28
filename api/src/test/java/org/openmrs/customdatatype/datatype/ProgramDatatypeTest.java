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
 * Copyright (C) OpenMRS, LLC. All Rights Reserved.
 */
package org.openmrs.customdatatype.datatype;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Program;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;

/**
 * test class for the org.openmrs.customdatatype.datatype.ProgramDatatype
 */
public class ProgramDatatypeTest extends BaseContextSensitiveTest {
	
	ProgramDatatype datatype;
	
	@Before
	public void before() {
		datatype = new ProgramDatatype();
	}
	
	/**
	 * @see Program#serialize(Program)
	 * @verifies return a program uuid during serialization
	 */
	public void serialize_shouldReturnAProgramUuidDuringSerialization() throws Exception {
		Program program = new Program();
		Assert.assertEquals(program.getUuid(), datatype.serialize(program));
	}
	
	/**
	 * @see Program#deserialize(String)
	 * @verifies reconstruct a program serialized by this handler
	 */
	@Test
	public void deserialize_shouldReconstructAProgramSerializedByThisHandler() throws Exception {
		Program program = Context.getProgramWorkflowService().getProgramByUuid("9bc5693a-f558-40c9-8177-145a4b119ca7");
		Assert.assertEquals(program, datatype.deserialize(datatype.serialize(program)));
	}
	
}
