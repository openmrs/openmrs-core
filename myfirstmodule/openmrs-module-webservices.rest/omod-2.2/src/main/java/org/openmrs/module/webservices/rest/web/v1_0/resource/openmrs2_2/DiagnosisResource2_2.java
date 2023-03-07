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

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.ConditionVerificationStatus;
import org.openmrs.Diagnosis;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.docs.swagger.core.property.EnumProperty;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.EmptySearchResult;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.PatientResource1_8;

/**
 * {@link Resource} for Diagnosis, supporting standard CRUD operations
 */
@Resource(name = RestConstants.VERSION_1 + "/patientdiagnoses", supportedClass = Diagnosis.class, supportedOpenmrsVersions = {
        "2.2.* - 9.*" })
public class DiagnosisResource2_2 extends DataDelegatingCrudResource<Diagnosis> {
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#getByUniqueId(java.lang.String)
	 */
	@Override
	public Diagnosis getByUniqueId(String diagnosisUuid) {
		return Context.getDiagnosisService().getDiagnosisByUuid(diagnosisUuid);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#delete(java.lang.Object,
	 *      java.lang.String, org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected void delete(Diagnosis diagnosis, String reason, RequestContext requestContext) throws ResponseException {
		Context.getDiagnosisService().voidDiagnosis(diagnosis, reason);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#newDelegate()
	 */
	@Override
	public Diagnosis newDelegate() {
		return new Diagnosis();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#save(java.lang.Object)
	 */
	@Override
	public Diagnosis save(Diagnosis diagnosis) {
		return Context.getDiagnosisService().save(diagnosis);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#purge(java.lang.Object,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public void purge(Diagnosis diagnosis, RequestContext requestContext) throws ResponseException {
		Context.getDiagnosisService().purgeDiagnosis(diagnosis);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#getRepresentationDescription(org.openmrs.module.webservices.rest.web.representation.Representation)
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation representation) {
		if (representation instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("diagnosis", Representation.REF);
			description.addProperty("condition", Representation.REF);
			description.addProperty("encounter", Representation.REF);
			description.addProperty("certainty");
			description.addProperty("rank");
			description.addProperty("voided");
			description.addProperty("display");
			description.addProperty("patient", Representation.REF);
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			description.addSelfLink();
			return description;
		} else if (representation instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("diagnosis");
			description.addProperty("patient", Representation.REF);
			description.addProperty("condition");
			description.addProperty("encounter");
			description.addProperty("certainty");
			description.addProperty("rank");
			description.addProperty("voided");
			description.addProperty("auditInfo");
			description.addProperty("display");
			description.addSelfLink();
			return description;
		}
		return null;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler#getGETModel(Representation)
	 */
	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl model = (ModelImpl) super.getGETModel(rep);
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			model
			        .property("uuid", new StringProperty())
			        .property("diagnosis", new StringProperty())
			        .property("condition", new StringProperty())
			        .property("certainty", new EnumProperty(ConditionVerificationStatus.class))
			        .property("rank", new IntegerProperty())
			        .property("patient", new RefProperty("#/definitions/PatientGetRef"))
			        .property("voided", new BooleanProperty());
		}
		return model;
	}
	
	/**
	 * @param diagnosis
	 * @return Diagnosis's name
	 */
	@PropertyGetter("display")
	public String getDisplayString(Diagnosis diagnosis) {
		if (diagnosis.getDiagnosis() == null) {
			return "";
		} else {
			if (diagnosis.getDiagnosis().getCoded() != null) {
				return diagnosis.getDiagnosis().getCoded().getName().getName();
			}
			return diagnosis.getDiagnosis().getNonCoded();
		}
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getCreatableProperties()
	 */
	@Override
	public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		
		description.addRequiredProperty("diagnosis");
		description.addRequiredProperty("encounter");
		description.addRequiredProperty("condition");
		description.addRequiredProperty("certainty");
		description.addRequiredProperty("patient");
		description.addRequiredProperty("rank");
		
		return description;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler#getCREATEModel(Representation)
	 */
	@Override
	public Model getCREATEModel(Representation rep) {
		
		return new ModelImpl()
		        .property("diagnosis", new StringProperty())
		        .property("encounter", new StringProperty())
		        .property("condition", new StringProperty())
		        .property("certainty", new StringProperty())
		        .property("patient", new StringProperty().example("uuid"))
		        .property("rank", new IntegerProperty());
		
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getUpdatableProperties()
	 */
	@Override
	public DelegatingResourceDescription getUpdatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addRequiredProperty("diagnosis");
		description.addRequiredProperty("condition");
		description.addRequiredProperty("rank");
		description.removeProperty("patient");
		description.addRequiredProperty("voided");
		description.addRequiredProperty("certainty");
		description.addRequiredProperty("encounter");
		
		return description;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler#getUPDATEModel(Representation)
	 */
	@Override
	public Model getUPDATEModel(Representation rep) {
		return new ModelImpl()
		        .property("diagnosis", new StringProperty())
		        .property("condition", new StringProperty())
		        .property("encounter", new StringProperty())
		        .property("certainty", new EnumProperty(ConditionVerificationStatus.class))
		        .property("rank", new IntegerProperty())
		        .property("voided", new BooleanProperty());
	}

	@Override
	protected PageableResult doSearch(RequestContext context) {
		String patientUuid = context.getRequest().getParameter("patientUuid");
		String fromDate = context.getRequest().getParameter("fromDate");
		if(StringUtils.isBlank(patientUuid) || StringUtils.isBlank(fromDate)) {
			return new EmptySearchResult();
		}
		Date dateFrom = (Date) ConversionUtil.convert(fromDate, Date.class);
		Patient patient = ((PatientResource1_8) Context.getService(RestService.class).getResourceBySupportedClass(
				Patient.class)).getByUniqueId(patientUuid);
		if (patient == null) {
			return new EmptySearchResult();
		}
		return new NeedsPaging<Diagnosis>(Context.getDiagnosisService().getDiagnoses(patient, dateFrom), context);
	}
}
