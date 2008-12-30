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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

/**
 * This class tests the all of the {@link Encounter} non-trivial object methods.
 * 
 * @see Encounter
 */
public class EncounterTest {
	
	/**
	 * Makes sure that two different encounter objects that have the same encounter id are
	 * considered equal
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldHaveEqualEncounterObjectsByEncounterId() throws Exception {
		Encounter encounter1 = new Encounter(1);
		// another encounter with the same encounter id
		Encounter encounter2 = new Encounter(1);
		
		Assert.assertTrue(encounter1.equals(encounter2));
	}
	
	/**
	 * Makes sure that two different encounter objects that have different encounter ids are
	 * considered unequal
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldNotHaveEqualEncounterObjectsByEncounterId() throws Exception {
		Encounter encounter1 = new Encounter(1);
		// another encounter with a different encounter id
		Encounter encounter2 = new Encounter(2);
		
		Assert.assertFalse(encounter1.equals(encounter2));
	}
	
	/**
	 * Makes sure that two different encounter objects that have the same encounter id are
	 * considered equal (checks for NPEs)
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldHaveEqualEncounterObjectsWithNoEncounterId() throws Exception {
		// an encounter object with no encounter id
		Encounter encounter = new Encounter();
		
		Assert.assertTrue(encounter.equals(encounter));
	}
	
	/**
	 * Makes sure that two different encounter objects are unequal when one of them doesn't have an
	 * encounter id defined (checks for NPEs)
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldNotHaveEqualEncounterObjectsWhenOneHasNullEncounterId() throws Exception {
		Encounter encounterWithId = new Encounter(1);
		// another encounter that doesn't have an encounter id
		Encounter encounterWithoutId = new Encounter();
		
		Assert.assertFalse(encounterWithId.equals(encounterWithoutId));
		
		// now test the reverse
		Assert.assertFalse(encounterWithoutId.equals(encounterWithId));
		
		Encounter anotherEncounterWithoutId = new Encounter();
		// now test with both not having an id
		Assert.assertFalse(encounterWithoutId.equals(anotherEncounterWithoutId));
	}
	
	/**
	 * Make sure we can call {@link Encounter#hashCode()} with all null attributes on encounter and
	 * still get a hashcode
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldGetHashCodeWithNullAttributes() throws Exception {
		new Encounter().hashCode();
	}
	
	/**
	 * When a null argument is passed to the {@link Encounter#removeObs(Obs)} method, errors should
	 * not be thrown.
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldNotThrowErrorWhenRemovingNullObs() throws Exception {
		
		// test removing a null obs from a null encounter.obs set
		Encounter encounterWithoutObsSet = new Encounter();
		encounterWithoutObsSet.removeObs(null);
		Encounter encounterWithObsSet = new Encounter();
		
		Set<Obs> obsSet = new HashSet<Obs>();
		obsSet.add(new Obs());
		
		// test removing a null obs from a non-null encounter.obs set
		encounterWithObsSet.setObs(obsSet);
		// make sure the encounter got the obs
		Assert.assertEquals(1, encounterWithObsSet.getAllObs(true).size());
		encounterWithObsSet.removeObs(null);
	}
	
	/**
	 * This tries to remove an obs from an encounter (using the {@link Encounter#removeObs(Obs)}
	 * method) that currently has the obs in its set of obs
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldRemoveObsSuccessfully() throws Exception {
		
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
	 * Make sure that the getObs* methods return a non-null set when the encounter.obs set is null
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldNotReturnNullFromGetObsMethodsWithNullObsSet() throws Exception {
		//create an Encounter 
		Encounter encounter = new Encounter();
		
		//assert that the three methods return an empty set when there are no Obs in the encounter:
		assertNotNull(encounter.getObs());
		assertEquals(encounter.getObs().size(), 0);
		
		assertNotNull(encounter.getObsAtTopLevel(true));
		assertEquals(encounter.getObsAtTopLevel(true).size(), 0);
		assertNotNull(encounter.getObsAtTopLevel(false));
		assertEquals(encounter.getObsAtTopLevel(false).size(), 0);
		
		assertNotNull(encounter.getAllObs(true));
		assertEquals(encounter.getAllObs(true).size(), 0);
		assertNotNull(encounter.getAllObs(false));
		assertEquals(encounter.getAllObs(false).size(), 0);
	}
	
	/**
	 * Make sure that a non-voided obs is returned by all of the getObs methods
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldGetObsFromGetObsStarMethods() throws Exception {
		Encounter encounter = new Encounter();
		
		//create and add an Obs
		Obs o = new Obs();
		encounter.addObs(o);
		
		//now assert that the obs is returned by getObs()
		assertNotNull(encounter.getObs());
		assertEquals(1, encounter.getObs().size());
		
		//assert that the obs is returned by getObsAtTopLevel()
		assertNotNull(encounter.getObsAtTopLevel(true));
		assertEquals(1, encounter.getObsAtTopLevel(true).size());
		assertNotNull(encounter.getObsAtTopLevel(false));
		assertEquals(1, encounter.getObsAtTopLevel(false).size());
		
		//assert that theobs is returned by getAllObs()
		assertNotNull(encounter.getAllObs(true));
		assertEquals(1, encounter.getAllObs(true).size());
		assertNotNull(encounter.getAllObs(false));
		assertEquals(1, encounter.getAllObs(false).size());
	}
	
	/**
	 * Make sure that a voided obs is not returned by any of the getObs* methods
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldNotGetVoidedObsFromObsStarMethods() throws Exception {
		Encounter enc = new Encounter();
		
		//create and add an Obs
		Obs o = new Obs();
		o.setVoided(true);
		enc.addObs(o);
		
		//the child Obs shouldn't be returned by the function getObs()
		assertEquals(0, enc.getObs().size());
		
		//test getObsAtTopLevel()
		assertNotNull(enc.getObsAtTopLevel(true));
		assertEquals(1, enc.getObsAtTopLevel(true).size());
		assertNotNull(enc.getObsAtTopLevel(false));
		assertEquals(0, enc.getObsAtTopLevel(false).size());
		
		//test getAllObs()
		assertNotNull(enc.getAllObs(true));
		assertEquals(1, enc.getAllObs(true).size());
		assertNotNull(enc.getAllObs(false));
		assertEquals(0, enc.getAllObs(false).size());
	}
	
	/**
	 * Test an encounter that has two obs on it: one parent obs and one child obs to that parent.
	 * The getObs() method should return only the child obs
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldOnlyGetChildObsFromGetObsMethod() throws Exception {
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
		Obs obsInEncounter = encounter.getObs().toArray(new Obs[] {})[0];
		assertTrue(childObs.equals(obsInEncounter));
		assertFalse(obsInEncounter.isObsGrouping());
	}
	
	/**
	 * Test an encounter that has two obs on it: one parent obs and one child obs to that parent.
	 * The getObsAtTopLevel() method should return only the parent obs
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldOnlyGetParentObsFromGetObsAtTopLevelMethod() throws Exception {
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
		Obs obsInEncounter = encounter.getObsAtTopLevel(false).toArray(new Obs[] {})[0];
		assertTrue(obsInEncounter.isObsGrouping());
	}
	
	/**
	 * Test an encounter that has two obs on it: one parent obs and one child obs to that parent.
	 * The getAllObs() method should return only the parent obs
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldGetBothParentAndChildObsFromGetAllObsMethod() throws Exception {
		Encounter encounter = new Encounter();
		
		//create and add an Obs
		Obs parentObs = new Obs();
		encounter.addObs(parentObs);
		
		//add a child to the obs and make sure that the Obs is an ObsGroup with one child:
		Obs childObs = new Obs();
		parentObs.addGroupMember(childObs);
		
		//assert that the parent obs is returned by getAllObs()
		assertNotNull(encounter.getAllObs(true));
		assertEquals(1, encounter.getAllObs(true).size());
		assertNotNull(encounter.getAllObs(false));
		assertEquals(1, encounter.getAllObs(false).size());
		Obs obsInEncounter = encounter.getAllObs(false).toArray(new Obs[] {})[0];
		assertTrue(obsInEncounter.isObsGrouping());
		
	}
	
	/**
	 * Test that a voided parent obs is not returned from the getObs* methods
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldNotGetVoidedObsFromGetObsStarMethods() throws Exception {
		Encounter enc = new Encounter();
		
		//create and add an Obs
		Obs parentObs = new Obs();
		enc.addObs(parentObs);
		parentObs.setVoided(true);
		
		//add a child to the obs and make sure that now that the Obs is an ObsGroup with one child:
		Obs childObs = new Obs();
		parentObs.addGroupMember(childObs);
		
		assertNotNull(enc.getObsAtTopLevel(true));
		assertEquals(1, enc.getObsAtTopLevel(true).size());
		
		// should not get an obs back here because its voided
		assertNotNull(enc.getObsAtTopLevel(false));
		assertEquals(0, enc.getObsAtTopLevel(false).size());
		
		assertNotNull(enc.getAllObs(true));
		assertEquals(1, enc.getAllObs(true).size());
		
		// should not get an obs back here because its voided
		assertNotNull(enc.getAllObs(false));
		assertEquals(0, enc.getAllObs(false).size());
	}
	
	/**
	 * Even if the childObs of the obsGroup is associated directly with the encounter, it should not
	 * be returned by the getObs() method
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldNotGetChildObsFromGetObsMethodsIfChildAlsoOnEncounter() throws Exception {
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
		Obs obsInEncounter = encounter.getObs().toArray(new Obs[] {})[0];
		assertFalse(obsInEncounter.isObsGrouping());
		
	}
	
	/**
	 * Only the grouped top level obs should be returned by the getObsAtTopLevel method.
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldGetParentObsOnlyWithChildObsAlsoInEncounterObsSet() throws Exception {
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
		Obs obsInEncounter = encounter.getObsAtTopLevel(false).toArray(new Obs[] {})[0];
		assertTrue(obsInEncounter.isObsGrouping());
	}
	
	/**
	 * Both the parent and the child obs should be returned by the getAllObs method
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldGetBothParentAndChildFromGetAllObsWithChildDirectlyOnEncounter() throws Exception {
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
	 * If a child is a direct member of an encounter.obs and also a child of another member of that
	 * encounter.obs and then it is removed from the parent obs, the child obs should now be
	 * returned from the getObs* methods.
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldGetBothChildAndParentObsFromGetObsStarMethodsAfterRemovingChildFromParentGrouping() throws Exception {
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
		// now all three methods should return  both obs:
		assertEquals(2, enc.getObs().size());
		assertEquals(2, enc.getObsAtTopLevel(false).size());
		assertEquals(2, enc.getAllObs(true).size());
	}
	
	/**
	 * Test an obs group setup with multiple levels of hierarchy.
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldGetObsWithTwoLevelsOfHierarchy() throws Exception {
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
	 * Make sure obs groups work for a third layer of grouping
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldGetObsWithThreeLevelsOfHierarchy() throws Exception {
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
	 * Make sure that there are no voided obs returned or children of voided objects with them at
	 * the third layer
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldNotGetViodedObsWithThreeLayersOfHierarchy() throws Exception {
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
	 * Make sure the Encounter(Integer) constructor sets the encounterId
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldSetEncounterIdFromConstructor() throws Exception {
		Encounter encounter = new Encounter(123);
		Assert.assertEquals(123, encounter.getEncounterId().intValue());
	}
	
	/**
	 * Try to add an empty obs object
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldAddObsWithNullValues() throws Exception {
		Encounter encounter = new Encounter();
		encounter.addObs(new Obs());
		assertEquals(1, encounter.getAllObs(true).size());
	}
	
	/**
	 * A null parameter passed to addObs should fail silently
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldNotFailWithNullObsPassedToAddObs() throws Exception {
		Encounter encounter = new Encounter();
		encounter.addObs(null);
		assertEquals(0, encounter.getAllObs(true).size());
	}
	
	/**
	 * Make sure the reverse setting of Obs.encounter is set after adding the obs.
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldGetReverseEncounterAttributeSetDuringAddObs() throws Exception {
		Encounter encounter = new Encounter();
		Obs obs = new Obs();
		encounter.addObs(obs);
		assertTrue(obs.getEncounter().equals(encounter));
	}
	
	/**
	 * Make sure a second obs can be added to an encounter.obs that already had an obs in it
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldAddObsToNonNullInitialObsSet() throws Exception {
		Encounter encounter = new Encounter();
		Set<Obs> obsSet = new HashSet<Obs>();
		obsSet.add(new Obs(1));
		
		encounter.setObs(obsSet);
		
		encounter.addObs(new Obs(2));
		assertEquals(2, encounter.getAllObs(true).size());
	}
	
	/**
	 * Make sure that the encounter attrs are copied to the newly added obs if the obs doesn't have
	 * them already.
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldAddEncounterAttrsToObsIfAttributesAreNull() throws Exception {
		// an encounter that will hav the date/location/patient on it
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
	 * Try to add an empty order object
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldAddOrderWithNullValues() throws Exception {
		Encounter encounter = new Encounter();
		encounter.addOrder(new Order());
		assertEquals(1, encounter.getOrders().size());
	}
	
	/**
	 * A null parameter passed to addOrder should fail silently
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldNotFailWithNullObsPassedToAddOrder() throws Exception {
		Encounter encounter = new Encounter();
		encounter.addOrder(null);
		assertEquals(0, encounter.getOrders().size());
	}
	
	/**
	 * Make sure the reverse setting of Order.encounter is set after adding the order.
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldGetReverseEncounterAttributeSetDuringAddOrder() throws Exception {
		Encounter encounter = new Encounter();
		Order order = new Order();
		encounter.addOrder(order);
		assertTrue(order.getEncounter().equals(encounter));
	}
	
	/**
	 * Make sure a second order can be added to an encounter.orders that already had an order in it
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldAddOrderToNonNullInitialOrderSet() throws Exception {
		Encounter encounter = new Encounter();
		Set<Order> orderSet = new HashSet<Order>();
		orderSet.add(new Order(1));
		
		encounter.setOrders(orderSet);
		
		encounter.addOrder(new Order(2));
		assertEquals(2, encounter.getOrders().size());
	}
	
	/**
	 * Should remove an order from an encounter.orders
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldRemoveOrderFromEncounter() throws Exception {
		Encounter encounter = new Encounter();
		Order order = new Order(1);
		encounter.addOrder(order);
		assertEquals(1, encounter.getOrders().size());
		
		encounter.removeOrder(order);
		assertEquals(0, encounter.getOrders().size());
	}
	
	/**
	 * The removeOrder method should fail quietly if given a null parameter
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldNotFailWhenRemovingNullOrderFromEncounter() throws Exception {
		Encounter encounter = new Encounter();
		encounter.removeOrder(null);
	}
	
	/**
	 * The removeOrder method should do nothing if given a Order is not in encounter.orders
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldNotFailWhenRemovingNonExistantOrderFromEncounter() throws Exception {
		Encounter encounter = new Encounter();
		encounter.removeOrder(new Order(123));
	}
	
	/**
	 * Call the toString method will null values for everything and make sure an NPE isn't caused
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldNotFailWithEmptyObjectAnToString() throws Exception {
		Encounter encounter = new Encounter();
		@SuppressWarnings("unused")
		String toStringOutput = encounter.toString();
	}
	
}
