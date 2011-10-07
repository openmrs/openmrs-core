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
package org.openmrs.customdatatype;

/**
 * Represents a custom datatype, which an administrator may use for global properties, attribute types, etc.
 * Handles conversion between a typed Java object and a reference string which can be persisted in a database
 * varchar column. 
 * @param <T> the Java class used for typed values
 * @since 1.9 
 */
public interface CustomDatatype<T> {
	
	/**
	 * A {@link CustomValueDescriptor} defines both a datatype and its configuration (e.g. a regex for a RegexValidatedString datatype).
	 * The framework will instantiate datatypes and call this method to set that configuration. Subclasses should define the format
	 * of this configuration.
	 * 
	 * @param config
	 */
	void setConfiguration(String config);
	
	/**
	 * Converts a typed value to a reference string (e.g. a UUID for a location, or a URI for an image in a PACS).
	 * Implementations of this method should also call {@link #validate(Object)}.
	 * @param typedValue run-time type should be T
	 * @return the {@link String} representation of the typed value, which will be persisted in the database
	 * @throws InvalidCustomValueException if the value is not valid
	 */
	String toReferenceString(T typedValue) throws InvalidCustomValueException;
	
	/**
	 * Converts a reference string to its typed value. This may be expensive.
	 * @param persistedValue
	 * @return converts a previously-serialized value back to its original type
	 * @throws InvalidCustomValueException if the persisted value is illegal (perhaps because datatype configuration
	 * was changed since this value was persisted)
	 */
	T fromReferenceString(String persistedValue) throws InvalidCustomValueException;
	
	/**
	 * TODO where are well-known view constants?
	 * @param referenceString
	 * @param view
	 * @return display representation of the given value, suitable for the given view
	 */
	String render(String referenceString, String view);
	
	/**
	 * Validates the given persisted value to see if it is a legal value for the given handler. (Implementations may
	 * implement this simply as validate(fromPersistentString(persistedValue)).
	 * 
	 * @param typedValue
	 */
	void validateReferenceString(String persistedValue) throws InvalidCustomValueException;
	
	/**
	 * Validates the given value to see if it is a legal value for the given handler. (For example the RegexValidatedText
	 * type checks against a regular expression.)
	 * @param typedValue
	 */
	void validate(T typedValue) throws InvalidCustomValueException;
	
}
