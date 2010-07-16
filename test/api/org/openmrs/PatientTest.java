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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.Verifies;

/**
 * This class should test all methods on the patient object. It should not worry about the extended
 * Person object -- that testing is done by {@link org.openmrs.PersonTest} This class does not touch
 * the database, so it does not need to extend the normal openmrs BaseTest TODO: Complete this class
 * by testing all other non getter/setters in the patient object
 */
public class PatientTest {
	
	/**
	 * Test the add/removeIdentifiers method in the patient object
	 * 
	 * @see {@link Patient#addIdentifier(PatientIdentifier)}
	 */
	@Test
	@Verifies(value = "should add identifier to current list", method = "addIdentifier(PatientIdentifier)")
	public void addIdentifier_shouldAddIdentifierToCurrentList() throws Exception {
		
		Patient p = new Patient();
		
		assertNotNull(p.getIdentifiers());
		
		PatientIdentifier pa1 = new PatientIdentifier();
		
		pa1.setIdentifier("firsttest");
		pa1.setIdentifierType(new PatientIdentifierType(1));
		pa1.setDateCreated(new Date());
		pa1.setVoided(false);
		p.addIdentifier(pa1);
		
		// make sure the identifier is added.
		assertTrue(
		    "There should be 1 identifier in the patient object but there is actually : " + p.getIdentifiers().size(), p
		            .getIdentifiers().size() == 1);
		
		// adding the same identifier should not increment the size
		p.addIdentifier(pa1);
		assertTrue(
		    "There should be 1 identifier in the patient object but there is actually : " + p.getIdentifiers().size(), p
		            .getIdentifiers().size() == 1);
		
		PatientIdentifier pa2 = new PatientIdentifier();
		pa2.setIdentifier("secondtest");
		pa2.setIdentifierType(new PatientIdentifierType(2));
		pa2.setVoided(false);
		
		p.addIdentifier(pa2);
		
		// make sure the identifier is added
		assertTrue("There should be 2 identifiers in the patient object but there is actually : "
		        + p.getIdentifiers().size(), p.getIdentifiers().size() == 2);
		
		PatientIdentifier pa3 = new PatientIdentifier();
		pa3.setIdentifier(pa1.getIdentifier());
		pa3.setIdentifierType(pa1.getIdentifierType());
		pa3.setDateCreated(pa1.getDateCreated());
		pa3.setVoided(false);
		
		p.addIdentifier(pa3);
		// make sure the identifier is NOT added
		assertTrue("There should be 2 identifiers in the patient object but there is actually : "
		        + p.getIdentifiers().size(), p.getIdentifiers().size() == 2);
		
		pa3.setIdentifier(pa3.getIdentifier() + "some new string to make sure it gets added");
		p.addIdentifier(pa3);
		// make sure the identifier IS added
		assertTrue("There should be 3 identifiers in the patient object but there is actually : "
		        + p.getIdentifiers().size(), p.getIdentifiers().size() == 3);
		
		p.removeIdentifier(pa3);
		assertTrue("There should be only 2 identifiers in the patient object now", p.getIdentifiers().size() == 2);
		
		pa3.setDateCreated(new Date(pa1.getDateCreated().getTime() + 1));
		p.addIdentifier(pa3);
		// make sure the identifier IS added
		assertTrue("There should be 3 identifiers in the patient object but there is actually : "
		        + p.getIdentifiers().size(), p.getIdentifiers().size() == 3);
		
		// test removing all of the identifiers
		p.removeIdentifier(pa3);
		assertTrue("There should be only 2 identifiers in the patient object now", p.getIdentifiers().size() == 2);
		p.removeIdentifier(pa2);
		assertTrue("There should be only 1 identifier in the patient object now", p.getIdentifiers().size() == 1);
		p.removeIdentifier(pa2);
		assertTrue("There should still be only 1 identifier in the patient object now", p.getIdentifiers().size() == 1);
		p.removeIdentifier(pa1);
		assertTrue("There shouldn't be any identifiers in the patient object now", p.getIdentifiers().size() == 0);
	}
	
	/**
	 * @see {@link Patient#addIdentifier(PatientIdentifier)}
	 */
	@Test
	@Verifies(value = "should not fail with null identifiers list", method = "addIdentifier(PatientIdentifier)")
	public void addIdentifier_shouldNotFailWithNullIdentifiersList() throws Exception {
		Patient p = new Patient();
		p.setIdentifiers(null);
		p.addIdentifier(new PatientIdentifier());
	}
	
	/**
	 * @see {@link Patient#getIdentifiers()}
	 */
	@Test
	@Verifies(value = "should not return null", method = "getIdentifiers()")
	public void getIdentifiers_shouldNotReturnNull() throws Exception {
		Patient p = new Patient();
		p.setIdentifiers(null);
		Assert.assertNotNull(p.getIdentifiers());
	}
	
