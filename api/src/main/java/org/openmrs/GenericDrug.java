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
package org.openmrs;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Wrapper around a Concept (with class=Drug) that represents an orderable
 * generic drug.
 */
public class GenericDrug extends BaseOrderable<DrugOrder> implements Orderable<DrugOrder> {
	
	private static final String IDENTIFIER_PREFIX = "org.openmrs.GenericDrug:concept=";
	
	private static final Log log = LogFactory.getLog(GenericDrug.class);
	
	/** constructor that sets default concept for orderable element */
	public GenericDrug(Concept concept) {
		super();
		this.concept = concept;
	}
	
	/**
	 * @see org.openmrs.Orderable#getUniqueIdentifier()
	 */
	@Override
	public String getUniqueIdentifier() {
		return IDENTIFIER_PREFIX + concept.getConceptId();
	}
	
	/**
	 * Gets a numeric identifier from a string identifier.
	 *
	 * @param identifier
	 *            the string identifier.
	 * @return the numeric identifier if it is a valid one, else null
	 * @should return numeric identifier of valid string identifier
	 * @should return null for an invalid string identifier
	 * @should fail if null or empty passed in
	 * @since 1.10
	 */
	public static Integer getNumericIdentifier(String identifier) {
		if (StringUtils.isBlank(identifier)) {
			throw new IllegalArgumentException("identifier cannot be null");
		}
		
		if (!identifier.startsWith(IDENTIFIER_PREFIX)) {
			return null;
		}
		
		try {
			return Integer.valueOf(identifier.substring(IDENTIFIER_PREFIX.length()));
		}
		catch (NumberFormatException ex) {
			log.error("invalid unique identifier for GenericDrug:" + identifier, ex);
		}
		
		return null;
	}
}
