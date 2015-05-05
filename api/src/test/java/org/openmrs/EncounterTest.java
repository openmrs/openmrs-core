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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ProviderService;
import org.openmrs.test.BaseContextMockTest;
import org.openmrs.test.Verifies;

/**
 * This class tests the all of the {@link Encounter} non-trivial object methods.
 * 
 * @see Encounter
 */
public class EncounterTest extends BaseContextMockTest {
	
	@Mock
	EncounterService encounterService;
	
	@Mock
	ProviderService providerService;
	
	/**
	 * @see {@link Encounter#toString()}
	 */
	@Test
	@Verifies(value = "should not fail with empty object", method = "toString()")
	public void toString_shouldNotFailWithEmptyObject() throws Exception {
		Encounter encounter = new Encounter();
		@SuppressWarnings("unused")
		String toStringOutput = encounter.toString();
	}
	
	/**
	 * @see {@link Encounter#removeObs(Obs)}
	 */
	@Test
	@Verifies(value = "should remove obs successfully", method = "removeObs(Obs)")
	public void removeObs_shouldRemoveObsSuccessfully() throws Exception {
		Obs obsToRemove = new Obs();
		
		Set<Obs> obsSet = new HashSet<Obs>();
		obsSet.add(obsToRemove);
		
		// add the set of obs to the encounter and make sure its there
		Encounter encounter = new Encounter();
		encounter.setObs(obsSet);
		Assert.assertEquals(1, encounter.getAllObs(true).size());
		Assert.assertTrue(encounter.getAllObs(true).contains(obsToRemove));
		
		// remove the obs and make sure its gone from the encounter
		encounter.removeObs(obsToRemove);
		Assert.assertEquals(0, encounter.getAllObs(true).size());
	}
	
	/**
	 * @see {@link Encounter#removeObs(Obs)}
	 */
	@Test
	@Verifies(value = "should not throw error when removing null obs from empty set", method = "removeObs(Obs)")
	public void removeObs_shouldNotThrowErrorWhenRemovingNullObsFromEmptySet() throws Exception {
		Encounter encounterWithoutObsSet = new Encounter();
		encounterWithoutObsSet.removeObs(null);
	}
	
	/**
	 * @see {@link Encounter#removeObs(Obs)}
	 */
	@Test
	@Verifies(value = "should not throw error when removing null obs from non empty set", method = "removeObs(Obs)")
	public void removeObs_shouldNotThrowErrorWhenRemovingNullObsFromNonEmptySet() throws Exception {
		Encounter encounterWithObsSet = new Encounter();
		Set<Obs> obsSet = new HashSet<Obs>();
		obsSet.add(new Obs());
		
		encounterWithObsSet.setObs(obsSet);
		// make sure the encounter got the obs
		Assert.assertEquals(1, encounterWithObsSet.getAllObs(true).size());
		encounterWithObsSet.removeObs(null);
	}
	
	/**
	 * @see {@link Encounter#getObs()}
	 */
	@Test
	@Verifies(value = "should not return null with null obs set", method = "getObs()")
	public void getObs_shouldNotReturnNullWithNullObsSet() throws Exception {
		Encounter encounter = new Encounter();
		
		assertNotNull(encounter.getObs());
		assertEquals(encounter.getObs().size(), 0);
	}
	
	/**
	 * @see {@link Encounter#getAllObs(null)}
	 */
	@Test
	@Verifies(value = "should not return null with null obs set", method = "getAllObs(null)")
	public void getAllObs_shouldNotReturnNullWithNullObsSet() throws Exception {
		Encounter encounter = new Encounter();
		assertNotNull(encounter.getAllObs(true));
		assertEquals(encounter.getAllObs(true).size(), 0);
		assertNotNull(encounter.getAllObs(false));
		assertEquals(encounter.getAllObs(false).size(), 0);
	}
	
	/**
	 * @see {@link Encounter#getObsAtTopLevel(null)}
	 */
	@Test
	@Verifies(value = "should not return null with null obs set", method = "getObsAtTopLevel(null)")
	public void getObsAtTopLevel_shouldNotReturnNullWithNullObsSet() throws Exception {
		Encounter encounter = new Encounter();
		assertNotNull(encounter.getObsAtTopLevel(true));
		assertEquals(encounter.getObsAtTopLevel(true).size(), 0);
		assertNotNull(encounter.getObsAtTopLevel(false));
		assertEquals(encounter.getObsAtTopLevel(false).size(), 0);
	}
	
	/**
	 * @see {@link Encounter#getObs()}
	 */
	@Test
	@Verifies(value = "should get obs", method = "getObs()")
	public void getObs_shouldGetObs() throws Exception {
		Encounter encounter = new Encounter();
		
		//create and add an Obs
		Obs o = new Obs();
		encounter.addObs(o);
		
		assertNotNull(encounter.getObs());
		assertEquals(1, encounter.getObs().size());
	}
	
	/**
	 * @see {@link Encounter#getObsAtTopLevel(null)}
	 */
	@Test
	@Verifies(value = "should get obs", method = "getObsAtTopLevel(null)")
	public void getObsAtTopLevel_shouldGetObs() throws Exception {
		Encounter encounter = new Encounter();
		
		//create and add an Obs
		Obs o = new Obs();
		encounter.addObs(o);
		
		assertNotNull(encounter.getObsAtTopLevel(true));
		assertEquals(1, encounter.getObsAtTopLevel(true).size());
		assertNotNull(encounter.getObsAtTopLevel(false));
		assertEquals(1, encounter.getObsAtTopLevel(false).size());
	}
	
	/**
	 * @see {@link Encounter#getAllObs(null)}
	 */
	@Test
	@Verifies(value = "should get obs", method = "getAllObs(null)")
	public void getAllObs_shouldGetObs() throws Exception {
		Encounter encounter = new Encounter();
		
		//create and add an Obs
		Obs o = new Obs();
		encounter.addObs(o);
		
		assertNotNull(encounter.getAllObs(true));
		assertEquals(1, encounter.getAllObs(true).size());
		assertNotNull(encounter.getAllObs(false));
		assertEquals(1, encounter.getAllObs(false).size());
	}
	
