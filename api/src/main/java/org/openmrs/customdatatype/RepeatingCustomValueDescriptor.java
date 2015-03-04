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
