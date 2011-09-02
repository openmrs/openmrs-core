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

import org.apache.commons.collections.CollectionUtils;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNameTag;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptReferenceTermMap;
import org.openmrs.User;
import org.openmrs.annotation.Handler;
import org.openmrs.aop.RequiredDataAdvice;
import org.openmrs.api.context.Context;

/**
 * This class deals with {@link ConceptName} objects when they are saved via a save* method in an
 * Openmrs Service. This handler is automatically called by the {@link RequiredDataAdvice} AOP
 * class. <br/>
 * This class does a lookup on all tag name for all child {@link ConceptNameTag}s that have a null
 * {@link ConceptNameTag#getConceptNameTagId()}.
 * 
 * @see RequiredDataHandler
 * @see SaveHandler
 * @see ConceptReferenceTerm
 * @since 1.9
 */
@Handler(supports = ConceptReferenceTerm.class)
public class ConceptReferenceTermSaveHandler implements SaveHandler<ConceptReferenceTerm> {
	
	/**
	 * This removes white space characters from the beginning and end of all strings
	 * 
	 * @see org.openmrs.api.handler.RequiredDataHandler#handle(org.openmrs.OpenmrsObject,
	 *      org.openmrs.User, java.util.Date, java.lang.String)
	 */
	public void handle(ConceptReferenceTerm conceptReferenceTerm, User currentUser, Date currentDate, String other) {
		
		if (conceptReferenceTerm.getConceptReferenceTermId() == null) {
			if (conceptReferenceTerm.getCreator() == null)
				conceptReferenceTerm.setCreator(Context.getAuthenticatedUser());
			if (conceptReferenceTerm.getDateCreated() == null)
				conceptReferenceTerm.setDateCreated(new Date());
		}
		if (conceptReferenceTerm.getName() != null) {
			conceptReferenceTerm.setName(conceptReferenceTerm.getName().trim());
		}
		if (conceptReferenceTerm.getCode() != null) {
			conceptReferenceTerm.setCode(conceptReferenceTerm.getCode().trim());
		}
		
		if (conceptReferenceTerm.getDescription() != null) {
			//set value to null if we have a blank description
			if (conceptReferenceTerm.getDescription().trim().length() == 0)
				conceptReferenceTerm.setDescription(null);
			else
				conceptReferenceTerm.setDescription(conceptReferenceTerm.getDescription().trim());
		}
		if (conceptReferenceTerm.getVersion() != null) {
			if (conceptReferenceTerm.getVersion().trim().length() == 0)
				conceptReferenceTerm.setVersion(null);
			else
				conceptReferenceTerm.setVersion(conceptReferenceTerm.getVersion().trim());
		}
		
		//always update the uuid just in case source and code have been edited
		//term.setUuid(term.getConceptSource().getHl7Code().concat("-").concat(term.getCode()));
		conceptReferenceTerm.setUuid(conceptReferenceTerm.getConceptSource().getHl7Code().concat("-").concat(
		    conceptReferenceTerm.getCode()));
		
		if (CollectionUtils.isNotEmpty(conceptReferenceTerm.getConceptReferenceTermMaps())) {
			for (ConceptReferenceTermMap map : conceptReferenceTerm.getConceptReferenceTermMaps())
				map.setTermA(conceptReferenceTerm);
		}
	}
}