	/**
	 * @see {@link Encounter#getObs()}
	 */
	@Test
	@Verifies(value = "should not get voided obs", method = "getObs()")
	public void getObs_shouldNotGetVoidedObs() throws Exception {
		Encounter enc = new Encounter();
		
		//create and add an Obs
		Obs o = new Obs();
		o.setVoided(true);
		enc.addObs(o);
		
		assertEquals(0, enc.getObs().size());
	}
	
	/**
	 * @see {@link Encounter#getObsAtTopLevel(null)}
	 */
	@Test
	@Verifies(value = "should not get voided obs", method = "getObsAtTopLevel(null)")
	public void getObsAtTopLevel_shouldNotGetVoidedObs() throws Exception {
		Encounter enc = new Encounter();
		
		//create and add an Obs
		Obs o = new Obs();
		o.setVoided(true);
		enc.addObs(o);
		
		assertNotNull(enc.getObsAtTopLevel(true));
		assertEquals(1, enc.getObsAtTopLevel(true).size());
		assertNotNull(enc.getObsAtTopLevel(false));
		assertEquals(0, enc.getObsAtTopLevel(false).size());
	}
	
	/**
	 * @see {@link Encounter#getAllObs(null)}
	 */
	@Test
	@Verifies(value = "should not get voided obs", method = "getAllObs(null)")
	public void getAllObs_shouldNotGetVoidedObs() throws Exception {
		Encounter enc = new Encounter();
		
		//create and add an Obs
		Obs o = new Obs();
		o.setVoided(true);
		enc.addObs(o);
		
		assertNotNull(enc.getAllObs(true));
		assertEquals(1, enc.getAllObs(true).size());
		assertNotNull(enc.getAllObs(false));
		assertEquals(0, enc.getAllObs(false).size());
	}
	
	/**
	 * @see {@link Encounter#getObs()}
	 */
	@Test
	@Verifies(value = "should only get child obs", method = "getObs()")
	public void getObs_shouldOnlyGetChildObs() throws Exception {
		Encounter encounter = new Encounter();
		
		//create and add an Obs
		Obs parentObs = new Obs();
		encounter.addObs(parentObs);
		
		//add a child to the obs and make sure that the Obs is an ObsGroup with one child:
		Obs childObs = new Obs();
		parentObs.addGroupMember(childObs);
		
		//obsGroup should recurse and ONLY the child obs should be picked up:
		assertEquals(1, encounter.getObs().size());
		// make sure that the obs is the oChild
		Obs obsInEncounter = (Obs) encounter.getObs().toArray()[0];
		assertTrue(childObs.equals(obsInEncounter));
		assertFalse(obsInEncounter.isObsGrouping());
	}
	
	/**
	 * @see {@link Encounter#getObsAtTopLevel(null)}
	 */
	@Test
	@Verifies(value = "should only get parents obs", method = "getObsAtTopLevel(null)")
	public void getObsAtTopLevel_shouldOnlyGetParentsObs() throws Exception {
		Encounter encounter = new Encounter();
		
		//create and add an Obs
		Obs parentObs = new Obs();
		encounter.addObs(parentObs);
		
		//add a child to the obs and make sure that the Obs is an ObsGroup with one child:
		Obs childObs = new Obs();
		parentObs.addGroupMember(childObs);
		
		//assert that the parent obs is returned by getObsAtTopLevel()
		assertNotNull(encounter.getObsAtTopLevel(true));
		assertEquals(1, encounter.getObsAtTopLevel(true).size());
		assertNotNull(encounter.getObsAtTopLevel(false));
		assertEquals(1, encounter.getObsAtTopLevel(false).size());
		
		// make sure that the obs is the parent obs
		Obs obsInEncounter = (Obs) encounter.getObsAtTopLevel(false).toArray()[0];
		assertTrue(obsInEncounter.isObsGrouping());
	}
	
	/**
	 * @see {@link Encounter#getAllObs(null)}
	 */
	@Test
	@Verifies(value = "should get both parent and child obs", method = "getAllObs(null)")
	public void getAllObs_shouldGetBothParentAndChildObs() throws Exception {
		Encounter encounter = new Encounter();
		
		//create and add an Obs
		Obs parentObs = new Obs();
		encounter.addObs(parentObs);
		
		//add a child to the obs and make sure that the Obs is an ObsGroup with one child:
		Obs childObs = new Obs();
		parentObs.addGroupMember(childObs);
		
		//assert that the parent obs is returned 
		assertNotNull(encounter.getAllObs(true));
		assertEquals(1, encounter.getAllObs(true).size());
		assertNotNull(encounter.getAllObs(false));
		assertEquals(1, encounter.getAllObs(false).size());
		Obs obsInEncounter = (Obs) encounter.getAllObs(false).toArray()[0];
		assertTrue(obsInEncounter.isObsGrouping());
	}
	
	/**
	 * @see {@link Encounter#getObs()}
	 */
	@Test
	@Verifies(value = "should not get child obs if child also on encounter", method = "getObs()")
	public void getObs_shouldNotGetChildObsIfChildAlsoOnEncounter() throws Exception {
		Encounter encounter = new Encounter();
		
		//create and add an Obs
		Obs parentObs = new Obs();
		encounter.addObs(parentObs);
		
		//add a child to the obs and make sure that now that the Obs is an ObsGroup with one child:
		Obs childObs = new Obs();
		parentObs.addGroupMember(childObs);
		
		// add the child obs directly to the encounter as well
		childObs.setEncounter(encounter);
		encounter.addObs(childObs);
		
		// do the check
		assertEquals(1, encounter.getObs().size());
		Obs obsInEncounter = (Obs) encounter.getObs().toArray()[0];
		assertFalse(obsInEncounter.isObsGrouping());
	}
	
