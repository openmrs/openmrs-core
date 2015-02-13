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

import java.util.Date;

/**
 * This class tests the all of the {@link EncounterProvider} non-trivial object methods.
 *
 * @see EncounterProvider
 */
public class EncounterProviderTest {
	
	/**
	 * @see {@link EncounterProvider#copy()}
	 */
	@Test
	@Verifies(value = "should copy all EncounterProvider data", method = "copy()")
	public void copy_shouldCopyAllEncounterProviderData() throws Exception {
		EncounterProvider encounterProvider = new EncounterProvider();
		
		encounterProvider.setCreator(new User());
		encounterProvider.setDateCreated(new Date());
		encounterProvider.setChangedBy(new User());
		encounterProvider.setDateChanged(new Date());
		encounterProvider.setVoidReason("void");
		encounterProvider.setDateVoided(new Date());
		
		encounterProvider.setEncounter(new Encounter());
		encounterProvider.setEncounterRole(new EncounterRole());
		encounterProvider.setProvider(new Provider());
		
		EncounterProvider encounterProviderCopy = encounterProvider.copy();
		
		Assert.assertNotEquals(encounterProvider, encounterProviderCopy);
		
		Assert.assertEquals(encounterProvider.getCreator(), encounterProviderCopy.getCreator());
		Assert.assertEquals(encounterProvider.getDateCreated(), encounterProviderCopy.getDateCreated());
		Assert.assertEquals(encounterProvider.getChangedBy(), encounterProviderCopy.getChangedBy());
		Assert.assertEquals(encounterProvider.getDateChanged(), encounterProviderCopy.getDateChanged());
		Assert.assertEquals(encounterProvider.getVoided(), encounterProviderCopy.getVoided());
		Assert.assertEquals(encounterProvider.getVoidReason(), encounterProviderCopy.getVoidReason());
		Assert.assertEquals(encounterProvider.getDateVoided(), encounterProviderCopy.getDateVoided());
		
		Assert.assertEquals(encounterProvider.getEncounter(), encounterProviderCopy.getEncounter());
		Assert.assertEquals(encounterProvider.getEncounterRole(), encounterProviderCopy.getEncounterRole());
		Assert.assertEquals(encounterProvider.getProvider(), encounterProviderCopy.getProvider());
	}
}
