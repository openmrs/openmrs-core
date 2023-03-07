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

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Concept;
import org.openmrs.ProgramAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.datatype.ConceptDatatype;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_9.BaseAttributeTypeCrudResource1_9;
import org.openmrs.util.OpenmrsUtil;

@Resource(name = RestConstants.VERSION_1 + "/programattributetype", supportedClass = ProgramAttributeType.class, supportedOpenmrsVersions = {
        "2.2.* - 9.*" })
public class ProgramAttributeTypeResource2_2 extends BaseAttributeTypeCrudResource1_9<ProgramAttributeType> {

	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {

		if (rep instanceof RefRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display");
			description.addProperty("description");
			description.addProperty("retired");
			description.addSelfLink();
			return description;
		}
		return super.getRepresentationDescription(rep);
	}

	@Override
	public ProgramAttributeType getByUniqueId(String uuid) {
		return Context.getProgramWorkflowService().getProgramAttributeTypeByUuid(uuid);
	}

	@Override
	public ProgramAttributeType newDelegate() {
		return new ProgramAttributeType();
	}

	@Override
	public ProgramAttributeType save(ProgramAttributeType programAttributeType) {
		return Context.getProgramWorkflowService().saveProgramAttributeType(programAttributeType);
	}

	@Override
	public void purge(ProgramAttributeType programAttributeType, RequestContext requestContext) throws ResponseException {
		Context.getProgramWorkflowService().purgeProgramAttributeType(programAttributeType);
	}

	@Override
	protected NeedsPaging<ProgramAttributeType> doGetAll(RequestContext context) throws ResponseException {
		return new NeedsPaging<ProgramAttributeType>(Context.getProgramWorkflowService()
		        .getAllProgramAttributeTypes(),
		        context);
	}

	@PropertyGetter("concept")
	public Object getConcept(ProgramAttributeType delegate) {
		if (OpenmrsUtil.nullSafeEquals(delegate.getDatatypeClassname(), ConceptDatatype.class.getCanonicalName()))
		{
			Concept concept;
			String id = delegate.getDatatypeConfig();
			if (StringUtils.isNumeric(id)) {
				concept = Context.getConceptService().getConcept(Integer.valueOf(id));
			} else {
				concept = Context.getConceptService().getConceptByUuid(id);
			}
			return ConversionUtil.convertToRepresentation(concept, Representation.FULL);
		}
		return null;
	}
}
