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

import java.util.Set;

import org.openmrs.ConceptComplex;
import org.openmrs.FormField;

/**
 * Interface for handling complex obs. Implementing classes are responsible for generating
 * {@link FormField}s to embed inside a parent {@link FormField}. When a form is submitted and it
 * has a {@link ConceptComplex} associated to an implementing handler class, then the handler's
 * serializeFormData method is invoked to perform the serialization of the complex data.
 * 
 * @since 1.10
 */
public interface SerializableComplexObsHandler extends ComplexObsHandler {
	
	/**
	 * Gets the form fields that should be added to the forms using complex concepts that are
	 * associated to this handler
	 * 
	 * @return Set of form fields
	 */
	public Set<FormField> getFormFields();
	
	/**
	 * Transforms the incoming data from one format to another. For example, this can be useful if
	 * the data is to be sent as an hl7 message which doesn't support xml
	 * 
	 * @param data the data to serialize
	 * @return the serialized form data
	 */
	public String serializeFormData(String data);
	
}
