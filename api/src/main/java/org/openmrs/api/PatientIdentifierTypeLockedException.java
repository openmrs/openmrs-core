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
 * Copyright (C) OpenMRS, LLC. All Rights Reserved.
 */
package org.openmrs.api;

import org.openmrs.util.OpenmrsConstants;

/**
 * This exception is thrown when a user tries manipulate of a patient identifier type while patient identifier types are locked
 * 
 * @see OpenmrsConstants#GLOBAL_PROPERTY_PATIENT_IDENTIFIER_TYPES_LOCKED
 * @see PatientService#checkIfPatientIdentifierTypesAreLocked() 
 */
public class PatientIdentifierTypeLockedException extends APIException {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Generic constructor that gives a normal reason why the user is not being allowed to manipulate of a patient identifier type.
	 */
	public PatientIdentifierTypeLockedException() {
		this("PatientIdentifierType.locked");
	}
	
	/**
	 * Convenience constructor to give the user a message other than normal default one
	 * @param message the reason to show to the user as to why we cannot manipulate of a patient identifier type
	 */
	public PatientIdentifierTypeLockedException(String message) {
		super(message);
	}
	
	/**
	 * Convenience constructor to give the user a message other than normal one and to chain
	 * this exception with a parent exception.
	 * 
	 * @param message the reason to show to the user as to why we cannot manipulate of a person attribute type
	 * @param cause the parent exception
	 */
	public PatientIdentifierTypeLockedException(String message, Throwable cause) {
		super(message, cause);
	}
	
	/**
	 * Convenience constructor used to only set the parent exception to chain with.
	 * This does not set the error message for the user as to why an exception is being thrown.
	 * The {@link #PatientIdentifierTypeLockedException(String, Throwable)} constructor is preferred over this one.
	 * @param cause the parent exception
	 */
	public PatientIdentifierTypeLockedException(Throwable cause) {
		super(cause);
	}
}
