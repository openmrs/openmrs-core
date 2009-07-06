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
package org.openmrs.serialization.xstream.strategy;

/**
 * This class just refers to the logic in XStream's source, it will generate the incremental id for
 * elements appearing in serialized xml.
 */
public class SequenceGenerator implements CustomReferenceByIdMarshaller.IDGenerator {
	
	private int counter;
	
	/**
	 * @param startsAt
	 */
	public SequenceGenerator(int startsAt) {
		this.counter = startsAt;
	}
	
	/**
	 * @see org.openmrs.serialization.xstream.strategy.CustomReferenceByIdMarshaller.IDGenerator#next(java.lang.Object)
	 */
	public String next(Object item) {
		return String.valueOf(counter++);
	}
	
}
