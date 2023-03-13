/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
