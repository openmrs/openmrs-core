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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;

/**
 *This class tests the Encounter object methods other than getters and setters
 *
 *TODO: finish this test class
 * 
 * @see Encounter
 */
public class EncounterTest {

	
	/**
	 * 
	 * Test getObs(), getObsGroupAtTopLevel, getAllObs() methods
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldObsGroupAwareMethods() throws Exception {
		//create an Encounter 
		Encounter enc = new Encounter();
		
		//assert that the three methods return an empty set when there are no Obs in the encounter:
		assertNotNull(enc.getObs());
		assertEquals(enc.getObs().size(), 0);
		assertNotNull(enc.getObsAtTopLevel(true));
		assertEquals(enc.getObsAtTopLevel(true).size(), 0);
		assertNotNull(enc.getAllObs(false));
		assertEquals(enc.getAllObs(false).size(), 0);
		
		//create and add an Obs
		Obs o = new Obs();
		o.setDateCreated(new Date());
		o.setLocation(new Location(1));
		o.setObsDatetime(new Date());
		o.setPerson(new Patient(2));
		o.setValueText("childObs");
		enc.addObs(o);
		
		//now assert that the obs is returned by getObs()
		assertNotNull(enc.getObs());
		assertEquals(1, enc.getObs().size());
		
		//assert that the obs is returned by getObsAtTopLevel()
		assertNotNull(enc.getObsAtTopLevel(true));
		assertEquals(1, enc.getObsAtTopLevel(true).size());
		assertNotNull(enc.getObsAtTopLevel(false));
		assertEquals(1, enc.getObsAtTopLevel(false).size());
		
		//assert that theobs is returned by getAllObs()
		assertNotNull(enc.getAllObs(true));
		assertEquals(enc.getAllObs(true).size(), 1);
		assertNotNull(enc.getAllObs(false));
		assertEquals(enc.getAllObs(false).size(), 1);
		
		
		
		//now void the obs and make sure that getObs no longer returns the Obs
		o.setVoided(true);
	
		
		//the child Obs shouldn't be returned by the function getObs()
		assertEquals(enc.getObs().size(), 0);
		
		//test isVoided arg for getObsAtTopLevel()
		
		assertNotNull(enc.getObsAtTopLevel(true));
		assertEquals(enc.getObsAtTopLevel(true).size(), 1);
		assertNotNull(enc.getObsAtTopLevel(false));
		assertEquals(enc.getObsAtTopLevel(false).size(), 0);
		
		//test isVoided arg for getAllObs()
		
		assertNotNull(enc.getAllObs(true));
		assertEquals(enc.getAllObs(true).size(), 1);
		assertNotNull(enc.getAllObs(false));
		assertEquals(enc.getAllObs(false).size(), 0);
		
		
		//unvoid the child Obs
		o.setVoided(false);
		 
		
		//now add a child to the obs and make sure that now that the Obs is an ObsGroup with one child:
		//create child obs:
		
		Obs oChild = new Obs();
		oChild.setDateCreated(new Date());
		oChild.setLocation(new Location(1));
		oChild.setObsDatetime(new Date());
		oChild.setPerson(new Patient(2));
		oChild.setValueText("childObs");
		
		o.addGroupMember(oChild);
		
		//obsGroup should recurse correctly, so the child obs should be picked up:
		assertEquals(1, enc.getObs().size());
		for (Obs oTmp : enc.getObs()){
			assertFalse(oTmp.isObsGrouping());
		}
		
		//assert that the parent obs is returned by getObsAtTopLevel()
		assertNotNull(enc.getObsAtTopLevel(true));
		assertEquals(enc.getObsAtTopLevel(true).size(), 1);
		assertNotNull(enc.getObsAtTopLevel(false));
		assertEquals(enc.getObsAtTopLevel(false).size(), 1);
			for (Obs oTmp : enc.getObsAtTopLevel(false)){
				assertTrue(oTmp.isObsGrouping());
			}
		
		//assert that the parent obs is returned by getAllObs()
		assertNotNull(enc.getAllObs(true));
		assertEquals(enc.getAllObs(true).size(), 1);
		assertNotNull(enc.getAllObs(false));
		assertEquals(enc.getAllObs(false).size(), 1);
		for (Obs oTmp : enc.getAllObs(false)){
			assertTrue(oTmp.isObsGrouping());
		}
		
		//void the obs and make sure that includeVoided in getObsAtTopLevel and getAllObs is applied correctly:
		
		o.setVoided(true);

		assertNotNull(enc.getObsAtTopLevel(true));
		assertEquals(enc.getObsAtTopLevel(true).size(), 1);
		assertNotNull(enc.getObsAtTopLevel(false));
		assertEquals(enc.getObsAtTopLevel(false).size(), 0);
		
		assertNotNull(enc.getAllObs(true));
		assertEquals(enc.getAllObs(true).size(), 1);
		assertNotNull(enc.getAllObs(false));
		assertEquals(enc.getAllObs(false).size(), 0);

		o.setVoided(false);
		
		//even if the childObs of the obsGroup is associated directly with the encounter, it shouldn't be returned
		//getObsAtTopLevel():
		oChild.setEncounter(enc);
		enc.addObs(oChild);
		assertEquals(enc.getObs().size(), 1);
		for (Obs oTmp : enc.getObs()){
			assertFalse(oTmp.isObsGrouping());
		}
		
		//this should only return the top-level obs in the obs group
		assertEquals(enc.getObsAtTopLevel(false).size(), 1);
		for (Obs oTmp : enc.getObsAtTopLevel(false)){
			assertTrue(oTmp.isObsGrouping());
		}
		//but both obs should be returned by getAllObs()
		
		assertEquals(enc.getAllObs(true).size(), 2);
		//this should return one of each -- the obsGroup and the child (because encounter_id is populated in the child Obs):
		int childGroups = 0;
		int parentGroups = 0;
		for (Obs oTmp : enc.getAllObs(false)){
			if (oTmp.isObsGrouping())
				parentGroups ++;
			else 
				childGroups ++;
		}
		assertEquals(childGroups, 1);
		assertEquals(parentGroups, 1);
		
		//remove the obsGrouping, so that both obs are now just children of the Encounter -- now
		//all three methods should return  both obs:
		
		o.removeGroupMember(oChild);
		assertEquals(enc.getObs().size(), 2);
		assertEquals(enc.getObsAtTopLevel(false).size(), 2);
		assertEquals(enc.getAllObs(true).size(), 2);
	
		//check out multi-dimensional hierarchy for getObs():
		
		o.addGroupMember(oChild);
		
		Obs o3 = new Obs();
		o3.setDateCreated(new Date());
		o3.setLocation(new Location(1));
		o3.setObsDatetime(new Date());
		o3.setPerson(new Patient(2));
		o3.setValueText("childObs");
		oChild.addGroupMember(o3);
		
		Obs o4 = new Obs();
		o4.setDateCreated(new Date());
		o4.setLocation(new Location(1));
		o4.setObsDatetime(new Date());
		o4.setPerson(new Patient(2));
		o4.setValueText("childObs");
		oChild.addGroupMember(o4);
		
		///now getObs() should return the two leaf obs 2 levels down:
		
		assertEquals(enc.getObs().size(), 2);
		
		//now o3 isn't a leaf, it holds o5, so the size of the set returned shouldn't change:
		Obs o5 = new Obs();
		o5.setDateCreated(new Date());
		o5.setLocation(new Location(1));
		o5.setObsDatetime(new Date());
		o5.setPerson(new Patient(2));
		o5.setValueText("childObs");
		o3.addGroupMember(o5);
		
		assertEquals(2, enc.getObs().size());
		
		//add another leaf to o3:
		
		Obs o6 = new Obs();
		o6.setDateCreated(new Date());
		o6.setLocation(new Location(1));
		o6.setObsDatetime(new Date());
		o6.setPerson(new Patient(2));
		o6.setValueText("childObs");
		o3.addGroupMember(o6);
		
		//now getObs() should return a set of size 3:
		
		assertEquals(enc.getObs().size(), 3);
		for (Obs oTmp : enc.getObs()){
			assertFalse(oTmp.isObsGrouping());
		}
		
		// test third recursive level
		Obs o7 = new Obs();
		o7.setDateCreated(new Date());
		o7.setLocation(new Location(1));
		o7.setObsDatetime(new Date());
		o7.setPerson(new Patient(2));
		o7.setValueText("childObs");
		Obs o8 = new Obs();
		o8.setDateCreated(new Date());
		o8.setLocation(new Location(1));
		o8.setObsDatetime(new Date());
		o8.setPerson(new Patient(2));
		o8.setValueText("childObs");
		
		o6.addGroupMember(o7);
		o6.addGroupMember(o8);
		
		assertEquals(enc.getObs().size(), 4);
			
		//make sure voided objects in the obsGroup hierarchy aren't getting returned:
		o6.setVoided(true);
		//now the set size should drop down to 2:
		assertEquals(enc.getObs().size(), 2);
	}
	
}