	/**
	 * @see {@link Encounter#getObsAtTopLevel(null)}
	 */
	@Test
	@Verifies(value = "should only return the grouped top level obs", method = "getObsAtTopLevel(null)")
	public void getObsAtTopLevel_shouldOnlyReturnTheGroupedTopLevelObs() throws Exception {
		Encounter encounter = new Encounter();
		
		//create and add an Obs
		Obs parentObs = new Obs();
		encounter.addObs(parentObs);
		
		//add a child to the obs and make sure that now that the Obs is an ObsGroup with one child:
		Obs childObs = new Obs();
		parentObs.addGroupMember(childObs);
		
		// add the child obs directly to the encounter as well
		childObs.setEncounter(encounter);
		encounter.addObs(childObs);
		
		// do the check
		assertEquals(1, encounter.getObsAtTopLevel(false).size());
		Obs obsInEncounter = (Obs) encounter.getObsAtTopLevel(false).toArray()[0];
		assertTrue(obsInEncounter.isObsGrouping());
	}
	
	/**
	 * @see {@link Encounter#getAllObs(null)}
	 */
	@Test
	@Verifies(value = "should get both parent and child with child directly on encounter", method = "getAllObs(null)")
	public void getAllObs_shouldGetBothParentAndChildWithChildDirectlyOnEncounter() throws Exception {
		Encounter enc = new Encounter();
		
		//create and add an Obs
		Obs parentObs = new Obs();
		enc.addObs(parentObs);
		
		//add a child to the obs and make sure that now that the Obs is an ObsGroup with one child:
		Obs childObs = new Obs();
		parentObs.addGroupMember(childObs);
		
		// add the child obs directly to the encounter as well
		childObs.setEncounter(enc);
		enc.addObs(childObs);
		
		// do the check
		assertEquals(2, enc.getAllObs(true).size());
		
		//this should return one of each -- the obsGroup and the child (because encounter_id is populated in the child Obs):
		int numberOfChildObs = 0;
		int numberofParentObs = 0;
		for (Obs oTmp : enc.getAllObs(false)) {
			if (oTmp.isObsGrouping())
				numberofParentObs++;
			else
				numberOfChildObs++;
		}
		assertEquals(1, numberOfChildObs);
		assertEquals(1, numberofParentObs);
	}
	
	/**
	 * @see {@link Encounter#getAllObs(null)}
	 */
	@Test
	@Verifies(value = "should get both child and parent obs after removing child from parent grouping", method = "getAllObs(null)")
	public void getAllObs_shouldGetBothChildAndParentObsAfterRemovingChildFromParentGrouping() throws Exception {
		Encounter enc = new Encounter();
		
		//create and add an Obs
		Obs parentObs = new Obs();
		enc.addObs(parentObs);
		
		//add a child to the obs and make sure that now that the Obs is an ObsGroup with one child:
		Obs childObs = new Obs();
		parentObs.addGroupMember(childObs);
		
		// add the child obs directly to the encounter as well
		childObs.setEncounter(enc);
		enc.addObs(childObs);
		
		//remove the obsGrouping, so that both obs are now just children of the Encounter 
		parentObs.removeGroupMember(childObs);
		
		assertEquals(2, enc.getAllObs(true).size());
	}
	
	/**
	 * @see {@link Encounter#getObs()}
	 */
	@Test
	@Verifies(value = "should get both child and parent obs after removing child from parent grouping", method = "getObs()")
	public void getObs_shouldGetBothChildAndParentObsAfterRemovingChildFromParentGrouping() throws Exception {
		Encounter enc = new Encounter();
		
		//create and add an Obs
		Obs parentObs = new Obs();
		enc.addObs(parentObs);
		
		//add a child to the obs and make sure that now that the Obs is an ObsGroup with one child:
		Obs childObs = new Obs();
		parentObs.addGroupMember(childObs);
		
		// add the child obs directly to the encounter as well
		childObs.setEncounter(enc);
		enc.addObs(childObs);
		
		//remove the obsGrouping, so that both obs are now just children of the Encounter 
		parentObs.removeGroupMember(childObs);
		
		// do the check
		assertEquals(2, enc.getObs().size());
	}
	
	/**
	 * @see {@link Encounter#getObsAtTopLevel(null)}
	 */
	@Test
	@Verifies(value = "should get both child and parent obs after removing child from parent grouping", method = "getObsAtTopLevel(null)")
	public void getObsAtTopLevel_shouldGetBothChildAndParentObsAfterRemovingChildFromParentGrouping() throws Exception {
		Encounter enc = new Encounter();
		
		//create and add an Obs
		Obs parentObs = new Obs();
		enc.addObs(parentObs);
		
		//add a child to the obs and make sure that now that the Obs is an ObsGroup with one child:
		Obs childObs = new Obs();
		parentObs.addGroupMember(childObs);
		
		// add the child obs directly to the encounter as well
		childObs.setEncounter(enc);
		enc.addObs(childObs);
		
		//remove the obsGrouping, so that both obs are now just children of the Encounter 
		parentObs.removeGroupMember(childObs);
		
		assertEquals(2, enc.getObsAtTopLevel(false).size());
	}
	
	/**
	 * @see {@link Encounter#getObs()}
	 */
	@Test
	@Verifies(value = "should get obs with two levels of hierarchy", method = "getObs()")
	public void getObs_shouldGetObsWithTwoLevelsOfHierarchy() throws Exception {
		Encounter enc = new Encounter();
		
		//create and add an Obs
		Obs parentObs = new Obs();
		enc.addObs(parentObs);
		
		//add a child to the obs and make sure that now that the Obs is an ObsGroup with one child:
		Obs childObs = new Obs();
		parentObs.addGroupMember(childObs);
		
		// add the child obs directly to the encounter as well
		childObs.setEncounter(enc);
		enc.addObs(childObs);
		
		// make the obs two levels deep
		Obs grandChildObsOne = new Obs();
		Obs grandChildObsTwo = new Obs();
		childObs.addGroupMember(grandChildObsOne);
		childObs.addGroupMember(grandChildObsTwo);
		
		/// now getObs() should return the two leaf obs 2 levels down:
		assertEquals(2, enc.getObs().size());
		assertTrue(enc.getObs().contains(grandChildObsOne));
		assertTrue(enc.getObs().contains(grandChildObsTwo));
		
		// now grandChildObsOne isn't a leaf, it holds greatGrandChildObsOne, but the size of the set returned shouldn't change:
		Obs greatGrandChildObsOne = new Obs();
		grandChildObsOne.addGroupMember(greatGrandChildObsOne);
		assertEquals(2, enc.getObs().size());
		assertTrue(enc.getObs().contains(greatGrandChildObsOne));
		assertTrue(enc.getObs().contains(grandChildObsTwo));
		
		//add a sibling to one of the leaves.  Add leaf to grandChildObsOne:
		Obs greatGrandChildObsTwo = new Obs();
		grandChildObsOne.addGroupMember(greatGrandChildObsTwo);
		
		//now getObs() should return a set of size 3:
		assertEquals(3, enc.getObs().size());
		
		// make sure none of them are grouping obs
		for (Obs oTmp : enc.getObs()) {
			assertFalse(oTmp.isObsGrouping());
		}
		assertTrue(enc.getObs().contains(greatGrandChildObsOne));
		assertTrue(enc.getObs().contains(greatGrandChildObsTwo));
		assertTrue(enc.getObs().contains(grandChildObsTwo));
	}
	
