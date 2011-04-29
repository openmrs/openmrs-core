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
package org.openmrs.attribute;

import org.openmrs.OpenmrsMetadata;
import org.openmrs.attribute.handler.AttributeHandler;

/**
 * Common interface for user-defined extensions to core domain objects, which would be handled by adding
 * custom database columns in a less generic system. 
 * For example Visit has VisitAttributes that are defined by VisitAttributeTypes (that implement
 * AttributeType<Visit>).
 * @param <OwningType> the type this attribute type can belong to
 * @see Attribute
 * @see Customizable
 * @see AttributeHandler
 * @since 1.9
 */
public interface AttributeType<OwningType extends Customizable<?>> extends OpenmrsMetadata {
	
	/**
	 * Implementations should never return null. Positive return values indicate a "required" attribute type.
	 * @return the minimum number of times that attributes of this type must be present on the OwningType.
	 */
	Integer getMinOccurs();
	
	/**
	 * Implementation should never return a number <= 0.
	 * @return 
	 */
	Integer getMaxOccurs();
	
	/**
	 * The complete list of available datatypes are defined by subclasses of {@link AttributeHandler}.
	 * @return the datatype represented by this attribute type, for example "regex-validated-string"
	 * @see AttributeHandler
	 */
	String getDatatype();
	
	/**
	 * May be null.
	 * @return the configuration to be passed to the handler for a datatype. For example if the
	 * datatype is "regex-validated-string", the handlerConfig would be the regular expression.
	 * @see AttributeHandler#setConfiguration(String)
	 */
	String getHandlerConfig();
	
}
