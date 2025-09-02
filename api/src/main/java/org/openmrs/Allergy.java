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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.envers.Audited;
import org.openmrs.util.OpenmrsUtil;

/**
 * Represent allergy
 */
@Entity
@Table(name = "allergy")
@Audited
public class Allergy extends BaseFormRecordableOpenmrsData {
	
	public static final long serialVersionUID = 1;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "allergy_id")
	private Integer allergyId;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "patient_id")
	private Patient patient;
	
	@Embedded
	private Allergen allergen;
	
	@ManyToOne
	@JoinColumn(name = "severity_concept_id")
	private Concept severity;
	
	@Column(name = "comments", length = 1024)
	private String comments;
	

	@OneToMany(mappedBy = "allergy", cascade = CascadeType.ALL, orphanRemoval = true)
	@LazyCollection(LazyCollectionOption.FALSE)
	private List<AllergyReaction> reactions = new ArrayList<>();
	
	@ManyToOne(optional = true)
	@JoinColumn(name = "encounter_id")
	private Encounter encounter;
	
	/**
	 * Default constructor
	 */
	public Allergy(){
	}
	
	/**
	 * @param patient the patient to set.
	 * @param allergen the allergen to set
	 * @param severity the severity to set
	 * @param comments the comment to set
	 * @param reactions the reactions to set
	 */
	public Allergy(Patient patient, Allergen allergen, Concept severity, String comments, List<AllergyReaction> reactions) {
		this.patient = patient;
		this.allergen = allergen;
		this.severity = severity;
		this.comments = comments;
		
		//we do not allow to be in a state where reactions is null
		if (reactions != null) {
			this.reactions = reactions;
		}
	}
	
    /**
     * @return the allergyId
     */
    public Integer getAllergyId() {
    	return allergyId;
    }

    /**
     * @param allergyId the allergyId to set
     */
    public void setAllergyId(Integer allergyId) {
    	this.allergyId = allergyId;
    }

	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return allergyId;
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer allergyId) {
		this.allergyId = allergyId;
	}
	
	
	/**
	 * @return Returns the patient
	 */
	public Patient getPatient() {
		return patient;
	}
	
	/**
	 * @param patient the patient to set
	 */
	public void setPatient(Patient patient) {
		this.patient = patient;
	}
	/**
	 * @return the allergyType
	 */
	public AllergenType getAllergenType() {
		return allergen.getAllergenType();
	}
	
	/**
	 * set the allergyType of the Allergy
	 * @param allergenType the allergyType to set
	 */
	public void setAllergenType(AllergenType allergenType) {
		this.allergen.setAllergenType(allergenType);
	}
	
	/**
	 * set the allergyType of the Allergy. Here the allergy type will be chosen from the enum values in the {@link AllergenType}, according to the given String type. 
	 * @param type the allergyType to set   
	 */
	public void setAllergenType(String type) {
		this.allergen.setAllergenType(StringUtils.isBlank(type) ? null : AllergenType.valueOf(type));
	}
	
	/**
	 * @return Returns the allergen
	 */
	public Allergen getAllergen() {
		return allergen;
	}
	
	/**
	 * @param allergen the allergen to set
	 */
	public void setAllergen(Allergen allergen) {
		this.allergen = allergen;
	}
	/**
	 * @return Returns the severity
	 */
	public Concept getSeverity() {
		return severity;
	}
	
	/**
	 * @param severity the severity to set
	 */
	public void setSeverity(Concept severity) {
		this.severity = severity;
	}
	
	/**
	 * @return Returns the comment
	 * @deprecated as of 2.3.0, replaced by {@link #getComments()}
	 */
	@Deprecated
	public String getComment() {
		return getComments();
	}
	
	/**
	 * @param comment the comment to set
	 * @deprecated as of 2.3.0, replaced by {@link #setComments(String)}
	 */
	@Deprecated
	public void setComment(String comment) {
		setComments(comment);
	}
	
	/**
	 * @return Returns the comments
	 * @since 2.3.0
	 */
	public String getComments() {
		return comments;
	}
	
	/**
	 * @param comments the comments to set
	 * @since 2.3.0
	 */
	public void setComments(String comments) {
		this.comments = comments;
	}
	/**
	 * @return Returns the reactions
	 */
	public List<AllergyReaction> getReactions() {
		return reactions;
	}
	
	/**
	 * @param reactions the reactions to set
	 */
	public void setReactions(List<AllergyReaction> reactions) {
		//we do not allow to be in a state where reactions is null
		if (reactions != null) {
			this.reactions = reactions;
		}
		else {
			this.reactions.clear();
		}
	}

	/**
	 * Adds a new allergy reaction
	 * 
	 * @param reaction the reaction to add
	 * @return true if the reaction was added, else false
	 */
	public boolean addReaction(AllergyReaction reaction) {
        if(getReactionConcepts().contains(reaction.getReaction())){
            return false;
        }
		reaction.setAllergy(this);
		return getReactions().add(reaction);
	}
	
	/**
	 * Removes an allergy reaction
	 * 
	 * @param reaction the reaction to remove
	 * @return true if the reaction was found and removed, else false.
	 */
	public boolean removeReaction(AllergyReaction reaction) {
		return getReactions().remove(reaction);
	}
	
	public Date getDateLastUpdated() {
		if (getDateChanged() != null) {
			return getDateChanged();
		}
		return getDateCreated();
	}
	
	/**
	 * Checks if this allergy has the same values as a given one.
	 * 
	 * @param allergy the allergy whose values to compare with
	 * @return true if the values match, else false
	 */
	public boolean hasSameValues(Allergy allergy) {
		if (!OpenmrsUtil.nullSafeEquals(getAllergyId(), allergy.getAllergyId())) {
			return false;
		}
		if (!OpenmrsUtil.nullSafeEquals(getPatient(), allergy.getPatient())) {
			//if object instances are different but with the same patient id, then not changed
			if (getPatient() != null && allergy.getPatient() != null) {
				if (!OpenmrsUtil.nullSafeEquals(getPatient().getPatientId(), allergy.getPatient().getPatientId())) {
					return false;
				}
			}
			else {
				return false;
			}
		}
		if (!OpenmrsUtil.nullSafeEquals(getAllergen().getCodedAllergen(), allergy.getAllergen().getCodedAllergen())) {
			//if object instances are different but with the same concept id, then not changed
			if (getAllergen().getCodedAllergen() != null && allergy.getAllergen().getCodedAllergen() != null) {
				if (!OpenmrsUtil.nullSafeEquals(getAllergen().getCodedAllergen().getConceptId(), allergy.getAllergen().getCodedAllergen().getConceptId())) {
					return false;
				}
			}
			else {
				return false;
			}
		}
		if (!OpenmrsUtil.nullSafeEquals(getAllergen().getNonCodedAllergen(), allergy.getAllergen().getNonCodedAllergen())) {
			return false;
		}
		if (!OpenmrsUtil.nullSafeEquals(getSeverity(), allergy.getSeverity())) {
			//if object instances are different but with the same concept id, then not changed
			if (getSeverity() != null && allergy.getSeverity() != null) {
				if (!OpenmrsUtil.nullSafeEquals(getSeverity().getConceptId(), allergy.getSeverity().getConceptId())) {
					return false;
				}
			}
			else {
				return false;
			}
		}
		if (!OpenmrsUtil.nullSafeEquals(getComment(), allergy.getComment())) {
			return false;
		}
		return hasSameReactions(allergy);
	}
	
	/**
	 * Checks if this allergy has the same reaction values as those in the given one
	 * 
	 * @param allergy the allergy who reaction values to compare with
	 * @return true if the values match, else false
	 */
	private boolean hasSameReactions(Allergy allergy) {
		if (getReactions().size() != allergy.getReactions().size()) {
			return false;
		}
		
		for (AllergyReaction reaction : getReactions()) {
			AllergyReaction rc = allergy.getAllergyReaction(reaction.getAllergyReactionId());
			if (!reaction.hasSameValues(rc)) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Gets an allergy reaction with a given id
	 * 
	 * @param allergyReactionId the allergy reaction id
	 * @return the allergy reaction with a matching id
	 */
	public AllergyReaction getAllergyReaction(Integer allergyReactionId) {
		for (AllergyReaction reaction : reactions) {
			if (OpenmrsUtil.nullSafeEquals(reaction.getAllergyReactionId(), allergyReactionId)) {
				return reaction;
			}
		}
		
		return null;
	}
	
	/**
	 * Copies all property values, apart from the id and uuid,
	 * from the given allergy into this object
	 * 
	 * @param allergy the allergy whose property values to copy
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	public void copy(Allergy allergy) {
		setAllergyId(null);
		setUuid(UUID.randomUUID().toString());
		setPatient(allergy.getPatient());
		setAllergen(allergy.getAllergen());
		setSeverity(allergy.getSeverity());
		setComment(allergy.getComment());
		setReactions(new ArrayList<>());
		
		for (AllergyReaction reaction : allergy.getReactions()) {
			reactions.add(reaction);
			reaction.setAllergyReactionId(null);
			reaction.setUuid(UUID.randomUUID().toString());
		}
	}

    private List<Concept> getReactionConcepts(){
        List<Concept> reactionConcepts = new ArrayList<>(getReactions().size());
        for (AllergyReaction ar : getReactions()) {
            reactionConcepts.add(ar.getReaction());
        }
        return reactionConcepts;
    }
    
    /**
	 * @return Returns the reactionNonCoded
	 */
	public String getReactionNonCoded() {
		for (AllergyReaction reaction : reactions) {
			if (StringUtils.isNotBlank(reaction.getReactionNonCoded())) {
				return reaction.getReactionNonCoded();
			}
		}
		return null;
	}
	
	/**
	 * Gets the reaction with a given concept
	 * 
	 * @param concept the concept
	 * @return the reaction if any exists
	 */
	public AllergyReaction getReaction(Concept concept) {
		for (AllergyReaction reaction : reactions) {
			if (reaction.getReaction().equals(concept)) {
				return reaction;
			}
		}
		return null;
	}
	
	/**
	 * Checks if we have the same allergen as that in the given allergy
	 * 
	 * @param allergy the given allergy whose allergen to check
	 * @return true if the same, else false
	 */
	public boolean hasSameAllergen(Allergy allergy) {
		if (allergen == null || allergy.getAllergen() == null) {
			return false;
		}
		return allergen.isSameAllergen(allergy.getAllergen());
	}
	
	/**
	 * Basic property getter for encounter
	 * 
	 * @return encounter - the associated encounter
	 * @since 2.5.0
	 */
	public Encounter getEncounter() {
		return encounter;
	}
	
	/**
	 * Basic property setter for encounter
	 *  
	 * @param encounter - the encounter to set
	 * @since 2.5.0
	 */
	public void setEncounter(Encounter encounter) {
		this.encounter = encounter;
	}
}
