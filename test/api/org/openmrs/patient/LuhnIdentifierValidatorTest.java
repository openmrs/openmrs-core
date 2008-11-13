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
import org.openmrs.patient.impl.LuhnIdentifierValidator;

/**
 * Tests the {@link LuhnIdentifierValidator}
 */
public class LuhnIdentifierValidatorTest {
	
	private LuhnIdentifierValidator validator = new LuhnIdentifierValidator();
	
	private String[] allowedIdentifiers = {"a", "123456", "ab32kcdak3", "chaseisreallycoolyay", "1", "moose", "MOOSE", "MooSE", "adD3Eddf429daD999"};
	private char[] allowedIdentifiersCheckDigits = {'D', 'G', 'J', 'H', 'I', 'H', 'H', 'H', 'B'};
	private char unusedCheckDigit = 'E';
	private int unusedCheckDigitInt = 0;
	private int[] allowedIdentifiersCheckDigitsInts = {3, 6, 9, 7, 8, 7, 7, 7, 1};
	private String[] invalidIdentifiers = {"", " ", "-", "adsfalasdf-adfasdf", "ABC DEF", "!234*", "++", " ABC", "def "};
	
	@Test
	public void shouldGetValidIdentifier(){
		
		//Make sure valid identifiers come back with the right check digit
		
		for(int i=0;i<allowedIdentifiers.length;i++){
			assertEquals(validator.getValidIdentifier(allowedIdentifiers[i]), allowedIdentifiers[i] + "-" + allowedIdentifiersCheckDigitsInts[i]);
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
		
		//Now test allowed identifiers that just have the wrong check digit.
		for(int i=0;i<allowedIdentifiers.length;i++){
			assertFalse(validator.isValid(allowedIdentifiers[i]+"-" + unusedCheckDigit));
			assertFalse(validator.isValid(allowedIdentifiers[i]+"-" + unusedCheckDigitInt));
		}
		
		//Now test allowed identifiers that have the right check digit.  Test with both
		//chars and ints.
		for(int i=0;i<allowedIdentifiers.length;i++){
			assertTrue(validator.isValid(allowedIdentifiers[i]+"-" + allowedIdentifiersCheckDigits[i]));
			assertTrue(validator.isValid(allowedIdentifiers[i]+"-" + allowedIdentifiersCheckDigitsInts[i]));
		}
	}
}
