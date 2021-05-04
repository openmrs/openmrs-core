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

public class PatientProgramAttribute extends BaseAttribute<ProgramAttributeType, PatientProgram> implements Attribute<ProgramAttributeType, PatientProgram> {
    private Integer patientProgramAttributeId;

	/**
	 * Default constructor for <tt>PatientProgramAttribute</tt>
	 *
	 * @since 2.4.1
	 */
	public PatientProgramAttribute(){
	}

	/**
	 * Constructor for <tt>PatientProgramAttribute</tt> that takes the
	 * primary key. 
	 *
	 * @param patientProgramAttributeId the id of the <tt>PatientProgramAttribute</tt>
	 * @since 2.4.1
	 */
	public PatientProgramAttribute(final Integer patientProgramAttributeId) {
		this.patientProgramAttributeId = patientProgramAttributeId;
	}

	@Override
    public Integer getId() {
        return getPatientProgramAttributeId();
    }

    @Override
    public void setId(Integer id) {
        setPatientProgramAttributeId(id);
    }

    public PatientProgram getPatientProgram() {
        return getOwner();
    }

    public void setPatientProgram(PatientProgram patientProgram) {
        setOwner(patientProgram);
    }

    public Integer getPatientProgramAttributeId() {
        return patientProgramAttributeId;
    }

    public void setPatientProgramAttributeId(Integer id) {
        this.patientProgramAttributeId = id;
    }
}
