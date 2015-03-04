/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.arden;

import java.io.Writer;

/**
 *
 */
public class Conclude {
	
	private Boolean concludeVal = null;
	
	public Conclude(boolean concludeVal) {
		this.concludeVal = concludeVal;
	}
	
	public String getConcludeVal() {
		String retVal;
		if (concludeVal == true) {
			retVal = "true";
		} else {
			retVal = "false";
		}
		
		return retVal;
	}
	
	public void write(Writer w) {
		try {
			w.append("\t\t\treturn ");
			w.append(getConcludeVal());
			w.append(";\n");
		}
		catch (Exception e) {}
	}
}
