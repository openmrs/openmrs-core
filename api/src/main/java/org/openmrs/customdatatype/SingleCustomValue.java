/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
 * @param <D> the descriptor for this value, e.g. VisitAttribute implements SingleCustomValue&lt;VisitAttributeType&gt;
 * @see CustomDatatype
 * @since 1.9
 */
public interface SingleCustomValue<D extends CustomValueDescriptor> {
	
	/**
	 * @return metadata describing this custom value
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
	 * Sets the typed value. (This will result in a call to {@link CustomDatatype#getReferenceStringForValue(Object)}
	 * @param typedValue
	 * @throws InvalidCustomValueException
	 */
	<T> void setValue(T typedValue) throws InvalidCustomValueException;
	
	/**
	 * @return whether or not setValue has been called (thus {@link CustomDatatype#save(Object, String)} needs to be called
	 */
	boolean isDirty();
	
}
