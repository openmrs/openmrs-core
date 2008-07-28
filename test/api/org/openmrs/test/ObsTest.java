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
package org.openmrs.test;

import java.util.Date;

import junit.framework.TestCase;

import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.APIException;

/**
 * This class tests all methods that are not getter or setters 
 * in the Obs java object
 * 
 * TODO: finish this test class for Obs
 * 
 * @see Obs
 */
public class ObsTest extends TestCase {

	/**
	 * Tests the get/setValueAsString methods
	 * 
	 * @throws Exception
	 */
	public void testShouldValueAsString() throws Exception {

		// TODO: finish this test method
		
		// TODO: test obs group recursive get value as string
		
	}
	
	/**
	 * Tests the addToGroup method in ObsGroup
	 * 
	 * @throws Exception
	 */
	public void testShouldAddandRemoveObsToGroup() throws Exception {
		
		Obs obs = new Obs(1);
		
		Obs obsGroup = new Obs(755);
		
		// These methods should not fail even with null attributes on the obs
		assertFalse(obsGroup.isObsGrouping());
		assertFalse(obsGroup.hasGroupMembers());
		
		// adding an obs when the obs group has no other obs
		// should not throw an error
		obsGroup.addGroupMember(obs);
		assertEquals(1, obsGroup.getGroupMembers().size());
		
		// check duplicate add. should only be one
		obsGroup.addGroupMember(obs);
		assertTrue(obsGroup.hasGroupMembers());
		assertEquals("Duplicate add should not increase the grouped obs size", 1, obsGroup.getGroupMembers().size());
		
		
		Obs obs2 = new Obs(2);
		
		obsGroup.removeGroupMember(obs2);
		assertTrue(obsGroup.hasGroupMembers());
		assertEquals("Removing a non existent obs should not decrease the number of grouped obs", 1, obsGroup.getGroupMembers().size());
		
		// testing removing an obs from a group that has a null obs list
		new Obs().removeGroupMember(obs2);
		
		obsGroup.removeGroupMember(obs);
		
		assertEquals(0, obsGroup.getGroupMembers().size());
		
		// try to add an obs group to itself
		try {
			obsGroup.addGroupMember(obsGroup);
			fail("An APIException about adding an obsGroup should have been thrown");
		}
		catch (APIException e) {
			// this exception is expected
		}
	}	
	
		/**
		 * tests the getRelatedObservations method:
		 */	
		public void testShouldGetRelatedObservations() throws Exception {
		// create a child Obs
		Obs o = new Obs();
		o.setDateCreated(new Date());
		o.setLocation(new Location(1));
		o.setObsDatetime(new Date());
		o.setPerson(new Patient(2));
		o.setValueText("childObs");
		
		//create its sibling
		Obs oSibling = new Obs();
		oSibling.setDateCreated(new Date());
		oSibling.setLocation(new Location(1));
		oSibling.setObsDatetime(new Date());
		oSibling.setValueText("childObs2");
		oSibling.setPerson(new Patient(2));
		
		//create a parent Obs
		Obs oParent = new Obs();
		oParent.setDateCreated(new Date());
		oParent.setLocation(new Location(1));
		oParent.setObsDatetime(new Date());
		oSibling.setValueText("parentObs");
		oParent.setPerson(new Patient(2));
		
		//create a grandparent obs
		Obs oGrandparent = new Obs();
		oGrandparent.setDateCreated(new Date());
		oGrandparent.setLocation(new Location(1));
		oGrandparent.setObsDatetime(new Date());
		oGrandparent.setPerson(new Patient(2));
		oSibling.setValueText("grandParentObs");
		
		oParent.addGroupMember(o);
		oParent.addGroupMember(oSibling);
		oGrandparent.addGroupMember(oParent);
		
		//create a leaf observation at the grandparent level
		Obs o2 = new Obs();
		o2.setDateCreated(new Date());
		o2.setLocation(new Location(1));
		o2.setObsDatetime(new Date());
		o2.setPerson(new Patient(2));
		o2.setValueText("grandparentLeafObs");	
		
		oGrandparent.addGroupMember(o2);
		
		/**
		 * test to make sure that if the original child obs calls getRelatedObservations,
		 * it returns itself and its siblings:
		 * original obs is one of two groupMembers, so relatedObservations should return a size of set 2
		 * 
		 * then, make sure that if oParent calls getRelatedObservations, it returns its own children
		 * as well as the leaf obs attached to the grandparentObs
		 * oParent has two members, and one leaf ancestor -- so a set of size 3 should be returned.
		 */
		assertEquals(o.getRelatedObservations().size(), 2);
		assertEquals(oParent.getRelatedObservations().size(), 3);
		
		
		// create  a great-grandparent obs
		Obs oGGP = new Obs();
		oGGP.setDateCreated(new Date());
		oGGP.setLocation(new Location(1));
		oGGP.setObsDatetime(new Date());
		oGGP.setPerson(new Patient(2));
		oGGP.setValueText("grandParentObs");
		oGGP.addGroupMember(oGrandparent);
		
		//create a leaf great-grandparent obs
		Obs oGGPleaf = new Obs();
		oGGPleaf.setDateCreated(new Date());
		oGGPleaf.setLocation(new Location(1));
		oGGPleaf.setObsDatetime(new Date());
		oGGPleaf.setPerson(new Patient(2));
		oGGPleaf.setValueText("grandParentObs");
		oGGP.addGroupMember(oGGPleaf);
		
		/**
		 * now run the previous assertions again.  this time there are two ancestor leaf obs, so the first
		 * assertion should still return a set of size 2, 
		 * but the second assertion sould return a set of size 4.
		 */
		assertEquals(o.getRelatedObservations().size(), 2);
		assertEquals(oParent.getRelatedObservations().size(), 4);
		
		//remove the grandparent leaf observation:
		
		oGrandparent.removeGroupMember(o2);
		
		//now the there is only one ancestor leaf obs:
		assertEquals(o.getRelatedObservations().size(), 2);
		assertEquals(oParent.getRelatedObservations().size(), 3);
		
		/**
		 * finally, test a non-obsGroup and non-member Obs to the function
		 * Obs o2 is now not connected to our heirarchy:
		 * an empty set should be returned:
		 */
		
		assertNotNull(o2.getRelatedObservations());
		assertEquals(o2.getRelatedObservations().size(), 0);

		
	}
	
}