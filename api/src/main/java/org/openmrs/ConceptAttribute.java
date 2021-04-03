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

import org.openmrs.attribute.Attribute;
import org.openmrs.attribute.BaseAttribute;

public class ConceptAttribute extends BaseAttribute<ConceptAttributeType, Concept> implements Attribute<ConceptAttributeType, Concept> {
	
	private Integer conceptAttributeId;
	
	public Concept getConcept() {
		return getOwner();
	}
	
	public void setConcept(Concept concept) {
		setOwner(concept);
	}
	
	public Integer getConceptAttributeId() {
		return this.conceptAttributeId;
	}
	
	public void setConceptAttributeId(Integer conceptAttributeId) {
		this.conceptAttributeId = conceptAttributeId;
	}
	
	@Override
	public Integer getId() {
		return getConceptAttributeId();
	}
	
	@Override
	public void setId(Integer id) {
		setConceptAttributeId(id);
	}
}
