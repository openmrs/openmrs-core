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
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.Person;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.AlreadyPaged;
import org.openmrs.module.webservices.rest.web.resource.impl.DataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.ServiceSearcher;
import org.openmrs.module.webservices.rest.web.response.ConversionException;
import org.openmrs.module.webservices.rest.web.response.IllegalRequestException;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.validation.ValidateUtil;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * {@link Resource} for Patients, supporting standard CRUD operations
 */
@Resource(name = RestConstants.VERSION_1 + "/patient", supportedClass = Patient.class, supportedOpenmrsVersions = { "1.8.*" })
public class PatientResource1_8 extends DataDelegatingCrudResource<Patient> {
	
	public PatientResource1_8() {
	}
	
	@PropertyGetter("person")
	public static Person getPerson(Patient instance) {
		return new Person(instance); //Must be a Person instead of Patient to prevent infinite recursion RESTWS-273
	}
	
	/**
	 * It is empty, because we set that already in the create method.
	 * <p>
	 * It takes String instead of Person so that the uuid is not resolved to a person, which leads
	 * to the Hibernate exception: the object is already associated with the session.
	 * 
	 * @param instance
	 * @param personUuid
	 */
	@PropertySetter("person")
	public static void setPerson(Patient instance, String personUuid) {
	}
	
	@PropertyGetter("identifiers")
	public static Set<PatientIdentifier> getIdentifiers(Patient instance) {
		return new LinkedHashSet<PatientIdentifier>(instance.getActiveIdentifiers());
	}
	
