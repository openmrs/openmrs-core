/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.aop;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.openmrs.OpenmrsObject;
import org.openmrs.Retireable;
import org.openmrs.User;
import org.openmrs.Voidable;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.api.handler.ConceptNameSaveHandler;
import org.openmrs.api.handler.RequiredDataHandler;
import org.openmrs.api.handler.RetireHandler;
import org.openmrs.api.handler.SaveHandler;
import org.openmrs.api.handler.UnretireHandler;
import org.openmrs.api.handler.UnvoidHandler;
import org.openmrs.api.handler.VoidHandler;
import org.openmrs.util.HandlerUtil;
import org.openmrs.util.Reflect;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.util.StringUtils;

/**
 * This class provides the AOP around each save, (un)void, and (un)retire method in the service
 * layer so that the required data (like creator, dateChanged, dateVoided, etc) can be set
 * automatically and the developer doesn't have to worry about doing it explicitly in the service
 * impl method. <br/>
 * <br/>
 * See /metadata/api/spring/applicationContext-service.xml for the mapping of this bean. <br/>
 * <br/>
 * For an Openmrs Service to use this AOP advice class and take advantage of its automatic variable
 * setting, it must have "&lt;ref local="requiredDataInterceptor"/>" in its "preInterceptors".<br/>
 * <br/>
 * By default, this should take care of any child collections on the object being acted on. Any
 * child collection of {@link OpenmrsObject}s will get "handled" (i.e., void data set up, save data
 * set up, or retire data set up, etc) by the same handler type that the parent object was handled
 * with.<br/>
 * <br/>
 * To add a new action to happen for a save* method, create a new class that extends
 * {@link RequiredDataHandler}. Add any <b>unique</b> code that needs to be done automatically
 * before the save. See {@link ConceptNameSaveHandler} as an example. (The code should be
 * <b>unique</b> because all other {@link SaveHandler}s will still be called <i>in addition to</i>
 * your new handler.) Be sure to add the {@link org.openmrs.annotation.Handler} annotation (like
 * "@Handler(supports=YourPojoThatHasUniqueSaveNeeds.class)") to your class so that it is picked up
 * by Spring automatically.<br/>
 * <br/>
 * To add a new action for a void* or retire* method, extend the {@link VoidHandler}/
 * {@link RetireHandler} class and override the handle method. Do not call super, because that code
 * would then be run twice because both handlers are registered. Be sure to add the
 * {@link org.openmrs.annotation.Handler} annotation (like
 * "@Handler(supports=YourPojoThatHasUniqueSaveNeeds.class)") to your class so that it is picked up
 * by Spring automatically.
 * 
 * @see RequiredDataHandler
 * @see SaveHandler
 * @see VoidHandler
 * @since 1.5
 */
public class RequiredDataAdvice implements MethodBeforeAdvice {
	
	// TODO put this somewhere and do it right and add class name, etc
	// TODO do this with an annotation on the field? or on the method?
	protected static List<String> fieldAccess = new ArrayList<String>();
	
	//private static Log log = LogFactory.getLog(RequiredDataAdvice.class);
	
	static {
		fieldAccess.add("Concept.answers");
		fieldAccess.add("Concept.names");
		fieldAccess.add("Encounter.obs");
		fieldAccess.add("Program.allWorkflows");
		
	}
	
