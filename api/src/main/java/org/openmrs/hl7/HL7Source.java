/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.hl7;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.envers.Audited;
import org.openmrs.BaseChangeableOpenmrsMetadata;

/**
 * Names a unique location that hl7 messages could be coming from.
 */
@Entity
@Table(name = "hl7_source")
@Audited
@AttributeOverrides({
		@AttributeOverride(name = "retired", column = @Column(name = "retired"))
})
public class HL7Source extends BaseChangeableOpenmrsMetadata {
	                                  
	private static final long serialVersionUID = 3062136520728193223L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hl7_source_gen")
	@GenericGenerator(
			name = "hl7_source_gen",
			parameters = @Parameter(name = "sequence", value = "hl7_source_hl7_source_id_seq")
	)
	@Column(name = "hl7_source_id")
	private Integer hl7SourceId;
	
	/**
	 * Empty constructor
	 */
	public HL7Source() {
	}
	
	/**
	 * Generic constructor
	 * 
	 * @param hl7SourceId primary key id
	 */
	public HL7Source(Integer hl7SourceId) {
		this.hl7SourceId = hl7SourceId;
	}
	
	/**
	 * @return Returns the hl7SourceId.
	 */
	public Integer getHL7SourceId() {
		return hl7SourceId;
	}
	
	/**
	 * @param hl7SourceId The hl7SourceId to set.
	 */
	public void setHL7SourceId(Integer hl7SourceId) {
		this.hl7SourceId = hl7SourceId;
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getHL7SourceId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setHL7SourceId(id);
	}
	
}
