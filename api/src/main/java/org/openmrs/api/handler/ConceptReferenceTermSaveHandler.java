/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.handler;

import java.util.Date;

import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptReferenceTermMap;
import org.openmrs.User;
import org.openmrs.annotation.Handler;
import org.openmrs.aop.RequiredDataAdvice;

/**
 * This class deals with {@link ConceptReferenceTerm} objects when they are saved via a save* method
 * in an Openmrs Service. This handler is automatically called by the {@link RequiredDataAdvice} AOP
 * class. <br/>
 * It sets the termA field for all {@link ConceptReferenceTermMap}s</li>
 *
 * @see RequiredDataHandler
 * @see SaveHandler
 * @see ConceptReferenceTerm
 * @since 1.9
 */
@Handler(supports = ConceptReferenceTerm.class)
public class ConceptReferenceTermSaveHandler implements SaveHandler<ConceptReferenceTerm> {
	
	/**
	 * Sets the concept reference term as the term A for all the {@link ConceptReferenceTermMap}s
	 * added to it.
	 *
	 * @see org.openmrs.api.handler.RequiredDataHandler#handle(org.openmrs.OpenmrsObject,
	 *      org.openmrs.User, java.util.Date, java.lang.String)
	 */
	public void handle(ConceptReferenceTerm conceptReferenceTerm, User currentUser, Date currentDate, String other) {
		if (conceptReferenceTerm.getConceptReferenceTermMaps() != null) {
			for (ConceptReferenceTermMap map : conceptReferenceTerm.getConceptReferenceTermMaps()) {
				map.setTermA(conceptReferenceTerm);
			}
		}
	}
}
