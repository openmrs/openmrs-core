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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.envers.Audited;
import org.openmrs.attribute.AttributeType;
import org.openmrs.attribute.BaseAttributeType;

@Entity
@Table(name = "concept_attribute_type")
@Audited
public class ConceptAttributeType extends BaseAttributeType<Concept> implements AttributeType<Concept> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "concept_attribute_type_id")
	private Integer conceptAttributeTypeId;
	
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
