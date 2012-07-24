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
	
	FULLY_SPECIFIED, SHORT, INDEX_TERM;
	
}
