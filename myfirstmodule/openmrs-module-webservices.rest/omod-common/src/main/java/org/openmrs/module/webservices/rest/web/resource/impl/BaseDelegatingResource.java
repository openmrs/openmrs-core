/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.resource.impl;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.proxy.HibernateProxy;
import org.openmrs.OpenmrsObject;
import org.openmrs.api.context.Context;
import org.openmrs.module.ModuleUtil;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.util.ReflectionUtil;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.RepHandler;
import org.openmrs.module.webservices.rest.web.annotation.SubClassHandler;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.module.webservices.rest.web.representation.CustomRepresentation;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.NamedRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.Converter;
import org.openmrs.module.webservices.rest.web.resource.api.Resource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription.Property;
import org.openmrs.module.webservices.rest.web.response.ConversionException;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.StringProperty;

/**
 * A base implementation of a resource or sub-resource that delegates operations to a wrapped
 * object. Implementations generally should extend either {@link DelegatingCrudResource} or
 * {@link DelegatingSubResource} rather than this class directly.
 * 
 * @param <T> the class we're delegating to
 */
public abstract class BaseDelegatingResource<T> extends BaseDelegatingConverter<T> implements Converter<T>, Resource, DelegatingResourceHandler<T> {
	
	private final Log log = LogFactory.getLog(getClass());
	
	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl model = new ModelImpl();
		if (rep instanceof DefaultRepresentation) {
			model
			        .property("links", new ArrayProperty()
			                .items(new ObjectProperty()
			                        .property("rel", new StringProperty().example("self|full"))
			                        .property("uri", new StringProperty(StringProperty.Format.URI))));
			
		} else if (rep instanceof FullRepresentation) {
			model
			        .property("auditInfo", new StringProperty())
			        .property("links", new ArrayProperty()
			                .items(new ObjectProperty()
			                        .property("rel", new StringProperty()).example("self")
			                        .property("uri", new StringProperty(StringProperty.Format.URI))));
			
		} else if (rep instanceof RefRepresentation) {
			model
			        .property("links", new ArrayProperty()
			                .items(new ObjectProperty()
			                        .property("rel", new StringProperty().example("self"))
			                        .property("uri", new StringProperty(StringProperty.Format.URI))));
		}
		return model;
	}
	
	protected Set<String> propertiesIgnoredWhenUpdating = new HashSet<String>();
	
	/**
	 * Properties that should silently be ignored if you try to get them. Implementations should
	 * generally configure this property with a list of properties that were added to their
	 * underlying domain object after the minimum OpenMRS version required by this module. For
	 * example PatientIdentifierTypeResource will allow "locationBehavior" to be missing, since it
	 * wasn't added to PatientIdentifierType until OpenMRS 1.9. delegate class
	 */
	protected Set<String> allowedMissingProperties = new HashSet<String>();
	
	/**
	 * If this resource represents a class hierarchy (rather than a single class), this will hold
	 * handlers for each subclass
	 */
	protected volatile List<DelegatingSubclassHandler<T, ? extends T>> subclassHandlers;
	
	/**
	 * Default constructor will set propertiesIgnoredWhenUpdating to include "display", "links", and
	 * "resourceVersion"
	 */
	protected BaseDelegatingResource() {
		propertiesIgnoredWhenUpdating.add("display");
		propertiesIgnoredWhenUpdating.add("links");
		propertiesIgnoredWhenUpdating.add("auditInfo");
		propertiesIgnoredWhenUpdating.add(RestConstants.PROPERTY_FOR_RESOURCE_VERSION);
	}
	
	/**
	 * All our resources support letting modules register subclass handlers. If any are registered,
	 * then the resource represents a class hierarchy, e.g. requiring a "type" parameter when
	 * creating a new instance.
	 * 
	 * @return whether there are any subclass handlers registered with this resource
	 */
	public boolean hasTypesDefined() {
		return subclassHandlers != null && subclassHandlers.size() > 0;
	}
	
	/**
	 * This will be automatically called with the first call to {@link #getSubclassHandler(Class)}
	 * or {@link #getSubclassHandler(String)}. It finds all subclass handlers intended for this
	 * resource, and registers them.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void init() {
		List<DelegatingSubclassHandler<T, ? extends T>> tmpSubclassHandlers = new ArrayList<DelegatingSubclassHandler<T, ? extends T>>();
		
		List<DelegatingSubclassHandler> handlers = Context.getRegisteredComponents(DelegatingSubclassHandler.class);
		for (DelegatingSubclassHandler handler : handlers) {
			
			Class<? extends DelegatingSubclassHandler> handlerClass = handler.getClass();
			Class forDelegateClass = ReflectionUtil.getParameterizedTypeFromInterface(handlerClass,
			    DelegatingSubclassHandler.class, 0);
			if (forDelegateClass == null) {
				throw new IllegalStateException(
				        "Could not determine type information. Make sure that "
				                + handlerClass.getName()
				                + " explicitly says implements DelegatingSubclassHandler<...>. It is not sufficient to just extend a base class that implements this.");
			}
			Resource resourceForHandler = Context.getService(RestService.class)
			        .getResourceBySupportedClass(forDelegateClass);
			if (getClass().equals(resourceForHandler.getClass())) {
				SubClassHandler annotation = handlerClass.getAnnotation(SubClassHandler.class);
				if (annotation != null) {
					String[] supportedOpenmrsVersions = annotation.supportedOpenmrsVersions();
					for (String version : supportedOpenmrsVersions) {
						if (ModuleUtil.matchRequiredVersions(OpenmrsConstants.OPENMRS_VERSION_SHORT, version)) {
							tmpSubclassHandlers.add(handler);
							break;
						}
					}
				} else {
					log.warn("SubclassHandler "
					        + handlerClass.getName()
					        + " does not have a @SubClassHandler annotation. This can cause conflicts in resolving handlers for your subclass.");
				}
			}
		}
		
		subclassHandlers = tmpSubclassHandlers;
	}
	
	/**
	 * Registers the given subclass handler.
	 * 
	 * @param handler
	 */
	public void registerSubclassHandler(DelegatingSubclassHandler<T, ? extends T> handler) {
		if (subclassHandlers == null) {
			init();
		}
		for (DelegatingSubclassHandler<T, ? extends T> current : subclassHandlers) {
			if (current.getClass().equals(handler.getClass())) {
				log.info("Tried to register a subclass handler, but the class is already registered: " + handler.getClass());
				return;
			}
		}
		subclassHandlers.add(handler);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler#getResourceVersion()
	 */
	@Override
	public String getResourceVersion() {
		return RestConstants.PROPERTY_FOR_RESOURCE_VERSION_DEFAULT_VALUE;
	}
	
	/**
	 * @return the value of the {@link org.openmrs.module.webservices.rest.web.annotation.Resource}
	 *         annotation on the concrete subclass
	 */
	protected String getResourceName() {
		org.openmrs.module.webservices.rest.web.annotation.Resource ann = getClass().getAnnotation(
		    org.openmrs.module.webservices.rest.web.annotation.Resource.class);
		if (ann == null)
			throw new RuntimeException("There is no " + Resource.class + " annotation on " + getClass());
		if (StringUtils.isEmpty(ann.name()))
			throw new RuntimeException(Resource.class.getSimpleName() + " annotation on " + getClass()
			        + " must specify a name");
		return ann.name();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.api.Converter#newInstance(java.lang.String)
	 */
	@Override
	public T newInstance(String type) {
		if (hasTypesDefined()) {
			if (type == null)
				throw new IllegalArgumentException(getClass().getSimpleName() + " requires a '"
				        + RestConstants.PROPERTY_FOR_TYPE + "' property to create a new object");
			DelegatingResourceHandler<? extends T> handler = getResourceHandler(type);
			return handler.newDelegate();
		} else {
			return newDelegate();
		}
	}
	
	/**
	 * Gets the delegate object with the given unique id. Implementations may decide whether
	 * "unique id" means a uuid, or if they also want to retrieve delegates based on a unique
	 * human-readable property.
	 * 
	 * @param uniqueId
	 * @return the delegate for the given uniqueId
	 */
	@Override
	public abstract T getByUniqueId(String uniqueId);
	
	/**
	 * Void or retire delegate, whichever action is appropriate for the resource type. Subclasses
	 * need to override this method, which is called internally by
	 * {@link #delete(String, String, RequestContext)}.
	 * 
	 * @param delegate
	 * @param reason
	 * @param context
	 * @throws ResponseException
	 */
	protected abstract void delete(T delegate, String reason, RequestContext context) throws ResponseException;
	
	/**
	 * Unvoid or unretire delegate, whichever action is appropriate for the resource type.
	 * Subclasses need to override this method, which is called internally by
	 * {@link #undelete(String, RequestContext)}.
	 * 
	 * @param delegate
	 * @param context
	 * @throws ResponseException
	 * @return Object
	 */
	protected T undelete(T delegate, RequestContext context) throws ResponseException {
		//Default implementation of this method if not overriden by sub-class is to raise an 
		//exception stating "undelete action not yet supported for this resource"
		throw new ResourceDoesNotSupportOperationException("undelete action not yet supported for this resource");
	}
	
	/**
	 * Purge delegate from persistent storage. Subclasses need to override this method, which is
	 * called internally by {@link #purge(String, RequestContext)}.
	 * 
	 * @param delegate
	 * @param context
	 * @throws ResponseException
	 */
	@Override
	public abstract void purge(T delegate, RequestContext context) throws ResponseException;
	
	/**
	 * Gets a description of resource's properties which can be set on creation.
	 * 
	 * @return the description
	 * @throws ResponseException
	 */
	@Override
	public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		return null;
	}
	
	/**
	 * Gets a description of resource's properties which can be edited.
	 * <p/>
	 * By default delegates to {@link #getCreatableProperties()} and removes sub-resources returned
	 * by {@link #getPropertiesToExposeAsSubResources()}.
	 * 
	 * @return the description
	 * @throws ResponseException
	 */
	@Override
	public DelegatingResourceDescription getUpdatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = getCreatableProperties();
		for (String property : getPropertiesToExposeAsSubResources()) {
			description.getProperties().remove(property);
		}
		return description;
	}
	
	@Override
	public Model getUPDATEModel(Representation rep) {
		ModelImpl model = (ModelImpl) getCREATEModel(rep);
		for (String property : getPropertiesToExposeAsSubResources()) {
			model.getProperties().remove(property);
		}
		return model;
	}
	
	/**
	 * Implementations should override this method if they support sub-resources
	 * 
	 * @return a list of properties available as sub-resources or an empty list
	 */
	public List<String> getPropertiesToExposeAsSubResources() {
		return Collections.emptyList();
	}
	
	/**
	 * Implementations should override this method if T is not uniquely identified by a "uuid"
	 * property.
	 * 
	 * @param delegate
	 * @return the uuid property of delegate
	 */
	protected String getUniqueId(T delegate) {
		try {
			return (String) getProperty(delegate, "uuid");
		}
		catch (Exception ex) {
			throw new RuntimeException("Cannot find String uuid property on " + delegate.getClass(), null);
		}
	}
	
	/**
	 * Creates an object of the given representation, pulling values from fields and methods as
	 * specified by a subclass
	 * 
	 * @param representation
	 * @return
	 * <strong>Should</strong> return valid RefRepresentation
	 * <strong>Should</strong> return valid DefaultRepresentation
	 * <strong>Should</strong> return valid FullRepresentation
	 */
	@Override
	public SimpleObject asRepresentation(T delegate, Representation representation) throws ConversionException {
		if (delegate == null)
			throw new NullPointerException();
		
		DelegatingResourceHandler<? extends T> handler = getResourceHandler(delegate);
		
		// first call getRepresentationDescription()
		DelegatingResourceDescription repDescription = handler.getRepresentationDescription(representation);
		if (repDescription != null) {
			SimpleObject simple = convertDelegateToRepresentation(delegate, repDescription);
			
			maybeDecorateWithType(simple, delegate);
			decorateWithResourceVersion(simple, representation);
			
			return simple;
		}
		
		// otherwise look for a method annotated to handle this representation
		Method meth = findAnnotatedMethodForRepresentation(handler.getClass(), representation);
		if (meth != null) {
			try {
				// TODO verify that the method takes 1 or 2 parameters
				SimpleObject simple;
				if (meth.getParameterTypes().length == 1)
					simple = (SimpleObject) meth.invoke(handler, delegate);
				else
					simple = (SimpleObject) meth.invoke(handler, delegate, representation);
				
				maybeDecorateWithType(simple, delegate);
				decorateWithResourceVersion(simple, representation);
				
				return simple;
			}
			catch (Exception ex) {
				throw new ConversionException(null, ex);
			}
		}
		
		// finally if it is a custom representation and not supported by any other handler
		if (representation instanceof CustomRepresentation) {
			repDescription = getCustomRepresentationDescription((CustomRepresentation) representation);
			if (repDescription != null) {
				return convertDelegateToRepresentation(delegate, repDescription);
			}
		}
		
		throw new ConversionException("Don't know how to get " + getClass().getSimpleName() + "(" + delegate.getClass()
		        + ") as " + representation.getRepresentation(), null);
	}
	
	/**
	 * <strong>Should</strong> return delegating resource description
	 */
	private DelegatingResourceDescription getCustomRepresentationDescription(CustomRepresentation representation) {
		DelegatingResourceDescription desc = new DelegatingResourceDescription();
		
		String def = representation.getRepresentation();
		def = def.startsWith("(") ? def.substring(1) : def;
		def = def.endsWith(")") ? def.substring(0, def.length() - 1) : def;
		String[] fragments = def.split(",");
		for (int i = 0; i < fragments.length; i++) {
			String[] field = fragments[i].split(":"); //split into field and representation
			if (field.length == 1) {
				if (!field[0].equals("links"))
					desc.addProperty(field[0]);
				if (field[0].equals("links")) {
					desc.addSelfLink();
					desc.addLink("default", ".?v=" + RestConstants.REPRESENTATION_DEFAULT);
				}
			} else {
				String property = field[0];
				String rep = field[1];
				
				// if custom representation
				if (rep.startsWith("(")) {
					StringBuilder customRep = new StringBuilder();
					customRep.append(rep);
					if (!rep.endsWith(")")) {
						for (int j = 2; j < field.length; j++) {
							customRep.append(":").append(field[j]);
						}
						int open = 1;
						for (i = i + 1; i < fragments.length; i++) {
							for (char fragment : fragments[i].toCharArray()) {
								if (fragment == '(') {
									open++;
								} else if (fragment == ')') {
									open--;
								}
							}
							
							customRep.append(",");
							customRep.append(fragments[i]);
							
							if (open == 0) {
								break;
							}
						}
					}
					desc.addProperty(property, new CustomRepresentation(customRep.toString()));
				} else {
					rep = rep.toUpperCase(); //normalize
					if (rep.equals("REF")) {
						desc.addProperty(property, Representation.REF);
					} else if (rep.equals("FULL")) {
						desc.addProperty(property, Representation.FULL);
					} else if (rep.equals("DEFAULT")) {
						desc.addProperty(property, Representation.DEFAULT);
					}
				}
			}
		}
		
		return desc;
	}
	
	/**
	 * Sets resourceVersion to {@link #getResourceVersion()} for representations other than REF.
	 * 
	 * @param simple the simplified representation which will be decorated with the resource version
	 * @param representation the type of representation
	 */
	private void decorateWithResourceVersion(SimpleObject simple, Representation representation) {
		if (!(representation instanceof RefRepresentation)) {
			simple.put(RestConstants.PROPERTY_FOR_RESOURCE_VERSION, getResourceVersion());
		}
	}
	
	/**
	 * If this resource supports subclasses, then we add a type property to the input, and return it
	 * 
	 * @param simple simplified representation which will be decorated with the user-friendly type
	 *            name
	 * @param delegate the object that simple represents
	 */
	private void maybeDecorateWithType(SimpleObject simple, T delegate) {
		if (hasTypesDefined())
			simple.add(RestConstants.PROPERTY_FOR_TYPE, getTypeName(delegate));
	}
	
	/**
	 * If this resources supports subclasses, this method gets the user-friendly type name for the
	 * given subclass
	 * 
	 * @param subclass
	 * @return
	 */
	protected String getTypeName(Class<? extends T> subclass) {
		if (hasTypesDefined()) {
			DelegatingSubclassHandler<T, ? extends T> handler = getSubclassHandler(subclass);
			if (handler != null)
				return handler.getTypeName();
			if (newDelegate().getClass().equals(subclass)) {
				String resourceName = getResourceName();
				int lastSlash = resourceName.lastIndexOf("/");
				resourceName = resourceName.substring(lastSlash + 1);
				return resourceName;
			}
		}
		return null;
	}
	
	/**
	 * @see #getTypeName(Class)
	 */
	protected String getTypeName(T delegate) {
		return getTypeName((Class<? extends T>) delegate.getClass());
	}
	
	/**
	 * @param type user-friendly type name
	 * @return the actual java class for this type
	 */
	protected Class<? extends T> getActualSubclass(String type) {
		DelegatingSubclassHandler<T, ? extends T> handler = getSubclassHandler(type);
		if (handler != null)
			return handler.getSubclassHandled();
		// otherwise we need to return our own declared class
		return ReflectionUtil.getParameterizedTypeFromInterface(getClass(), DelegatingResourceHandler.class, 0);
	}
	
	/**
	 * @param type user-friendly type name
	 * @return a subclass handler if any is suitable for type, or this resource itself if it is
	 *         suitable
	 */
	protected DelegatingResourceHandler<? extends T> getResourceHandler(String type) {
		if (type == null || !hasTypesDefined())
			return this;
		DelegatingSubclassHandler<T, ? extends T> handler = getSubclassHandler(type);
		if (handler != null)
			return handler;
		if (getResourceName().endsWith(type))
			return this;
		throw new IllegalArgumentException("type=" + type + " is not handled by this resource (" + getClass()
		        + ") or any subclass");
	}
	
	/**
	 * Delegates to @see {@link #getResourceHandler(Class)}
	 */
	@SuppressWarnings("unchecked")
	protected DelegatingResourceHandler<? extends T> getResourceHandler(T delegate) {
		if (!hasTypesDefined())
			return this;
		if (delegate == null)
			return null;
		return getResourceHandler((Class<? extends T>) delegate.getClass());
	}
	
	/**
	 * @param clazz
	 * @return a subclass handler if any is suitable for the given class, or this resource itself if
	 *         no subclass handler works
	 */
	protected DelegatingResourceHandler<? extends T> getResourceHandler(Class<? extends T> clazz) {
		if (!hasTypesDefined())
			return this;
		DelegatingResourceHandler<? extends T> handler = getSubclassHandler(clazz);
		if (handler != null)
			return handler;
		return this;
	}
	
	/**
	 * @param subclass
	 * @return the handler most appropriate for the given subclass, or null if none is suitable
	 */
	protected DelegatingSubclassHandler<T, ? extends T> getSubclassHandler(Class<? extends T> subclass) {
		if (subclassHandlers == null) {
			init();
		}
		
		if (!hasTypesDefined())
			return null;
		// look for an exact match
		for (DelegatingSubclassHandler<T, ? extends T> handler : subclassHandlers) {
			Class<? extends T> subclassHandled = handler.getSubclassHandled();
			if (subclass.equals(subclassHandled))
				return handler;
		}
		
		// TODO should we recurse to subclass's superclass, e.g. so DrugOrderHandler can handle HivDrugOrder if no handler is defined?
		
		// didn't find anything suitable
		return null;
	}
	
	/**
	 * @param type the user-friendly name of a registered subclass handler
	 * @return the handler for the given user-friendly type name
	 */
	protected DelegatingSubclassHandler<T, ? extends T> getSubclassHandler(String type) {
		if (hasTypesDefined()) {
			if (subclassHandlers == null) {
				init();
			}
			for (DelegatingSubclassHandler<T, ? extends T> handler : subclassHandlers) {
				if (type.equals(handler.getTypeName()))
					return handler;
			}
		}
		return null;
	}
	
	/**
	 * @param delegate
	 * @param propertyMap
	 * @param description
	 * @param mustIncludeRequiredProperties
	 * @throws ResponseException
	 * <strong>Should</strong> allow setting a null value
	 */
	public void setConvertedProperties(T delegate, Map<String, Object> propertyMap,
	        DelegatingResourceDescription description, boolean mustIncludeRequiredProperties) throws ConversionException {
		Map<String, Property> allowedProperties = new LinkedHashMap<String, Property>(description.getProperties());
		
		Map<String, Object> propertiesToSet = new HashMap<String, Object>(propertyMap);
		propertiesToSet.keySet().removeAll(propertiesIgnoredWhenUpdating);
		
		// Apply properties in the order specified in the resource description (necessary e.g. so the obs resource
		// can apply "concept" before "value"); we have already excluded unchanged and ignored properties.
		// Because some resources (e.g. any AttributeResource) require some properties to be set before others can
		// be fetched, we apply each property in its iteration, rather than testing everything first and applying later.
		for (String property : allowedProperties.keySet()) {
			if (!propertiesToSet.containsKey(property)) {
				continue;
			}
			if (propertiesToSet.containsKey(property)) {
				// Ignore any properties that were not actually changed, also covering the case where you post back an
				// incomplete rep of a complex property
				Object oldValue = getProperty(delegate, property);
				Object newValue = propertiesToSet.get(property);
				if (unchangedValue(oldValue, newValue)) {
					propertiesToSet.remove(property);
					continue;
				}
				
				setProperty(delegate, property, propertiesToSet.get(property));
			}
		}
		
		// If any non-settable properties remain after the above logic, fail
		Collection<String> notAllowedProperties = CollectionUtils.subtract(propertiesToSet.keySet(),
		    allowedProperties.keySet());
		// Do allow posting back an unchanged value to an unchangeable property
		for (Iterator<String> iterator = notAllowedProperties.iterator(); iterator.hasNext();) {
			String property = iterator.next();
			Object oldValue = getProperty(delegate, property);
			Object newValue = propertiesToSet.get(property);
			if (unchangedValue(oldValue, newValue)) {
				iterator.remove();
			}
		}
		if (!notAllowedProperties.isEmpty()) {
			throw new ConversionException("Some properties are not allowed to be set: "
			        + StringUtils.join(notAllowedProperties, ", "));
		}
		
		if (mustIncludeRequiredProperties) {
			Set<String> missingProperties = new HashSet<String>();
			for (Entry<String, Property> prop : allowedProperties.entrySet()) {
				if (prop.getValue().isRequired() && !propertyMap.containsKey(prop.getKey())) {
					missingProperties.add(prop.getKey());
				}
			}
			if (!missingProperties.isEmpty()) {
				throw new ConversionException("Some required properties are missing: "
				        + StringUtils.join(missingProperties, ", "));
			}
		}
	}
	
	private boolean unchangedValue(Object oldValue, Object newValue) {
		if (newValue instanceof Map && oldValue != null && !(oldValue instanceof Map)) {
			newValue = ConversionUtil.convert(newValue, oldValue.getClass());
			if (oldValue instanceof OpenmrsObject) {
				return ((OpenmrsObject) oldValue).getUuid().equals(((OpenmrsObject) newValue).getUuid());
			}
		}
		return OpenmrsUtil.nullSafeEquals(oldValue, newValue);
	}
	
	/**
	 * Finds a method on clazz or a superclass that is annotated with {@link RepHandler} and is
	 * suitable for rep
	 * 
	 * @param clazz
	 * @param rep
	 * @return
	 */
	private Method findAnnotatedMethodForRepresentation(Class<?> clazz, Representation rep) {
		for (Method method : clazz.getMethods()) {
			RepHandler ann = method.getAnnotation(RepHandler.class);
			if (ann != null && ann.value().isAssignableFrom(rep.getClass())) {
				if (!(rep instanceof NamedRepresentation) || ann.name().equals(rep.getRepresentation())) {
					return method;
				}
			}
		}
		return null;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.api.Converter#getProperty(java.lang.Object,
	 *      java.lang.String)
	 */
	@Override
	public Object getProperty(T instance, String propertyName) throws ConversionException {
		try {
			DelegatingResourceHandler<? extends T> handler = getResourceHandler(instance);
			
			// try to find a @PropertyGetter-annotated method
			Method annotatedGetter = ReflectionUtil.findPropertyGetterMethod(handler, propertyName);
			if (annotatedGetter != null) {
				return annotatedGetter.invoke(handler, instance);
			}
			
			return PropertyUtils.getProperty(instance, propertyName);
		}
		catch (Exception ex) {
			// some properties are allowed to be missing, since they may have been added in later OpenMRS versions
			if (allowedMissingProperties.contains(propertyName))
				return null;
			throw new ConversionException(propertyName + " on " + instance.getClass(), ex);
		}
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.api.Converter#setProperty(java.lang.Object,
	 *      java.lang.String, java.lang.Object)
	 */
	@Override
	public void setProperty(Object instance, String propertyName, Object value) throws ConversionException {
		if (propertiesIgnoredWhenUpdating.contains(propertyName)) {
			return;
		}
		try {
			DelegatingResourceHandler<? extends T> handler;
			
			try {
				handler = getResourceHandler((T) instance);
			}
			catch (Exception e) {
				// this try/catch isn't really needed because of java erasure behaviour at run time.
				// but I'm putting in here just in case
				handler = this;
			}
			
			// try to find a @PropertySetter-annotated method
			Method annotatedSetter = ReflectionUtil.findPropertySetterMethod(handler, propertyName);
			if (annotatedSetter != null) {
				Type expectedType = annotatedSetter.getGenericParameterTypes()[1];
				value = ConversionUtil.convert(value, expectedType);
				annotatedSetter.invoke(handler, instance, value);
				return;
			}
			
			// we need the generic type of this property, not just the class
			Method setter = PropertyUtils.getPropertyDescriptor(instance, propertyName).getWriteMethod();
			
			// Convert the value to the specified type
			value = ConversionUtil.convert(value, setter.getGenericParameterTypes()[0], instance);
			
			setPropertyWhichMayBeAHibernateCollection(instance, propertyName, value);
		}
		catch (Exception ex) {
			throw new ConversionException(propertyName + " on " + instance.getClass(), ex);
		}
	}
	
	/**
	 * Removes any elements from the passed-in collection that aren't of the given type. This is a
	 * convenience method for subclass-aware resources that want to limit query results to a given
	 * type.
	 * 
	 * @param collection
	 * @param type a user-friendly type name
	 */
	protected void filterByType(Collection<T> collection, String type) {
		for (Iterator<T> i = collection.iterator(); i.hasNext();) {
			T instance = i.next();
			if (!getTypeName(instance).equals(type))
				i.remove();
		}
	}
	
	/**
	 * Convenience method that looks for a specific method on the subclass handler for the given
	 * type
	 * 
	 * @param type user-friendly type name
	 * @param methodName
	 * @param argumentTypes
	 * @return the indicated method if it exists, null otherwise
	 */
	protected Method findSubclassHandlerMethod(String type, String methodName, Class<?>... argumentTypes) {
		DelegatingSubclassHandler<T, ? extends T> handler = getSubclassHandler(type);
		if (handler == null)
			return null;
		try {
			return handler.getClass().getMethod(methodName, argumentTypes);
		}
		catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Convenience method that finds a specific method on the subclass handler for the given type,
	 * and invokes it
	 * 
	 * @param type user-friendly type name
	 * @param methodName
	 * @param arguments
	 * @return the result of invoking the indicated method, or null if the method wasn't found
	 */
	protected Object findAndInvokeSubclassHandlerMethod(String type, String methodName, Object... arguments) {
		Class<?>[] argumentTypes = new Class<?>[arguments.length];
		for (int i = 0; i < arguments.length; ++i) {
			Class<?> t = arguments[i].getClass();
			if (arguments[i] instanceof HibernateProxy) {
				t = ((HibernateProxy) arguments[i]).getHibernateLazyInitializer().getPersistentClass();
			}
			argumentTypes[i] = t;
		}
		Method method = findSubclassHandlerMethod(type, methodName, argumentTypes);
		if (method == null)
			return null;
		try {
			DelegatingSubclassHandler<T, ? extends T> handler = getSubclassHandler(type);
			return method.invoke(handler, arguments);
		}
		catch (RuntimeException ex) {
			throw ex;
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	/**
	 * @param delegate
	 * @return the URI for the given delegate object
	 */
	@SuppressWarnings("unchecked")
	@Override
	public String getUri(Object delegate) {
		if (delegate == null)
			return "";
		
		org.openmrs.module.webservices.rest.web.annotation.Resource res = getClass().getAnnotation(
		    org.openmrs.module.webservices.rest.web.annotation.Resource.class);
		if (res != null) {
			return RestConstants.URI_PREFIX + res.name() + "/" + getUniqueId((T) delegate);
		}
		throw new RuntimeException(getClass() + " needs a @Resource or @SubResource annotation");
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.util.ReflectionUtil#findMethod(Class,String)
	 * @deprecated It is always best to annotate the method with @PropertyGetter instead of finding
	 *             it this way, because properties defined this way cannot be included in custom
	 *             representations
	 */
	@Deprecated
	protected Method findMethod(String name) {
		return ReflectionUtil.findMethod(getClass(), name);
	}
	
	/**
	 * Gets the audit information of a resource.
	 * 
	 * @param resource the resource.
	 * @return a {@link SimpleObject} with the audit information.
	 */
	@PropertyGetter("auditInfo")
	public SimpleObject getAuditInfo(Object resource) {
		return ConversionUtil.getAuditInfo(resource);
	}
	
	@Override
	public T newDelegate(SimpleObject object) {
		return newDelegate();
	}
	
}
