/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * 
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import org.codehaus.jackson.SerializableString;
import org.codehaus.jackson.io.CharacterEscapes;
import org.codehaus.jackson.io.SerializedString;

/**
 * An instance of this class can be passed to an ObjectMapper instance when serializing objects to
 * JSON using the jackson API so as to escape html and scripts inside html tags
 */
public class OpenmrsCharacterEscapes extends CharacterEscapes {
	
	private int[] asciiEscapes;
	
	public OpenmrsCharacterEscapes() {
		// start with set of characters known to require escaping (double-quote, backslash etc)
		int[] esc = CharacterEscapes.standardAsciiEscapesForJSON();
		
		// and force escaping of a few others:
		esc['<'] = CharacterEscapes.ESCAPE_CUSTOM;
		esc['>'] = CharacterEscapes.ESCAPE_CUSTOM;
		
		asciiEscapes = esc;
	}
	
	@Override
	public int[] getEscapeCodesForAscii() {
		return asciiEscapes;
	}
	
	@Override
	public SerializableString getEscapeSequence(int ch) {
		if (ch == '<') {
			return new SerializedString("&lt;");
		} else if (ch == '>') {
			return new SerializedString("&gt;");
		}
		return null;
	}
}
