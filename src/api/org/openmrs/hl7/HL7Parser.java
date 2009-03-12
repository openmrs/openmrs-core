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
package org.openmrs.hl7;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.io.IOUtils;

import ca.uhn.hl7v2.HL7Exception;

public class HL7Parser {
	
	public HL7Parser() {
	}
	
	public void parseHL7InQueue(HL7InQueue hl7InQueue) throws HL7Exception {
		String hl7Data = hl7InQueue.getHL7Data();
		parse(IOUtils.toInputStream(hl7Data));
	}
	
	public void parse(InputStream hl7Stream) throws HL7Exception {
		new BufferedReader(new InputStreamReader(hl7Stream));
		
	}
	
}
