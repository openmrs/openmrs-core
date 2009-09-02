package org.openmrs;


import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.Verifies;

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
		
		first.setIdentifier("EXAMPLE IDENTIFIER");
		first.setPatient(patient);
		first.setIdentifierType(new PatientIdentifierType(1));
		
		Assert.assertTrue(first + " and " + second + " should be equal." , second.equals(first));
	}
}
