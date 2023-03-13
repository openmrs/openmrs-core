/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.util.OpenmrsUtil;

/**
 * Represent allergy reactions
 */
public class AllergyReaction extends BaseOpenmrsObject implements java.io.Serializable{
	
	public static final long serialVersionUID = 1;


	private Integer allergyReactionId;
	
	private Allergy allergy;
	
	private Concept reaction;
	
	private String reactionNonCoded;
	
	/**
	 * Default constructor
	 */
	public AllergyReaction(){
		
	}
	
	/**
	 * @param allergy the allergy to set
	 * @param reaction the reaction to set
	 * @param reactionNonCoded the reactionNonCoded to set
	 */
	public AllergyReaction(Allergy allergy, Concept reaction, String reactionNonCoded) {
		this.allergy = allergy;
		this.reaction = reaction;
		this.reactionNonCoded = reactionNonCoded;
	}
	
	public Integer getAllergyReactionId() {
		return allergyReactionId;
	}

	public void setAllergyReactionId(Integer allergyReactionId) {
		this.allergyReactionId = allergyReactionId;
	}

	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return allergyReactionId;
	}

	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer allergyReactionId) {
		this.allergyReactionId = allergyReactionId;
	}
	
	/**
	 * @return Returns the allergy
	 */
	public Allergy getAllergy() {
		return allergy;
	}
	
	/**
	 * @param allergy the allergy to set
	 */
	public void setAllergy(Allergy allergy) {
		this.allergy = allergy;
	}
	
	/**
	 * @return Returns the reaction
	 */
	public Concept getReaction() {
		return reaction;
	}
	
	/**
	 * @param reaction the reaction to set
	 */
	public void setReaction(Concept reaction) {
		this.reaction = reaction;
	}
	
	/**
	 * @return Returns the reactionNonCoded
	 */
	public String getReactionNonCoded() {
		return reactionNonCoded;
	}
	
	/**
	 * @param reactionNonCoded the reactionNonCoded to set
	 */
	public void setReactionNonCoded(String reactionNonCoded) {
		this.reactionNonCoded = reactionNonCoded;
	}
	
	@Override
    public String toString() {
	    if (StringUtils.isNotBlank(reactionNonCoded)) {
	    	return reactionNonCoded;
	    }
	    return reaction.getName().getName();
    }
	
	/**
	 * Checks if this reaction has the same values as the given one
	 * 
	 * @param reaction the reaction whose values to compare with
	 * @return true if the values match, else false
	 */
	public boolean hasSameValues(AllergyReaction reaction) {
		if (!OpenmrsUtil.nullSafeEquals(getAllergyReactionId(), reaction.getAllergyReactionId())) {
			return false;
		}
		if (!OpenmrsUtil.nullSafeEquals(getReaction(), reaction.getReaction())) {
			//if object instances are different but with the same concept id, then not changed
			if (getReaction() != null && reaction.getReaction() != null) {
				if (!OpenmrsUtil.nullSafeEquals(getReaction().getConceptId(), reaction.getReaction().getConceptId())) {
					return false;
				}
			}
			else {
				return false;
			}
		}
		return OpenmrsUtil.nullSafeEquals(getReactionNonCoded(), reaction.getReactionNonCoded());
	}
}
