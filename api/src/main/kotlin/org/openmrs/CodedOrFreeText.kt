/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs

import jakarta.persistence.Embeddable
import jakarta.persistence.ManyToOne

/**
 * A Concept object can represent either a question or an answer to a data point. That data point is
 * usually an [Obs].
 *
 * ConceptName is the real world term used to express a Concept within the idiom of a particular
 * locale.
 *
 * The purpose of this class therefore is to record information of a single encounter by taking the
 * concept and concept name or a string for a concept that may not be in the database.
 * 
 * @since 2.2
 */
@Embeddable
class CodedOrFreeText(
	@ManyToOne
	var coded: Concept? = null,
	
	@ManyToOne
	var specificName: ConceptName? = null,
	
	var nonCoded: String? = null
)
