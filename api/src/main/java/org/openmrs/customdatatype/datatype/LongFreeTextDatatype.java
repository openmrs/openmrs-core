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
package org.openmrs.customdatatype.datatype;

import org.openmrs.api.context.Context;
import org.openmrs.api.db.ClobDatatypeStorage;
import org.openmrs.customdatatype.CustomDatatype;
import org.openmrs.customdatatype.InvalidCustomValueException;
import org.springframework.stereotype.Component;

/**
 * Free-text datatype, represented by a plain String in Java, but stored in the 
 * database as a CLOB or similar.
 * @since 1.9
 */
@Component
public class LongFreeTextDatatype implements CustomDatatype<String> {
	
	/**
	 * @see org.openmrs.customdatatype.CustomDatatype#setConfiguration(java.lang.String)
	 */
	@Override
	public void setConfiguration(String config) {
		// no configuration options
	}
	
	/**
	 * @see org.openmrs.customdatatype.CustomDatatype#save(java.lang.Object, java.lang.String)
	 */
	@Override
	public String save(String typedValue, String existingValueReference) throws InvalidCustomValueException {
		// get existing object or create a new one
		ClobDatatypeStorage storage = existingValueReference != null ? Context.getDatatypeService()
		        .getClobDatatypeStorageByUuid(existingValueReference) : new ClobDatatypeStorage();
		
		storage.setValue(typedValue);
		storage = Context.getDatatypeService().saveClobDatatypeStorage(storage);
		
		return storage.getUuid();
	}
	
	/**
	 * @see org.openmrs.customdatatype.CustomDatatype#getReferenceStringForValue(java.lang.Object)
	 */
	@Override
	public String getReferenceStringForValue(String typedValue) throws UnsupportedOperationException {
		// this doesn't make sense in this case, because there may be multiple 
		// stored clobs with the same value
		throw new UnsupportedOperationException();
	}
	
	/**
	 * @see org.openmrs.customdatatype.CustomDatatype#fromReferenceString(java.lang.String)
	 */
	@Override
	public String fromReferenceString(String referenceString) throws InvalidCustomValueException {
		return Context.getDatatypeService().getClobDatatypeStorageByUuid(referenceString).getValue();
	}
	
	/**
	 * @see org.openmrs.customdatatype.CustomDatatype#getTextSummary(java.lang.String)
	 */
	@Override
	public CustomDatatype.Summary getTextSummary(String referenceString) {
		/*
		 * Use this code snippet instead if we think that fetching the clob is fast enough
		 * 
		ClobDatatypeStorage storage = Context.getDatatypeService().getClobDatatypeStorageByUuid(referenceString);
		if (storage == null)
			return Context.getMessageSourceService().getMessage("CustomDatatype.error.missingValue");
		
		// truncate the value to render a summary
		String s = storage.getValue();
		if (s.length() > 100)
			return new CustomDatatype.Summary(s.substring(0, 100), false);
		else
			return new CustomDatatype.Summary(summary,  true);
		*/

		String ret = Context.getMessageSourceService().getMessage(
		    "org.openmrs.customdatatype.datatype.LongFreeTextDatatype.placeholderValue", new Object[] { referenceString },
		    Context.getLocale());
		return new CustomDatatype.Summary(ret, false);
	}
	
	/**
	 * @see org.openmrs.customdatatype.CustomDatatype#validate(java.lang.Object)
	 */
	@Override
	public void validate(String typedValue) throws InvalidCustomValueException {
		// any non-null String is legal
		if (typedValue == null) {
			throw new InvalidCustomValueException("Cannot be null");
		}
	}
	
}
