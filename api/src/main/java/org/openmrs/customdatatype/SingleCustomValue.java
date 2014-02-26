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

import org.openmrs.GlobalProperty;
import org.openmrs.Visit;
import org.openmrs.VisitAttribute;

/**
 * Either a one-off custom value (e.g. a {@link GlobalProperty}) or a single custom value within a {@link Customizable}
 * object that may hold multiple (e.g. a {@link VisitAttribute} within a {@link Visit}).
 * The "referenceString" property is a String suitable for persistance in a database varchar column. It is typically a
 * reference to the real value (e.g. it is the UUID of a location or the URI of an image in a PACS).
 * The "objectValue" property accessors are convenience methods that use a {@link CustomDatatype} to convert
 * to/from the String serializedValue.
 * @param <D> the descriptor for this value, e.g. VisitAttribute implements SingleCustomValue<VisitAttributeType>
 * @see {@link CustomDatatype}
 * @since 1.9
 */
public interface SingleCustomValue<D extends CustomValueDescriptor> {
	
	/**
	 * The metadata describing this custom value
	 * @return
	 */
	D getDescriptor();
	
	/**
	 * @return the value persisted in a database in a varchar column. Not necessarily human-readable.
	 * @throws NotYetPersistedException if valueReference hasn't been set by the CustomDatatype yet
	 */
	String getValueReference() throws NotYetPersistedException;
	
	/**
	 * Directly set the String value that OpenMRS should persist in the database
	 * in a varchar column. Implementations should validate this value and throw an
	 * {@link InvalidCustomValueException} if it's invalid, rather than setting it blindly. 
	 * If you are coding against the OpenMRS API, you should use {@link #setValue(Object)}
	 * instead.
	 * @param valueToPersist
	 */
	void setValueReferenceInternal(String valueToPersist) throws InvalidCustomValueException;
	
	/**
	 * Convenience method to get the typed version of the serializedValue. (This will result in a call
	 * to a {@link CustomDatatype#fromReferenceString(String)}.)
	 * @return typed value, converted from serializedValue by a CustomDatatype
	 * @throws InvalidCustomValueException 
	 */
	Object getValue() throws InvalidCustomValueException;
	
	/**
	 * Sets the typed value. (This will result in a call to {@link CustomDatatype#toReferenceString(Object)}
	 * @param typedValue
	 * @throws InvalidCustomValueException
	 */
	<T> void setValue(T typedValue) throws InvalidCustomValueException;
	
	/**
	 * @return whether or not setValue has been called (thus {@link CustomDatatype#save(Object, String)} needs to be called
	 */
	boolean isDirty();
	
}
