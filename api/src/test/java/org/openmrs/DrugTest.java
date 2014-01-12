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

/**
 * Contains test methods for the {@link org.openmrs.Drug} class.
 */
public class DrugTest {

	/**
	 * @see {@link Drug#addDrugReferenceMap(DrugReferenceMap)}
	 */
	@Test
	@Verifies(value = "should set drug as the drug to which a mapping is being added", method = "addDrugReferenceMap(DrugReferenceMap)")
	public void addDrugReferenceMap_shouldSetDrugAsTheDrugToWhichAMappingIsBeingAdded() throws Exception {
		Drug drug1 = new Drug(1);
		Drug drug2 = new Drug(2);
		DrugReferenceMap map = new DrugReferenceMap(1);
		map.setDrug(drug2);
		drug1.addDrugReferenceMap(map);
		Assert.assertEquals(true, drug1.equals(drug1.getDrugReferenceMaps().iterator().next()));
	}

	/**
	 * @see {@link org.openmrs.Drug#addDrugReferenceMap(DrugReferenceMap)}
	 */
	@Test
	@Verifies(value = "should not add duplicate {@link DrugReferenceMap}s", method = "addDrugReferenceMap(DrugReferenceMap)")
	public void addDrugReferenceMap_shouldNotAddDuplicateDrugReferenceMaps() throws Exception {
		Drug drug = new Drug(1);

		DrugReferenceMap map1 = new DrugReferenceMap(1);
		DrugReferenceMap map2 = new DrugReferenceMap(2);
		DrugReferenceMap map2Duplicate = new DrugReferenceMap(2);

		drug.addDrugReferenceMap(map1);
		drug.addDrugReferenceMap(map2);
		drug.addDrugReferenceMap(map2Duplicate);

		Assert.assertEquals(2, drug.getDrugReferenceMaps().size());
	}
}
