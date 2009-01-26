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
import org.openmrs.test.Verifies;

/**
 * This class tests the all of the {@link Encounter} non-trivial object methods.
 * 
 * @see Encounter
 */
public class EncounterTest {
	
	/**
	 * @see {@link Encounter#equals(Object)}
	 */
	@Test
	@Verifies(value = "should equal encounter with same encounter id", method = "equals(Object)")
	public void equals_shouldEqualEncounterWithSameEncounterId() throws Exception {
		Encounter encounter1 = new Encounter(1);
		// another encounter with the same encounter id
		Encounter encounter2 = new Encounter(1);
		
		Assert.assertTrue(encounter1.equals(encounter2));
	}
	
	/**
	 * @see {@link Encounter#equals(Object)}
	 */
	@Test
	@Verifies(value = "should not equal encounter with different encounter id", method = "equals(Object)")
	public void equals_shouldNotEqualEncounterWithDifferentEncounterId() throws Exception {
		Encounter encounter1 = new Encounter(1);
		// another encounter with a different encounter id
		Encounter encounter2 = new Encounter(2);
		
		Assert.assertFalse(encounter1.equals(encounter2));
	}
	
	/**
	 * @see {@link Encounter#equals(Object)}
	 */
	@Test
	@Verifies(value = "should not equal on null", method = "equals(Object)")
	public void equals_shouldNotEqualOnNull() throws Exception {
		Assert.assertFalse(new Encounter(1).equals(null));
	}
	
	/**
	 * @see {@link Encounter#hashCode()}
	 */
	@Test
	@Verifies(value = "should have different hash code when not equal", method = "hashCode()")
	public void hashCode_shouldHaveDifferentHashCodeWhenNotEqual() throws Exception {
		Encounter encounter1 = new Encounter(1);
		// another encounter with a different encounter id
		Encounter encounter2 = new Encounter(2);
		
		Assert.assertNotSame(encounter1.hashCode(), encounter2.hashCode());
	}
	
	/**
	 * @see {@link Encounter#hashCode()}
	 */
	@Test
	@Verifies(value = "should have same hashcode when equal", method = "hashCode()")
	public void hashCode_shouldHaveSameHashcodeWhenEqual() throws Exception {
		Encounter encounter1 = new Encounter(1);
		// another encounter with a different encounter id
		Encounter encounter2 = new Encounter(1);
		
		Assert.assertSame(encounter1.hashCode(), encounter2.hashCode());
	}
	
	/**
	 * @see {@link Encounter#equals(Object)}
	 */
	@Test
	@Verifies(value = "should have equal encounter objects with no encounter ids", method = "equals(Object)")
	public void equals_shouldHaveEqualEncounterObjectsWithNoEncounterIds() throws Exception {
		// an encounter object with no encounter id
		Encounter encounter = new Encounter();
		
		Assert.assertTrue(encounter.equals(encounter));
	}
	
	/**
	 * @see {@link Encounter#equals(Object)}
	 */
	@Test
	@Verifies(value = "should not have equal encounter objects when one has null encounter id", method = "equals(Object)")
	public void equals_shouldNotHaveEqualEncounterObjectsWhenOneHasNullEncounterId() throws Exception {
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
	 * @see {@link Encounter#hashCode()}
	 */
	@Test
	@Verifies(value = "should get hash code with null attributes", method = "hashCode()")
	public void hashCode_shouldGetHashCodeWithNullAttributes() throws Exception {
		new Encounter().hashCode();
	}
	
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
		Obs obsInEncounter = encounter.getObs().toArray(new Obs[] {})[0];
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
		Obs obsInEncounter = encounter.getObsAtTopLevel(false).toArray(new Obs[] {})[0];
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
		Obs obsInEncounter = encounter.getAllObs(false).toArray(new Obs[] {})[0];
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
		Obs obsInEncounter = encounter.getObs().toArray(new Obs[] {})[0];
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
		Obs obsInEncounter = encounter.getObsAtTopLevel(false).toArray(new Obs[] {})[0];
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
	@Verifies(value = "should add order to non nul initial order set", method = "addOrder(Order)")
	public void addOrder_shouldAddOrderToNonNulInitialOrderSet() throws Exception {
		Encounter encounter = new Encounter();
		Set<Order> orderSet = new HashSet<Order>();
		orderSet.add(new Order(1));
		
		encounter.setOrders(orderSet);
		
		encounter.addOrder(new Order(2));
		assertEquals(2, encounter.getOrders().size());
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
	
}
