/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertSame;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

/**
 * This class should test all methods on the patient object. It should not worry about the extended
 * Person object -- that testing is done by {@link org.openmrs.PersonTest} This class does not touch
 * the database, so it does not need to extend the normal openmrs BaseTest TODO: Complete this class
 * by testing all other non getter/setters in the patient object
 */
public class PatientTest {
	
	/**
	 * Test the constructor method in patient object that takes in a patient as object and creates a copy
	 */
	@Test
	public void patient_shouldConstructCloneWhenPassedPatient() {
		Patient p = new Patient();
		
		Set<PersonName> pnSet = new HashSet<>();
		PersonName pn = new PersonName();
		pn.setFamilyName("familyName");
		pn.setGivenName("givenName");
		pn.setMiddleName("middleName");
		pnSet.add(pn);
			
		PatientIdentifier pi1 = new PatientIdentifier();
		PatientIdentifierType identifierType = new PatientIdentifierType(1);
		Location location = new Location(1);

		pi1.setIdentifier("theid");
		pi1.setIdentifierType(identifierType);
		pi1.setLocation(location);
		pi1.setVoided(true);

		PatientIdentifier pi2 = new PatientIdentifier();
		PatientIdentifierType identifierType2 = new PatientIdentifierType(2);
		Location location2 = new Location(2);

		pi2.setIdentifier("theid2");
		pi2.setIdentifierType(identifierType2);
		pi2.setLocation(location2);
		pi2.setVoided(false);
		
		p.setNames(pnSet);
		p.addIdentifier(pi1);
		p.addIdentifier(pi2);
		p.setAllergyStatus("TestAllergy");
		p.setId(1);
		
		Patient p2 = new Patient(p);

		assertEquals(p, p2);
		assertEquals(p.getAllergyStatus(), p2.getAllergyStatus());
		assertEquals(p.getNames(), p2.getNames());
		assertEquals(p.getGivenName(), p2.getGivenName());
		assertEquals(p.getIdentifiers(),p2.getIdentifiers());
		// Check that each identifier refers to the correct patient object
		for (PatientIdentifier pid : p2.getIdentifiers()) {
			assertSame(p2, pid.getPatient());
		}
		// Make sure that the original patient hasn't been dirtied
		for (PatientIdentifier pid : p.getIdentifiers()) {
			assertSame(p, pid.getPatient());
		}
		assertEquals(p.getPatientId(), p2.getPatientId());
		assertEquals(p.getId(), p2.getId());
		assertEquals(p.getPerson(), p2.getPerson());
	}
	
	/**
	 * Test the add/removeIdentifiers method in the patient object
	 * 
	 * @see Patient#addIdentifier(PatientIdentifier)
	 */
	@Test
	public void addIdentifier_shouldAddIdentifierToCurrentList() {
		
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
	 * Regression test for TRUNK-3118
	 */
	@Test
	public void addIdentifier_shouldAddNewIdentifierForExistingVoidedIdentifier() {
		Patient p = new Patient();
		PatientIdentifier pa1 = new PatientIdentifier();
		PatientIdentifierType identifierType = new PatientIdentifierType(1);
		Location location = new Location(1);
		
		pa1.setIdentifier("theid");
		pa1.setIdentifierType(identifierType);
		pa1.setLocation(location);
		pa1.setVoided(true);
		p.addIdentifier(pa1);
		
		PatientIdentifier pa2 = new PatientIdentifier();
		pa2.setIdentifier("theid");
		pa2.setIdentifierType(identifierType);
		pa1.setLocation(location);
		pa2.setVoided(false);
		
		// this should not fail
		p.addIdentifier(pa2);
		
		// make sure we still have it in there
		assertTrue("The second identifier has not been added as a new identifier", p.getActiveIdentifiers().contains(pa2));
	}
	
	/**
	 * @see Patient#addIdentifier(PatientIdentifier)
	 */
	@Test
	public void addIdentifier_shouldNotFailWithNullIdentifiersList() {
		Patient p = new Patient();
		p.setIdentifiers(null);
		p.addIdentifier(new PatientIdentifier());
	}
	
	/**
	 * @see Patient#getIdentifiers()
	 */
	@Test
	public void getIdentifiers_shouldNotReturnNull() {
		Patient p = new Patient();
		p.setIdentifiers(null);
		Assert.assertNotNull(p.getIdentifiers());
	}
	
	/**
	 * @see Patient#addIdentifier(PatientIdentifier)
	 */
	@Test
	public void addIdentifier_shouldNotAddIdentifierThatIsInListAlready() {
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
	 * @see Patient#removeIdentifier(PatientIdentifier)
	 */
	@Test
	public void removeIdentifier_shouldRemoveIdentifierIfExists() {
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
	public void removeIdentifier_shouldTestIdentifierCollectionChanged() {
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
		p.removeIdentifier((pa3));
		pa3.setVoided(true);
		p.addIdentifier((pa3));
		pis = p.getIdentifiers().toArray(pis);
		assertThat(p.getIdentifiers(), contains(pa1, pa2, pa4, pa3));
		
		//THIS IS RIGHT
		pa3.setVoided(false); //set it back to false so we can remove it
		p.removeIdentifier(pa3);
		pis = p.getIdentifiers().toArray(pis); //pis now has 3 elements
		pa3.setVoided(true);
		p.addIdentifier(pa3);
		pis = p.getIdentifiers().toArray(pis); //pis is sorted correctly
		assertTrue(p.getIdentifiers().contains(pa3)); //this works too
	}
	
	/**
	 * @see Patient#getActiveIdentifiers()
	 */
	@Test
	public void getActiveIdentifiers_shouldReturnPreferredIdentifiersFirstInTheList() {
		Patient p = new Patient();
		p.setIdentifiers(null);
		PatientIdentifier pa1 = new PatientIdentifier();
		PatientIdentifier pa2 = new PatientIdentifier();
		PatientIdentifier pa3 = new PatientIdentifier();
		
		pa1.setIdentifier("first");
		pa1.setIdentifierType(new PatientIdentifierType(1));
		pa1.setDateCreated(new Date());
		pa1.setVoided(false);
		pa1.setPreferred(true);
		
		pa2.setIdentifier("second");
		pa2.setIdentifierType(new PatientIdentifierType(1));
		pa2.setDateCreated(new Date());
		pa2.setVoided(false);
		pa2.setPreferred(false);
		
		pa3.setIdentifier("third");
		pa3.setIdentifierType(new PatientIdentifierType(1));
		pa3.setDateCreated(new Date());
		pa3.setVoided(false);
		pa3.setPreferred(false);
		
		p.addIdentifier(pa1);
		p.addIdentifier(pa2);
		p.addIdentifier(pa3);
		
		pa1.setPreferred(false);
		pa2.setPreferred(true);
		pa3.setVoided(true);
		
		assertTrue("With the third identifier voided, there should only be 2 identifiers",
		    p.getActiveIdentifiers().size() == 2);
		assertTrue("Preferred identifier should be first in the list", p.getActiveIdentifiers().get(0) == pa2);
		assertTrue("Non-preferred identifier should be last in the list", p.getActiveIdentifiers().get(1) == pa1);
	}
}
