/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.customdatatype.datatype;

import org.openmrs.Program;
import org.openmrs.customdatatype.SerializingCustomDatatype;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class ProgramDatatype extends SerializingCustomDatatype<Program> {
	
	private final static XStream xstream = new XStream(new DomDriver());
	
	/**
	 * @see org.openmrs.customdatatype.SerializingCustomDatatype#serialize(java.lang.Object)
	 */
	@Override
	public String serialize(Program typedValue) {
		if (typedValue == null)
			return null;
		return xstream.toXML(typedValue);
	}
	
	/**
	 * @see org.openmrs.customdatatype.SerializingCustomDatatype#deserialize(java.lang.String)
	 */
	@Override
	public Program deserialize(String serializedValue) {
		return (Program) xstream.fromXML(serializedValue);
	}
	
}
