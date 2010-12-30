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

import org.openmrs.ConceptName;
import org.openmrs.ConceptNameTag;
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
 * @see ConceptName
 * @since 1.5
 */
@Handler(supports = ConceptName.class)
public class ConceptNameSaveHandler implements SaveHandler<ConceptName> {
	
	/**
	 * This method does a lookup on all tag name for all child {@link ConceptNameTag}s that have a
	 * null {@link ConceptNameTag#getConceptNameTagId()}.
	 * 
	 * @see org.openmrs.api.handler.RequiredDataHandler#handle(org.openmrs.OpenmrsObject,
	 *      org.openmrs.User, java.util.Date, java.lang.String)
	 * @should not fail if tags is null
	 * @should replace tags without ids with database fetched tag
	 * @should not replace tags without ids that are not in the database
	 * @should not replace tags that have ids
	 */
	public void handle(ConceptName conceptName, User currentUser, Date currentDate, String reason) {
		
		// put Integer conceptNameTagIds onto ConceptNameTags that are missing them
		if (conceptName.getTags() != null) {
			for (ConceptNameTag tag : conceptName.getTags()) {
				if (tag.getConceptNameTagId() == null) {
					ConceptNameTag possibleReplacementTag = Context.getConceptService()
					        .getConceptNameTagByName(tag.getTag());
					if (possibleReplacementTag != null) {
						conceptName.removeTag(tag);
						conceptName.addTag(possibleReplacementTag);
					}
				}
			}
		}
	}
	
}