	@PropertySetter("identifiers")
	public static void setIdentifiers(Patient instance, List<PatientIdentifier> identifiers)
	        throws ResourceDoesNotSupportOperationException {
		if (instance.getIdentifiers() != null && instance.getIdentifiers().containsAll(identifiers)) {
			return;
		}
		if (instance.getIdentifiers() != null && !instance.getIdentifiers().isEmpty()) {
			throw new ResourceDoesNotSupportOperationException("Identifiers can only be set for newly created objects!");
		}
		if (identifiers == null || identifiers.isEmpty()) {
			throw new ResourceDoesNotSupportOperationException("At least one identifier required");
		}
		
		boolean hasPreferred = false;
		for (PatientIdentifier identifier : identifiers) {
			if (identifier.isPreferred()) {
				if (!hasPreferred) {
					hasPreferred = true;
				} else {
					throw new ResourceDoesNotSupportOperationException("Only one preferred identifier allowed");
				}
			}
		}
		
		if (!hasPreferred) {
			//Mark the first one as preferred if none marked
			identifiers.iterator().next().setPreferred(true);
		}
		
		instance.getIdentifiers().clear();
		for (PatientIdentifier identifier : identifiers) {
			instance.addIdentifier(identifier);
		}
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#getRepresentationDescription(org.openmrs.module.webservices.rest.web.representation.Representation)
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display");
			description.addProperty("identifiers", Representation.REF);
			description.addProperty("person", Representation.DEFAULT);
			description.addProperty("voided");
			description.addSelfLink();
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display");
			description.addProperty("identifiers", Representation.DEFAULT);
			description.addProperty("person", Representation.FULL);
			description.addProperty("voided");
			description.addProperty("auditInfo");
			description.addSelfLink();
			return description;
		}
		return null;
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl model = (ModelImpl) super.getGETModel(rep);
		//FIXME check uuid, display in ref rep
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			model
			        .property("uuid", new StringProperty())
			        .property("display", new StringProperty())
			        .property("identifiers", new ArrayProperty(new RefProperty("#/definitions/PatientIdentifierGetRef")))
			        .property("preferred", new BooleanProperty()._default(false))
			        .property("voided", new BooleanProperty());
		}
		if (rep instanceof DefaultRepresentation) {
			model
			        .property("person", new RefProperty("#/definitions/PersonGetRef"));
		} else if (rep instanceof FullRepresentation) {
			model
			        .property("person", new RefProperty("#/definitions/PersonGet"));
		}
		return model;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		ModelImpl model = new ModelImpl()
		        .property("person", new StringProperty().example("uuid"))
		        .property("identifiers", new ArrayProperty(new RefProperty("#/definitions/PatientIdentifierCreate")))
		        
		        .required("person").required("identifiers");
		if (rep instanceof FullRepresentation) {
			model
			        .property("person", new RefProperty("#/definitions/PersonCreate"));
		}
		return model;
	}
	
	@Override
	public Model getUPDATEModel(Representation rep) {
		return new ModelImpl()
		        .property("person", new RefProperty("#/definitions/PersonGet"))
		        
		        .required("person");
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getCreatableProperties()
	 */
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addRequiredProperty("person");
		description.addRequiredProperty("identifiers");
		return description;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getUpdatableProperties()
	 */
	@Override
	public DelegatingResourceDescription getUpdatableProperties() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addRequiredProperty("person");
		return description;
	}
	
	/**
	 * The method is overwritten, because we need to create a patient from an existing person. In
	 * the POST body only person and identifiers are provided and other properties must come from
	 * the existing person. We need to promote the existing person to be a patient by overwriting it
	 * and at the same time preserving all person properties.
	 * 
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#create(org.openmrs.module.webservices.rest.SimpleObject,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public Object create(SimpleObject propertiesToCreate, RequestContext context) throws ResponseException {
		Patient delegate = getPatient(propertiesToCreate);
		ValidateUtil.validate(delegate);
		delegate = save(delegate);
		return ConversionUtil.convertToRepresentation(delegate, Representation.DEFAULT);
	}
	
	public Patient getPatient(SimpleObject propertiesToCreate) {
		Object personProperty = propertiesToCreate.get("person");
		Person person = null;
		if (personProperty == null) {
			throw new ConversionException("The person property is missing");
		} else if (personProperty instanceof String) {
			person = Context.getPersonService().getPersonByUuid((String) personProperty);
			Context.evictFromSession(person);
		} else if (personProperty instanceof Map) {
			person = (Person) ConversionUtil.convert(personProperty, Person.class);
			propertiesToCreate.put("person", "");
		}
		
		Patient delegate = new Patient(person);
		setConvertedProperties(delegate, propertiesToCreate, getCreatableProperties(), true);
		return delegate;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#newDelegate()
	 */
	@Override
	public Patient newDelegate() {
		return new Patient();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#save(java.lang.Object)
	 */
	@Override
	public Patient save(Patient patient) {
		return Context.getPatientService().savePatient(patient);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#getByUniqueId(java.lang.String)
	 */
	@Override
	public Patient getByUniqueId(String uuid) {
		return Context.getPatientService().getPatientByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#delete(java.lang.Object,
	 *      java.lang.String, org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public void delete(Patient patient, String reason, RequestContext context) throws ResponseException {
		if (patient.isVoided()) {
			// DELETE is idempotent, so we return success here
			return;
		}
		Context.getPatientService().voidPatient(patient, reason);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#undelete(java.lang.Object,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected Patient undelete(Patient patient, RequestContext context) throws ResponseException {
		if (patient.isVoided()) {
			patient = Context.getPatientService().unvoidPatient(patient);
		}
		return patient;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#purge(java.lang.Object,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public void purge(Patient patient, RequestContext context) throws ResponseException {
		if (patient == null) {
			// DELETE is idempotent, so we return success here
			return;
		}
		Context.getPatientService().purgePatient(patient);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doSearch(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected AlreadyPaged<Patient> doSearch(RequestContext context) {
		String attributesParam = context.getParameter("attributesToFindDuplicatesBy");
		if (attributesParam != null) {
			List<String> attributes = Arrays.asList((attributesParam.split(",")));

			if (attributes.size() < 2) {
				throw new IllegalRequestException("Choose a minimum of two attributes");
			}

			List<Patient> duplicatedPatientsFound = Context.getPatientService().getDuplicatePatientsByAttributes(attributes);

			return new AlreadyPaged<Patient>(context, duplicatedPatientsFound, false, (long) duplicatedPatientsFound.size());
		}

		return new ServiceSearcher<Patient>(PatientService.class, "getPatients", "getCountOfPatients").search(
		    context.getParameter("q"), context);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#getPropertiesToExposeAsSubResources()
	 */
	@Override
	public List<String> getPropertiesToExposeAsSubResources() {
		return Arrays.asList("identifiers");
	}
	
	/**
	 * @param patient
	 * @return identifier + name (for concise display purposes)
	 */
	@PropertyGetter("display")
	public String getDisplayString(Patient patient) {
		if (patient.getPatientIdentifier() == null)
			return "";
		
		return patient.getPatientIdentifier().getIdentifier() + " - " + patient.getPersonName().getFullName();
	}
	
	@Override
	public Object update(String uuid, SimpleObject propertiesToUpdate, RequestContext context) throws ResponseException {
		if (propertiesToUpdate.get("person") == null) {
			return super.update(uuid, propertiesToUpdate, context);
		}
		Patient patient = getPatientForUpdate(uuid, propertiesToUpdate);
		ValidateUtil.validate(patient);
		patient = save(patient);
		return ConversionUtil.convertToRepresentation(patient, Representation.DEFAULT);
	}
	
	public Patient getPatientForUpdate(String uuid, Map<String, Object> propertiesToUpdate) {
		Patient patient = getByUniqueId(uuid);
		PersonResource1_8 personResource = (PersonResource1_8) Context.getService(RestService.class)
		        .getResourceBySupportedClass(Person.class);
		personResource.setConvertedProperties(patient, (Map<String, Object>) propertiesToUpdate.get("person"),
		    personResource.getUpdatableProperties(), false);
		return patient;
	}
	
}
