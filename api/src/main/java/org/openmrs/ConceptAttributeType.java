/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import org.openmrs.attribute.AttributeType;
import org.openmrs.attribute.BaseAttributeType;

public class ConceptAttributeType extends BaseAttributeType<Concept> implements AttributeType<Concept> {
	
	private Integer conceptAttributeTypeId;

	/**
	 * Default constructor for <tt>ConceptAttributeType</tt>
	 *
	 * @since 2.4.1
	 */
	public ConceptAttributeType(){
	}

	/**
	 * Constructor for <tt>ConceptAttributeType</tt> that takes the
	 * primary key. 
	 *
	 * @param conceptAttributeTypeId the id of the <tt>ConceptAttributeType</tt>
	 * @since 2.4.1
	 */
	public ConceptAttributeType(final Integer conceptAttributeTypeId) {
		this.conceptAttributeTypeId = conceptAttributeTypeId;
	}

	public Integer getConceptAttributeTypeId() {
		return conceptAttributeTypeId;
	}
	
	public void setConceptAttributeTypeId(Integer conceptAttributeTypeId) {
		this.conceptAttributeTypeId = conceptAttributeTypeId;
	}
	
	@Override
	public Integer getId() {
		return getConceptAttributeTypeId();
	}
	
	@Override
	public void setId(Integer id) {
		setConceptAttributeTypeId(id);
	}
}
