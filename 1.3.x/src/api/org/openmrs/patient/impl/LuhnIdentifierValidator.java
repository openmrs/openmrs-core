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

import org.openmrs.patient.UnallowedIdentifierException;

/**
 * A IdentifierValidator based on the Regenstrief Institute's version of the Luhn Algorithm.
 */
public class LuhnIdentifierValidator extends BaseHyphenatedIdentifierValidator {

	private static final String ALLOWED_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_";
	private static final String LUHN_NAME = "Luhn CheckDigit Validator";
	
	@Override
	protected int getCheckDigit(String undecoratedIdentifier){
//		 remove leading or trailing whitespace, convert to uppercase
		String trimmedUppercaseUndecoratedIdentifier = undecoratedIdentifier.trim().toUpperCase();

		// this will privatebe a running total
		int sum = 0;

		// loop through digits from right to left
		for (int i = 0; i < trimmedUppercaseUndecoratedIdentifier.length(); i++) {

			// set ch to "current" character to be processed
			char ch = trimmedUppercaseUndecoratedIdentifier.charAt(trimmedUppercaseUndecoratedIdentifier.length()
					- i - 1);
			
			// our "digit" is calculated using ASCII value - 48
			int digit = (int) ch - 48;

			// weight will be the current digit's contribution to
			// the running total
			int weight;
			if (i % 2 == 0) {

				// for alternating digits starting with the rightmost, we
				// use our formula this is the same as multiplying x 2 and
				// adding digits together for values 0 to 9. Using the
				// following formula allows us to gracefully calculate a
				// weight for non-numeric "digits" as well (from their
				// ASCII value - 48).
				weight = (2 * digit) - (int) (digit / 5) * 9;

			} else {

				// even-positioned digits just contribute their ascii
				// value minus 48
				weight = digit;

			}

			// keep a running total of weights
			sum += weight;

		}

		// avoid sum less than 10 (if characters below "0" allowed,
		// this could happen)
		sum = Math.abs(sum) + 10;

		// check digit is amount needed to reach next number
		// divisible by ten
		return (10 - (sum % 10)) % 10;
	}
	
	/**
	 * @see org.openmrs.patient.IdentifierValidator#getName()
	 */
	@Override
	public String getName() {
		return LUHN_NAME;
	}

	/**
     * @see org.openmrs.patient.IdentifierValidator#getAllowedCharacters()
     */
	@Override
    public String getAllowedCharacters() {
	    return ALLOWED_CHARS;
    }
	
	/**
	 * @see org.openmrs.patient.IdentifierValidator#getValidIdentifier(java.lang.String)
	 */
	@Override
	public String getValidIdentifier(String undecoratedIdentifier)
	        throws UnallowedIdentifierException {
		
		checkAllowedIdentifier(undecoratedIdentifier);
		
		int checkDigit = getCheckDigit(undecoratedIdentifier);
		
		String result = undecoratedIdentifier + "-" + checkDigit;
		return result;
	}
}
