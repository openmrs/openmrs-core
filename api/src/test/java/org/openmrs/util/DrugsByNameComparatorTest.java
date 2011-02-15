package org.openmrs.util;

import junit.framework.Assert;

import org.junit.Test;
import org.openmrs.Drug;
import org.openmrs.test.Verifies;

/**
 * The Class DrugsByNameComparatorTest. Contains tests for DrugsByNameCOmparator
 */
public class DrugsByNameComparatorTest {
	
	/**
	 * Compare drug names ignoring numericals_simple test with no numericals.
	 */
	@Test
	@Verifies(value = "should return negative if Name for drug1 comes before that of drug2", method = "compare(Drug d1, Drug d2)")
	public void compareDrugNamesIgnoringNumericals_simpleTestwithNoNumericals() {
		Drug drug1 = new Drug();
		drug1.setName("ABCD");
		Drug drug2 = new Drug();
		drug2.setName("BCDE");
		DrugsByNameComparator dComparator = new DrugsByNameComparator();
		int actualValue = dComparator.compare(drug1, drug2);
		Assert.assertEquals(actualValue, -1);
	}
	
	/**
	 * Compare drug names ignoring numericals_simple test for upper case.
	 */
	@Test
	@Verifies(value = "should return zero if Name for drug1 comes before that of drug2", method = "compare(Drug d1, Drug d2)")
	public void compareDrugNamesIgnoringNumericals_simpleTestforUpperCase() {
		Drug drug1 = new Drug();
		drug1.setName("ABCD");
		Drug drug2 = new Drug();
		drug2.setName("abcd");
		DrugsByNameComparator dComparator = new DrugsByNameComparator();
		int actualValue = dComparator.compare(drug1, drug2);
		Assert.assertEquals(actualValue, 0);
	}
	
	/**
	 * Compare drug names ignoring numericals_test ignoring special characters.
	 */
	@Test
	@Verifies(value = "should return positive if Name for drug1 comes before that of drug2", method = "compare(Drug d1, Drug d2)")
	public void compareDrugNamesIgnoringNumericals_testIgnoringSpecialCharacters() {
		Drug drug1 = new Drug();
		drug1.setName("AB-AB");
		Drug drug2 = new Drug();
		drug2.setName("ABAA");
		DrugsByNameComparator dComparator = new DrugsByNameComparator();
		int actualValue = dComparator.compare(drug1, drug2);
		Assert.assertEquals(actualValue, 1);
	}
	
	/**
	 * Compare drug names ignoring numericals_test ignoring numericals.
	 */
	@Test
	@Verifies(value = "should return positive if Name for drug1 comes before that of drug2", method = "compare(Drug d1, Drug d2)")
	public void compareDrugNamesIgnoringNumericals_testIgnoringNumericals() {
		Drug drug1 = new Drug();
		drug1.setName("AB1AB");
		Drug drug2 = new Drug();
		drug2.setName("ABAA");
		DrugsByNameComparator dComparator = new DrugsByNameComparator();
		int actualValue = dComparator.compare(drug1, drug2);
		Assert.assertEquals(actualValue, 1);
	}
	
}
