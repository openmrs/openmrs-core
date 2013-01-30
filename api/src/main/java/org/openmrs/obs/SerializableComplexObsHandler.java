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
package org.openmrs.obs;

/**
 * Interface for handling complex obs. Implementing classes are responsible for generating a segment
 * to embed inside a form schema and serialization of the complex data to a meaningful format.
 * 
 * @since 1.10
 */
public interface SerializableComplexObsHandler extends ComplexObsHandler {
	
	/**
	 * Generates the segment to be included in a form schema. Note that this should exclude the xml
	 * declaration, schema opening and closing tags
	 * 
	 * @param format the format of the generated text e.g xml, json etc
	 * @return the generated segment
	 */
	public String getSchema(String format);
	
	/**
	 * Transforms the incoming data from one format to another. For example, this can be useful if
	 * the data is to be sent as an hl7 message which doesn't support xml
	 * 
	 * @param data the data to serialize
	 * @return the serialized form data
	 */
	public String serializeFormData(String data);
	
}