	/**
	 * @see {@link Encounter#getObs()}
	 */
	@Test
	@Verifies(value = "should get obs with three levels of hierarchy", method = "getObs()")
	public void getObs_shouldGetObsWithThreeLevelsOfHierarchy() throws Exception {
		Encounter enc = new Encounter();
		
		//create and add an Obs
		Obs parentObs = new Obs();
		enc.addObs(parentObs);
		
		//add a child to the obs and make sure that now that the Obs is an ObsGroup with one child:
		Obs childObs = new Obs();
		parentObs.addGroupMember(childObs);
		
		// add the child obs directly to the encounter as well
		childObs.setEncounter(enc);
		enc.addObs(childObs);
		
		// make the obs two levels deep
		Obs grandChildObsOne = new Obs();
		Obs grandChildObsTwo = new Obs();
		Obs greatGrandChildObsOne = new Obs();
		Obs greatGrandChildObsTwo = new Obs();
		childObs.addGroupMember(grandChildObsOne);
		childObs.addGroupMember(grandChildObsTwo);
		grandChildObsOne.addGroupMember(greatGrandChildObsOne);
		grandChildObsOne.addGroupMember(greatGrandChildObsTwo);
		
		// test the third level
		Obs greatGreatGrandChildObsOne = new Obs();
		Obs greatGreatGrandChildObsTwo = new Obs();
		greatGrandChildObsTwo.addGroupMember(greatGreatGrandChildObsOne);
		greatGrandChildObsTwo.addGroupMember(greatGreatGrandChildObsTwo);
		
		assertEquals(4, enc.getObs().size());
		assertTrue(enc.getObs().contains(greatGrandChildObsOne));
		assertTrue(enc.getObs().contains(greatGreatGrandChildObsOne));
		assertTrue(enc.getObs().contains(greatGreatGrandChildObsTwo));
		assertTrue(enc.getObs().contains(grandChildObsTwo));
	}
	
	/**
	 * @see {@link Encounter#getObs()}
	 */
	@Test
	@Verifies(value = "should not get voided obs with three layers of hierarchy", method = "getObs()")
	public void getObs_shouldNotGetVoidedObsWithThreeLayersOfHierarchy() throws Exception {
		Encounter enc = new Encounter();
		
		//create and add an Obs
		Obs parentObs = new Obs();
		enc.addObs(parentObs);
		
		//add a child to the obs and make sure that now that the Obs is an ObsGroup with one child:
		Obs childObs = new Obs();
		parentObs.addGroupMember(childObs);
		
		// add the child obs directly to the encounter as well
		childObs.setEncounter(enc);
		enc.addObs(childObs);
		
		// make the obs two levels deep
		Obs grandChildObsOne = new Obs();
		Obs grandChildObsTwo = new Obs();
		Obs greatGrandChildObsOne = new Obs();
		Obs greatGrandChildObsTwo = new Obs();
		childObs.addGroupMember(grandChildObsOne);
		childObs.addGroupMember(grandChildObsTwo);
		grandChildObsOne.addGroupMember(greatGrandChildObsOne);
		grandChildObsOne.addGroupMember(greatGrandChildObsTwo);
		
		// set up the third level
		Obs greatGreatGrandChildObsOne = new Obs();
		Obs greatGreatGrandChildObsTwo = new Obs();
		greatGrandChildObsTwo.addGroupMember(greatGreatGrandChildObsOne);
		greatGrandChildObsTwo.addGroupMember(greatGreatGrandChildObsTwo);
		
		// make sure voided objects in the obsGroup hierarchy aren't getting returned:
		greatGrandChildObsTwo.setVoided(true);
		
		//now the set size should drop down to 2 because the obs we voided had two child leaves:
		assertEquals(2, enc.getObs().size());
		assertTrue(enc.getObs().contains(greatGrandChildObsOne));
		assertTrue(enc.getObs().contains(grandChildObsTwo));
		assertFalse(enc.getObs().contains(greatGreatGrandChildObsOne));
		assertFalse(enc.getObs().contains(greatGreatGrandChildObsTwo));
	}
	
	/**
	 * @see {@link Encounter#Encounter(Integer)}
	 */
	@Test
	@Verifies(value = "should set encounter id", method = "Encounter(Integer)")
	public void Encounter_shouldSetEncounterId() throws Exception {
		Encounter encounter = new Encounter(123);
		Assert.assertEquals(123, encounter.getEncounterId().intValue());
	}
	
	/**
	 * @see {@link Encounter#addObs(Obs)}
	 */
	@Test
	@Verifies(value = "should add obs with null values", method = "addObs(Obs)")
	public void addObs_shouldAddObsWithNullValues() throws Exception {
		Encounter encounter = new Encounter();
		encounter.addObs(new Obs());
		assertEquals(1, encounter.getAllObs(true).size());
	}
	
	/**
	 * @see {@link Encounter#addObs(Obs)}
	 */
	@Test
	@Verifies(value = "should not fail with null obs", method = "addObs(Obs)")
	public void addObs_shouldNotFailWithNullObs() throws Exception {
		Encounter encounter = new Encounter();
		encounter.addObs(null);
		assertEquals(0, encounter.getAllObs(true).size());
	}
	
