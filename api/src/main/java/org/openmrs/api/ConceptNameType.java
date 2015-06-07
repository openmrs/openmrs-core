/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api;

import org.openmrs.ConceptName;

/**
 * The concept name type enumeration <br>
 * <br>
 * <b>FULLY_SPECIFIED</b> - Indicates that the name is marked as the fully specified name, which is
 * returned by default for a locale in case there is no preferred name set, a concept can have only
 * one fully specified name per locale. It will also be set as the preferred name in its locale if
 * no name is explicitly set as prefererred for the same locale and concept.<br>
 * <br>
 * <b>SHORT</b> - Indicates the name is marked as the short name for the concept, only one name can
 * be marked short per locale for a concept <br>
 * <br>
 * <b>INDEX_TERM</b> - Indicates that the name is marked as a search term for the concept, it could
 * be a common mis spelt version of any of the names for the concept, typically this name will be
 * used for searching purposes. <br>
 * <br>
 * NOTE: Any name with a null Concept name type is deemed a synonym. ONLY a fully specified name or
 * synonym can be marked as preferred
 * 
 * @see ConceptName
 * @since Version 1.7
 */
public enum ConceptNameType {
	
	FULLY_SPECIFIED,
	SHORT,
	INDEX_TERM;
	
}
