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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;
import org.hibernate.envers.Audited;
import org.openmrs.util.OpenmrsConstants;

/**
 * A ConceptProposal is a temporary holder for concept that should be in the system. When defining
 * an observation, a user can "propose" a new concept if one isn't found already. The proposal is a
 * simple text entry that will be reviewed later. When a proposal is (edited and) accepted, the
 * encounter that prompted this proposal is updated with a new observation pointing at the new (or
 */

@Entity
@Table(name = "concept_proposal")
@Audited
public class ConceptProposal extends BaseOpenmrsObject {
	
	public static final long serialVersionUID = 57344L;
	
	// Fields
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "concept_proposal_id")
	private Integer conceptProposalId;
	
	@ManyToOne
	@JoinColumn(name = "encounter_id")
	private Encounter encounter;
	
	@ManyToOne
	@JoinColumn(name = "obs_concept_id")
	private Concept obsConcept;
	
	@ManyToOne
	@JoinColumn(name = "obs_id")
	private Obs obs;
	
	@ManyToOne
	@JoinColumn(name = "mapped_concept_id")
	private Concept mappedConcept;

	/**
	 * @deprecated Only kept for legacy dataset and Hibernate mapping compatibility. Avoid using this field directly.
	 */
	@Deprecated
	@ManyToOne
	@JoinColumn(name = "concept_id")
	private Concept concept;

	@Column(name = "original_text")
	private String originalText;
	
	@Column(name = "final_text")
	private String finalText;
	
	@Column(name = "state")
	private String state;
	
	@Column(name = "comments")
	private String comments;
	
	@ManyToOne
	@JoinColumn(name = "creator")
	private User creator;
	
	@Column(name = "date_created")
	private Date dateCreated;
	
	@ManyToOne
	@JoinColumn(name = "changed_by")
	private User changedBy;
	
	@Column(name = "date_changed")
	private Date dateChanged;
	
	// Constructors
	
	/** default constructor */
	public ConceptProposal() {
	}
	
	/** constructor with id */
	public ConceptProposal(Integer conceptProposalId) {
		this.conceptProposalId = conceptProposalId;
	}
	
	/**
	 * @return Returns the changedBy.
	 */
	public User getChangedBy() {
		return changedBy;
	}
	
	/**
	 * @param changedBy The changedBy to set.
	 */
	public void setChangedBy(User changedBy) {
		this.changedBy = changedBy;
	}
	
	/**
	 * @return Returns the conceptProposalId.
	 */
	public Integer getConceptProposalId() {
		return conceptProposalId;
	}
	
	/**
	 * @param conceptProposalId The conceptProposalId to set.
	 */
	public void setConceptProposalId(Integer conceptProposalId) {
		this.conceptProposalId = conceptProposalId;
	}
	
	/**
	 * @return Returns the creator.
	 */
	public User getCreator() {
		return creator;
	}
	
	/**
	 * @param creator The creator to set.
	 */
	public void setCreator(User creator) {
		this.creator = creator;
	}
	
	/**
	 * @return Returns the dateChanged.
	 */
	public Date getDateChanged() {
		return dateChanged;
	}
	
	/**
	 * @param dateChanged The dateChanged to set.
	 */
	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
	}
	
	/**
	 * @return Returns the dateCreated.
	 */
	public Date getDateCreated() {
		return dateCreated;
	}
	
	/**
	 * @param dateCreated The dateCreated to set.
	 */
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	
	/**
	 * @return Returns the originalText.
	 */
	public String getOriginalText() {
		return originalText;
	}
	
	/**
	 * @param originalText The originalText to set.
	 */
	public void setOriginalText(String originalText) {
		this.originalText = originalText;
	}
	
	/**
	 * @return Returns the final text.
	 */
	public String getFinalText() {
		return finalText;
	}
	
	/**
	 * @param t The final text to set.
	 */
	public void setFinalText(String t) {
		this.finalText = t;
	}
	
	/**
	 * @return Returns the comments.
	 */
	public String getComments() {
		return comments;
	}
	
	/**
	 * @param comments The comments to set.
	 */
	public void setComments(String comments) {
		this.comments = comments;
	}
	
	/**
	 * @return Returns the state.
	 */
	public String getState() {
		return state;
	}
	
	/**
	 * @param state The state to set.
	 */
	public void setState(String state) {
		this.state = state;
	}
	
	/**
	 * @return Returns the encounter.
	 */
	public Encounter getEncounter() {
		return encounter;
	}
	
	/**
	 * @param encounter The encounter to set.
	 */
	public void setEncounter(Encounter encounter) {
		this.encounter = encounter;
	}
	
	@Override
	public String toString() {
		if (conceptProposalId == null) {
			return "";
		}
		return conceptProposalId.toString();
	}
	
	/**
	 * @return Returns the obs.
	 */
	public Obs getObs() {
		return obs;
	}
	
	/**
	 * @param obs The obs to set.
	 */
	public void setObs(Obs obs) {
		this.obs = obs;
	}
	
	/**
	 * @return Returns the obsConcept.
	 */
	public Concept getObsConcept() {
		return obsConcept;
	}
	
	/**
	 * @param obsConcept The obsConcept to set.
	 */
	public void setObsConcept(Concept obsConcept) {
		this.obsConcept = obsConcept;
	}
	
	/**
	 * @return Returns the mappedConcept.
	 */
	public Concept getMappedConcept() {
		return mappedConcept;
	}
	
	/**
	 * @param mappedConcept The mappedConcept to set.
	 */
	public void setMappedConcept(Concept mappedConcept) {
		this.mappedConcept = mappedConcept;
	}
	
	/**
	 * Convenience method to mark this proposal as rejected. Be sure to call
	 * Context.getConceptService().saveConceptProposal(/thisObject/) after calling this method
	 */
	public void rejectConceptProposal() {
		setState(OpenmrsConstants.CONCEPT_PROPOSAL_REJECT);
		setFinalText("");
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getConceptProposalId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setConceptProposalId(id);
	}
}
