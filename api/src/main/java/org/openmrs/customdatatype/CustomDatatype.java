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
	 * The OpenMRS service layer calls this method when a custom value of this datatype is saved (created or edited). Implementations
	 * should persist the typed value, and return a valueReference that can be used to access that value in the future.
	 * (Simple datatype implementations that don't require external storage may just serialize their typedValue to a String and
	 * return that.)
	 * 
	 * Implementations may safely assume that validate was called on typedValue before this method is called.
	 * 
	 * @param typedValue
	 * @param existingValueReference If null, the custom value is being saved for the first time. If not null, this custom value has
	 * been saved before with the given reference. Implementations may choose to return the same value reference if they are overwriting
	 * the old value on remote storage. 
	 * @return a valueReference that may be used in the future to retrieve typedValue
	 * @throws InvalidCustomValueException
	 */
	String save(T typedValue, String existingValueReference) throws InvalidCustomValueException;
	
	/**
	 * Gets the reference string that would be persisted for the given typed value. (This allows efficient searching for exact attribute
	 * values.)
	 * 
	 * @param typedValue
	 * @return
	 * @throws UnsupportedOperationException  if it is not feasible to calculate this efficiently (e.g. you'd need to go to remote storage)
	 */
	String getReferenceStringForValue(T typedValue) throws UnsupportedOperationException;
	
	/**
	 * Converts a reference string to its typed value. This may be expensive, especially if the datatype needs
	 * to go to remote storage.
	 * 
	 * @param referenceString
	 * @return the actual typed value for the given referenceString
	 * @throws InvalidCustomValueException if the persisted value is illegal (perhaps because datatype configuration
	 * was changed since this value was persisted)
	 */
	T fromReferenceString(String referenceString) throws InvalidCustomValueException;
	
	/**
	 * Converts a reference string to a short (generally < 100 characters) plain-text representation of its value. The return
	 * value also indicates whether this representation is a complete view of the value, or if there is more to display. 
	 * Implementations of this method must be high-performance, e.g. if the method is called thousands of times for a table
	 * of objects with custom values.
	 * 
	 * @param referenceString
	 * @return a summary representation of the given value
	 */
	Summary getTextSummary(String referenceString);
	
	/**
	 * Validates the given value to see if it is a legal value for the given handler. (For example the RegexValidatedText
	 * type checks against a regular expression.)
	 * @param typedValue
	 */
	void validate(T typedValue) throws InvalidCustomValueException;
	
	/**
	 * A short reprepresentation of a custom value, along with an indication of whether this is the complete value,
	 * or just a summary.
	 */
	public class Summary {
		
		private String summary;
		
		private boolean complete;
		
		/**
		 * @param summary
		 * @param complete
		 */
		public Summary(String summary, boolean complete) {
			this.summary = summary;
			this.complete = complete;
		}
		
		/**
		 * @return the short representation of a custom value
		 */
		public String getSummary() {
			return summary;
		}
		
		/**
		 * @param summary the summary to set
		 */
		public void setSummary(String summary) {
			this.summary = summary;
		}
		
		/**
		 * @return if true, then getSummary() returns a complete view of the custom value; otherwise the value is
		 * in fact a summary 
		 */
		public boolean isComplete() {
			return complete;
		}
		
		/**
		 * @param complete the complete to set
		 */
		public void setComplete(boolean complete) {
			this.complete = complete;
		}
		
		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return summary;
		}
	}
	
}
