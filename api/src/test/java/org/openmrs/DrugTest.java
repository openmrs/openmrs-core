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
import org.openmrs.test.BaseContextSensitiveTest;

/**
 * Contains test methods for {@link org.openmrs.Drug}.
 */
public class DrugTest extends BaseContextSensitiveTest {
	
	private final static String UUID_1 = "333cd82c-7d3d-11e3-8633-13f177b345d8";
	
	private final static String UUID_2 = "4eef1530-7d3d-11e3-ac6d-e388e198a21e";
	
	/**
	 * @verifies set drug as the drug to which a mapping is being added
	 * @see Drug#addDrugReferenceMap(DrugReferenceMap)
	 */
	@Test
	public void addDrugReferenceMap_shouldSetDrugAsTheDrugToWhichAMappingIsBeingAdded() throws Exception {
		Drug drug1 = new Drug();
		drug1.setUuid(UUID_1);
		Drug drug2 = new Drug();
		drug2.setUuid(UUID_2);
		
		DrugReferenceMap map = new DrugReferenceMap();
		map.setDrug(drug2);
		drug1.addDrugReferenceMap(map);
		Assert.assertEquals(drug1, drug1.getDrugReferenceMaps().iterator().next().getDrug());
	}
	
	/**
	 * @verifies should not add duplicate drug reference maps
	 * @see Drug#addDrugReferenceMap(DrugReferenceMap)
	 */
	@Test
	public void addDrugReferenceMap_shouldShouldNotAddDuplicateDrugReferenceMaps() throws Exception {
		Drug drug = new Drug();
		
		DrugReferenceMap map1 = new DrugReferenceMap();
		map1.setUuid(UUID_1);
		DrugReferenceMap map2 = new DrugReferenceMap();
		map2.setUuid(UUID_2);
		DrugReferenceMap map2Duplicate = new DrugReferenceMap();
		map2Duplicate.setUuid(UUID_2);
		
		drug.addDrugReferenceMap(map1);
		drug.addDrugReferenceMap(map2);
		drug.addDrugReferenceMap(map2Duplicate);
		
		Assert.assertEquals(2, drug.getDrugReferenceMaps().size());
	}
}
