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

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.openmrs.api.DiagnosisService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;

/**
 * This class tests the all of the {@link Encounter} non-trivial object methods.
 * 
 * @see Encounter
 */
public class EncounterTest extends BaseContextSensitiveTest {
	
	@Mock
	EncounterService encounterService;
	
	@Mock
	ProviderService providerService;
	
	/**
	 * @see Encounter#toString()
	 */
	@Test
	public void toString_shouldNotFailWithEmptyObject() {
		Encounter encounter = new Encounter();
		@SuppressWarnings("unused")
		String toStringOutput = encounter.toString();
	}
	
	/**
	 * @see Encounter#removeObs(Obs)
	 */
	@Test
	public void removeObs_shouldRemoveObsSuccessfully() {
		Obs obsToRemove = new Obs();
		
		Set<Obs> obsSet = new HashSet<>();
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
	 * @see Encounter#removeObs(Obs)
	 */
	@Test
	public void removeObs_shouldNotThrowErrorWhenRemovingNullObsFromEmptySet() {
		Encounter encounterWithoutObsSet = new Encounter();
		encounterWithoutObsSet.removeObs(null);
	}
	
	/**
	 * @see Encounter#removeObs(Obs)
	 */
	@Test
	public void removeObs_shouldNotThrowErrorWhenRemovingNullObsFromNonEmptySet() {
		Encounter encounterWithObsSet = new Encounter();
		Set<Obs> obsSet = new HashSet<>();
		obsSet.add(new Obs());
		
		encounterWithObsSet.setObs(obsSet);
		// make sure the encounter got the obs
		Assert.assertEquals(1, encounterWithObsSet.getAllObs(true).size());
		encounterWithObsSet.removeObs(null);
	}
	
	/**
	 * @see Encounter#getObs()
	 */
	@Test
	public void getObs_shouldNotReturnNullWithNullObsSet() {
		Encounter encounter = new Encounter();
		
		assertNotNull(encounter.getObs());
		assertEquals(encounter.getObs().size(), 0);
	}
	
	/**
	 * @see Encounter#getAllObs(null)
	 */
	@Test
	public void getAllObs_shouldNotReturnNullWithNullObsSet() {
		Encounter encounter = new Encounter();
		assertNotNull(encounter.getAllObs(true));
		assertEquals(encounter.getAllObs(true).size(), 0);
		assertNotNull(encounter.getAllObs(false));
		assertEquals(encounter.getAllObs(false).size(), 0);
	}


	/**
	 * @see Encounter#getAllObs(null)
	 */
	@Test
	public void getAllObs_shouldGetObsInTheSameOrderObsIsAddedToTheEncounter() {
		Encounter encounter = new Encounter();
		Obs obs1= new Obs();
		obs1.setValueText("first obs");
		encounter.addObs(obs1);
		Obs obs2= new Obs();
		obs2.setValueText("second obs");
		encounter.addObs(obs2);
		Obs obs3= new Obs();
		obs3.setValueText("third obs");
		encounter.addObs(obs3);

		Set<Obs> allObs = encounter.getAllObs(true);
		assertNotNull(allObs);
		assertEquals(3, allObs.size());
		Iterator<Obs> obsIterator = allObs.iterator();
		assertEquals("first obs", obsIterator.next().getValueText());
		assertEquals("second obs", obsIterator.next().getValueText());
		assertEquals("third obs", obsIterator.next().getValueText());
	}


	/**
	 * @see Encounter#getObsAtTopLevel(null)
	 */
	@Test
	public void getObsAtTopLevel_shouldNotReturnNullWithNullObsSet() {
		Encounter encounter = new Encounter();
		assertNotNull(encounter.getObsAtTopLevel(true));
		assertEquals(encounter.getObsAtTopLevel(true).size(), 0);
		assertNotNull(encounter.getObsAtTopLevel(false));
		assertEquals(encounter.getObsAtTopLevel(false).size(), 0);
	}

	/**
	 * @see Encounter#getObsAtTopLevel(boolean)
	 */
	@Test
	public void getObsAtTopLevel_shouldGetObsInTheSameOrderObsIsAddedToTheEncounter() {
		Encounter encounter = new Encounter();
		Obs obs1= new Obs();
		obs1.setValueText("first obs");
		encounter.addObs(obs1);
		Obs obs2= new Obs();
		obs2.setValueText("second obs");
		encounter.addObs(obs2);
		Obs obs3= new Obs();
		obs3.setValueText("third obs");
		encounter.addObs(obs3);

		Set<Obs> obsAtTopLevel = encounter.getObsAtTopLevel(true);
		assertNotNull(obsAtTopLevel);
		assertEquals(3, obsAtTopLevel.size());
		Iterator<Obs> obsIterator = obsAtTopLevel.iterator();
		assertEquals("first obs", obsIterator.next().getValueText());
		assertEquals("second obs", obsIterator.next().getValueText());
		assertEquals("third obs", obsIterator.next().getValueText());
	}

	/**
	 * @see Encounter#getObs()
	 */
	@Test
	public void getObs_shouldGetObsInTheSameOrderObsIsAddedToTheEncounter() {
		Encounter encounter = new Encounter();
		Obs obs1= new Obs();
		obs1.setValueText("first obs");
		encounter.addObs(obs1);
		Obs obs2= new Obs();
		obs2.setValueText("second obs");
		encounter.addObs(obs2);
		Obs obs3= new Obs();
		obs3.setValueText("third obs");
		encounter.addObs(obs3);

		Set<Obs> obs = encounter.getObs();
		assertNotNull(obs);
		assertEquals(3, obs.size());
		Iterator<Obs> obsIterator = obs.iterator();
		assertEquals("first obs", obsIterator.next().getValueText());
		assertEquals("second obs", obsIterator.next().getValueText());
		assertEquals("third obs", obsIterator.next().getValueText());
	}

	/**
	 * @see Encounter#getObs()
	 */
	@Test
	public void getObs_shouldGetObs() {
		Encounter encounter = new Encounter();
		
		//create and add an Obs
		Obs o = new Obs();
		encounter.addObs(o);
		
		assertNotNull(encounter.getObs());
		assertEquals(1, encounter.getObs().size());
	}
	
	/**
	 * @see Encounter#getObsAtTopLevel(null)
	 */
	@Test
	public void getObsAtTopLevel_shouldGetObs() {
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
	 * @see Encounter#getAllObs(null)
	 */
	@Test
	public void getAllObs_shouldGetObs() {
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
	 * @see Encounter#getObs()
	 */
	@Test
	public void getObs_shouldNotGetVoidedObs() {
		Encounter enc = new Encounter();
		
		//create and add an Obs
		Obs o = new Obs();
		o.setVoided(true);
		enc.addObs(o);
		
		assertEquals(0, enc.getObs().size());
	}
	
	/**
	 * @see Encounter#getObsAtTopLevel(null)
	 */
	@Test
	public void getObsAtTopLevel_shouldNotGetVoidedObs() {
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
	 * @see Encounter#getAllObs(null)
	 */
	@Test
	public void getAllObs_shouldNotGetVoidedObs() {
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
	 * @see Encounter#getObs()
	 */
	@Test
	public void getObs_shouldOnlyGetChildObs() {
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
	 * @see Encounter#getObsAtTopLevel(null)
	 */
	@Test
	public void getObsAtTopLevel_shouldOnlyGetParentsObs() {
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
	 * @see Encounter#getAllObs(null)
	 */
	@Test
	public void getAllObs_shouldGetBothParentAndChildObs() {
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
	 * @see Encounter#getObs()
	 */
	@Test
	public void getObs_shouldNotGetChildObsIfChildAlsoOnEncounter() {
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
	 * @see Encounter#getObsAtTopLevel(null)
	 */
	@Test
	public void getObsAtTopLevel_shouldOnlyReturnTheGroupedTopLevelObs() {
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
	 * @see Encounter#getAllObs(null)
	 */
	@Test
	public void getAllObs_shouldGetBothParentAndChildWithChildDirectlyOnEncounter() {
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
	 * @see Encounter#getAllObs(null)
	 */
	@Test
	public void getAllObs_shouldGetBothChildAndParentObsAfterRemovingChildFromParentGrouping() {
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
	 * @see Encounter#getObs()
	 */
	@Test
	public void getObs_shouldGetBothChildAndParentObsAfterRemovingChildFromParentGrouping() {
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
	 * @see Encounter#getObsAtTopLevel(null)
	 */
	@Test
	public void getObsAtTopLevel_shouldGetBothChildAndParentObsAfterRemovingChildFromParentGrouping() {
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
	 * @see Encounter#getObs()
	 */
	@Test
	public void getObs_shouldGetObsWithTwoLevelsOfHierarchy() {
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
	 * @see Encounter#getObs()
	 */
	@Test
	public void getObs_shouldGetObsWithThreeLevelsOfHierarchy() {
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
	 * @see Encounter#getObs()
	 */
	@Test
	public void getObs_shouldNotGetVoidedObsWithThreeLayersOfHierarchy() {
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
	 * @see Encounter#Encounter(Integer)
	 */
	@Test
	public void Encounter_shouldSetEncounterId() {
		Encounter encounter = new Encounter(123);
		Assert.assertEquals(123, encounter.getEncounterId().intValue());
	}
	
	/**
	 * @see Encounter#addObs(Obs)
	 */
	@Test
	public void addObs_shouldAddObsWithNullValues() {
		Encounter encounter = new Encounter();
		encounter.addObs(new Obs());
		assertEquals(1, encounter.getAllObs(true).size());
	}
	
	/**
	 * @see Encounter#addObs(Obs)
	 */
	@Test
	public void addObs_shouldNotFailWithNullObs() {
		Encounter encounter = new Encounter();
		encounter.addObs(null);
		assertEquals(0, encounter.getAllObs(true).size());
	}
	
	/**
	 * @see Encounter#addObs(Obs)
	 */
	@Test
	public void addObs_shouldSetEncounterAttributeOnObs() {
		Encounter encounter = new Encounter();
		Obs obs = new Obs();
		encounter.addObs(obs);
		assertTrue(obs.getEncounter().equals(encounter));
	}
	
	/**
	 * @see Encounter#addObs(Obs)
	 */
	@Test
	public void addObs_shouldAddObsToNonNullInitialObsSet() {
		Encounter encounter = new Encounter();
		Set<Obs> obsSet = new HashSet<>();
		obsSet.add(new Obs(1));
		
		encounter.setObs(obsSet);
		
		encounter.addObs(new Obs(2));
		assertEquals(2, encounter.getAllObs(true).size());
	}
	
	/**
	 * @see Encounter#addObs(Obs)
	 */
	@Test
	public void addObs_shouldAddEncounterAttrsToObsIfAttributesAreNull() {
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
	 * @see Encounter#addObs(Obs)
	 */
	@Test
	public void addObs_shouldAddEncounterAttrsToObsGroupMembersIfAttributesAreNull() {
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
	 * @see Encounter#addOrder(Order)
	 */
	@Test
	public void addOrder_shouldAddOrderWithNullValues() {
		Encounter encounter = new Encounter();
		encounter.addOrder(new Order());
		assertEquals(1, encounter.getOrders().size());
	}
	
	/**
	 * @see Encounter#addOrder(Order)
	 */
	@Test
	public void addOrder_shouldNotFailWithNullObsPassedToAddOrder() {
		Encounter encounter = new Encounter();
		encounter.addOrder(null);
		assertEquals(0, encounter.getOrders().size());
	}
	
	/**
	 * @see Encounter#addOrder(Order)
	 */
	@Test
	public void addOrder_shouldSetEncounterAttribute() {
		Encounter encounter = new Encounter();
		Order order = new Order();
		encounter.addOrder(order);
		assertTrue(order.getEncounter().equals(encounter));
	}
	
	/**
	 * @see Encounter#addOrder(Order)
	 */
	@Test
	public void addOrder_shouldAddOrderToNonNullInitialOrderSet() {
		Encounter encounter = new Encounter();
		Set<Order> orderSet = new HashSet<>();
		orderSet.add(new Order(1));
		
		encounter.setOrders(orderSet);
		
		encounter.addOrder(new Order(2));
		assertEquals(2, encounter.getOrders().size());
	}
	
	/**
	 * @see Encounter#getOrders()
	 */
	@Test
	public void addOrders_shouldAddOrderToEncounterWhenAddingOrderToSetReturnedFromGetOrders() {
		Encounter encounter = new Encounter();
		Order order = new Order();
		encounter.getOrders().add(order);
		
		assertEquals(1, encounter.getOrders().size());
	}
	
	/**
	 * @see Encounter#removeOrder(Order)
	 */
	@Test
	public void removeOrder_shouldRemoveOrderFromEncounter() {
		Encounter encounter = new Encounter();
		Order order = new Order(1);
		encounter.addOrder(order);
		assertEquals(1, encounter.getOrders().size());
		
		encounter.removeOrder(order);
		assertEquals(0, encounter.getOrders().size());
	}
	
	/**
	 * @see Encounter#removeOrder(Order)
	 */
	@Test
	public void removeOrder_shouldNotFailWhenRemovingNullOrder() {
		Encounter encounter = new Encounter();
		encounter.removeOrder(null);
	}
	
	/**
	 * @see Encounter#removeOrder(Order)
	 */
	@Test
	public void removeOrder_shouldNotFailWhenRemovingNonExistentOrder() {
		Encounter encounter = new Encounter();
		encounter.removeOrder(new Order(123));
	}
	
	/**
	 * @see Encounter#addProvider(EncounterRole,Provider)
	 */
	@Test
	public void addProvider_shouldAddProviderForNewRole() {
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
	 */
	@Test
	public void addProvider_shouldAddSecondProviderForRole() {
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
	 * @throws IllegalAccessException
	 * @see Encounter#addProvider(EncounterRole,Provider)
	 */
	@Test
	public void addProvider_shouldNotAddSameProviderTwiceForRole() throws IllegalAccessException {
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
	 * @see Encounter#getProvidersByRole(EncounterRole)
	 */
	@Test
	public void getProvidersByRole_shouldReturnEmptySetForNoRole() {
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
	 */
	@Test
	public void getProvidersByRole_shouldReturnEmptySetForNullRole() {
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
	 */
	@Test
	public void getProvidersByRole_shouldReturnProvidersForRole() {
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
	 */
	@Test
	public void getProvidersByRoles_shouldReturnAllRolesAndProviders() {
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
	 */
	@Test
	public void getProvidersByRoles_shouldReturnEmptyMapIfNoProviders() {
		//given
		Encounter encounter = new Encounter();
		
		//when
		Map<EncounterRole, Set<Provider>> providersByRoles = encounter.getProvidersByRoles();
		
		//then
		Assert.assertEquals(0, providersByRoles.size());
	}
	
	/**
	 * @see Encounter#setProvider(EncounterRole,Provider)
	 */
	@Test
	public void setProvider_shouldClearProvidersAndSetProviderForRole() {
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
	 */
	@Test
	public void setProvider_shouldSetProviderForNewRole() {
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
	 * @see Encounter#setProvider(EncounterRole,Provider)
	 */
	@Test
	public void setProvider_shouldVoidExistingEncounterProvider() {
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
	 * @see Encounter#removeProvider(EncounterRole,Provider)
	 */
	@Test
	public void removeProvider_shouldVoidExistingEncounterProvider() {
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
	 * @see Encounter#copyAndAssignToAnotherPatient(org.openmrs.Patient)
	 */
	@Test
	public void copy_shouldCopyAllEncounterDataExceptVisitAndAssignCopiedEncounterToGivenPatient() {
		Encounter encounter = new Encounter();
		
		encounter.setCreator(new User());
		encounter.setDateCreated(new Date());
		encounter.setChangedBy(new User());
		encounter.setDateChanged(new Date());
		encounter.setVoided(true);
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
		
		Patient patient = new Patient(7);
		
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
		Assert.assertEquals(0, encounterCopy.getOrders().size());
		
		Assert.assertEquals(1, encounter.getProvidersByRole(encounterRole).size());
		Assert.assertEquals(1, encounterCopy.getProvidersByRole(encounterRole).size());
		Assert.assertEquals(true, encounter.getProvidersByRole(encounterRole).containsAll(
		    encounterCopy.getProvidersByRole(encounterRole)));
		
		Assert.assertNotNull(encounter.getVisit());
		Assert.assertNull(encounterCopy.getVisit());
		
		Assert.assertEquals(patient, encounterCopy.getPatient());
	}

	/**
	 * @see Encounter#removeProvider(EncounterRole,Provider)
	 */
	@Test
	public void multipleAddingAndRemovingOfSameProvider_shouldNotFail() {

		Encounter encounter = new Encounter();
		EncounterRole role = new EncounterRole();
		Provider provider = new Provider();

		encounter.addProvider(role, provider);

		Assert.assertEquals(1, encounter.getProvidersByRole(role).size());
		Assert.assertTrue(encounter.getProvidersByRole(role).contains(provider));

		encounter.removeProvider(role, provider);

		//the size should be 0 for non voided providers
		Assert.assertEquals(0, encounter.getProvidersByRole(role).size());

		encounter.addProvider(role, provider);
		Assert.assertEquals(1, encounter.getProvidersByRole(role).size());

		encounter.removeProvider(role, provider);
		Assert.assertEquals(0, encounter.getProvidersByRole(role).size());

	}

	/**
	 * @see Encounter#hasDiagnosis(Diagnosis)
	 */
	@Test
	public void hasDiagnosis_shouldReturnTrueIfEncounterHasDiagnosis(){
		Encounter encounter = new Encounter();
		Diagnosis diagnosis = new Diagnosis();
		diagnosis.setEncounter(encounter);
		diagnosis.setCondition(new Condition());
		diagnosis.setCertainty(ConditionVerificationStatus.CONFIRMED);
		diagnosis.setPatient(new Patient());
		diagnosis.setRank(2);
		
		Set<Diagnosis> diagnoses = new HashSet<>();
		diagnoses.add(diagnosis);
		
		encounter.setDiagnoses(diagnoses);
		
		Assert.assertTrue(encounter.hasDiagnosis(diagnosis));
	}

	/**
	 * @see Encounter#hasDiagnosis(Diagnosis)
	 */
	@Test
	public void hasDiagnosis_shouldReturnFalseIfEncounterDoesNotHaveDiagnosis(){
		Encounter encounter = new Encounter();
		Diagnosis diagnosis = new Diagnosis();
		diagnosis.setEncounter(encounter);
		diagnosis.setCondition(new Condition());
		diagnosis.setCertainty(ConditionVerificationStatus.PROVISIONAL);
		diagnosis.setPatient(new Patient());
		diagnosis.setRank(1);
		Set<Diagnosis> diagnoses = new HashSet<>();
		encounter.setDiagnoses(diagnoses);

		Assert.assertFalse(encounter.hasDiagnosis(diagnosis));
	}

}
