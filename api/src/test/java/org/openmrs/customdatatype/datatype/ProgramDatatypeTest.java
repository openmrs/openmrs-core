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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Program;

public class ProgramDatatypeTest {
	
	ProgramDatatype datatype;
	
	Integer id = 1;
	
	@Before
	public void before() {
		datatype = new ProgramDatatype();
	}
	
	/**
	 * @see Program#deserialize(String)
	 * @verifies reconstruct a program serialized by this handler
	 */
	@Test
	public void deserialize_shouldReconstructASimplestProgramSerializedByThisHandler() throws Exception {
		Program program = new Program(id);
		Assert.assertEquals(program, datatype.deserialize(datatype.serialize(program)));
	}
	
	/**
	 * @see Program#serialize(java.util.Date)
	 * @verifies compare equals of source and reconstructed program
	 */
	@Test
	public void serialize_shouldBeEqualsSourceAndReconstructedLocation() throws Exception {
		Program program = new Program(id);
		program.setConcept(new Concept());
		program.setOutcomesConcept(new Concept());
		program.setAllWorkflows(null);
		
		Program deserializedLocation = datatype.deserialize(datatype.serialize(program));
		
		Assert.assertEquals(program.getId(), deserializedLocation.getId());
		Assert.assertEquals(program.getConcept(), deserializedLocation.getConcept());
		Assert.assertEquals(program.getOutcomesConcept(), deserializedLocation.getOutcomesConcept());
		Assert.assertEquals(program.getAllWorkflows(), deserializedLocation.getAllWorkflows());
	}
}
