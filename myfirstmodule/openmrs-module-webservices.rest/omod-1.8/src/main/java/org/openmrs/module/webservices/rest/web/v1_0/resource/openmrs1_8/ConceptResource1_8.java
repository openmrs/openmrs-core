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
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import org.apache.commons.lang.StringUtils;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptDescription;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNumeric;
import org.openmrs.ConceptSearchResult;
import org.openmrs.Drug;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.helper.HibernateCollectionHelper;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.RepHandler;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.NamedRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.AlreadyPaged;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ConversionException;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.util.LocaleUtility;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * {@link Resource} for {@link Concept}, supporting standard CRUD operations
 */
@Resource(name = RestConstants.VERSION_1 + "/concept", order = 2, supportedClass = Concept.class, supportedOpenmrsVersions = "1.8.*")
public class ConceptResource1_8 extends DelegatingCrudResource<Concept> {
	
	public ConceptResource1_8() {
		//RESTWS-439
		//Concept numeric fields
		allowedMissingProperties.add("hiNormal");
		allowedMissingProperties.add("hiAbsolute");
		allowedMissingProperties.add("hiCritical");
		allowedMissingProperties.add("lowNormal");
		allowedMissingProperties.add("lowAbsolute");
		allowedMissingProperties.add("lowCritical");
		allowedMissingProperties.add("units");
		allowedMissingProperties.add("precise");
		allowedMissingProperties.add("allowDecimal");
		allowedMissingProperties.add("displayPrecision");
	}
	
