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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "program_attribute_type")
@Audited
public class ProgramAttributeType extends BaseAttributeType<PatientProgram> implements AttributeType<PatientProgram> {

	@Id
	@Column(name = "program_attribute_type_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
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
