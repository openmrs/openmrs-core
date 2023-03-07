/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs2_2;

import org.openmrs.PatientProgram;
import org.openmrs.PatientProgramAttribute;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_10.ProgramEnrollmentResource1_10;

import java.util.Arrays;
import java.util.List;

@Resource(name = RestConstants.VERSION_1 + "/programenrollment", supportedClass = PatientProgram.class, supportedOpenmrsVersions = {
        "2.2.* - 9.*" }, order = 0)
public class ProgramEnrollmentResource2_2 extends ProgramEnrollmentResource1_10 {
	
	@PropertySetter("attributes")
	public static void setAttributes(PatientProgram instance, List<PatientProgramAttribute> attrs) {
		for (PatientProgramAttribute attr : attrs) {
			instance.addAttribute(attr);
		}
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		DelegatingResourceDescription parentRep = super.getRepresentationDescription(rep);
		if (rep instanceof DefaultRepresentation) {
			parentRep.addProperty("attributes", Representation.REF);
			return parentRep;
		} else if (rep instanceof FullRepresentation) {
			parentRep.addProperty("states", Representation.REF);
			parentRep.addProperty("attributes", Representation.DEFAULT);
			return parentRep;
		} else {
			return null;
		}
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription delegatingResourceDescription = super.getCreatableProperties();
		delegatingResourceDescription.addProperty("attributes");
		return delegatingResourceDescription;
	}
	
	@Override
	public DelegatingResourceDescription getUpdatableProperties() {
		DelegatingResourceDescription delegatingResourceDescription = super.getUpdatableProperties();
		delegatingResourceDescription.addProperty("attributes");
		return delegatingResourceDescription;
	}
	
	@Override
	public List<String> getPropertiesToExposeAsSubResources() {
		return Arrays.asList("attributes");
	}
	
}
