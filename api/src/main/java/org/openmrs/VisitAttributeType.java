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

import org.hibernate.envers.Audited;
import org.openmrs.attribute.AttributeType;
import org.openmrs.attribute.BaseAttributeType;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A user-defined extension to the {@link Visit} class.
 * @see AttributeType 
 */
@Entity
@Table(name = "visit_attribute_type")
@Audited
@AttributeOverrides(value = {
	@AttributeOverride(name = "description", column = @Column(name = "description", length = 1024))
})
public class VisitAttributeType extends BaseAttributeType<Visit> implements AttributeType<Visit> {

	@Id
	@Column(name = "visit_attribute_type_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer visitAttributeTypeId;
	
	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getVisitAttributeTypeId();
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setVisitAttributeTypeId(id);
	}
	
	/**
	 * @return the visitAttributeTypeId
	 */
	public Integer getVisitAttributeTypeId() {
		return visitAttributeTypeId;
	}
	
	/**
	 * @param visitAttributeTypeId the visitAttributeTypeId to set
	 */
	public void setVisitAttributeTypeId(Integer visitAttributeTypeId) {
		this.visitAttributeTypeId = visitAttributeTypeId;
	}
	
}
