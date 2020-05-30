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

import java.util.Date;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * This class tests the all of the {@link EncounterProvider} non-trivial object methods.
 *
 * @see EncounterProvider
 */
public class EncounterProviderTest {
	
	/**
	 * @see EncounterProvider#copy()
	 */
	@Test
	public void copy_shouldCopyAllEncounterProviderData() {
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
		
		Assertions.assertNotEquals(encounterProvider, encounterProviderCopy);
		
		Assertions.assertEquals(encounterProvider.getCreator(), encounterProviderCopy.getCreator());
		Assertions.assertEquals(encounterProvider.getDateCreated(), encounterProviderCopy.getDateCreated());
		Assertions.assertEquals(encounterProvider.getChangedBy(), encounterProviderCopy.getChangedBy());
		Assertions.assertEquals(encounterProvider.getDateChanged(), encounterProviderCopy.getDateChanged());
		Assertions.assertEquals(encounterProvider.getVoided(), encounterProviderCopy.getVoided());
		Assertions.assertEquals(encounterProvider.getVoidReason(), encounterProviderCopy.getVoidReason());
		Assertions.assertEquals(encounterProvider.getDateVoided(), encounterProviderCopy.getDateVoided());
		
		Assertions.assertEquals(encounterProvider.getEncounter(), encounterProviderCopy.getEncounter());
		Assertions.assertEquals(encounterProvider.getEncounterRole(), encounterProviderCopy.getEncounterRole());
		Assertions.assertEquals(encounterProvider.getProvider(), encounterProviderCopy.getProvider());
	}
}
