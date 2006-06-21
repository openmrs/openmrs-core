package org.openmrs.api.util;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openmrs.util.Helper;

public class HelperTest extends TestCase {
	
	public void testGetCheckDigit() throws Exception {
		
		String[] ids = {"9", "99", "999", "123MT", "asdf"};
		int[] cds = {1, 2, 3, 2, 8} ;
		
		for (int i = 0; i< ids.length; i++) {
			System.out.println(ids[i]);
			assertEquals(Helper.getCheckDigit(ids[i]), cds[i]);
		}
		
		String[] ids2 = {"9-1", "99-2", "999-3", "123MT-2", "asdf-8"};
		for (int i = 0; i< ids2.length; i++) {
			System.out.println(ids2[i]);
			assertTrue(Helper.isValidCheckDigit(ids2[i]));
		}
		
		String[] ids3 = {"asdf-7", "9-2", "9-4"};
		for (int i = 0; i< ids3.length; i++) {
			System.out.println(ids3[i]);
			assertFalse(Helper.isValidCheckDigit(ids3[i]));
		}
		
		String[] ids4 = {"#@!", "234-3-3", "-3", "2134"};
		for (int i = 0; i< ids4.length; i++) {
			try {
				System.out.println(ids4[i]);
				Helper.isValidCheckDigit(ids4[i]);
				fail();
			}
			catch (Exception e) {}
		}
	}
	
	public void testIsValidCheckDigit() throws Exception {
		
	}

	
	
	public static Test suite() {
		return new TestSuite(HelperTest.class, "Basic Helper class tests");
	}

}
