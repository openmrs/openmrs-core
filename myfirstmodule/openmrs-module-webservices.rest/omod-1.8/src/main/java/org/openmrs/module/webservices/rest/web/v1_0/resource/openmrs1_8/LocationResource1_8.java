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

import java.util.List;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import org.apache.commons.collections.CollectionUtils;
import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.MetadataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

/**
 * {@link Resource} for {@link Location}, supporting standard CRUD operations
 */
@Resource(name = RestConstants.VERSION_1 + "/location", supportedClass = Location.class, supportedOpenmrsVersions = "1.8.*")
public class LocationResource1_8 extends MetadataDelegatingCrudResource<Location> {
	
	/**
	 * @see DelegatingCrudResource#getRepresentationDescription(Representation)
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display");
			description.addProperty("name");
			description.addProperty("description");
			description.addProperty("address1");
			description.addProperty("address2");
			description.addProperty("cityVillage");
			description.addProperty("stateProvince");
			description.addProperty("country");
			description.addProperty("postalCode");
			description.addProperty("latitude");
			description.addProperty("longitude");
			description.addProperty("countyDistrict");
			description.addProperty("address3");
			description.addProperty("address4");
			description.addProperty("address5");
			description.addProperty("address6");
			description.addProperty("tags", Representation.REF);
			description.addProperty("parentLocation", Representation.REF);
			description.addProperty("childLocations", Representation.REF);
			description.addProperty("retired");
			description.addSelfLink();
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display");
			description.addProperty("name");
			description.addProperty("description");
			description.addProperty("address1");
			description.addProperty("address2");
			description.addProperty("cityVillage");
			description.addProperty("stateProvince");
			description.addProperty("country");
			description.addProperty("postalCode");
			description.addProperty("latitude");
			description.addProperty("longitude");
			description.addProperty("countyDistrict");
			description.addProperty("address3");
			description.addProperty("address4");
			description.addProperty("address5");
			description.addProperty("address6");
			description.addProperty("tags", Representation.DEFAULT);
			description.addProperty("parentLocation", Representation.DEFAULT);
			description.addProperty("childLocations", Representation.DEFAULT);
			description.addProperty("retired");
			description.addProperty("auditInfo");
			description.addSelfLink();
			return description;
		}
		return null;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getCreatableProperties()
	 */
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		
		description.addRequiredProperty("name");
		
		description.addProperty("description");
		description.addProperty("address1");
		description.addProperty("address2");
		description.addProperty("cityVillage");
		description.addProperty("stateProvince");
		description.addProperty("country");
		description.addProperty("postalCode");
		description.addProperty("latitude");
		description.addProperty("longitude");
		description.addProperty("countyDistrict");
		description.addProperty("address3");
		description.addProperty("address4");
		description.addProperty("address5");
		description.addProperty("address6");
		description.addProperty("tags");
		description.addProperty("parentLocation");
		description.addProperty("childLocations");
		
