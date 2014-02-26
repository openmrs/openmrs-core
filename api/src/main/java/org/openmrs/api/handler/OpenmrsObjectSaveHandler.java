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
package org.openmrs.api.handler;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.UUID;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.OpenmrsObject;
import org.openmrs.User;
import org.openmrs.Voidable;
import org.openmrs.annotation.AllowEmptyStrings;
import org.openmrs.annotation.AllowLeadingOrTrailingWhitespace;
import org.openmrs.annotation.Handler;
import org.openmrs.aop.RequiredDataAdvice;
import org.openmrs.api.APIException;

/**
 * This class deals with any object that implements {@link OpenmrsObject}. When an
 * {@link OpenmrsObject} is saved (via a save* method in a service), this handler is automatically
 * called by the {@link RequiredDataAdvice} AOP class. <br/>
 * <br/>
 * This class sets the uuid property on the given OpenmrsObject to a randomly generated <a
 * href="http://wikipedia.org/wiki/UUID">UUID</a> if it is non-null.
 *
 * @see RequiredDataHandler
 * @see SaveHandler
 * @since 1.5
 */
@Handler(supports = OpenmrsObject.class)
public class OpenmrsObjectSaveHandler implements SaveHandler<OpenmrsObject> {
	
	private static final Log log = LogFactory.getLog(OpenmrsObjectSaveHandler.class);
	
	/**
	 * This sets the uuid property on the given OpenmrsObject if it is non-null.
	 *
	 * @see org.openmrs.api.handler.RequiredDataHandler#handle(org.openmrs.OpenmrsObject,
	 *      org.openmrs.User, java.util.Date, java.lang.String)
	 * @should set empty string properties to null
	 * @should not set empty string properties to null for AllowEmptyStrings annotation
	 * @should not trim empty strings for AllowLeadingOrTrailingWhitespace annotation
	 * @should trim strings without AllowLeadingOrTrailingWhitespace annotation
	 * @should trim empty strings for AllowEmptyStrings annotation
	 */
	public void handle(OpenmrsObject openmrsObject, User creator, Date dateCreated, String reason) {
		if (openmrsObject.getUuid() == null) {
			openmrsObject.setUuid(UUID.randomUUID().toString());
		}
		
		//Set all empty string properties, that do not have the AllowEmptyStrings annotation, to null.
		//And also trim leading and trailing white space for properties that do not have the
		//AllowLeadingOrTrailingWhitespace annotation.
		PropertyDescriptor[] properties = PropertyUtils.getPropertyDescriptors(openmrsObject);
		for (PropertyDescriptor property : properties) {
			
			if (property.getPropertyType() == null) {
				continue;
			}
			
			// Ignore properties that don't have a getter (e.g. GlobalProperty.valueReferenceInternal) or
			// don't have a setter (e.g. Patient.familyName)
			if (property.getWriteMethod() == null || property.getReadMethod() == null) {
				continue;
			}
			
			// Ignore properties that have a deprecated getter or setter
			if (property.getWriteMethod().getAnnotation(Deprecated.class) != null
			        || property.getReadMethod().getAnnotation(Deprecated.class) != null) {
				continue;
			}
			
			//We are dealing with only strings
			if (!property.getPropertyType().equals(String.class)) {
				continue;
			}
			
			try {
				Object value = PropertyUtils.getProperty(openmrsObject, property.getName());
				if (value == null) {
					continue;
				}
				
				Object valueBeforeTrim = value;
				if (property.getWriteMethod().getAnnotation(AllowLeadingOrTrailingWhitespace.class) == null) {
					value = ((String) value).trim();
					
					//If we have actually trimmed any space, set the trimmed value.
					if (!valueBeforeTrim.equals(value)) {
						PropertyUtils.setProperty(openmrsObject, property.getName(), value);
					}
				}
				
				//Check if user is interested in setting empty strings to null
				if (property.getWriteMethod().getAnnotation(AllowEmptyStrings.class) != null) {
					continue;
				}
				
				if ("".equals(value)) {
					//Set to null only if object is not already voided
					if (!(openmrsObject instanceof Voidable && ((Voidable) openmrsObject).isVoided())) {
						PropertyUtils.setProperty(openmrsObject, property.getName(), null);
					}
				}
			}
			catch (UnsupportedOperationException ex) {
				// there is no need to log this. These should be (mostly) silently skipped over 
				if (log.isInfoEnabled()) {
					log.info("The property " + property.getName() + " is no longer supported and should be ignored.", ex);
				}
			}
			catch (InvocationTargetException ex) {
				if (log.isWarnEnabled()) {
					log.warn("Failed to access property " + property.getName() + "; accessor threw exception.", ex);
				}
			}
			catch (Exception ex) {
				throw new APIException(
				        "Failed to change property value from empty string to null for " + property.getName(), ex);
			}
		}
	}
}
