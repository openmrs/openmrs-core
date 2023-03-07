/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_10;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import org.openmrs.CareSetting;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.docs.swagger.core.property.EnumProperty;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.MetadataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

/**
 * {@link org.openmrs.module.webservices.rest.web.annotation.Resource} for
 * {@link org.openmrs.CareSetting}, supporting standard CRUD operations
 */
@Resource(name = RestConstants.VERSION_1 + "/caresetting", supportedClass = CareSetting.class, supportedOpenmrsVersions = {
        "1.10.* - 9.*" })
public class CareSettingResource1_10 extends MetadataDelegatingCrudResource<CareSetting> {
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#getRepresentationDescription(org.openmrs.module.webservices.rest.web.representation.Representation)
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("name");
			description.addProperty("description");
			description.addProperty("retired");
			description.addProperty("careSettingType");
			description.addProperty("display");
			description.addSelfLink();
			if (rep instanceof DefaultRepresentation) {
				description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			} else {
				description.addProperty("auditInfo");
			}
			return description;
		}
		return null;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getCreatableProperties()
	 */
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl model = (ModelImpl) super.getGETModel(rep);
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			model
			        .property("careSettingType", new EnumProperty(CareSetting.CareSettingType.class));
		}
		return model;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#newDelegate()
	 */
	@Override
	public CareSetting newDelegate() {
		return new CareSetting();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#save(Object)
	 */
	@Override
	public CareSetting save(CareSetting careSetting) {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	/**
	 * Fetches a careSettings by uuid or name
	 * 
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#getByUniqueId(String)
	 */
	@Override
	public CareSetting getByUniqueId(String uniqueId) {
		CareSetting cs = Context.getOrderService().getCareSettingByUuid(uniqueId);
		if (cs == null) {
			cs = Context.getOrderService().getCareSettingByName(uniqueId);
		}
		return cs;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#purge(Object,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public void purge(CareSetting careSetting, RequestContext context) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doGetAll(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected NeedsPaging<CareSetting> doGetAll(RequestContext context) {
		return new NeedsPaging<CareSetting>(Context.getOrderService().getCareSettings(context.getIncludeAll()), context);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doSearch(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected NeedsPaging<CareSetting> doSearch(RequestContext context) {
		List<CareSetting> careSettings = Context.getOrderService().getCareSettings(context.getIncludeAll());
		for (Iterator<CareSetting> iterator = careSettings.iterator(); iterator.hasNext();) {
			CareSetting cs = iterator.next();
			if (!Pattern.compile(Pattern.quote(context.getParameter("q")), Pattern.CASE_INSENSITIVE).matcher(cs.getName())
			        .find()) {
				iterator.remove();
			}
		}
		return new NeedsPaging<CareSetting>(careSettings, context);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getResourceVersion()
	 */
	@Override
	public String getResourceVersion() {
		return RestConstants1_10.RESOURCE_VERSION;
	}
}