	/**
	 * @see {@link Encounter#addObs(Obs)}
	 */
	@Test
	@Verifies(value = "should set encounter attribute on obs", method = "addObs(Obs)")
	public void addObs_shouldSetEncounterAttributeOnObs() throws Exception {
		Encounter encounter = new Encounter();
		Obs obs = new Obs();
		encounter.addObs(obs);
		assertTrue(obs.getEncounter().equals(encounter));
	}
	
	/**
	 * @see {@link Encounter#addObs(Obs)}
	 */
	@Test
	@Verifies(value = "should add obs to non null initial obs set", method = "addObs(Obs)")
	public void addObs_shouldAddObsToNonNullInitialObsSet() throws Exception {
		Encounter encounter = new Encounter();
		Set<Obs> obsSet = new HashSet<Obs>();
		obsSet.add(new Obs(1));
		
		encounter.setObs(obsSet);
		
		encounter.addObs(new Obs(2));
		assertEquals(2, encounter.getAllObs(true).size());
	}
	
	/**
	 * @see {@link Encounter#addObs(Obs)}
	 */
	@Test
	@Verifies(value = "should add encounter attrs to obs if attributes are null", method = "addObs(Obs)")
	public void addObs_shouldAddEncounterAttrsToObsIfAttributesAreNull() throws Exception {
		/// an encounter that will hav the date/location/patient on it
		Encounter encounter = new Encounter();
		
		Date date = new Date();
		encounter.setEncounterDatetime(date);
		
		Location location = new Location(1);
		encounter.setLocation(location);
		
		Patient patient = new Patient(1);
		encounter.setPatient(patient);
		
		// add an obs that doesn't have date/location/patient set on it.
		Obs obs = new Obs(123);
		encounter.addObs(obs);
		
		// make sure it was added
		assertEquals(1, encounter.getAllObs(true).size());
		
		// check the values of the obs attrs to see if they were added
		assertTrue(obs.getObsDatetime().equals(date));
		assertTrue(obs.getLocation().equals(location));
		assertTrue(obs.getPerson().equals(patient));
	}
	
	/**
	 * @see {@link Encounter#addObs(Obs)}
	 */
	@Test
	@Verifies(value = "should add encounter attrs to obs if attributes are null", method = "addObs(Obs)")
	public void addObs_shouldAddEncounterAttrsToObsGroupMembersIfAttributesAreNull() throws Exception {
		/// an encounter that will hav the date/location/patient on it
		Encounter encounter = new Encounter();
		
		Date date = new Date();
		encounter.setEncounterDatetime(date);
		
		Location location = new Location(1);
		encounter.setLocation(location);
		
		Patient patient = new Patient(1);
		encounter.setPatient(patient);
		
		// add an obs that doesn't have date/location/patient set on it.
		Obs obs = new Obs(123);
		Obs childObs = new Obs(456);
		obs.addGroupMember(childObs);
		
		//check for infinite recursion
		// childObs-->childObs2   and childObs2-->childObs
		Obs childObs2 = new Obs(456);
		childObs.addGroupMember(childObs2);
		childObs2.addGroupMember(childObs);
		
		assertTrue(obs.getGroupMembers() != null && obs.getGroupMembers().size() == 1);
		
		encounter.addObs(obs);
		
		// check the values of the obs attrs to see if they were added
		assertTrue(childObs.getObsDatetime().equals(date));
		assertTrue(childObs.getLocation().equals(location));
		assertTrue(childObs.getPerson().equals(patient));
		assertTrue(childObs2.getObsDatetime().equals(date));
		assertTrue(childObs2.getLocation().equals(location));
		assertTrue(childObs2.getPerson().equals(patient));
	}
	
	/**
	 * @see {@link Encounter#addOrder(Order)}
	 */
	@Test
	@Verifies(value = "should add order with null values", method = "addOrder(Order)")
	public void addOrder_shouldAddOrderWithNullValues() throws Exception {
		Encounter encounter = new Encounter();
		encounter.addOrder(new Order());
		assertEquals(1, encounter.getOrders().size());
	}
	
	/**
	 * @see {@link Encounter#addOrder(Order)}
	 */
	@Test
	@Verifies(value = "should not fail with null obs passed to add order", method = "addOrder(Order)")
	public void addOrder_shouldNotFailWithNullObsPassedToAddOrder() throws Exception {
		Encounter encounter = new Encounter();
		encounter.addOrder(null);
		assertEquals(0, encounter.getOrders().size());
	}
	
	/**
	 * @see {@link Encounter#addOrder(Order)}
	 */
	@Test
	@Verifies(value = "should set encounter attribute", method = "addOrder(Order)")
	public void addOrder_shouldSetEncounterAttribute() throws Exception {
		Encounter encounter = new Encounter();
		Order order = new Order();
		encounter.addOrder(order);
		assertTrue(order.getEncounter().equals(encounter));
	}
	
	/**
	 * @see {@link Encounter#addOrder(Order)}
	 */
	@Test
	@Verifies(value = "should add order to non null initial order set", method = "addOrder(Order)")
	public void addOrder_shouldAddOrderToNonNullInitialOrderSet() throws Exception {
		Encounter encounter = new Encounter();
		Set<Order> orderSet = new HashSet<Order>();
		orderSet.add(new Order(1));
		
		encounter.setOrders(orderSet);
		
		encounter.addOrder(new Order(2));
		assertEquals(2, encounter.getOrders().size());
	}
	
	/**
	 * @see {@link Encounter#getOrders()}
	 */
	@Test
	@Verifies(value = "should add order to encounter when adding order to set returned from getOrders", method = "getOrders()")
	public void addOrders_shouldAddOrderToEncounterWhenAddingOrderToSetReturnedFromGetOrders() throws Exception {
		Encounter encounter = new Encounter();
		Order order = new Order();
		encounter.getOrders().add(order);
		
		assertEquals(1, encounter.getOrders().size());
	}
	
