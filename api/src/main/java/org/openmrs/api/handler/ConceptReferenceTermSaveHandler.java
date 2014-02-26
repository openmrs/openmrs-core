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
