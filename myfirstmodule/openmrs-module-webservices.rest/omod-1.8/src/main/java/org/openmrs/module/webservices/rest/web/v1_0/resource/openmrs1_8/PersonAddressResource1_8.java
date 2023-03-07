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
import io.swagger.models.properties.StringProperty;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.annotation.SubResource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubResource;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * {@link Resource} for PersonAddress, supporting standard CRUD operations
 */
@SubResource(parent = PersonResource1_8.class, path = "address", supportedClass = PersonAddress.class, supportedOpenmrsVersions = {
        "1.8.* - 1.12.*" })
public class PersonAddressResource1_8 extends DelegatingSubResource<PersonAddress, Person, PersonResource1_8> {
	
	public PersonAddressResource1_8() {
		allowedMissingProperties.add("startDate");
		allowedMissingProperties.add("endDate");
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getRepresentationDescription(org.openmrs.module.webservices.rest.web.representation.Representation)
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("display");
			description.addProperty("uuid");
			description.addProperty("preferred");
			description.addProperty("address1");
			description.addProperty("address2");
			description.addProperty("cityVillage");
			description.addProperty("stateProvince");
			description.addProperty("country");
			description.addProperty("postalCode");
			description.addProperty("countyDistrict");
			description.addProperty("address3");
			description.addProperty("address4");
			description.addProperty("address5");
			description.addProperty("address6");
			description.addProperty("startDate");
			description.addProperty("endDate");
			description.addProperty("latitude");
			description.addProperty("longitude");
			description.addProperty("voided");
			description.addSelfLink();
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("display");
			description.addProperty("uuid");
			description.addProperty("preferred");
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
			description.addProperty("startDate");
			description.addProperty("endDate");
			description.addProperty("latitude");
			description.addProperty("longitude");
			description.addProperty("voided");
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
		description.addProperty("preferred");
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
		description.addProperty("startDate");
		description.addProperty("endDate");
		description.addProperty("latitude");
		description.addProperty("longitude");
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
		ModelImpl model = ((ModelImpl) super.getGETModel(rep))
		        .property("uuid", new StringProperty())
		        .property("display", new StringProperty());
		
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			model
			        .property("preferred", new BooleanProperty()._default(false))
			        .property("address1", new StringProperty())
			        .property("address2", new StringProperty())
			        .property("cityVillage", new StringProperty())
			        .property("stateProvince", new StringProperty())
			        .property("country", new StringProperty())
			        .property("postalCode", new StringProperty())
			        .property("countyDistrict", new StringProperty())
			        .property("address3", new StringProperty())
			        .property("address4", new StringProperty())
			        .property("address5", new StringProperty())
			        .property("address6", new StringProperty())
			        .property("startDate", new DateProperty())
			        .property("endDate", new DateProperty())
			        .property("latitude", new StringProperty())
			        .property("longitude", new StringProperty())
			        .property("voided", new BooleanProperty());
		}
		return model;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		return new ModelImpl()
		        .property("preferred", new BooleanProperty()._default(false))
		        .property("address1", new StringProperty())
		        .property("address2", new StringProperty())
		        .property("cityVillage", new StringProperty())
		        .property("stateProvince", new StringProperty())
		        .property("country", new StringProperty())
		        .property("postalCode", new StringProperty())
		        .property("countyDistrict", new StringProperty())
		        .property("address3", new StringProperty())
		        .property("address4", new StringProperty())
		        .property("address5", new StringProperty())
		        .property("address6", new StringProperty())
		        .property("startDate", new DateProperty())
		        .property("endDate", new DateProperty())
		        .property("latitude", new StringProperty())
		        .property("longitude", new StringProperty());
	}
	
	@Override
	public Model getUPDATEModel(Representation rep) {
		return getCREATEModel(rep);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getByUniqueId(java.lang.String)
	 */
	@Override
	public PersonAddress getByUniqueId(String uuid) {
		return Context.getPersonService().getPersonAddressByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#delete(java.lang.Object,
	 *      java.lang.String, org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public void delete(PersonAddress address, String reason, RequestContext context) throws ResponseException {
		///API had no void methods as of 1.8 other
		//we should be calling voidPersonAddress that was added in 1.9
		address.setVoided(true);
		address.setVoidedBy(Context.getAuthenticatedUser());
		address.setDateVoided(new Date());
		address.setVoidReason(reason);
		Context.getPersonService().savePerson(address.getPerson());
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#purge(java.lang.Object,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public void purge(PersonAddress address, RequestContext context) throws ResponseException {
		///API has no void methods as of 1.8 and earlier versios,
		//we should be calling voidPersonAddress(PersonAddress, Reason) that was added in 1.9
		address.getPerson().removeAddress(address);
		Context.getPersonService().savePerson(address.getPerson());
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler#save(java.lang.Object)
	 */
	@Override
	public PersonAddress save(PersonAddress newAddress) {
		// make sure that the address has actually been added to the person
		boolean needToAdd = true;
		for (PersonAddress pa : newAddress.getPerson().getAddresses()) {
			if (pa.equals(newAddress)) {
				needToAdd = false;
				break;
			}
		}
		
		if (needToAdd) {
			newAddress.getPerson().addAddress(newAddress);
		}
		
		// if this address is marked preferred, then we need to clear any others that are marked as preferred
		if (newAddress.isPreferred()) {
			for (PersonAddress pa : newAddress.getPerson().getAddresses()) {
				if (!pa.equals(newAddress)) {
					pa.setPreferred(false);
				}
			}
		}
		
		Context.getPersonService().savePerson(newAddress.getPerson());
		
		return newAddress;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#newDelegate()
	 */
	@Override
	public PersonAddress newDelegate() {
		return new PersonAddress();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubResource#getParent(java.lang.Object)
	 */
	@Override
	public Person getParent(PersonAddress instance) {
		return instance.getPerson();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubResource#setParent(java.lang.Object,
	 *      java.lang.Object)
	 */
	@Override
	public void setParent(PersonAddress instance, Person parent) {
		instance.setPerson(parent);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubResource#doGetAll(java.lang.Object,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public NeedsPaging<PersonAddress> doGetAll(Person parent, RequestContext context) throws ResponseException {
		//We don't return voided addresses
		List<PersonAddress> nonVoidedAddresses = new ArrayList<PersonAddress>(parent.getAddresses().size());
		for (PersonAddress personAddress : parent.getAddresses()) {
			if (!personAddress.isVoided())
				nonVoidedAddresses.add(personAddress);
		}
		return new NeedsPaging<PersonAddress>(nonVoidedAddresses, context);
	}
	
	/**
	 * Gets the display string for a person address.
	 * 
	 * @param address the address object.
	 * @return the display string.
	 */
	@PropertyGetter("display")
	public String getDisplayString(PersonAddress address) {
		return address.getAddress1();
	}
}
