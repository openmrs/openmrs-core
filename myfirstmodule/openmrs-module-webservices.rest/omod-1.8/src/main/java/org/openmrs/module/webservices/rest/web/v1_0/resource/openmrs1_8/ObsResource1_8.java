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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Concept;
import org.openmrs.ConceptNumeric;
import org.openmrs.Drug;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.ObsService;
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
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.api.Uploadable;
import org.openmrs.module.webservices.rest.web.resource.impl.DataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.EmptySearchResult;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ConversionException;
import org.openmrs.module.webservices.rest.web.response.IllegalRequestException;
import org.openmrs.module.webservices.rest.web.response.ObjectNotFoundException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.obs.ComplexData;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.DateProperty;
import io.swagger.models.properties.DateTimeProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;

/**
 * {@link Resource} for Obs, supporting standard CRUD operations
 */
@Resource(name = RestConstants.VERSION_1 + "/obs", order = 2, supportedClass = Obs.class, supportedOpenmrsVersions = { "1.8.*" })
public class ObsResource1_8 extends DataDelegatingCrudResource<Obs> implements Uploadable {
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#delete(java.lang.Object,
	 *      java.lang.String, org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected void delete(Obs delegate, String reason, RequestContext context) throws ResponseException {
		if (delegate.isVoided()) {
			// DELETE is idempotent, so we return success here
			return;
		}
		Context.getObsService().voidObs(delegate, reason);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#undelete(java.lang.Object,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected Obs undelete(Obs delegate, RequestContext context) throws ResponseException {
		if (delegate.isVoided()) {
			delegate = Context.getObsService().unvoidObs(delegate);
		}
		return delegate;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getByUniqueId(java.lang.String)
	 */
	@Override
	public Obs getByUniqueId(String uniqueId) {
		return Context.getObsService().getObsByUuid(uniqueId);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getRepresentationDescription(org.openmrs.module.webservices.rest.web.representation.Representation)
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			// TODO how to handle valueCodedName?
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display");
			description.addProperty("concept", Representation.REF);
			description.addProperty("person", Representation.REF);
			description.addProperty("obsDatetime");
			description.addProperty("accessionNumber");
			description.addProperty("obsGroup", Representation.REF);
			description.addProperty("valueCodedName", Representation.REF);
			description.addProperty("groupMembers");
			description.addProperty("comment");
			description.addProperty("location", Representation.REF);
			description.addProperty("order", Representation.REF);
			description.addProperty("encounter", Representation.REF);
			description.addProperty("voided");
			description.addProperty("value");
			description.addProperty("valueModifier");
			description.addSelfLink();
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			return description;
		} else if (rep instanceof FullRepresentation) {
			// TODO how to handle valueCodedName?
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display");
			description.addProperty("concept");
			description.addProperty("person", Representation.REF);
			description.addProperty("obsDatetime");
			description.addProperty("accessionNumber");
			description.addProperty("obsGroup");
			description.addProperty("valueCodedName");
			description.addProperty("groupMembers", Representation.FULL);
			description.addProperty("comment");
			description.addProperty("location");
			description.addProperty("order");
			description.addProperty("encounter");
			description.addProperty("voided");
			description.addProperty("auditInfo");
			description.addProperty("value");
			description.addProperty("valueModifier");
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
		
		description.addRequiredProperty("person");
		description.addRequiredProperty("obsDatetime");
		description.addRequiredProperty("concept");
		
		description.addProperty("location");
		description.addProperty("order");
		description.addProperty("encounter");
		description.addProperty("accessionNumber");
		description.addProperty("groupMembers");
		description.addProperty("valueCodedName");
		description.addProperty("comment");
		description.addProperty("value");
		description.addProperty("valueModifier");
		
		return description;
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl model = (ModelImpl) super.getGETModel(rep);
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			model.property("uuid", new StringProperty()).property("display", new StringProperty())
			        .property("obsDatetime", new DateProperty()).property("accessionNumber", new StringProperty())
			        .property("comment", new StringProperty()).property("voided", new BooleanProperty())
			        .property("value", new StringProperty()).property("valueModifier", new StringProperty());
		}
		if (rep instanceof DefaultRepresentation) {
			model.property("concept", new RefProperty("#/definitions/ConceptGetRef"))
			        .property("person", new RefProperty("#/definitions/PersonGetRef"))
			        .property("obsGroup", new RefProperty("#/definitions/ObsGetRef"))
			        .property("groupMembers", new ArrayProperty(new RefProperty("#/definitions/ObsGetRef")))
			        .property("valueCodedName", new RefProperty("#/definitions/ConceptNameGetRef"))
			        .property("location", new RefProperty("#/definitions/LocationGetRef"))
			        .property("order", new RefProperty("#/definitions/OrderGetRef"))
			        .property("encounter", new RefProperty("#/definitions/EncounterGetRef"));
		} else if (rep instanceof FullRepresentation) {
			model.property("concept", new RefProperty("#/definitions/ConceptGet"))
			        .property("person", new RefProperty("#/definitions/PersonGet"))
			        .property("obsGroup", new RefProperty("#/definitions/ObsGet"))
			        .property("groupMembers", new ArrayProperty(new RefProperty("#/definitions/ObsGet")))
			        .property("valueCodedName", new RefProperty("#/definitions/ConceptNameGet"))
			        .property("location", new RefProperty("#/definitions/LocationGet"))
			        .property("order", new RefProperty("#/definitions/OrderGet"))
			        .property("encounter", new RefProperty("#/definitions/EncounterGet"));
		}
		return model;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		return new ModelImpl().property("person", new StringProperty().example("uuid"))
		        .property("obsDatetime", new DateTimeProperty()).property("concept", new StringProperty().example("uuid"))
		        .property("location", new StringProperty()).property("order", new StringProperty())
		        .property("encounter", new StringProperty()).property("accessionNumber", new StringProperty())
		        .property("groupMembers", new ArrayProperty(new StringProperty()))
		        .property("valueCodedName", new StringProperty()).property("comment", new StringProperty())
		        .property("voided", new BooleanProperty()).property("value", new StringProperty())
		        .property("valueModifier", new StringProperty())
		        
		        .required("person").required("obsDatetime").required("concept");
	}
	