	/**
	 * @see org.springframework.aop.MethodBeforeAdvice#before(java.lang.reflect.Method,
	 *      java.lang.Object[], java.lang.Object)
	 * @should not fail on update method with no arguments
	 */
	@SuppressWarnings("unchecked")
	public void before(Method method, Object[] args, Object target) throws Throwable {
		String methodName = method.getName();
		
		// the "create" is there to cover old deprecated methods since AOP doesn't occur
		// on method calls within a class, only on calls to methods from external classes to methods
		// "update" is not an option here because there are multiple methods that start with "update" but is
		// not updating the primary argument. eg: ConceptService.updateConceptWord(Concept)
		if (methodName.startsWith("save") || methodName.startsWith("create")) {
			// skip out early if there are no arguments
			if (args == null || args.length == 0)
				return;
			
			Object mainArgument = args[0];
			
			// fail early on a null parameter
			if (mainArgument == null)
				return;
			
			// if a second argument exists, pass that to the save handler as well
			// (with current code, it means we're either in an obs save or a user save)
			String other = null;
			if (args.length > 1) {
				other = (String) args[1];
			}
			
			// if the first argument is an OpenmrsObject, handle it now
			Reflect reflect = new Reflect(OpenmrsObject.class);
			
			if (reflect.isSuperClass(mainArgument)) {
				recursivelyHandle(SaveHandler.class, (OpenmrsObject) mainArgument, other);
			}
			// if the first argument is a list of openmrs objects, handle them all now
			else if (Reflect.isCollection(mainArgument) && isOpenmrsObjectCollection(mainArgument)) {
				Collection<OpenmrsObject> openmrsObjects = (Collection<OpenmrsObject>) mainArgument;
				
				for (OpenmrsObject object : openmrsObjects) {
					recursivelyHandle(SaveHandler.class, object, other);
				}
				
			}
			
		} else if (methodName.startsWith("void")) {
			Voidable voidable = (Voidable) args[0];
			String voidReason = (String) args[1];
			recursivelyHandle(VoidHandler.class, voidable, voidReason);
			
		} else if (methodName.startsWith("unvoid")) {
			Voidable voidable = (Voidable) args[0];
			Date originalDateVoided = voidable.getDateVoided();
			recursivelyHandle(UnvoidHandler.class, voidable, Context.getAuthenticatedUser(), originalDateVoided, null, null);
			
		} else if (methodName.startsWith("retire")) {
			Retireable retirable = (Retireable) args[0];
			String retireReason = (String) args[1];
			recursivelyHandle(RetireHandler.class, retirable, retireReason);
			
		} else if (methodName.startsWith("unretire")) {
			Retireable retirable = (Retireable) args[0];
			Date originalDateRetired = retirable.getDateRetired();
			recursivelyHandle(UnretireHandler.class, retirable, Context.getAuthenticatedUser(), originalDateRetired, null,
			    null);
			
		}
		
	}
	
	/**
	 * Convenience method for {@link #recursivelyHandle(Class, OpenmrsObject, User, Date, String)}.
	 * Calls that method with the current user and the current Date.
	 * 
	 * @param <H> the type of Handler to get (should extend {@link RequiredDataHandler})
	 * @param handlerType the type of Handler to get (should extend {@link RequiredDataHandler})
	 * @param openmrsObject the object that is being acted upon
	 * @param reason an optional second argument that was passed to the service method (usually a
	 *            void/retire reason)
	 * @see #recursivelyHandle(Class, OpenmrsObject, User, Date, String)
	 */
	@SuppressWarnings("unchecked")
	public static <H extends RequiredDataHandler> void recursivelyHandle(Class<H> handlerType, OpenmrsObject openmrsObject,
	                                                                     String reason) {
		recursivelyHandle(handlerType, openmrsObject, Context.getAuthenticatedUser(), new Date(), reason, null);
	}
	
	/**
	 * This loops over all declared collections on the given object and all declared collections on
	 * parent objects to use the given <code>handlerType</code>.
	 * 
	 * @param <H> the type of Handler to get (should extend {@link RequiredDataHandler})
	 * @param handlerType the type of Handler to get (should extend {@link RequiredDataHandler})
	 * @param openmrsObject the object that is being acted upon
	 * @param currentUser the current user to set recursively on the object
	 * @param currentDate the date to set recursively on the object
	 * @param other an optional second argument that was passed to the service method (usually a
	 *            void/retire reason)
	 * @param alreadyHandled an optional list of objects that have already been handled and should
	 *            not be processed again. this is intended to prevent infinite recursion when
	 *            handling collection properties.
	 * @see HandlerUtil#getHandlersForType(Class, Class)
	 */
	@SuppressWarnings("unchecked")
	public static <H extends RequiredDataHandler> void recursivelyHandle(Class<H> handlerType, OpenmrsObject openmrsObject,
	                                                                     User currentUser, Date currentDate, String other,
	                                                                     List<OpenmrsObject> alreadyHandled) {
		if (openmrsObject == null)
			return;
		
		Class<? extends OpenmrsObject> openmrsObjectClass = openmrsObject.getClass();
		
		if (alreadyHandled == null) {
			alreadyHandled = new ArrayList<OpenmrsObject>();
		}
		
		// fetch all handlers for the object being saved
		List<H> handlers = HandlerUtil.getHandlersForType(handlerType, openmrsObjectClass);
		
		// loop over all handlers, calling onSave on each
		for (H handler : handlers) {
			handler.handle(openmrsObject, currentUser, currentDate, other);
		}
		alreadyHandled.add(openmrsObject);
		
		Reflect reflect = new Reflect(OpenmrsObject.class);
		List<Field> allInheritedFields = reflect.getInheritedFields(openmrsObjectClass);
		
		// loop over all child collections of OpenmrsObjects and recursively save on those
		for (Field field : allInheritedFields) {
			if (reflect.isCollectionField(field)) {
				
				// the collection we'll be looping over
				Collection<OpenmrsObject> childCollection = getChildCollection(openmrsObject, field);
				
				if (childCollection != null) {
					for (Object collectionElement : childCollection) {
						if (!alreadyHandled.contains(collectionElement)) {
							recursivelyHandle(handlerType, (OpenmrsObject) collectionElement, currentUser, currentDate,
							    other, alreadyHandled);
						}
					}
				}
			}
		}
		
	}
	
