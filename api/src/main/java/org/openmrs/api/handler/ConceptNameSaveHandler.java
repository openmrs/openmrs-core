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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import org.openmrs.ConceptName;
import org.openmrs.ConceptNameTag;
import org.openmrs.User;
import org.openmrs.annotation.Handler;
import org.openmrs.aop.RequiredDataAdvice;
import org.openmrs.api.context.Context;

/**
 * This class deals with {@link ConceptName} objects when they are saved via a save* method in an
 * Openmrs Service. This handler is automatically called by the {@link RequiredDataAdvice} AOP
 * class. <br>
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
	 * <strong>Should</strong> not fail if tags is null
	 * <strong>Should</strong> replace tags without ids with database fetched tag
	 * <strong>Should</strong> not replace tags without ids that are not in the database
	 * <strong>Should</strong> not replace tags that have ids
	 */
	@Override
	public void handle(ConceptName conceptName, User currentUser, Date currentDate, String reason) {
		
		// put Integer conceptNameTagIds onto ConceptNameTags that are missing them
		if (conceptName.getTags() != null) {
			Collection<ConceptNameTag> replacementTags = new ArrayList<>();
			
			Iterator<ConceptNameTag> tagsIt = conceptName.getTags().iterator();
			while (tagsIt.hasNext()) {
				ConceptNameTag tag = tagsIt.next();
				
				if (tag.getConceptNameTagId() == null) {
					ConceptNameTag replacementTag = Context.getConceptService().getConceptNameTagByName(tag.getTag());
					
					if (replacementTag != null) {
						tagsIt.remove();
						replacementTags.add(replacementTag);
					}
				}
			}
			
			if (!replacementTags.isEmpty()) {
				conceptName.getTags().addAll(replacementTags);
			}
		}
	}
	
}
