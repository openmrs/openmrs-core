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

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.Condition;
import org.openmrs.Patient;
import org.openmrs.api.ConditionService;
import org.openmrs.api.context.Context;
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
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.PatientResource1_8;

/**
 * {@link Resource} for Condition, supporting standard CRUD operations
 */
@Resource(name = RestConstants.VERSION_1 + "/condition", supportedClass = Condition.class, supportedOpenmrsVersions = {
        "2.2.* - 9.*" })
public class ConditionResource2_2 extends DataDelegatingCrudResource<Condition> {
	
	private ConditionService conditionService = Context.getConditionService();
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#getRepresentationDescription(org.openmrs.module.webservices.rest.web.representation.Representation)
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation representation) {
		if (representation instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display");
			description.addProperty("condition", Representation.REF);
			description.addProperty("patient", Representation.REF);
			description.addProperty("clinicalStatus");
			description.addProperty("verificationStatus");
			description.addProperty("previousVersion", Representation.REF);
			description.addProperty("onsetDate");
			description.addProperty("endDate");
			description.addProperty("voided");
			description.addSelfLink();
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			return description;
		} else if (representation instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display");
			description.addProperty("condition");
			description.addProperty("patient", Representation.REF);
			description.addProperty("clinicalStatus");
			description.addProperty("verificationStatus");
			description.addProperty("previousVersion");
			description.addProperty("onsetDate");
			description.addProperty("endDate");
			description.addProperty("additionalDetail");
			description.addProperty("voided");
			description.addProperty("auditInfo");
			description.addSelfLink();
			return description;
		}
		return null;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler#getGETModel(Representation)
	 */
	public Model getGETModel(Representation rep) {
		ModelImpl model = ((ModelImpl) super.getGETModel(rep));
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			model
			        .property("uuid", new StringProperty())
			        .property("display", new StringProperty())
			        .property("condition", new StringProperty())
			        .property("patient", new RefProperty("#/definitions/PatientGetRef"))
			        .property("clinicalStatus", new StringProperty())
			        .property("verificationStatus", new StringProperty())
			        .property("previousVersion", new StringProperty())
			        .property("onsetDate", new StringProperty())
			        .property("endDate", new StringProperty())
			        .property("additionalDetail", new StringProperty())
			        .property("voided", new StringProperty());
		}
		return model;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler#getCREATEModel(Representation)
	 */
	@Override
	public Model getCREATEModel(Representation rep) {
		return new ModelImpl()
		        .property("condition", new StringProperty())
		        .property("patient", new StringProperty().example("uuid"))
		        .property("clinicalStatus", new StringProperty())
		        .property("verificationStatus", new StringProperty())
		        .property("previousVersion", new StringProperty())
		        .property("onsetDate", new StringProperty())
		        .property("endDate", new StringProperty())
		        .property("additionalDetail", new StringProperty());
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler#getUPDATEModel(Representation)
	 */
	@Override
	public Model getUPDATEModel(Representation representation) {
		return new ModelImpl()
		        .property("condition", new StringProperty())
		        .property("clinicalStatus", new StringProperty())
		        .property("verificationStatus", new StringProperty())
		        .property("previousVersion", new StringProperty())
		        .property("onsetDate", new StringProperty())
		        .property("endDate", new StringProperty())
		        .property("additionalDetail", new StringProperty())
		        .property("voided", new StringProperty());
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getCreatableProperties()
	 */
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		
		description.addProperty("condition");
		description.addProperty("patient");
		description.addProperty("clinicalStatus");
		description.addProperty("verificationStatus");
		description.addProperty("onsetDate");
		description.addProperty("endDate");
		description.addProperty("additionalDetail");
		
		return description;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getUpdatableProperties()
	 */
	@Override
	public DelegatingResourceDescription getUpdatableProperties() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		
		description.addProperty("condition");
		description.removeProperty("patient");
		description.addProperty("clinicalStatus");
		description.addProperty("verificationStatus");
		description.addProperty("onsetDate");
		description.addProperty("endDate");
		description.addProperty("additionalDetail");
		description.addProperty("voided");
		
		return description;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#getByUniqueId(java.lang.String)
	 */
	@Override
	public Condition getByUniqueId(String uuid) {
		return conditionService.getConditionByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#delete(java.lang.Object,
	 *      java.lang.String, org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected void delete(Condition condition, String reason, RequestContext requestContext) throws ResponseException {
		conditionService.voidCondition(condition, reason);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#newDelegate()
	 */
	@Override
	public Condition newDelegate() {
		return new Condition();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#save(java.lang.Object)
	 */
	@Override
	public Condition save(Condition condition) {
		return conditionService.saveCondition(condition);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#purge(java.lang.Object,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public void purge(Condition condition, RequestContext requestContext) throws ResponseException {
		conditionService.purgeCondition(condition);
	}
	
	/**
	 * @param condition - the condition to get the name of
	 * @return condition's name
	 */
	@PropertyGetter("display")
	public String getDisplayString(Condition condition) {
		if (condition.getCondition() == null) {
			return "";
		} else {
			if (condition.getCondition().getCoded() != null)
				return condition.getCondition().getCoded().getName().getName();
			
			return condition.getCondition().getNonCoded();
		}
	}

	@Override
	protected PageableResult doSearch(RequestContext context) {
		String patientUuid = context.getRequest().getParameter("patientUuid");
		String includeInactive = context.getRequest().getParameter("includeInactive");
		ConditionService conditionService = Context.getConditionService();
		if (StringUtils.isBlank(patientUuid)) {
			return new EmptySearchResult();
		}
		Patient patient = ((PatientResource1_8) Context.getService(RestService.class).getResourceBySupportedClass(
				Patient.class)).getByUniqueId(patientUuid);
		if (patient == null) {
			return new EmptySearchResult();
		}
		if (StringUtils.isNotBlank(includeInactive)) {
			boolean isIncludeInactive = BooleanUtils.toBoolean(includeInactive);
			if (isIncludeInactive) {
				return new NeedsPaging<Condition>(conditionService.getAllConditions(patient), context);
			} else {
				return new NeedsPaging<Condition>(conditionService.getActiveConditions(patient), context);
			}
		}
		else {
			return new NeedsPaging<Condition>(conditionService.getActiveConditions(patient), context);
		}
	}
}
