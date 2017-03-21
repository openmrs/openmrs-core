/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.handler;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.UUID;

import org.apache.commons.beanutils.PropertyUtils;
import org.openmrs.Obs;
import org.openmrs.OpenmrsObject;
import org.openmrs.User;
import org.openmrs.Voidable;
import org.openmrs.annotation.AllowEmptyStrings;
import org.openmrs.annotation.AllowLeadingOrTrailingWhitespace;
import org.openmrs.annotation.Handler;
import org.openmrs.aop.RequiredDataAdvice;
import org.openmrs.api.APIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class deals with any object that implements {@link OpenmrsObject}. When an
 * {@link OpenmrsObject} is saved (via a save* method in a service), this handler is automatically
 * called by the {@link RequiredDataAdvice} AOP class. <br>
 * <br>
 * This class sets the uuid property on the given OpenmrsObject to a randomly generated <a
 * href="http://wikipedia.org/wiki/UUID">UUID</a> if it is non-null.
 *
 * @see RequiredDataHandler
 * @see SaveHandler
 * @since 1.5
 */
@Handler(supports = OpenmrsObject.class)
public class OpenmrsObjectSaveHandler implements SaveHandler<OpenmrsObject> {
	
	private static final Logger log = LoggerFactory.getLogger(OpenmrsObjectSaveHandler.class);
	
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
	@Override
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
            //TODO We shouldn't be doing this for all immutable types and fields
			if (openmrsObject instanceof Obs ||!property.getPropertyType().equals(String.class)) {
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
				
				if ("".equals(value) && !(openmrsObject instanceof Voidable && ((Voidable) openmrsObject).getVoided())) {
					//Set to null only if object is not already voided
					PropertyUtils.setProperty(openmrsObject, property.getName(), null);
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
				throw new APIException("failed.change.property.value", new Object[] { property.getName() }, ex);
			}
		}
	}
}
