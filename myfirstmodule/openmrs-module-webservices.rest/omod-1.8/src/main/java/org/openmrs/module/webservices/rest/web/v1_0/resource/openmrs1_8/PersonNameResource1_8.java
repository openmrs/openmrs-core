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
import io.swagger.models.properties.StringProperty;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.api.context.Context;
import org.openmrs.layout.web.name.NameSupport;
import org.openmrs.layout.web.name.NameTemplate;
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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * {@link Resource} for PersonNames, supporting standard CRUD operations
 */
@SubResource(parent = PersonResource1_8.class, path = "name", supportedClass = PersonName.class, supportedOpenmrsVersions = {
        "1.8.* - 1.12.*" })
public class PersonNameResource1_8 extends DelegatingSubResource<PersonName, Person, PersonResource1_8> {
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("display");
			description.addProperty("uuid");
			description.addProperty("givenName");
			description.addProperty("middleName");
			description.addProperty("familyName");
			description.addProperty("familyName2");
			description.addProperty("voided");
			description.addSelfLink();
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("display");
			description.addProperty("uuid");
			description.addProperty("givenName");
			description.addProperty("middleName");
			description.addProperty("familyName");
			description.addProperty("familyName2");
			description.addProperty("preferred");
			description.addProperty("prefix");
			description.addProperty("familyNamePrefix");
			description.addProperty("familyNameSuffix");
			description.addProperty("degree");
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
		description.addRequiredProperty("givenName");
		description.addRequiredProperty("familyName");
		description.addProperty("middleName");
		description.addProperty("familyName2");
		description.addProperty("preferred");
		description.addProperty("prefix");
		description.addProperty("familyNamePrefix");
		description.addProperty("familyNameSuffix");
		description.addProperty("degree");
		description.addProperty("preferred");
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
		ModelImpl model = (ModelImpl) super.getGETModel(rep);
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			model
			        .property("display", new StringProperty())
			        .property("uuid", new StringProperty())
			        .property("givenName", new StringProperty())
			        .property("middleName", new StringProperty())
			        .property("familyName", new StringProperty())
			        .property("familyName2", new StringProperty())
			        .property("voided", new BooleanProperty());
		}
		if (rep instanceof FullRepresentation) {
			model
			        .property("preferred", new BooleanProperty())
			        .property("prefix", new StringProperty())
			        .property("familyNamePrefix", new StringProperty())
			        .property("familyNameSuffix", new StringProperty())
			        .property("degree", new StringProperty());
		}
		return model;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		return new ModelImpl()
		        .property("givenName", new StringProperty())
		        .property("middleName", new StringProperty())
		        .property("familyName", new StringProperty())
		        .property("familyName2", new StringProperty())
		        .property("preferred", new BooleanProperty()._default(false))
		        .property("prefix", new StringProperty())
		        .property("familyNamePrefix", new StringProperty())
		        .property("familyNameSuffix", new StringProperty())
		        .property("degree", new StringProperty())
		        
		        .required("givenName").required("familyName");
	}
	
	@Override
	public Model getUPDATEModel(Representation rep) {
		return getCREATEModel(rep);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubResource#getParent(java.lang.Object)
	 */
	@Override
	public Person getParent(PersonName instance) {
		return instance.getPerson();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubResource#setParent(java.lang.Object,
	 *      java.lang.Object)
	 */
	@Override
	public void setParent(PersonName instance, Person person) {
		instance.setPerson(person);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.api.SubResource#doGetAll(java.lang.Object,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public NeedsPaging<PersonName> doGetAll(Person parent, RequestContext context) throws ResponseException {
		List<PersonName> names = new ArrayList<PersonName>();
		if (parent != null) {
			names.addAll(parent.getNames());
		}
		return new NeedsPaging<PersonName>(names, context);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getByUniqueId(java.lang.String)
	 */
	@Override
	public PersonName getByUniqueId(String uuid) {
		return Context.getPersonService().getPersonNameByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#delete(java.lang.Object,
	 *      java.lang.String, org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public void delete(PersonName pn, String reason, RequestContext context) throws ResponseException {
		pn.setVoided(true);
		pn.setVoidedBy(Context.getAuthenticatedUser());
		pn.setVoidReason(reason);
		pn.setDateVoided(new Date());
		Context.getPersonService().savePerson(pn.getPerson());
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#purge(java.lang.Object,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public void purge(PersonName pn, RequestContext context) throws ResponseException {
		pn.getPerson().removeName(pn);
		Context.getPersonService().savePerson(pn.getPerson());
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler#save(java.lang.Object)
	 */
	@Override
	public PersonName save(PersonName newName) {
		// make sure that the name has actually been added to the person
		boolean needToAdd = true;
		for (PersonName pn : newName.getPerson().getNames()) {
			if (pn.equals(newName)) {
				needToAdd = false;
				break;
			}
		}
		if (needToAdd)
			newName.getPerson().addName(newName);
		Context.getPersonService().savePerson(newName.getPerson());
		return newName;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#newDelegate()
	 */
	@Override
	public PersonName newDelegate() {
		return new PersonName();
	}
	
	/**
	 * Gets the display string for a person name.
	 * 
	 * @param personName the person name object.
	 * @return the display string.
	 */
	@PropertyGetter("display")
	public String getDisplayString(PersonName personName) {
		try {
			NameTemplate nameTemplate = NameSupport.getInstance().getDefaultLayoutTemplate();
			
			if (nameTemplate != null) {
				// need to use reflection since the format method was not added until later versions of openmrs
				Method format = NameTemplate.class.getDeclaredMethod("format", PersonName.class);
				return (String) format.invoke(nameTemplate, personName);
			}
		}
		catch (Exception e) {
			// fall through to just returning full name if no format method found or format fails
		}
		
		// otherwise, just return full name
		return personName.getFullName();
	}
}
