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
package org.openmrs.patient.impl;

import org.openmrs.patient.IdentifierValidator;
import org.openmrs.patient.UnallowedIdentifierException;

/**
 * An abstract class for identifier validators for identifiers that have a hyphen before a single check digit.  
 * Identifiers can not be null, must have at least one character before the check digit,
 * and can not contain white space.
 * 
 * Integers 0-9 or characters A-J are allowed for the check digit.  A character is used by default.
 */
public abstract class BaseHyphenatedIdentifierValidator implements
        IdentifierValidator {

	protected abstract int getCheckDigit(String undecoratedIdentifier);
	
	/**
	 * @see org.openmrs.patient.IdentifierValidator#getAllowedCharacters()
	 */
	public abstract String getAllowedCharacters();

	/**
	 * @see org.openmrs.patient.IdentifierValidator#getName()
	 */
	public abstract String getName();

	/**
	 * @see org.openmrs.patient.IdentifierValidator#getValidIdentifier(java.lang.String)
	 */
	public String getValidIdentifier(String undecoratedIdentifier)
	        throws UnallowedIdentifierException {
		
		checkAllowedIdentifier(undecoratedIdentifier);
		
		char checkLetter = convertCheckDigitToChar(getCheckDigit(undecoratedIdentifier));
		
		String result = undecoratedIdentifier + "-" + checkLetter;
		return result;
	}

	/**
	 * @see org.openmrs.patient.IdentifierValidator#isValid(java.lang.String)
	 */
	public boolean isValid(String identifier)
	        throws UnallowedIdentifierException {
		
		if ( identifier.indexOf("-") < 1 ) {
			throw new UnallowedIdentifierException("Identifier must contain something besides the check digit.");
		}
		
		String idWithoutCheckDigit = identifier.substring(0, identifier.indexOf("-"));

		checkAllowedIdentifier(idWithoutCheckDigit);
		
		int computedCheckDigit = getCheckDigit(idWithoutCheckDigit);

		String checkDigit = identifier.substring(identifier.indexOf("-") + 1, identifier.length());
		
		if(checkDigit.length() != 1)
			throw new UnallowedIdentifierException("Identifier must have a check digit of length 1.");
		
		if ( checkDigit.equalsIgnoreCase("A") ) checkDigit = "0";
		if ( checkDigit.equalsIgnoreCase("B") ) checkDigit = "1";
		if ( checkDigit.equalsIgnoreCase("C") ) checkDigit = "2";
		if ( checkDigit.equalsIgnoreCase("D") ) checkDigit = "3";
		if ( checkDigit.equalsIgnoreCase("E") ) checkDigit = "4";
		if ( checkDigit.equalsIgnoreCase("F") ) checkDigit = "5";
		if ( checkDigit.equalsIgnoreCase("G") ) checkDigit = "6";
		if ( checkDigit.equalsIgnoreCase("H") ) checkDigit = "7";
		if ( checkDigit.equalsIgnoreCase("I") ) checkDigit = "8";
		if ( checkDigit.equalsIgnoreCase("J") ) checkDigit = "9";
		
		int givenCheckDigit = 10;
		
		try{
			givenCheckDigit = Integer.valueOf(checkDigit);
		}catch(NumberFormatException e){
			throw new UnallowedIdentifierException("Check digit must either be a character from A to J or a single digit integer.");
		}

		return (computedCheckDigit == givenCheckDigit);
	}

	/**
     * @param undecoratedIdentifier
     * @throws UnallowedIdentifierException if identifier contains unallowed characters or is 
     * otherwise invalid.
     */
    protected void checkAllowedIdentifier(String undecoratedIdentifier) throws UnallowedIdentifierException{
    	if(undecoratedIdentifier == null)
    		throw new UnallowedIdentifierException("Identifier can not be null.");
    	if(undecoratedIdentifier.length() == 0 )
    		throw new UnallowedIdentifierException("Identifier must contain at least one character.");
    	if(undecoratedIdentifier.contains(" "))
			throw new UnallowedIdentifierException("Identifier may not contain white space.");
    	for(int i =0 ; i<undecoratedIdentifier.length(); i++){
    		if(getAllowedCharacters().indexOf(undecoratedIdentifier.charAt(i))==-1)
    			throw new UnallowedIdentifierException("\"" + undecoratedIdentifier.charAt(i) + "\" is an invalid character.");
    	}
    }
    
	/**
     * Not doing this with ASCII math to be extra careful.
     * 
     * @param checkDigit
     * @return
     */
    private char convertCheckDigitToChar(int checkDigit) {
    	switch(checkDigit){
    		case 0: return 'A';
    		case 1: return 'B';
    		case 2: return 'C';
    		case 3: return 'D';
    		case 4: return 'E';
    		case 5: return 'F';
    		case 6: return 'G';
    		case 7: return 'H';
    		case 8: return 'I';
    		case 9: return 'J';
    		default: return 'X';
    	}
    }
}
