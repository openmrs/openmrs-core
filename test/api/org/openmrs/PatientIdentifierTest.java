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
package org.openmrs;


import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.Verifies;

/**
 * Tests methods on the {@link PatientIdentifier} class (without using the Context)
 */
public class PatientIdentifierTest {
	
	/**
	 * @see {@link PatientIdentifier#equals(Object)}
	 * 
	 */
	@Test
	@Verifies(value = "should compare when patient and identifier and type is null", method = "equals(Object)")
	public void equals_shouldCompareWhenPatientAndIdentifierAndTypeIsNull()
			throws Exception {
		
		Patient patient = new Patient();
		
		PatientIdentifier second = new PatientIdentifier();
		second.setIdentifier("EXAMPLE IDENTIFIER");
		second.setPatient(patient);
		second.setIdentifierType(new PatientIdentifierType(1));
		
		PatientIdentifier first = new PatientIdentifier();

		Assert.assertNull(first.getPatient());
		Assert.assertNull(first.getIdentifier());
		Assert.assertNull(first.getIdentifierType());
		
		Assert.assertFalse(first + " and " + second + " should not equal." , second.equals(first));
	}

	/**
	 * @see {@link PatientIdentifier#equals(Object)}
	 * 
	 */
	@Test
	@Verifies(value = "should return false if one patient identifier id is null", method = "equals(Object)")
	public void equals_shouldReturnFalseIfOnePatientIdentifierIdIsNull()
			throws Exception {
		PatientIdentifier nonNullPII = new PatientIdentifier();
		nonNullPII.setPatientIdentifierId(123);
		PatientIdentifier nullPII = new PatientIdentifier();
		Assert.assertNotSame(nonNullPII, nullPII);
		Assert.assertNotSame(nullPII, nonNullPII);
	}

	/**
	 * @see {@link PatientIdentifier#equals(Object)}
	 * 
	 */
	@Test
	@Verifies(value = "should return true if comparing same object with null ids", method = "equals(Object)")
	public void equals_shouldReturnTrueIfComparingSameObjectWithNullIds()
			throws Exception {
		PatientIdentifier nullPII = new PatientIdentifier();
		Assert.assertEquals(nullPII, nullPII);
	}

	/**
	 * @see {@link PatientIdentifier#equals(Object)}
	 * 
	 */
	@Test
	@Verifies(value = "should return true if patient identifier ids are same", method = "equals(Object)")
	public void equals_shouldReturnTrueIfPatientIdentifierIdsAreSame()
			throws Exception {
		PatientIdentifier nonNullPII = new PatientIdentifier();
		nonNullPII.setPatientIdentifierId(123);
		PatientIdentifier withPatientPII = new PatientIdentifier();
		nonNullPII.setPatientIdentifierId(123);
		nonNullPII.setPatient(new Patient(1));
		
		Assert.assertNotSame(nonNullPII, withPatientPII);
		Assert.assertNotSame(withPatientPII, nonNullPII);
	}
}