	@Override
	public Model getUPDATEModel(Representation rep) {
		return new ModelImpl(); //FIXME missing props
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#newDelegate()
	 */
	@Override
	public Obs newDelegate() {
		return new Obs();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#purge(java.lang.Object,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public void purge(Obs delegate, RequestContext context) throws ResponseException {
		Context.getObsService().purgeObs(delegate);
		
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler#save(java.lang.Object)
	 */
	@Override
	public Obs save(Obs delegate) {
		Obs savedObs = Context.getObsService().saveObs(delegate, "REST web service");
		return Context.getObsService().getObs(savedObs.getId());
	}
	
	/**
	 * Display string for Obs
	 * 
	 * @param obs
	 * @return String ConceptName = value
	 */
	@PropertyGetter("display")
	public String getDisplayString(Obs obs) {
		if (obs.getConcept() == null)
			return "";
		
		return obs.getConcept().getName() + ": " + obs.getValueAsString(Context.getLocale());
	}
	
	/**
	 * Retrives the Obs Value as string
	 * 
	 * @param obs
	 * @return
	 */
	@PropertyGetter("value")
	public static Object getValue(Obs obs) throws ConversionException {
		if (obs.isComplex()) {
			//Note that complex obs value is handled by ObsComplexValueController1_8
			SimpleObject so = new SimpleObject();
			so.put("display", "raw file");
			SimpleObject links = new SimpleObject();
			links.put("rel", "self");
			links.put("uri", new ObsResource1_8().getUri(obs) + "/value");
			so.put("links", links);
			return so;
		}
		
		if (obs.isObsGrouping())
			return null;
		
		if (obs.getValueDatetime() != null) {
			return ConversionUtil.convert(obs.getValueDatetime(), Date.class);
		}
		
		if (obs.getValueDrug() != null) {
			return obs.getValueDrug();
		}
		
		if (obs.getValueCoded() != null) {
			return obs.getValueCoded();
		}
		
		if (obs.getValueComplex() != null) {
			return obs.getValueComplex();
		}
		
		if (obs.getValueText() != null) {
			if ("org.openmrs.Location".equals(obs.getComment())) { // string first to make it null-safe
				try {
					return Context.getLocationService().getLocation(new Integer(obs.getValueText()));
				}
				catch (NumberFormatException e) {
					return Context.getLocationService().getLocationByUuid(obs.getValueText());
				}
			} else {
				return obs.getValueText();
			}
			
		}
		
		if (obs.getValueNumeric() != null) {
			return obs.getValueNumeric();
		}
		
		return null;
	}
	
	/**
	 * Sets the members of an obs group
	 * 
	 * @param obsGroup the obs group whose members to set
	 * @param members the members to set
	 */
	@PropertySetter("groupMembers")
	public static void setGroupMembers(Obs obsGroup, Set<Obs> members) {
		for (Obs member : members) {
			member.setObsGroup(obsGroup);
		}
		obsGroup.setGroupMembers(members);
	}
	
	/**
	 * Checks if there are more than one obs in GroupMembers and converts into a DEFAULT
	 * representation
	 * 
	 * @param obs
	 * @return Object
	 * @throws ConversionException
	 */
	@PropertyGetter("groupMembers")
	public static Object getGroupMembers(Obs obs) throws ConversionException {
		if (obs.getGroupMembers() != null && !obs.getGroupMembers().isEmpty()) {
			return obs.getGroupMembers();
		}
		return null;
	}
	
	/**
	 * Annotated setter for Concept
	 * 
	 * @param obs
	 * @param value
	 */
	@PropertySetter("concept")
	public static void setConcept(Obs obs, Object value) {
		obs.setConcept(Context.getConceptService().getConceptByUuid((String) value));
	}
	
	/**
	 * Annotated setter for ConceptValue
	 * 
	 * @param obs
	 * @param value
	 * @throws ParseException
	 * @throws ConversionException
	 * <strong>Should</strong> return uuid for foncept true
	 * <strong>Should</strong> return uuid for concept false
	 * <strong>Should</strong> throw exception on unexpected value
	 * <strong>Should</strong> return uuid for primitive true
	 * <strong>Should</strong> return uuid for primitive false
	 */
	@PropertySetter("value")
	public static void setValue(Obs obs, Object value) throws ParseException, ConversionException, IOException {
		if (value != null) {
			if (obs.isComplex()) {
				byte[] bytes = DatatypeConverter.parseBase64Binary(value.toString());
				
				ComplexData complexData = new ComplexData(obs.getUuid() + ".raw", new ByteArrayInputStream(bytes));
				obs.setComplexData(complexData);
			} else if (obs.getConcept().getDatatype().isCoded()) {
				// setValueAsString is not implemented for coded obs (in core)
				
				//We want clients to be able to fetch a coded value in one rest call
				//and set the returned payload as the obs value
				if (value instanceof Map) {
					Object uuid = ((Map) value).get(RestConstants.PROPERTY_UUID);
					if (uuid != null) {
						value = uuid.toString();
					}
				}
				
				Concept valueCoded = (Concept) ConversionUtil.convert(value, Concept.class);
				if (valueCoded == null) {
					//try checking if this this is value drug
					Drug valueDrug = (Drug) ConversionUtil.convert(value, Drug.class);
					if (valueDrug != null) {
						obs.setValueCoded(valueDrug.getConcept());
						obs.setValueDrug(valueDrug);
					} else {
						throw new ObjectNotFoundException(obs.getConcept().getName().getName() + ":" + value.toString());
					}
					
				} else {
					obs.setValueCoded(valueCoded);
				}
				
			} else {
				if (obs.getConcept().isNumeric()) {
					//get the actual persistent object rather than the hibernate proxy
					ConceptNumeric concept = Context.getConceptService().getConceptNumeric(obs.getConcept().getId());
					String units = concept.getUnits();
					if (StringUtils.isNotBlank(units)) {
						String originalValue = value.toString().trim();
						if (originalValue.endsWith(units))
							value = originalValue.substring(0, originalValue.indexOf(units)).trim();
						else {
							//check that that this value has no invalid units
							try {
								Double.parseDouble(originalValue);
							}
							catch (NumberFormatException e) {
								throw new APIException(originalValue + " has invalid units", e);
							}
						}
					}
				} else if (obs.getConcept().getDatatype().isBoolean()) {
					if (value instanceof Concept) {
						value = ((Concept) value).getUuid();
					}
					if (value.equals(Context.getConceptService().getTrueConcept().getUuid())) {
						value = true;
					} else if (value.equals(Context.getConceptService().getFalseConcept().getUuid())) {
						value = false;
					} else if (!value.getClass().isAssignableFrom(Boolean.class)) {
						List<String> trueValues = Arrays.asList("true", "1", "on", "yes");
						List<String> falseValues = Arrays.asList("false", "0", "off", "no");
						
						String val = value.toString().trim().toLowerCase();
						if (trueValues.contains(val)) {
							value = Boolean.TRUE;
						} else if (falseValues.contains(val)) {
							value = Boolean.FALSE;
						}
						
						if (!(Boolean.TRUE.equals(value) || Boolean.FALSE.equals(value))) {
							throw new ConversionException("Unexpected value: " + value + " set as the value of boolean. "
							        + trueValues + falseValues + ", ConceptService.getTrueConcept or "
							        + ", ConceptService.getFalseConcept expected");
						}
					}
				}
				obs.setValueAsString(value.toString());
			}
		} else {
			throw new APIException("The value for an observation cannot be null");
		}
	}
	
	/**
	 * Gets obs by patient or encounter (paged according to context if necessary) only if a patient
	 * or encounter parameter exists respectively in the request set on the {@link RequestContext}
	 * otherwise searches for obs that match the specified query
	 * 
	 * @param context
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doSearch(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected PageableResult doSearch(RequestContext context) {
		String patientUuid = context.getRequest().getParameter("patient");
		if (patientUuid != null) {
			Patient patient = ((PatientResource1_8) Context.getService(RestService.class).getResourceBySupportedClass(
			    Patient.class)).getByUniqueId(patientUuid);
			if (patient == null)
				return new EmptySearchResult();
			List<Obs> obs = Context.getObsService().getObservationsByPerson(patient);
			return new NeedsPaging<Obs>(obs, context);
		}
		
		String encounterUuid = context.getRequest().getParameter("encounter");
		if (encounterUuid != null) {
			Encounter enc = ((EncounterResource1_8) Context.getService(RestService.class).getResourceBySupportedClass(
			    Encounter.class)).getByUniqueId(encounterUuid);
			if (enc == null)
				return new EmptySearchResult();
			
			List<Obs> obs = new ArrayList<Obs>(enc.getAllObs(context.getIncludeAll()));
			return new NeedsPaging<Obs>(obs, context);
		}
		
		return new NeedsPaging<Obs>(Context.getObsService().getObservations(context.getParameter("q")), context);
	}
	
	@Override
	public Object upload(MultipartFile file, RequestContext context) throws ResponseException, IOException {
		String json = context.getParameter("json");
		if (json == null) {
			throw new IllegalRequestException("Obs metadata must be included in a request parameter named 'json'.");
		}
		
		SimpleObject object = SimpleObject.parseJson(json);
		Obs obs = convert(object);
		
		if (!obs.isComplex()) {
			throw new IllegalRequestException("Complex concept must be set in order to create a complex obs with data.");
		}
		
		ObsService obsService = Context.getObsService();
		
		ComplexData complexData = new ComplexData(file.getOriginalFilename(), new ByteArrayInputStream(file.getBytes()));
		obs.setComplexData(complexData);
		
		obs = obsService.saveObs(obs, null);
		
		return (SimpleObject) ConversionUtil.convertToRepresentation(obs, Representation.DEFAULT);
	}
	
}