	/**
	 * @see {@link Patient#addIdentifier(PatientIdentifier)}
	 */
	@Test
	@Verifies(value = "should not add identifier that is in list already", method = "addIdentifier(PatientIdentifier)")
	public void addIdentifier_shouldNotAddIdentifierThatIsInListAlready() throws Exception {
		Patient p = new Patient();
		
		assertNotNull(p.getIdentifiers());
		
		PatientIdentifier pa1 = new PatientIdentifier();
		
		pa1.setIdentifier("firsttest");
		pa1.setIdentifierType(new PatientIdentifierType(1));
		pa1.setDateCreated(new Date());
		pa1.setVoided(false);
		p.addIdentifier(pa1);
		
		// adding the same identifier should not increment the size
		p.addIdentifier(pa1);
		assertTrue(
		    "There should be 1 identifier in the patient object but there is actually : " + p.getIdentifiers().size(), p
		            .getIdentifiers().size() == 1);
		
	}
	
	/**
	 * @see {@link Patient#removeIdentifier(PatientIdentifier)}
	 */
	@Test
	@Verifies(value = "should remove identifier if exists", method = "removeIdentifier(PatientIdentifier)")
	public void removeIdentifier_shouldRemoveIdentifierIfExists() throws Exception {
		Patient p = new Patient();
		
		PatientIdentifier pa1 = new PatientIdentifier();
		
		pa1.setIdentifier("firsttest");
		pa1.setIdentifierType(new PatientIdentifierType(1));
		pa1.setDateCreated(new Date());
		pa1.setVoided(false);
		p.addIdentifier(pa1);
		
		// adding the same identifier should not increment the size
		p.addIdentifier(pa1);
		
		PatientIdentifier pa2 = new PatientIdentifier();
		pa2.setIdentifier("secondtest");
		pa2.setIdentifierType(new PatientIdentifierType(2));
		pa2.setVoided(false);
		
		p.addIdentifier(pa2);
		
		PatientIdentifier pa3 = new PatientIdentifier();
		pa3.setIdentifierType(pa1.getIdentifierType());
		pa3.setIdentifier(pa3.getIdentifier() + "some new string to make sure it gets added");
		pa3.setVoided(true);
		pa3.setDateCreated(new Date(pa1.getDateCreated().getTime() + 1));
		p.addIdentifier(pa3);
		
		// test removing all of the identifiers
		p.removeIdentifier(pa3);
		assertTrue("There should be only 2 identifiers in the patient object now", p.getIdentifiers().size() == 2);
		p.removeIdentifier(pa2);
		assertTrue("There should be only 1 identifier in the patient object now", p.getIdentifiers().size() == 1);
		p.removeIdentifier(pa2);
		assertTrue("There should still be only 1 identifier in the patient object now", p.getIdentifiers().size() == 1);
		p.removeIdentifier(pa1);
		assertTrue("There shouldn't be any identifiers in the patient object now", p.getIdentifiers().size() == 0);
	}
	
	@Test
	@Verifies(value = "should verify identifiers order in the collection order", method = "removeIdentifier(PatientIdentifier)")
	public void removeIdentifier_shouldTestIdentifierCollectionChanged() throws Exception {
		Patient p = new Patient();
		
		PatientIdentifier pa1 = new PatientIdentifier();
		PatientIdentifier pa2 = new PatientIdentifier();
		PatientIdentifier pa3 = new PatientIdentifier();
		PatientIdentifier pa4 = new PatientIdentifier();
		
		pa2.setIdentifier("2nd-date");
		pa2.setIdentifierType(new PatientIdentifierType(1));
		pa2.setDateCreated(new Date(1000));
		pa2.setVoided(false);
		p.addIdentifier(pa2);
		
		pa4.setIdentifier("last-date");
		pa4.setIdentifierType(new PatientIdentifierType(1));
		pa4.setDateCreated(new Date(pa2.getDateCreated().getTime() + 1000));
		pa4.setVoided(false);
		p.addIdentifier(pa4);
		
		pa1.setIdentifier("first-date");
		pa1.setIdentifierType(new PatientIdentifierType(1));
		pa1.setDateCreated(new Date(pa2.getDateCreated().getTime() - 1000));
		pa1.setVoided(false);
		p.addIdentifier(pa1);
		
		pa3.setIdentifier("3rd-date");
		pa3.setIdentifierType(new PatientIdentifierType(1));
		pa3.setDateCreated(new Date(pa2.getDateCreated().getTime() + 500));
		pa3.setVoided(false);
		p.addIdentifier(pa3);
		
		//now the order should be: first-date, 2nd-date, 3rd-date, last-date
		PatientIdentifier[] pis = new PatientIdentifier[] {};
		pis = p.getIdentifiers().toArray(pis); //NOTE: this is correct -- see the order in array in debug
		assertTrue(p.getIdentifiers().contains(pa3)); //this works
		
		//now change voided on 3rd-date; that should move it to last position: voided IDs are the last in order
		pa3.setVoided(true);
		pis = p.getIdentifiers().toArray(pis); //THIS IS WRONG
		assertTrue(p.getIdentifiers().contains(pa3)); //this fails
		
		//THIS IS RIGHT
		pa3.setVoided(false); //set it back to false so we can remove it
		p.removeIdentifier(pa3);
		pis = p.getIdentifiers().toArray(pis); //pis now has 3 elements
		pa3.setVoided(true);
		p.addIdentifier(pa3);
		pis = p.getIdentifiers().toArray(pis); //pis is sorted correctly
		assertTrue(p.getIdentifiers().contains(pa3)); //this works too
	}
}
