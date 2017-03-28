/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Drug;

/**
 * The Class DrugsByNameComparatorTest. Contains tests for DrugsByNameComparator
 */
public class DrugsByNameComparatorTest {
	
	/**
	 * @see DrugsByNameComparator#compareDrugNamesIgnoringNumericals(Drug,Drug)
	 */
	@Test
	public void compareDrugNamesIgnoringNumericals_shouldReturnNegativeIfNameForDrug1ComesBeforeThatOfDrug2()
	{
		Drug drug1 = new Drug();
		drug1.setName("ABCD");
		Drug drug2 = new Drug();
		drug2.setName("BCDE");
		DrugsByNameComparator dComparator = new DrugsByNameComparator();
		int actualValue = dComparator.compare(drug1, drug2);
		Assert.assertEquals(actualValue, -1);
	}
	
	/**
	 * @see DrugsByNameComparator#compareDrugNamesIgnoringNumericals(Drug,Drug)
	 */
	@Test
	public void compareDrugNamesIgnoringNumericals_shouldReturnZeroIfNameForDrug1ComesBeforeThatOfDrug2() {
		Drug drug1 = new Drug();
		drug1.setName("ABCD");
		Drug drug2 = new Drug();
		drug2.setName("abcd");
		DrugsByNameComparator dComparator = new DrugsByNameComparator();
		int actualValue = dComparator.compare(drug1, drug2);
		Assert.assertEquals(actualValue, 0);
	}
	
	/**
	 * @see DrugsByNameComparator#compareDrugNamesIgnoringNumericals(Drug,Drug)
	 */
	@Test
	public void compareDrugNamesIgnoringNumericals_shouldReturnPositiveIfNameForDrug1ComesBeforeThatOfDrug2IgnoringDashes()
	{
		Drug drug1 = new Drug();
		drug1.setName("AB-AB");
		Drug drug2 = new Drug();
		drug2.setName("ABAA");
		DrugsByNameComparator dComparator = new DrugsByNameComparator();
		int actualValue = dComparator.compare(drug1, drug2);
		Assert.assertEquals(actualValue, 1);
	}
	
	/**
	 * @see DrugsByNameComparator#compareDrugNamesIgnoringNumericals(Drug,Drug)
	 */
	@Test
	public void compareDrugNamesIgnoringNumericals_shouldReturnPositiveIfNameForDrug1ComesBeforeThatOfDrug2IgnoringNumerics()
	{
		Drug drug1 = new Drug();
		drug1.setName("AB1AB");
		Drug drug2 = new Drug();
		drug2.setName("ABAA");
		DrugsByNameComparator dComparator = new DrugsByNameComparator();
		int actualValue = dComparator.compare(drug1, drug2);
		Assert.assertEquals(actualValue, 1);
	}
	
}
