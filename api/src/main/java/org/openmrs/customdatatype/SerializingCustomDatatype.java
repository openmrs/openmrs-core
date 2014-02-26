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
 *
 */
public abstract class SerializingCustomDatatype<T> implements CustomDatatype<T> {
	
	/**
	 * @param typedValue (has already had validate called)
	 * @return a String representation of typedValue
	 */
	public abstract String serialize(T typedValue);
	
	/**
	 * @param serializedValue
	 * @return the reconstructed typed version of serializedValue
	 */
	public abstract T deserialize(String serializedValue);
	
	/**
	 * Most implementations should override this method to return plain-text summary of the typed value, as defined
	 * by {@link CustomDatatype#getTextSummary(String)}. If {@link #deserialize(String)} is expensive, then the
	 * implementation should override {@link #getTextSummary(String)} instead.
	 *
	 * The default implementation returns typedValue.toString(), and indicates it is complete.
	 *
	 * @param typedValue
	 * @return a plain-text summary of the typed value
	 */
	public CustomDatatype.Summary doGetTextSummary(T typedValue) {
		return new CustomDatatype.Summary(typedValue.toString(), true);
	}
	
	/**
	 * Does nothing in the default implementation
	 * @see org.openmrs.customdatatype.CustomDatatype#setConfiguration(java.lang.String)
	 */
	@Override
	public void setConfiguration(String config) {
		// not used
	}
	
	/**
	 * Passes for all non-null values in the default implementation
	 * @see org.openmrs.customdatatype.CustomDatatype#validate(java.lang.Object)
	 */
	public void validate(T typedValue) throws InvalidCustomValueException {
		if (typedValue == null) {
			throw new InvalidCustomValueException("cannot be null");
		}
	}
	
	/**
	 * @see org.openmrs.customdatatype.CustomDatatype#fromReferenceString(java.lang.String)
	 */
	@Override
	public T fromReferenceString(String persistedValue) throws InvalidCustomValueException {
		return deserialize(persistedValue);
	}
	
	/**
	 * @see org.openmrs.customdatatype.CustomDatatype#save(java.lang.Object, java.lang.String)
	 */
	public String save(T typedValue, String existingValueReference) throws InvalidCustomValueException {
		validate(typedValue);
		return serialize(typedValue);
	}
	
	/**
	 * @see org.openmrs.customdatatype.CustomDatatype#getReferenceStringForValue(java.lang.Object)
	 */
	public String getReferenceStringForValue(T typedValue) throws UnsupportedOperationException {
		return serialize(typedValue);
	}
	
	/**
	 * Default implementation calls {@link #doGetTextSummary(Object)}. Most implementations should override that
	 * other method, but if {@link #deserialize(String)} is expensive, then you should override this method instead.
	 * @see org.openmrs.customdatatype.CustomDatatype#getTextSummary(java.lang.String)
	 */
	@Override
	public CustomDatatype.Summary getTextSummary(String referenceString) {
		if (referenceString == null) {
			return new CustomDatatype.Summary("", true);
		} else {
			return doGetTextSummary(deserialize(referenceString));
		}
	}
	
}
