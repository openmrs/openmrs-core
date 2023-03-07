/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.DateProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import org.openmrs.activelist.ActiveListItem;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

/**
 * Subclass of {@link DataDelegatingCrudResource} with helper methods specific to
 * {@link ActiveListItem}
 */
public abstract class BaseActiveListItemResource1_8<T extends ActiveListItem> extends DataDelegatingCrudResource<T> {
	
	@Override
	public Model getCREATEModel(Representation rep) {
		ModelImpl model = new ModelImpl()
		        .property("person", new StringProperty())
		        .property("startDate", new DateProperty())
		        .property("comments", new StringProperty())
		        .property("startObs", new StringProperty())
		        .property("stopObs", new StringProperty())
		        
		        .required("person").required("startDate");
		if (rep instanceof FullRepresentation) {
			model
			        .property("person", new RefProperty("#/definitions/PersonCreate"))
			        .property("startObs", new RefProperty("#/definitions/ObsCreate"))
			        .property("stopObs", new RefProperty("#/definitions/ObsCreate"));
		}
		return model;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getRepresentationDescription(org.openmrs.module.webservices.rest.web.representation.Representation)
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display");
			description.addProperty("person", Representation.REF);
			description.addProperty("activeListType", Representation.REF);
			description.addProperty("startDate");
			description.addProperty("endDate");
			description.addProperty("startObs", Representation.REF);
			description.addProperty("stopObs", Representation.REF);
			description.addProperty("comments");
			description.addProperty("voided");
			description.addSelfLink();
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display");
			description.addProperty("person", Representation.REF);
			description.addProperty("activeListType", Representation.REF);
			description.addProperty("startDate");
			description.addProperty("endDate");
			description.addProperty("startObs", Representation.REF);
			description.addProperty("stopObs", Representation.REF);
			description.addProperty("comments");
			description.addProperty("voided");
			description.addProperty("auditInfo");
			description.addSelfLink();
			return description;
		}
		return null;
	}
	
	public Model getGETModel(Representation rep) {
		ModelImpl model = ((ModelImpl) super.getGETModel(rep))
		        .property("uuid", new StringProperty())
		        .property("display", new StringProperty())
		        .property("startDate", new DateProperty())
		        .property("endDate", new DateProperty())
		        .property("comments", new StringProperty())
		        .property("voided", new BooleanProperty());
		if (rep instanceof DefaultRepresentation) {
			model
			        .property("person", new RefProperty("#/definitions/PersonGet"))
			        .property("activeListType", new StringProperty()) //FIXME type
			        .property("startObs", new RefProperty("#/definitions/ObsGet"))
			        .property("stopObs", new RefProperty("#/definitions/ObsGetRef"));
		} else if (rep instanceof FullRepresentation) {
			model
			        .property("person", new RefProperty("#/definitions/PersonGetRef"))
			        .property("activeListType", new StringProperty()) //FIXME type
			        .property("startObs", new RefProperty("#/definitions/ObsGetRef"))
			        .property("stopObs", new RefProperty("#/definitions/ObsGetRef"));
		}
		return model;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getCreatableProperties()
	 */
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addRequiredProperty("person");
		description.addRequiredProperty("startDate");
		description.addProperty("comments");
		description.addProperty("startObs");
		description.addProperty("stopObs");
		
		return description;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getByUniqueId(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public T getByUniqueId(String uniqueId) {
		return (T) Context.getActiveListService().getActiveListItemByUuid(uniqueId);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler#save(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public T save(T delegate) {
		return (T) Context.getActiveListService().saveActiveListItem(delegate);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#delete(java.lang.Object,
	 *      java.lang.String, org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected void delete(T delegate, String reason, RequestContext context) throws ResponseException {
		if (delegate.isVoided()) {
			// DELETE is idempotent, so we return success here
			return;
		}
		Context.getActiveListService().voidActiveListItem(delegate, reason);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#purge(java.lang.Object,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public void purge(T delegate, RequestContext context) throws ResponseException {
		// TODO: add a purge method to core (TRUNK-3725), then add this here in restws (RESTWS-295)
		//Context.getActiveListService().purgeActiveListItem(delegate);
	}
	
	// TODO: add these lines into a "stop" method call.  RESTWS-296
	//delegate.setComments("Stopped from a REST web service call");
	//Context.getActiveListService().removeActiveListItem(delegate, null);
	
}