	/**
	 * @see {@link Encounter#removeOrder(Order)}
	 */
	@Test
	@Verifies(value = "should remove order from encounter", method = "removeOrder(Order)")
	public void removeOrder_shouldRemoveOrderFromEncounter() throws Exception {
		Encounter encounter = new Encounter();
		Order order = new Order(1);
		encounter.addOrder(order);
		assertEquals(1, encounter.getOrders().size());
		
		encounter.removeOrder(order);
		assertEquals(0, encounter.getOrders().size());
	}
	
	/**
	 * @see {@link Encounter#removeOrder(Order)}
	 */
	@Test
	@Verifies(value = "should not fail when removing null order", method = "removeOrder(Order)")
	public void removeOrder_shouldNotFailWhenRemovingNullOrder() throws Exception {
		Encounter encounter = new Encounter();
		encounter.removeOrder(null);
	}
	
	/**
	 * @see {@link Encounter#removeOrder(Order)}
	 */
	@Test
	@Verifies(value = "should not fail when removing non existent order", method = "removeOrder(Order)")
	public void removeOrder_shouldNotFailWhenRemovingNonExistentOrder() throws Exception {
		Encounter encounter = new Encounter();
		encounter.removeOrder(new Order(123));
	}
	
	/**
	 * @see Encounter#addProvider(EncounterRole,Provider)
	 * @verifies add provider for new role
	 */
	@Test
	public void addProvider_shouldAddProviderForNewRole() throws Exception {
		//given
		Encounter encounter = new Encounter();
		EncounterRole encounterRole = new EncounterRole();
		Provider provider = new Provider();
		
		//when
		encounter.addProvider(encounterRole, provider);
		
		//then
		Assert.assertTrue(encounter.getProvidersByRole(encounterRole).contains(provider));
	}
	
	/**
	 * @see Encounter#addProvider(EncounterRole,Provider)
	 * @verifies add second provider for role
	 */
	@Test
	public void addProvider_shouldAddSecondProviderForRole() throws Exception {
		//given
		Encounter encounter = new Encounter();
		EncounterRole role = new EncounterRole();
		Provider provider1 = new Provider();
		Provider provider2 = new Provider();
		
		//when
		encounter.addProvider(role, provider1);
		encounter.addProvider(role, provider2);
		
		//then
		List<Provider> providers = Arrays.asList(provider1, provider2);
		Assert.assertTrue(encounter.getProvidersByRole(role).containsAll(providers));
	}
	
	/**
	 * @see Encounter#addProvider(EncounterRole,Provider)
	 * @verifies not add same provider twice for role
	 */
	@Test
	public void addProvider_shouldNotAddSameProviderTwiceForRole() throws Exception {
		//given
		Encounter encounter = new Encounter();
		EncounterRole role = new EncounterRole();
		Provider provider1 = new Provider();
		
		//when
		encounter.addProvider(role, provider1);
		encounter.addProvider(role, provider1);
		
		//then
		// we need to cheat and use reflection to look at the private encounterProviders property; we don't want the getProvidersByRole method hiding duplicates from us
		Collection<EncounterProvider> providers = (Collection<EncounterProvider>) FieldUtils.readField(encounter,
		    "encounterProviders", true);
		Assert.assertEquals(1, providers.size());
		Assert.assertTrue(encounter.getProvidersByRole(role).contains(provider1));
	}
	
	/**
	 * @see Encounter#getProvider()
	 * @verifies return null if there is no provider for person
	 */
	@Test
	public void getProvider_shouldReturnNullIfThereIsNoProviderForPerson() throws Exception {
		//given
		Encounter encounter = new Encounter();
		EncounterRole role = new EncounterRole();
		Provider provider = new Provider();
		encounter.addProvider(role, provider);
		
		//when
		Person result = encounter.getProvider();
		
		//then
		Assert.assertNull(result);
	}
	
	/**
	 * @see Encounter#getProvider()
	 * @verifies return null if there is no providers
	 */
	@Test
	public void getProvider_shouldReturnNullIfThereIsNoProviders() throws Exception {
		//given
		Encounter encounter = new Encounter();
		
		//when
		Person result = encounter.getProvider();
		
		//then
		Assert.assertNull(result);
	}
	
	/**
	 * @see Encounter#getProvider()
	 * @verifies return provider for person
	 */
	@Test
	public void getProvider_shouldReturnProviderForPerson() throws Exception {
		//given
		Encounter encounter = new Encounter();
		EncounterRole role = new EncounterRole();
		Provider provider = new Provider();
		Person person = new Person();
		provider.setPerson(person);
		encounter.addProvider(role, provider);
		
		//when
		Person result = encounter.getProvider();
		
		//then
		Assert.assertEquals(person, result);
	}
	
	/**
	 * @see Encounter#getProvider()
	 * @verifies should exclude voided providers
	 */
	@Test
	public void getProvider_shouldExcludeVoidedProviders() throws Exception {
		//given
		Encounter encounter = new Encounter();
		EncounterRole role = new EncounterRole();
		
		Provider provider = new Provider();
		Provider anotherProvider = new Provider();
		
		Person person = new Person();
		Person anotherPerson = new Person();
		
		provider.setPerson(person);
		anotherProvider.setPerson(anotherPerson);
		
		// add the first provider
		encounter.setProvider(role, provider);
		
		// replace with the second provider
		encounter.setProvider(role, anotherProvider);
		
		//when
		Person result = encounter.getProvider();
		
		//then
		Assert.assertEquals(anotherPerson, result);
		
	}
	
	/**
	 * @see Encounter#getProvider()
	 * @verifies return same provider for person if called twice
	 */
	@Test
	public void getProvider_shouldReturnSameProviderForPersonIfCalledTwice() throws Exception {
		//given
		Encounter encounter = new Encounter();
		EncounterRole role = new EncounterRole();
		
		Provider provider = new Provider();
		Person person = new Person();
		provider.setPerson(person);
		encounter.addProvider(role, provider);
		
		Provider provider2 = new Provider();
		Person person2 = new Person();
		provider2.setPerson(person2);
		encounter.addProvider(role, provider2);
		
		//when
		Person result = encounter.getProvider();
		Person result2 = encounter.getProvider();
		
		//then
		Assert.assertEquals(result, result2);
	}
	
