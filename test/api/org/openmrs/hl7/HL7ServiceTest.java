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
package org.openmrs.hl7;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

/**
 * Tests methods in the {@link HL7Service}
 */
public class HL7ServiceTest extends BaseContextSensitiveTest {
	
	/**
	 * @see {@link HL7Service#saveHL7InQueue(HL7InQueue)}
	 */
	@Test
	@Verifies(value = "should add generated uuid if uuid is null", method = "saveHL7InQueue(HL7InQueue)")
	public void saveHL7InQueue_shouldAddGeneratedUuidIfUuidIsNull() throws Exception {
		HL7InQueue hl7 = new HL7InQueue();
		
		hl7.setHL7Data("dummy data");
		hl7.setHL7Source(new HL7Source(1));
		hl7.setHL7SourceKey("a random key");
		hl7.setMessageState(HL7Constants.HL7_STATUS_PROCESSING);
		
		Context.getHL7Service().saveHL7InQueue(hl7);
		Assert.assertNotNull(hl7.getUuid());
	}
}
