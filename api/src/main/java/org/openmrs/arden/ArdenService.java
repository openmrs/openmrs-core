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

import org.openmrs.api.APIException;

public interface ArdenService {
	
	/**
	 * @param file - mlm file to be parsed
	 * @should get and parse mlms
	 */
	public void compileFile(String file, String outFolder);
	
	/**
	 * Parse arden rule definition from a string and then output java file in the designated output
	 * directory. The output of the parsing will be a subclass of the <code>Rule</code> object that
	 * can be used using the logic service
	 * 
	 * @param ardenRuleDefinition the rule definition to be parsed
	 * @param outFolder the java output directory location. this is not the full path with the
	 *            package structure
	 * @throws Exception when the rule definition parser doesn't recognize the arden structure
	 * @since 1.8
	 */
	public void compile(String ardenRuleDefinition, String outFolder) throws APIException;
	
}