		return description;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getUpdatableProperties()
	 */
	@Override
	public DelegatingResourceDescription getUpdatableProperties() {
		return getCreatableProperties();
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl modelImpl = (ModelImpl) super.getGETModel(rep);
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			modelImpl
			        .property("address1", new StringProperty())
			        .property("address2", new StringProperty())
			        .property("cityVillage", new StringProperty())
			        .property("stateProvince", new StringProperty())
			        .property("country", new StringProperty())
			        .property("postalCode", new StringProperty())
			        .property("latitude", new StringProperty())
			        .property("longitude", new StringProperty())
			        .property("countyDistrict", new StringProperty())
			        .property("address3", new StringProperty())
			        .property("address4", new StringProperty())
			        .property("address5", new StringProperty())
			        .property("address6", new StringProperty());
		}
		if (rep instanceof DefaultRepresentation) {
			modelImpl
			        .property("tags", new ArrayProperty(new RefProperty("#/definitions/LocationtagGetRef")))
			        .property("parentLocation", new RefProperty("#/definitions/LocationGetRef"))
			        .property("childLocations", new ArrayProperty(new RefProperty("#/definitions/LocationGetRef")));
		} else if (rep instanceof FullRepresentation) {
			modelImpl
			        .property("tags", new ArrayProperty(new RefProperty("#/definitions/LocationtagGet")))
			        .property("parentLocation", new RefProperty("#/definitions/LocationGet"))
			        .property("childLocations", new ArrayProperty(new RefProperty("#/definitions/LocationGet")));
		}
		return modelImpl;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		return ((ModelImpl) super.getCREATEModel(rep))
		        .property("address1", new StringProperty())
		        .property("address2", new StringProperty())
		        .property("cityVillage", new StringProperty())
		        .property("stateProvince", new StringProperty())
		        .property("country", new StringProperty())
		        .property("postalCode", new StringProperty())
		        .property("latitude", new StringProperty())
		        .property("longitude", new StringProperty())
		        .property("countyDistrict", new StringProperty())
		        .property("address3", new StringProperty())
		        .property("address4", new StringProperty())
		        .property("address5", new StringProperty())
		        .property("address6", new StringProperty())
		        .property("tags", new ArrayProperty(new StringProperty()))
		        .property("parentLocation", new StringProperty())
		        .property("childLocations", new ArrayProperty(new StringProperty()));
	}
	
	@Override
	public Model getUPDATEModel(Representation rep) {
		return getCREATEModel(rep);
	}
	
	/**
	 * @see DelegatingCrudResource#newDelegate()
	 */
	@Override
	public Location newDelegate() {
		return new Location();
	}
	
	/**
	 * @see DelegatingCrudResource#save(java.lang.Object)
	 */
	@Override
	public Location save(Location location) {
		return Context.getLocationService().saveLocation(location);
	}
	
	/**
	 * Fetches a location by uuid, if no match is found, it tries to look up one with a matching
	 * name with the assumption that the passed parameter is a location name
	 * 
	 * @see DelegatingCrudResource#getByUniqueId(java.lang.String)
	 */
	@Override
	public Location getByUniqueId(String uuid) {
		Location location = Context.getLocationService().getLocationByUuid(uuid);
		//We assume the caller was fetching by name
		if (location == null)
			location = Context.getLocationService().getLocation(uuid);
		
		return location;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#purge(java.lang.Object,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public void purge(Location location, RequestContext context) throws ResponseException {
		if (location == null)
			return;
		Context.getLocationService().purgeLocation(location);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doGetAll(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected NeedsPaging<Location> doGetAll(RequestContext context) {
		return new NeedsPaging<Location>(Context.getLocationService().getAllLocations(context.getIncludeAll()), context);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doSearch(org.openmrs.module.webservices.rest.web.RequestContext)
	 *      A query string and/or a tag uuid can be passed in; if both are passed in, returns an
	 *      intersection of the results; excludes retired locations
	 */
	@Override
	protected PageableResult doSearch(RequestContext context) {
		
		LocationService locationService = Context.getLocationService();
		
		String tagUuid = context.getParameter("tag");
		String query = context.getParameter("q");
		
		List<Location> locationsByTag = null;
		List<Location> locationsByQuery = null;
		
		if (tagUuid != null) {
			LocationTag locationTag = locationService.getLocationTagByUuid(tagUuid);
			locationsByTag = locationService.getLocationsByTag(locationTag);
		}
		
		if (query != null) {
			locationsByQuery = locationService.getLocations(query);
		}
		
		if (locationsByTag == null) {
			return new NeedsPaging<Location>(locationsByQuery, context);
		} else if (locationsByQuery == null) {
			return new NeedsPaging<Location>(locationsByTag, context);
		} else {
			return new NeedsPaging<Location>(
			        (List<Location>) CollectionUtils.intersection(locationsByQuery, locationsByTag), context);
		}
	}
}