	@RepHandler(RefRepresentation.class)
	public SimpleObject asRef(Concept delegate) throws ConversionException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("uuid");
		description.addProperty("display", "displayString", Representation.DEFAULT);
		if (delegate.isRetired()) {
			description.addProperty("retired");
		}
		description.addSelfLink();
		return convertDelegateToRepresentation(delegate, description);
	}
	
	@RepHandler(FullRepresentation.class)
	public SimpleObject asFull(Concept delegate) throws ConversionException {
		DelegatingResourceDescription description = fullRepresentationDescription(delegate);
		return convertDelegateToRepresentation(delegate, description);
	}
	
	@RepHandler(value = NamedRepresentation.class, name = "fullchildren")
	public SimpleObject asFullChildren(Concept delegate) throws ConversionException {
		Set<String> path = new HashSet<String>();
		path.add(delegate.getUuid());
		assertNoCycles(delegate, path);
		
		return asFullChildrenInternal(delegate);
	}
	
	protected void assertNoCycles(Concept delegate, Set<String> path) throws ConversionException {
		for (Concept member : delegate.getSetMembers()) {
			if (path.add(member.getUuid())) {
				assertNoCycles(member, path);
			} else {
				throw new ConversionException("Cycles in children are not supported. Concept with uuid "
				        + delegate.getUuid() + " repeats in a set.");
			}
			path.remove(member.getUuid());
		}
	}
	
	/**
	 * It is used internally for the fullchildren representation. Contrary to the fullchildren
	 * handler it does not check for cycles.
	 * 
	 * @param delegate
	 * @return
	 * @throws ConversionException
	 */
	@RepHandler(value = NamedRepresentation.class, name = "fullchildreninternal")
	public SimpleObject asFullChildrenInternal(Concept delegate) throws ConversionException {
		DelegatingResourceDescription description = fullRepresentationDescription(delegate);
		description.removeProperty("setMembers");
		description.addProperty("setMembers", new NamedRepresentation("fullchildreninternal"));
		description.removeProperty("answers");
		description.addProperty("answers", Representation.FULL);
		return convertDelegateToRepresentation(delegate, description);
	}
	
	@Override
	public List<Representation> getAvailableRepresentations() {
		List<Representation> availableRepresentations = super.getAvailableRepresentations();
		availableRepresentations.add(new NamedRepresentation("fullchildren"));
		return availableRepresentations;
	}
	
	protected DelegatingResourceDescription fullRepresentationDescription(Concept delegate) {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("uuid");
		description.addProperty("display");
		description.addProperty("name", Representation.DEFAULT);
		description.addProperty("datatype", Representation.DEFAULT);
		description.addProperty("conceptClass", Representation.DEFAULT);
		description.addProperty("set");
		description.addProperty("version");
		description.addProperty("retired");
		
		description.addProperty("names", Representation.DEFAULT);
		description.addProperty("descriptions", Representation.DEFAULT);
		
		description.addProperty("mappings", Representation.DEFAULT);
		
		description.addProperty("answers", Representation.DEFAULT);
		description.addProperty("setMembers", Representation.DEFAULT);
		description.addProperty("auditInfo");
		description.addSelfLink();
		if (delegate.isNumeric()) {
			description.addProperty("hiNormal");
			description.addProperty("hiAbsolute");
			description.addProperty("hiCritical");
			description.addProperty("lowNormal");
			description.addProperty("lowAbsolute");
			description.addProperty("lowCritical");
			description.addProperty("units");
			description.addProperty("precise");
		}
		return description;
	}
	
	/**
	 * @see DelegatingCrudResource#getRepresentationDescription(Representation)
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display");
			description.addProperty("name", Representation.DEFAULT);
			description.addProperty("datatype", Representation.REF);
			description.addProperty("conceptClass", Representation.REF);
			description.addProperty("set");
			description.addProperty("version");
			description.addProperty("retired");
			
			description.addProperty("names", Representation.REF);
			description.addProperty("descriptions", Representation.REF);
			
			description.addProperty("mappings", Representation.REF);
			
			description.addProperty("answers", Representation.REF);
			description.addProperty("setMembers", Representation.REF);
			//description.addProperty("conceptMappings", Representation.REF);  add as subresource
			
			description.addSelfLink();
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			return description;
		}
		return null;
	}
	
	public Model getGETModel(Representation rep) {
		ModelImpl modelImpl = ((ModelImpl) super.getGETModel(rep))
		        .property("uuid", new StringProperty())
		        .property("display", new StringProperty());
		if (rep instanceof DefaultRepresentation) {
			modelImpl
			        .property("name", new RefProperty("#/definitions/ConceptNameGet"))
			        .property("datatype", new RefProperty("#/definitions/ConceptdatatypeGetRef"))
			        .property("conceptClass", new RefProperty("#/definitions/ConceptclassGetRef"))
			        .property("set", new BooleanProperty())
			        .property("version", new StringProperty())
			        .property("retired", new BooleanProperty())
			        .property("names", new ArrayProperty(new RefProperty("#/definitions/ConceptNameGetRef"))) //FIXME
			        .property("descriptions", new ArrayProperty(new RefProperty("#/definitions/ConceptDescriptionGetRef"))) //FIXME
			        .property("mappings", new ArrayProperty(new RefProperty("#/definitions/ConceptMappingGetRef"))) //FIXME
			        .property("answers", new ArrayProperty(new ObjectProperty())) //FIXME
			        .property("setMembers", new ArrayProperty(new ObjectProperty())); //FIXME
		}
		return modelImpl;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		ModelImpl model = new ModelImpl()
		        .property("names", new ArrayProperty(new RefProperty("#/definitions/ConceptNameCreate")))
		        .property("datatype", new StringProperty().example("uuid"))
		        .property("set", new BooleanProperty())
		        .property("version", new StringProperty())
		        .property("answers", new ArrayProperty(new StringProperty().example("uuid")))
		        .property("setMembers", new ArrayProperty(new StringProperty().example("uuid")))
		        
		        //ConceptNumeric properties
		        .property("hiNormal", new StringProperty())
		        .property("hiAbsolute", new StringProperty())
		        .property("hiCritical", new StringProperty())
		        .property("lowNormal", new StringProperty())
		        .property("lowAbsolute", new StringProperty())
		        .property("lowCritical", new StringProperty())
		        .property("units", new StringProperty())
		        .property("allowDecimal", new StringProperty())
		        .property("displayPrecision", new StringProperty())
		        
		        .required("names").required("datatype").required("conceptClass");
		if (rep instanceof DefaultRepresentation) {
			model
			        .property("conceptClass", new StringProperty())
			        .property("descriptions", new ArrayProperty(new StringProperty()))
			        .property("mappings", new ArrayProperty(new StringProperty()));
		}
		else if (rep instanceof FullRepresentation) {
			model
			        .property("conceptClass", new RefProperty("#/definitions/ConceptclassCreate"))
			        .property("descriptions", new ArrayProperty(new RefProperty("#/definitions/ConceptDescriptionCreate")))
			        .property("mappings", new ArrayProperty(new RefProperty("#/definitions/ConceptMappingCreate")));
		}
		return model;
	}
	
	@Override
	public Model getUPDATEModel(Representation representation) {
		return new ModelImpl()
		        .property("name", new RefProperty("#/definitions/ConceptNameCreate"))
		        .property("names", new ArrayProperty(new RefProperty("#/definitions/ConceptNameCreate")))
		        .property("descriptions", new ArrayProperty(new RefProperty("#/definitions/ConceptDescriptionCreate")));
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getCreatableProperties()
	 */
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addRequiredProperty("names");
		description.addRequiredProperty("datatype");
		description.addRequiredProperty("conceptClass");
		
		description.addProperty("descriptions");
		description.addProperty("set");
		description.addProperty("version");
		description.addProperty("mappings");
		description.addProperty("answers");
		description.addProperty("setMembers");
		
		//ConceptNumeric properties
		description.addProperty("hiNormal");
		description.addProperty("hiAbsolute");
		description.addProperty("hiCritical");
		description.addProperty("lowNormal");
		description.addProperty("lowAbsolute");
		description.addProperty("lowCritical");
		description.addProperty("units");
		description.addProperty("allowDecimal");
		description.addProperty("displayPrecision");
		return description;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getUpdatableProperties()
	 */
	@Override
	public DelegatingResourceDescription getUpdatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = super.getUpdatableProperties();
		
		description.addProperty("name");
		description.addProperty("names");
		description.addProperty("descriptions");
		
		return description;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getPropertiesToExposeAsSubResources()
	 */
	@Override
	public List<String> getPropertiesToExposeAsSubResources() {
		return Arrays.asList("names", "descriptions", "conceptMappings");
	}
	
	/**
	 * Sets the name property to be the fully specified name of the Concept in the current locale
	 * 
	 * @param instance
	 * @param name
	 */
	@PropertySetter("name")
	public static void setFullySpecifiedName(Concept instance, String name) {
		ConceptName fullySpecifiedName = new ConceptName(name, Context.getLocale());
		instance.setFullySpecifiedName(fullySpecifiedName);
	}
	
	/**
	 * It's needed, because of ConversionException: Don't know how to handle collection class:
	 * interface java.util.Collection If request to update Concept updates ConceptName, adequate
	 * resource takes care of it, so this method just adds new and removes deleted names.
	 * 
	 * @param instance
	 * @param names
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	@PropertySetter("names")
	public static void setNames(Concept instance, List<ConceptName> names) throws IllegalAccessException,
	        InvocationTargetException, NoSuchMethodException {
		
		new HibernateCollectionHelper<Concept, ConceptName>(
		                                                    instance) {
			
			@Override
			public int compare(ConceptName left, ConceptName right) {
				if (Objects.equals(left.getUuid(), right.getUuid())) {
					return 0;
				}
				boolean areEqual = (Objects.equals(left.getName(), right.getName())
				        && Objects.equals(left.getConceptNameType(), right.getConceptNameType()) && Objects.equals(
				    left.getLocale(), right.getLocale()));
				return areEqual ? 0 : 1;
			}
			
			@Override
			public Collection<ConceptName> getAll() {
				return instance.getNames();
			}
			
			@Override
			public void add(ConceptName item) {
				instance.addName(item);
			}
			
			@Override
			public void remove(ConceptName item) {
				instance.removeName(item);
			}
		}.set(names);
	}
	
	/**
	 * It's needed, because of ConversionException: Don't know how to handle collection class:
	 * interface java.util.Collection
	 * 
	 * @param instance
	 * @param descriptions
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	@PropertySetter("descriptions")
	public static void setDescriptions(Concept instance, List<ConceptDescription> descriptions)
	        throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		
		new HibernateCollectionHelper<Concept, ConceptDescription>(
		                                                           instance) {
			
			@Override
			public int compare(ConceptDescription left, ConceptDescription right) {
				if (Objects.equals(left.getUuid(), right.getUuid())) {
					return 0;
				}
				boolean areEqual = (Objects.equals(left.getDescription(), right.getDescription()) && Objects.equals(
				    left.getLocale(), right.getLocale()));
				return areEqual ? 0 : 1;
			}
			
			@Override
			public Collection<ConceptDescription> getAll() {
				return instance.getDescriptions();
			}
			
			@Override
			public void add(ConceptDescription item) {
				instance.addDescription(item);
			}
			
			@Override
			public void remove(ConceptDescription item) {
				instance.removeDescription(item);
			}
		}.set(descriptions);
	}
	
	/**
	 * It's needed, because of ConversionException: Don't know how to handle collection class:
	 * interface java.util.Collection
	 * 
	 * @param instance
	 * @param mappings
	 */
	@PropertySetter("mappings")
	public static void setMappings(Concept instance, List<ConceptMap> mappings) {
		instance.getConceptMappings().clear();
		for (ConceptMap map : mappings) {
			instance.addConceptMapping(map);
		}
	}
	
	@PropertyGetter("mappings")
	public static List<ConceptMap> getMappings(Concept instance) {
		return new ArrayList<ConceptMap>(instance.getConceptMappings());
	}
	
	/**
	 * Gets the display name of the Concept delegate
	 * 
	 * @param instance the delegate instance to get the display name off
	 */
	@PropertyGetter("display")
	public String getDisplayName(Concept instance) {
		ConceptName cn = instance.getName();
		return cn == null ? null : cn.getName();
	}
	
	/**
	 * {@link #newDelegate(SimpleObject)} is used instead to support ConceptNumeric
	 * 
	 * @see DelegatingCrudResource#newDelegate()
	 */
	@Override
	public Concept newDelegate() {
		throw new ResourceDoesNotSupportOperationException("Should use newDelegate(SimpleObject) instead");
	}
	
	@Override
	public Concept newDelegate(SimpleObject object) {
		String datatypeUuid = (String) object.get("datatype");
		if (ConceptDatatype.NUMERIC_UUID.equals(datatypeUuid)) {
			return new ConceptNumeric();
		} else {
			return new Concept();
		}
	}
	
	/**
	 * @see DelegatingCrudResource#save(java.lang.Object)
	 */
	@Override
	public Concept save(Concept c) {
		return Context.getConceptService().saveConcept(c);
	}
	
	/**
	 * Fetches a concept by uuid
	 * 
	 * @see DelegatingCrudResource#getByUniqueId(java.lang.String)
	 */
	@Override
	public Concept getByUniqueId(String uuid) {
		return Context.getConceptService().getConceptByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#purge(java.lang.Object,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public void purge(Concept concept, RequestContext context) throws ResponseException {
		if (concept == null)
			return;
		Context.getConceptService().purgeConcept(concept);
	}
	
	/**
	 * This does not include retired concepts
	 * 
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doGetAll(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected NeedsPaging<Concept> doGetAll(RequestContext context) {
		List<Concept> allConcepts = Context.getConceptService().getAllConcepts(null, true, context.getIncludeAll());
		return new NeedsPaging<Concept>(allConcepts, context);
	}
	
	/**
	 * Concept searches support the following additional query parameters:
	 * <ul>
	 * <li>answerTo=(uuid): restricts results to concepts that are answers to the given concept uuid
	 * </li>
	 * <li>memberOf=(uuid): restricts to concepts that are set members of the given concept set's
	 * uuid</li>
	 * </ul>
	 * 
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doSearch(RequestContext)
	 */
	@Override
	protected PageableResult doSearch(RequestContext context) {
		ConceptService service = Context.getConceptService();
		Integer startIndex = null;
		Integer limit = null;
		boolean canPage = true;
		
		// Collect information for answerTo and memberOf query parameters
		String answerToUuid = context.getRequest().getParameter("answerTo");
		String memberOfUuid = context.getRequest().getParameter("memberOf");
		Concept answerTo = null;
		List<Concept> memberOfList = null;
		if (StringUtils.isNotBlank(answerToUuid)) {
			try {
				answerTo = (Concept) ConversionUtil.convert(answerToUuid, Concept.class);
			}
			catch (ConversionException ex) {
				log.error("Unexpected exception while retrieving answerTo Concept with UUID " + answerToUuid, ex);
			}
		}
		
		if (StringUtils.isNotBlank(memberOfUuid)) {
			Concept memberOf = service.getConceptByUuid(memberOfUuid);
			memberOfList = service.getConceptsByConceptSet(memberOf);
			canPage = false; // ConceptService does not support memberOf searches, so paging must be deferred.
		}
		
		// Only set startIndex and limit if we can return paged results
		if (canPage) {
			startIndex = context.getStartIndex();
			limit = context.getLimit();
		}
		
		List<ConceptSearchResult> searchResults;
		
		// get the user's locales...and then convert that from a set to a list
		List<Locale> locales = new ArrayList<Locale>(LocaleUtility.getLocalesInOrder());
		
		searchResults = service.getConcepts(context.getParameter("q"), locales, context.getIncludeAll(), null, null, null,
		    null, answerTo, startIndex, limit);
		
		// convert search results into list of concepts
		List<Concept> results = new ArrayList<Concept>(searchResults.size());
		for (ConceptSearchResult csr : searchResults) {
			// apply memberOf filter
			if (memberOfList == null || memberOfList.contains(csr.getConcept()))
				results.add(csr.getConcept());
		}
		
		PageableResult result = null;
		if (canPage) {
			Integer count = service.getCountOfConcepts(context.getParameter("q"), locales, false,
			    Collections.<ConceptClass> emptyList(), Collections.<ConceptClass> emptyList(),
			    Collections.<ConceptDatatype> emptyList(), Collections.<ConceptDatatype> emptyList(), answerTo);
			boolean hasMore = count > startIndex + limit;
			result = new AlreadyPaged<Concept>(context, results, hasMore, Long.valueOf(count));
		} else {
			result = new NeedsPaging<Concept>(results, context);
		}
		
		return result;
	}
	
	@Override
	protected void delete(Concept c, String reason, RequestContext context) throws ResponseException {
		if (c.isRetired()) {
			// since DELETE should be idempotent, we return success here
			return;
		}
		Context.getConceptService().retireConcept(c, reason);
	}
	
	/**
	 * @param instance
	 * @return the list of Concepts or Drugs
	 */
	@PropertyGetter("answers")
	public static Object getAnswers(Concept instance) {
		List<ConceptAnswer> conceptAnswers = new ArrayList<ConceptAnswer>();
		conceptAnswers.addAll(instance.getAnswers(false));
		Collections.sort(conceptAnswers);
		
		List<Object> answers = new ArrayList<Object>();
		for (ConceptAnswer conceptAnswer : conceptAnswers) {
			if (conceptAnswer.getAnswerDrug() != null) {
				answers.add(conceptAnswer.getAnswerDrug());
			} else if (conceptAnswer.getAnswerConcept() != null) {
				answers.add(conceptAnswer.getAnswerConcept());
			}
		}
		
		return answers;
	}
	
	/**
	 * @param instance
	 * @param answerUuids the list of Concepts or Drugs
	 * @throws ResourceDoesNotSupportOperationException
	 */
	@PropertySetter("answers")
	public static void setAnswers(Concept instance, List<String> answerUuids /*Concept or Drug uuid*/)
	        throws ResourceDoesNotSupportOperationException {
		
		// remove answers that are not in the new list
		Iterator<ConceptAnswer> iterator = instance.getAnswers(false).iterator();
		while (iterator.hasNext()) {
			ConceptAnswer answer = iterator.next();
			String conceptUuid = answer.getConcept().getUuid();
			String drugUuid = (answer.getAnswerDrug() != null) ? answer.getAnswerDrug().getUuid() : null;
			if (answerUuids.contains(conceptUuid)) {
				answerUuids.remove(conceptUuid); // remove from passed in list
			} else if (answerUuids.contains(drugUuid)) {
				answerUuids.remove(drugUuid); // remove from passed in list
			} else {
				instance.removeAnswer(answer); // remove from concept question object
			}
		}
		
		List<Object> answerObjects = new ArrayList<Object>(answerUuids.size());
		for (String uuid : answerUuids) {
			Concept c = Context.getConceptService().getConceptByUuid(uuid);
			if (c != null) {
				answerObjects.add(c);
			} else {
				// it is a drug
				Drug drug = Context.getConceptService().getDrugByUuid(uuid);
				if (drug != null)
					answerObjects.add(drug);
				else
					throw new ResourceDoesNotSupportOperationException("There is no concept or drug with given uuid: "
					        + uuid);
			}
		}
		
		// add in new answers
		for (Object obj : answerObjects) {
			ConceptAnswer answerToAdd = null;
			if (obj.getClass().isAssignableFrom(Concept.class))
				answerToAdd = new ConceptAnswer((Concept) obj);
			else
				answerToAdd = new ConceptAnswer(((Drug) obj).getConcept(), (Drug) obj);
			
			answerToAdd.setCreator(Context.getAuthenticatedUser());
			answerToAdd.setDateCreated(new Date());
			instance.addAnswer(answerToAdd);
		}
	}
	
	/**
	 * @param instance
	 * @param setMembers the list of Concepts
	 */
	@PropertySetter("setMembers")
	public static void setSetMembers(Concept instance, List<Concept> setMembers) {
		instance.getConceptSets().clear();
		
		if (setMembers == null || setMembers.isEmpty()) {
			instance.setSet(false);
		} else {
			instance.setSet(true);
			
			for (Concept setMember : setMembers) {
				instance.addSetMember(setMember);
			}
		}
	}
}
