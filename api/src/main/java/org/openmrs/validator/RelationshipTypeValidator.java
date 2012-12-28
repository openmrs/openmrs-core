/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
* Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.validator;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.RelationshipType;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validates attributes on the {@link RelationshipType} object.
 * 
 * @since 1.10
 */
@Handler(supports = { RelationshipType.class }, order = 50)
public class RelationshipTypeValidator implements Validator {
	
	/** Log for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Determines if the command object being submitted is a valid type
	 * 
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public boolean supports(Class c) {
		return c.equals(RelationshipType.class);
	}
	
	/**
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	public void validate(Object obj, Errors errors) {
		RelationshipType relationshipType = (RelationshipType) obj;
		if (relationshipType == null) {
			errors.rejectValue("relationshipType", "error.general");
		} else {
		
		/**
		*check duplicate name 		
		*/		
			List<RelationshipType> rts = Context.getPersonService().getRelationshipTypes(relationshipType.toString());
			
			/**
			*set name fields for comparison
			*/
			relationshipType.setName(relationshipType.toString());
			for(org.openmrs.BaseOpenmrsMetadata dupobj : rts) {
				dupobj.setName(dupobj.toString());
			}	
		
			/**
			*unset name field
			*/
			
			relationshipType.setName(null);
		}
	}
}