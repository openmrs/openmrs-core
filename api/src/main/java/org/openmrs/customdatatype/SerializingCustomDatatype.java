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
	 * Most implementations should override this method to return a user-suitable String representation of
	 * typedValue in the given view. 
	 * 
	 * The default implementation returns typedValue.toString().
	 * 
	 * @param typedValue
	 * @param view
	 * @return
	 */
	public String doRender(T typedValue, String view) {
		return typedValue.toString();
	}
	
	/**
	 * This method will be called when a consumer wants to generate a view of an object very quickly, for example because
	 * they want to display 1000 <T>s in a list. The default implementation calls {@link #deserialize(String)} and {@link #doRender(Object, String)}
	 * with the default view. If an implementation's deserialize is slow, it should override this too.
	 * 
	 * @param serializedValue
	 * @return
	 */
	public String getQuickSummary(String serializedValue) {
		return doRender(deserialize(serializedValue), CustomDatatype.VIEW_DEFAULT);
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
		if (typedValue == null)
			throw new InvalidCustomValueException("cannot be null");
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
	 * Default implementation calls {@link #doRender(Object, String)}. Most implementations should override that
	 * other method, but if {@link #deserialize(String)} is expensive, then you should override this method instead.
	 * @see org.openmrs.customdatatype.CustomDatatype#render(java.lang.String, java.lang.String)
	 */
	@Override
	public String render(String serializedValue, String view) {
		if (CustomDatatype.VIEW_FAST.equals(view))
			return getQuickSummary(serializedValue);
		else
			return doRender(deserialize(serializedValue), view);
	}
	
}
