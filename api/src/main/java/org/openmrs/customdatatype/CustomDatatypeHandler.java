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
package org.openmrs.customdatatype;

import org.openmrs.attribute.AttributeType;

/**
 * Subclasses of this interface represent different ways of building UI widgets to handle {@link CustomDatatype}s.
 * @param <DT> the {@link CustomDatatype} class that this class handles
 * @param <T> the java type of values handled by DT
 * @since 1.9
 */
public interface CustomDatatypeHandler<DT extends CustomDatatype<T>, T> {
	
	/**
	 * An {@link AttributeType} will typically be configured with a handler and a configuration. The framework
	 * takes care of constructing a handler, and setting its configuration (if any)  
	 * @param handlerConfig
	 */
	void setHandlerConfiguration(String handlerConfig);
	
}
