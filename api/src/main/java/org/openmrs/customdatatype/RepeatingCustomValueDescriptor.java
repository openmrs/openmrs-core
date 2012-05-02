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

/**
 * Metadata describing how a custom value may repeat within an owning type, as well as how it is stored. (For example
 * a VisitAttributeType describes the behavior of the VisitAttributes within a Visit
 * @since 1.9 
 */
public interface RepeatingCustomValueDescriptor extends CustomValueDescriptor {
	
	/**
	 * Implementations should never return null. Positive return values indicate a "required" value.
	 * @return the minimum number of values of this type that must be present for the owner to be valid
	 */
	Integer getMinOccurs();
	
	/**
	 * Implementation should never return a number <= 0.
	 * @return the maximum number of values of this type that may be present for the owner to be valid
	 */
	Integer getMaxOccurs();
	
}