	/**
	 * @see Encounter#getProvidersByRole(EncounterRole)
	 * @verifies return empty set for no role
	 */
	@Test
	public void getProvidersByRole_shouldReturnEmptySetForNoRole() throws Exception {
		//given
		Encounter encounter = new Encounter();
		EncounterRole role = new EncounterRole();
		Provider provider = new Provider();
		encounter.addProvider(role, provider);
		
		EncounterRole role2 = new EncounterRole();
		
		//when
		Set<Provider> providers = encounter.getProvidersByRole(role2);
		
		//then
		Assert.assertEquals(0, providers.size());
	}
	
	/**
	 * @see Encounter#getProvidersByRole(EncounterRole)
	 * @verifies return empty set for null role
	 */
	@Test
	public void getProvidersByRole_shouldReturnEmptySetForNullRole() throws Exception {
		//given
		Encounter encounter = new Encounter();
		EncounterRole role = new EncounterRole();
		Provider provider = new Provider();
		encounter.addProvider(role, provider);
		
		//when
		Set<Provider> providers = encounter.getProvidersByRole(null);
		
		//then
		Assert.assertEquals(0, providers.size());
	}
	
	/**
	 * @see Encounter#getProvidersByRole(EncounterRole)
	 * @verifies return providers for role
	 */
	@Test
	public void getProvidersByRole_shouldReturnProvidersForRole() throws Exception {
		//given
		Encounter encounter = new Encounter();
		EncounterRole role = new EncounterRole();
		
		Provider provider = new Provider();
		encounter.addProvider(role, provider);
		
		Provider provider2 = new Provider();
		encounter.addProvider(role, provider2);
		
		EncounterRole role2 = new EncounterRole();
		Provider provider3 = new Provider();
		encounter.addProvider(role2, provider3);
		
		//when
		Set<Provider> providers = encounter.getProvidersByRole(role);
		
		//then
		Assert.assertEquals(2, providers.size());
		Assert.assertTrue(providers.containsAll(Arrays.asList(provider, provider2)));
	}
	
	/**
	 * @see Encounter#getProvidersByRoles()
	 * @verifies return all roles and providers
	 */
	@Test
	public void getProvidersByRoles_shouldReturnAllRolesAndProviders() throws Exception {
		//given
		Encounter encounter = new Encounter();
		EncounterRole role = new EncounterRole();
		
		Provider provider = new Provider();
		encounter.addProvider(role, provider);
		
		Provider provider2 = new Provider();
		encounter.addProvider(role, provider2);
		
		EncounterRole role2 = new EncounterRole();
		Provider provider3 = new Provider();
		encounter.addProvider(role2, provider3);
		
		//when
		Map<EncounterRole, Set<Provider>> providersByRoles = encounter.getProvidersByRoles();
		
		//then
		Assert.assertEquals("Roles", 2, providersByRoles.size());
		Assert.assertTrue("Roles", providersByRoles.keySet().containsAll(Arrays.asList(role, role2)));
		
		Assert.assertEquals("Providers for role", 2, providersByRoles.get(role).size());
		Assert.assertTrue("Providers for role", providersByRoles.get(role).containsAll(Arrays.asList(provider, provider2)));
		
		Assert.assertEquals("Provider for role2", 1, providersByRoles.get(role2).size());
		Assert.assertTrue("Providers for role2", providersByRoles.get(role2).contains(provider3));
	}
	
	/**
	 * @see Encounter#getProvidersByRoles()
	 * @verifies return empty map if no providers
	 */
	@Test
	public void getProvidersByRoles_shouldReturnEmptyMapIfNoProviders() throws Exception {
		//given
		Encounter encounter = new Encounter();
		
		//when
		Map<EncounterRole, Set<Provider>> providersByRoles = encounter.getProvidersByRoles();
		
		//then
		Assert.assertEquals(0, providersByRoles.size());
	}
	
	/**
	 * @see Encounter#setProvider(EncounterRole,Provider)
	 * @verifies clear providers and set provider for role
	 */
	@Test
	public void setProvider_shouldClearProvidersAndSetProviderForRole() throws Exception {
		//given
		Encounter encounter = new Encounter();
		EncounterRole role = new EncounterRole();
		
		Provider provider = new Provider();
		encounter.addProvider(role, provider);
		
		Provider provider2 = new Provider();
		encounter.addProvider(role, provider2);
		
		Provider provider3 = new Provider();
		
		//when
		encounter.setProvider(role, provider3);
		
		//then
		Assert.assertEquals(1, encounter.getProvidersByRole(role).size());
		Assert.assertTrue(encounter.getProvidersByRole(role).contains(provider3));
	}
	
	/**
	 * @see Encounter#setProvider(EncounterRole,Provider)
	 * @verifies set provider for new role
	 */
	@Test
	public void setProvider_shouldSetProviderForNewRole() throws Exception {
		//given
		Encounter encounter = new Encounter();
		EncounterRole role = new EncounterRole();
		Provider provider = new Provider();
		
		//when
		encounter.setProvider(role, provider);
		
		//then
		Assert.assertEquals(1, encounter.getProvidersByRole(role).size());
		Assert.assertTrue(encounter.getProvidersByRole(role).contains(provider));
	}
	
	/**
	 * @see Encounter#setProvider(Person)
	 * @verifies set existing provider for unknown role
	 */
	@Test
	public void setProvider_shouldSetExistingProviderForUnknownRole() throws Exception {
		//given
		Encounter encounter = new Encounter();
		EncounterRole unknownRole = new EncounterRole();
		Person person = new Person();
		Provider provider = new Provider();
		provider.setPerson(person);
		List<Provider> providers = new ArrayList<Provider>();
		providers.add(provider);
		
		when(encounterService.getEncounterRoleByUuid(EncounterRole.UNKNOWN_ENCOUNTER_ROLE_UUID)).thenReturn(unknownRole);
		
		when(providerService.getProvidersByPerson(person)).thenReturn(providers);
		
		//when
		encounter.setProvider(person);
		
		//then
		assertEquals(1, encounter.getProvidersByRoles().size());
		assertEquals(1, encounter.getProvidersByRole(unknownRole).size());
		assertEquals(provider, encounter.getProvidersByRole(unknownRole).iterator().next());
	}
	
