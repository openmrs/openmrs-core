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
package org.openmrs.arden;

import org.openmrs.api.APIException;
import org.springframework.transaction.annotation.Transactional;

@Transactional
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
