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

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.envers.Audited;
import org.openmrs.attribute.AttributeType;
import org.openmrs.attribute.BaseAttributeType;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "program_attribute_type")
@Audited
@AttributeOverrides(value = {
	@AttributeOverride(name = "description", column = @Column(name = "description", length = 1024))
})
public class ProgramAttributeType extends BaseAttributeType<PatientProgram> implements AttributeType<PatientProgram> {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "program_attribute_type_id_seq")
	@GenericGenerator(
		name = "program_attribute_type_id_seq",
		strategy = "native",
		parameters = @Parameter(name = "sequence", value = "program_attribute_type_program_attribute_type_id_seq")
	)
	@Column(name = "program_attribute_type_id")
    private Integer programAttributeTypeId;

    @Override
    public Integer getId() {
        return getProgramAttributeTypeId();
    }

    @Override
    public void setId(Integer id) {
        setProgramAttributeTypeId(id);
    }

    public Integer getProgramAttributeTypeId() {
        return programAttributeTypeId;
    }

    public void setProgramAttributeTypeId(Integer programAttributeTypeId) {
        this.programAttributeTypeId = programAttributeTypeId;
    }
}