	/**
	 * @see Encounter#setProvider(EncounterRole,Provider)
	 * @verifies void existing EncounterProvider
	 */
	@Test
	public void setProvider_shouldVoidExistingEncounterProvider() throws Exception {
		Encounter encounter = new Encounter();
		EncounterRole role = new EncounterRole();
		Provider provider1 = new Provider();
		Provider provider2 = new Provider();
		
		encounter.setProvider(role, provider1);
		encounter.setProvider(role, provider2);
		
		//the size should be 1 for non voided providers
		Assert.assertEquals(1, encounter.getProvidersByRole(role, false).size());
		
		//should contain the second provider since the first was voided.
		Assert.assertTrue(encounter.getProvidersByRole(role, false).contains(provider2));
		
		//the size should be 2 if we include voided providers
		Assert.assertEquals(2, encounter.getProvidersByRole(role, true).size());
		
		//should contain both the first (voided) and second (non voided) providers
		Assert.assertTrue(encounter.getProvidersByRole(role, true).containsAll(Arrays.asList(provider1, provider2)));
	}
	
	/**
	 * @see Encounter#setProvider(EncounterRole,Provider)
	 * @verifies previously voided provider correctly re-added
	 */
	@Test
	public void setProvider_shouldAddPreviouslyVoidedProviderAgain() throws Exception {
		//given
		Encounter encounter = new Encounter();
		EncounterRole role = new EncounterRole();
		
		Provider provider = new Provider();
		Provider anotherProvider = new Provider();
		
		Person person = new Person();
		Person anotherPerson = new Person();
		
		provider.setPerson(person);
		anotherProvider.setPerson(anotherPerson);
		
		// add the first provider
		encounter.setProvider(role, provider);
		
		// replace with the second provider
		encounter.setProvider(role, anotherProvider);
		
		// now replace back with the first provider
		encounter.setProvider(role, provider);
		
		//when
		Person result = encounter.getProvider();
		
		//then
		Assert.assertEquals(person, result);
	}
	
	/**
	 * @see Encounter#removeProvider(EncounterRole,Provider)
	 * @verifies void existing EncounterProvider
	 */
	@Test
	public void removeProvider_shouldVoidExistingEncounterProvider() throws Exception {
		Encounter encounter = new Encounter();
		EncounterRole role = new EncounterRole();
		Provider provider = new Provider();
		
		encounter.addProvider(role, provider);
		
		Assert.assertEquals(1, encounter.getProvidersByRole(role).size());
		Assert.assertTrue(encounter.getProvidersByRole(role).contains(provider));
		
		encounter.removeProvider(role, provider);
		
		//the size should be 0 for non voided providers
		Assert.assertEquals(0, encounter.getProvidersByRole(role).size());
		
		//the size should be 1 if we include voided providers
		Assert.assertEquals(1, encounter.getProvidersByRole(role, true).size());
		
		//should contain the voided provider
		Assert.assertTrue(encounter.getProvidersByRole(role, true).contains(provider));
	}
	
	/**
	 * @see {@link Encounter#copyAndAssignToAnotherPatient(org.openmrs.Patient)}
	 */
	@Test
	@Verifies(value = "should copy all Encounter data except visit and assign copied Encounter to given Patient", method = "copy()")
	public void copy_shouldCopyAllEncounterDataExceptVisitAndAssignCopiedEncounterToGivenPatient() throws Exception {
		Encounter encounter = new Encounter();
		
		encounter.setCreator(new User());
		encounter.setDateCreated(new Date());
		encounter.setChangedBy(new User());
		encounter.setDateChanged(new Date());
		encounter.setVoidReason("void");
		encounter.setDateVoided(new Date());
		
		encounter.setEncounterDatetime(new Date());
		encounter.setEncounterType(new EncounterType());
		encounter.setForm(new Form());
		encounter.setLocation(new Location());
		encounter.setPatient(new Patient());
		
		encounter.addObs(new Obs());
		encounter.addOrder(new Order());
		
		EncounterRole encounterRole = new EncounterRole();
		encounter.addProvider(encounterRole, new Provider());
		
		encounter.setVisit(new Visit());
		
		Patient patient = new Patient(1234);
		
		Encounter encounterCopy = encounter.copyAndAssignToAnotherPatient(patient);
		
		Assert.assertNotEquals(encounter, encounterCopy);
		
		Assert.assertEquals(encounter.getCreator(), encounterCopy.getCreator());
		Assert.assertEquals(encounter.getDateCreated(), encounterCopy.getDateCreated());
		Assert.assertEquals(encounter.getChangedBy(), encounterCopy.getChangedBy());
		Assert.assertEquals(encounter.getDateChanged(), encounterCopy.getDateChanged());
		Assert.assertEquals(encounter.getVoided(), encounterCopy.getVoided());
		Assert.assertEquals(encounter.getVoidReason(), encounterCopy.getVoidReason());
		Assert.assertEquals(encounter.getDateVoided(), encounterCopy.getDateVoided());
		
		Assert.assertEquals(encounter.getEncounterDatetime(), encounterCopy.getEncounterDatetime());
		Assert.assertEquals(encounter.getEncounterType(), encounterCopy.getEncounterType());
		Assert.assertEquals(encounter.getForm(), encounterCopy.getForm());
		Assert.assertEquals(encounter.getLocation(), encounterCopy.getLocation());
		
		Assert.assertEquals(1, encounter.getObs().size());
		Assert.assertEquals(1, encounterCopy.getObs().size());
		Assert.assertEquals(1, encounter.getOrders().size());
		Assert.assertEquals(1, encounterCopy.getOrders().size());
		
		Assert.assertEquals(1, encounter.getProvidersByRole(encounterRole).size());
		Assert.assertEquals(1, encounterCopy.getProvidersByRole(encounterRole).size());
		Assert.assertEquals(true, encounter.getProvidersByRole(encounterRole).containsAll(
		    encounterCopy.getProvidersByRole(encounterRole)));
		
		Assert.assertNotNull(encounter.getVisit());
		Assert.assertNull(encounterCopy.getVisit());
		
		Assert.assertEquals(patient, encounterCopy.getPatient());
	}
}
