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

import org.openmrs.BaseOpenmrsMetadata;
import org.openmrs.customdatatype.Customizable;

/**
 * Abstract base implementation of {@link AttributeType}. Actual implementations (e.g. VisitAttributeType,
 * ProviderAttributeType) should be able to extend this and provide very little of their own code.
 * @since 1.9
 */
public abstract class BaseAttributeType<OwningType extends Customizable<?>> extends BaseOpenmrsMetadata implements AttributeType<OwningType> {
	
	private Integer minOccurs = 0;
	
	private Integer maxOccurs = null;
	
	private String datatypeClassname;
	
	private String datatypeConfig;
	
	private String preferredHandlerClassname;
	
	private String handlerConfig;
	
	/**
	 * @see org.openmrs.customdatatype.RepeatingCustomValueDescriptor#getMinOccurs()
	 */
	@Override
	public Integer getMinOccurs() {
		return minOccurs;
	}
	
	/**
	 * @see org.openmrs.customdatatype.RepeatingCustomValueDescriptor#getMaxOccurs()
	 */
	@Override
	public Integer getMaxOccurs() {
		return maxOccurs;
	}
	
	/**
	 * @see org.openmrs.customdatatype.CustomValueDescriptor#getDatatypeClassname()
	 */
	@Override
	public String getDatatypeClassname() {
		return datatypeClassname;
	}
	
	/**
	 * @see org.openmrs.customdatatype.CustomValueDescriptor#getDatatypeConfig()
	 */
	@Override
	public String getDatatypeConfig() {
		return datatypeConfig;
	}
	
	/**
	 * @see org.openmrs.customdatatype.CustomValueDescriptor#getPreferredHandlerClassname()
	 */
	@Override
	public String getPreferredHandlerClassname() {
		return preferredHandlerClassname;
	}
	
	/**
	 * @see org.openmrs.customdatatype.CustomValueDescriptor#getHandlerConfig()
	 */
	@Override
	public String getHandlerConfig() {
		return handlerConfig;
	}
	
	/**
	 * @param minOccurs the minOccurs to set
	 */
	public void setMinOccurs(Integer minOccurs) {
		this.minOccurs = minOccurs;
	}
	
	/**
	 * @param maxOccurs the maxOccurs to set
	 */
	public void setMaxOccurs(Integer maxOccurs) {
		this.maxOccurs = maxOccurs;
	}
	
	/**
	 * @param datatypeClassname the datatypeClassname to set
	 */
	public void setDatatypeClassname(String datatypeClassname) {
		this.datatypeClassname = datatypeClassname;
	}
	
	/**
	 * @param datatypeConfig the datatypeConfig to set
	 */
	public void setDatatypeConfig(String datatypeConfig) {
		this.datatypeConfig = datatypeConfig;
	}
	
	/**
	 * @param preferredHandlerClassname the preferredHandlerClassname to set
	 */
	public void setPreferredHandlerClassname(String preferredHandlerClassname) {
		this.preferredHandlerClassname = preferredHandlerClassname;
	}
	
	/**
	 * @param handlerConfig the handlerConfig to set
	 */
	public void setHandlerConfig(String handlerConfig) {
		this.handlerConfig = handlerConfig;
	}
	
}