	/**
	 * This method gets a child attribute off of an OpenmrsObject. It usually uses the getter for
	 * the attribute, but can use the direct field (even if its private) if told to by the
	 * {@link #fieldAccess} list.
	 * 
	 * @param openmrsObject the object to get the collection off of
	 * @param field the name of the field that is the collection
	 * @return the actual collection of objects that is on the given <code>openmrsObject</code>
	 * @should get value of given child collection on given field
	 * @should be able to get private fields in fieldAccess list
	 * @should throw APIException if getter method not found
	 */
	@SuppressWarnings("unchecked")
	protected static Collection<OpenmrsObject> getChildCollection(OpenmrsObject openmrsObject, Field field) {
		String fieldName = field.getName();
		String classdotfieldname = field.getDeclaringClass().getSimpleName() + "." + fieldName;
		String getterName = "get" + StringUtils.capitalize(fieldName);
		
		try {
			// checks the fieldAccess list for something like "Concept.answers"
			if (fieldAccess.contains(classdotfieldname)) {
				boolean previousFieldAccessibility = field.isAccessible();
				field.setAccessible(true);
				Collection<OpenmrsObject> childCollection = (Collection<OpenmrsObject>) field.get(openmrsObject);
				field.setAccessible(previousFieldAccessibility);
				return childCollection;
				
			} else {
				// access the field via its getter method
				Class<? extends OpenmrsObject> openmrsObjectClass = openmrsObject.getClass();
				
				Method getterMethod = openmrsObjectClass.getMethod(getterName, (Class[]) null);
				return (Collection<OpenmrsObject>) getterMethod.invoke(openmrsObject, new Object[] {});
				
			}
		}
		catch (IllegalAccessException e) {
			if (fieldAccess.contains(classdotfieldname))
				throw new APIException("Unable to get field: " + fieldName + " on " + openmrsObject.getClass());
			else
				throw new APIException("Unable to use getter method: " + getterName + " for field: " + fieldName + " on "
				        + openmrsObject.getClass());
		}
		catch (InvocationTargetException e) {
			throw new APIException("Unable to run getter method: " + getterName + " for field: " + fieldName + " on "
			        + openmrsObject.getClass());
		}
		catch (NoSuchMethodException e) {
			throw new APIException("Unable to find getter method: " + getterName + " for field: " + fieldName + " on "
			        + openmrsObject.getClass());
		}
	}
	
	/**
	 * Checks the given {@link Class} to see if it A) is a {@link Collection}/{@link Set}/
	 * {@link List}, and B) contains {@link OpenmrsObject}s
	 * 
	 * @param arg the actual object being passed in
	 * @return true if it is a Collection of some kind of OpenmrsObject
	 * @should return true if class is openmrsObject list
	 * @should return true if class is openmrsObject set
	 */
	@SuppressWarnings("unchecked")
	protected static boolean isOpenmrsObjectCollection(Object arg) {
		
		// kind of a hacky way to test for a list of openmrs objects, but java strips out
		// the generic info for 1.4 compat, so we don't have accesst to that info here
		try {
			@SuppressWarnings("unused")
			Collection<OpenmrsObject> openmrsObjects = (Collection<OpenmrsObject>) arg;
			return true;
		}
		catch (ClassCastException ex) {
			ex.printStackTrace();
		}
		return false;
	}
}
