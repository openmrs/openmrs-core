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
import org.openmrs.test.Verifies;

/**
 * test class for the org.openmrs.customdatatype.datatype.ProgramDatatype
 */
public class ProgramDatatypeTest extends BaseContextSensitiveTest {
	
	ProgramDatatype datatype;
	
	private String uuid = "da4a0391-ba62-4fad-ad66-1e3722d16380";
	
	@Before
	public void before() {
		datatype = new ProgramDatatype();
	}
	
	/**
	 * @see Program#serialize(Program)
	 */
	@Test
	@Verifies(value = "return a program uuid during serialization", method = "serialize(Program)")
	public void serialize_shouldReturnAProgramUuidDuringSerialization() throws Exception {
		Program program = new Program();
		program.setUuid(uuid);
		
		Assert.assertEquals(program.getUuid(), datatype.serialize(program));
	}
	
	/**
	 * @see Program#deserialize(String)
	 */
	@Test
	@Verifies(value = "reconstruct a program serialized by this handler", method = "deserialize(String)")
	public void deserialize_shouldReconstructAProgramSerializedByThisHandler() throws Exception {
		Program program = Context.getProgramWorkflowService().getProgramByUuid(uuid);
		Assert.assertNotNull(program);
		
		Assert.assertEquals(program, datatype.deserialize(uuid));
	}
}
