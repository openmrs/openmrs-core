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
import org.openmrs.ProgramAttributeType;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.SubResource;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_9.BaseAttributeCrudResource1_9;

import java.util.Collection;
import java.util.List;

@SubResource(parent = ProgramEnrollmentResource2_2.class, path = "attribute", supportedClass = PatientProgramAttribute.class, supportedOpenmrsVersions = {
        "2.2.* - 9.*" })
public class PatientProgramAttributeResource2_2 extends BaseAttributeCrudResource1_9<PatientProgramAttribute, PatientProgram, ProgramEnrollmentResource2_2> {
	
	@PropertySetter("attributeType")
	public static void setAttributeType(PatientProgramAttribute instance, ProgramAttributeType attr) {
		instance.setAttributeType(attr);
	}
	
	@Override
	public PatientProgram getParent(PatientProgramAttribute instance) {
		return instance.getPatientProgram();
	}
	
	@Override
	public void setParent(PatientProgramAttribute patientProgramAttribute, PatientProgram bahmniPatientProgram) {
		patientProgramAttribute.setPatientProgram(bahmniPatientProgram);
		
	}
	
	@Override
	public PageableResult doGetAll(PatientProgram parent, RequestContext context)
	        throws ResponseException {
		return new NeedsPaging<PatientProgramAttribute>((List<PatientProgramAttribute>) parent.getActiveAttributes(),
		        context);
	}
	
	@Override
	public PatientProgramAttribute getByUniqueId(String uniqueId) {
		return Context.getService(ProgramWorkflowService.class).getPatientProgramAttributeByUuid(uniqueId);
	}
	
	@Override
	protected void delete(PatientProgramAttribute delegate, String reason, RequestContext context)
	        throws ResponseException {
		delegate.setVoided(true);
		delegate.setVoidReason(reason);
		Context.getService(ProgramWorkflowService.class).savePatientProgram(delegate.getPatientProgram());
	}
	
	@Override
	public PatientProgramAttribute newDelegate() {
		return new PatientProgramAttribute();
	}
	
	@Override
	public PatientProgramAttribute save(PatientProgramAttribute delegate) {
		boolean needToAdd = true;
		Collection<PatientProgramAttribute> activeAttributes = delegate.getPatientProgram().getActiveAttributes();
		for (PatientProgramAttribute pa : activeAttributes) {
			if (pa.equals(delegate)) {
				needToAdd = false;
				break;
			}
		}
		if (needToAdd) {
			delegate.getPatientProgram().addAttribute(delegate);
		}
		Context.getService(ProgramWorkflowService.class).savePatientProgram(delegate.getPatientProgram());
		return delegate;
	}
	
	@Override
	public void purge(PatientProgramAttribute patientProgramAttribute, RequestContext requestContext)
	        throws ResponseException {
		throw new UnsupportedOperationException("Cannot purge PatientProgramAttribute");
	}
}
