/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
