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
package org.openmrs.patient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.openmrs.patient.impl.VerhoeffIdentifierValidator;

/**
 *
 */
public class VerhoeffIdentifierValidatorTest {

	private VerhoeffIdentifierValidator validator = new VerhoeffIdentifierValidator();
	
	private String[] allowedIdentifiers = {"12345678", "87654321", "11111111", "64537218", "00000000"};
	private char[] allowedIdentifiersCheckDigits = {'G', 'E', 'B', 'A', 'B'};
	private int[] allowedIdentifiersCheckDigitsInt = {6, 4, 1, 0, 2};
	private char unusedCheckDigit = 'C';
	private String[] invalidIdentifiers = {"", " ", "-", "adsfalasdf-adfasdf", "ABC DEF", "!234*", "++", " ABC", "def ", "ab32kcdak3", "chaseisreallycoolyay", "1", "moose", "MOOSE", "MooSE", "adD3Eddf429daD999"};
	
	@Test
	public void shouldGetValidIdentifier(){
		
		//Make sure valid identifiers come back with the right check digit
		
		for(int i=0;i<allowedIdentifiers.length;i++){
			assertEquals(validator.getValidIdentifier(allowedIdentifiers[i]), allowedIdentifiers[i] + "-" + allowedIdentifiersCheckDigits[i]);
		}
		
		//Make sure invalid identifiers throw an exception
		
		for(int j=0;j<invalidIdentifiers.length;j++){
			try{
				validator.getValidIdentifier(invalidIdentifiers[j]);
				fail("Identifier " + invalidIdentifiers[j] + " should have failed.");
			}catch(Exception e){}
		}
	}
	
	@Test
	public void shouldIsValid(){
		//Make sure invalid identifiers throw an exception
		
		for(int j=0;j<invalidIdentifiers.length;j++){
			try{
				validator.isValid(invalidIdentifiers[j]);
				fail("Identifier " + invalidIdentifiers[j] + " should have failed.");
			}catch(Exception e){}
		}
		
		for(int j=0;j<invalidIdentifiers.length;j++){
			try{
				validator.isValid(invalidIdentifiers[j] + "-H");
				fail("Identifier " + invalidIdentifiers[j] + " should have failed.");
			}catch(Exception e){}
		}
		
		for(int i=0;i<allowedIdentifiers.length;i++){
			try{
				validator.isValid(allowedIdentifiers[i] + "-X");
				fail("Identifier " + allowedIdentifiers[i] + " should have failed.");
			}catch(Exception e){}
			try{
				validator.isValid(allowedIdentifiers[i] + "-10");
				fail("Identifier " + allowedIdentifiers[i] + " should have failed.");
			}catch(Exception e){}
		}
		
		//Make sure check digit can't be numeric
		for(int j=0;j<invalidIdentifiers.length;j++){
			try{
				validator.isValid(allowedIdentifiers[j] + "-" + allowedIdentifiersCheckDigitsInt[j]);
				fail("Identifier " + allowedIdentifiers[j] + " should have failed.");
			}catch(Exception e){}
		}
		
		//Now test allowed identifiers that just have the wrong check digit.
		for(int i=0;i<allowedIdentifiers.length;i++){
			assertFalse(validator.isValid(allowedIdentifiers[i]+"-" + unusedCheckDigit));
		}
		
		//Now test allowed identifiers that have the right check digit.  Test with both
		//chars and ints.
		for(int i=0;i<allowedIdentifiers.length;i++){
			assertTrue(validator.isValid(allowedIdentifiers[i]+"-" + allowedIdentifiersCheckDigits[i]));
		}
		

	}
}
