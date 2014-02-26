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
package org.openmrs.util;

import junit.framework.Assert;

import org.junit.Test;
import org.openmrs.Drug;

/**
 * The Class DrugsByNameComparatorTest. Contains tests for DrugsByNameCOmparator
 */
public class DrugsByNameComparatorTest {
	
	/**
	 * @see DrugsByNameComparator#compareDrugNamesIgnoringNumericals(Drug,Drug)
	 * @verifies return negative if name for drug1 comes before that of drug2
	 */
	@Test
	public void compareDrugNamesIgnoringNumericals_shouldReturnNegativeIfNameForDrug1ComesBeforeThatOfDrug2()
	        throws Exception {
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
	 * @verifies return zero if name for drug1 comes before that of drug2
	 */
	@Test
	public void compareDrugNamesIgnoringNumericals_shouldReturnZeroIfNameForDrug1ComesBeforeThatOfDrug2() throws Exception {
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
	 * @verifies return positive if name for drug1 comes before that of drug2 ignoring dashes
	 */
	@Test
	public void compareDrugNamesIgnoringNumericals_shouldReturnPositiveIfNameForDrug1ComesBeforeThatOfDrug2IgnoringDashes()
	        throws Exception {
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
	 * @verifies return positive if name for drug1 comes before that of drug2 ignoring numerics
	 */
	@Test
	public void compareDrugNamesIgnoringNumericals_shouldReturnPositiveIfNameForDrug1ComesBeforeThatOfDrug2IgnoringNumerics()
	        throws Exception {
		Drug drug1 = new Drug();
		drug1.setName("AB1AB");
		Drug drug2 = new Drug();
		drug2.setName("ABAA");
		DrugsByNameComparator dComparator = new DrugsByNameComparator();
		int actualValue = dComparator.compare(drug1, drug2);
		Assert.assertEquals(actualValue, 1);
	}
	
}
