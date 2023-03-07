/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs2_1;

import java.util.ArrayList;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.DateProperty;
import io.swagger.models.properties.StringProperty;
import org.openmrs.Cohort;
import org.openmrs.CohortMembership;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.SubResource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubResource;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

@SubResource(parent = CohortResource2_1.class, path = "membership", supportedClass = CohortMembership.class, supportedOpenmrsVersions = {
        "2.1.* - 9.*" })
public class CohortMembershipResource2_1 extends DelegatingSubResource<CohortMembership, Cohort, CohortResource2_1> {
	
	@Override
	public Cohort getParent(CohortMembership instance) {
		return instance.getCohort();
	}
	
	@Override
	public void setParent(CohortMembership instance, Cohort parent) {
		instance.setCohort(parent);
	}
	
	@Override
	public CohortMembership newDelegate() {
		return new CohortMembership();
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display");
			description.addProperty("startDate");
			description.addProperty("endDate");
			description.addProperty("patientUuid");
			description.addSelfLink();
			description.addLink("patient", "/" + RestConstants.VERSION_1 + "/patient/{patientUuid}");
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display");
			description.addProperty("startDate");
			description.addProperty("endDate");
			description.addProperty("patientUuid");
			description.addProperty("auditInfo");
			description.addSelfLink();
			description.addLink("patient", "/" + RestConstants.VERSION_1 + "/patient/{patientUuid}");
			return description;
		}
		return null;
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription d = new DelegatingResourceDescription();
		d.addProperty("patientUuid");
		d.addProperty("startDate");
		d.addProperty("endDate");
		return d;
	}
	
	@Override
	public DelegatingResourceDescription getUpdatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription d = new DelegatingResourceDescription();
		d.addProperty("startDate");
		d.addProperty("endDate");
		return d;
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl model = (ModelImpl) super.getGETModel(rep);
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			model
			        .property("uuid", new StringProperty())
			        .property("display", new StringProperty())
			        .property("startDate", new DateProperty())
			        .property("endDate", new DateProperty())
			        .property("patientUuid", new StringProperty());
		}
		//FIXME missing props
		return model;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		return new ModelImpl()
		        .property("patientUuid", new StringProperty())
		        .property("startDate", new DateProperty())
		        .property("endDate", new DateProperty());
	}
	
	@Override
	public Model getUPDATEModel(Representation rep) {
		return new ModelImpl()
		        .property("startDate", new DateProperty())
		        .property("endDate", new DateProperty());
	}
	
	@PropertyGetter("display")
	public String getDisplay(CohortMembership cohortMembership) {
		return "Patient in cohort (see link with rel=patient)";
	}
	
	@PropertyGetter("patientUuid")
	public String getPatientUuid(CohortMembership cohortMembership) {
		if (cohortMembership.getPatientId() == null) {
			return null;
		}
		Patient patient = Context.getPatientService().getPatient(cohortMembership.getPatientId());
		if (patient == null) {
			throw new IllegalStateException("CohortMembership " + cohortMembership.getUuid() + " refers to a patient that "
			        + "does not exist: " + cohortMembership.getPatientId());
		}
		return patient.getUuid();
	}
	
	@PropertySetter("patientUuid")
	public void setPatientUuid(CohortMembership cohortMembership, String patientUuid) {
		Patient patient = Context.getPatientService().getPatientByUuid(patientUuid);
		cohortMembership.setPatientId(patient.getPatientId());
	}
	
	@Override
	public CohortMembership save(CohortMembership delegate) {
		Context.getCohortService().saveCohort(delegate.getCohort());
		return delegate;
	}
	
	@Override
	public PageableResult doGetAll(Cohort parent, RequestContext context) throws ResponseException {
		return new NeedsPaging<CohortMembership>(new ArrayList<CohortMembership>(parent.getMemberships()), context);
	}
	
	@Override
	public CohortMembership getByUniqueId(String uniqueId) {
		return Context.getCohortService().getCohortMembershipByUuid(uniqueId);
	}
	
	@Override
	protected void delete(CohortMembership delegate, String reason, RequestContext context) throws ResponseException {
		Context.getCohortService().voidCohortMembership(delegate, reason);
	}
	
	@Override
	public void purge(CohortMembership delegate, RequestContext context) throws ResponseException {
		Context.getCohortService().purgeCohortMembership(delegate);
	}
}
