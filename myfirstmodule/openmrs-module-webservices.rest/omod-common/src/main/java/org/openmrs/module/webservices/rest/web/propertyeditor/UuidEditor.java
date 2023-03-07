/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.propertyeditor;

import java.beans.PropertyEditorSupport;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.beanutils.PropertyUtils;
import org.openmrs.api.OpenmrsService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.RuntimeWrappedException;
import org.openmrs.module.webservices.rest.web.response.ObjectNotFoundException;

/**
 * You can use this to override the property editors in the OpenMRS core, that are based off of
 * primary keys instead of UUIDs.
 */
public class UuidEditor extends PropertyEditorSupport {
	
	Class<? extends OpenmrsService> serviceClass;
	
	Method method;
	
	public UuidEditor(Class<? extends OpenmrsService> serviceClass, String methodName) throws SecurityException,
	        NoSuchMethodException {
		this.serviceClass = serviceClass;
		OpenmrsService service = Context.getService(serviceClass);
		method = service.getClass().getMethod(methodName, String.class);
	}
	
	@Override
	public void setAsText(String uuid) throws IllegalArgumentException {
		OpenmrsService service = Context.getService(serviceClass);
		try {
			Object val = method.invoke(service, uuid);
			if (val == null)
				throw new RuntimeWrappedException(new ObjectNotFoundException());
			setValue(val);
		}
		catch (IllegalAccessException ex) {
			throw new RuntimeException(ex);
		}
		catch (InvocationTargetException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	@Override
	public String getAsText() {
		try {
			return (String) PropertyUtils.getProperty(getValue(), "uuid");
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
}
