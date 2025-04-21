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

/**
 * A ConceptProposal is a temporary holder for concept that should be in the system. When defining
 * an observation, a user can "propose" a new concept if one isn't found already. The proposal is a
 * simple text entry that will be reviewed later. When a proposal is (edited and) accepted, the
 * encounter that prompted this proposal is updated with a new observation pointing at the new (or
 */

@Entity
@Table(name = "concept_proposal")
@Audited
public class ConceptProposal extends BaseOpenmrsObject implements java.io.Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "concept_proposal_id")
	private Integer conceptProposalId;

	@ManyToOne
	@JoinColumn(name = "concept_id")
	private Concept concept;

	@ManyToOne
	@JoinColumn(name = "obs_id")
	private Obs obs;

	@ManyToOne
	@JoinColumn(name = "encounter_id")
	private Encounter encounter;

	@ManyToOne
	@JoinColumn(name = "obs_concept_id")
	private Concept obsConcept;

	@ManyToOne
	@JoinColumn(name = "mapped_concept_id")
	private Concept mappedConcept;

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
	public ConceptProposal() {
	}

	public ConceptProposal(Integer conceptProposalId) {
		this.conceptProposalId = conceptProposalId;
	}

	// Getters and Setters
	public Integer getConceptProposalId() {
		return conceptProposalId;
	}

	public void setConceptProposalId(Integer conceptProposalId) {
		this.conceptProposalId = conceptProposalId;
	}

	public Concept getConcept() {
		return concept;
	}

	public void setConcept(Concept concept) {
		this.concept = concept;
	}

	public Obs getObs() {
		return obs;
	}

	public void setObs(Obs obs) {
		this.obs = obs;
	}

	public Encounter getEncounter() {
		return encounter;
	}

	public void setEncounter(Encounter encounter) {
		this.encounter = encounter;
	}

	public Concept getObsConcept() {
		return obsConcept;
	}

	public void setObsConcept(Concept obsConcept) {
		this.obsConcept = obsConcept;
	}

	public Concept getMappedConcept() {
		return mappedConcept;
	}

	public void setMappedConcept(Concept mappedConcept) {
		this.mappedConcept = mappedConcept;
	}

	public String getOriginalText() {
		return originalText;
	}

	public void setOriginalText(String originalText) {
		this.originalText = originalText;
	}

	public String getFinalText() {
		return finalText;
	}

	public void setFinalText(String finalText) {
		this.finalText = finalText;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public User getChangedBy() {
		return changedBy;
	}

	public void setChangedBy(User changedBy) {
		this.changedBy = changedBy;
	}

	public Date getDateChanged() {
		return dateChanged;
	}

	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
	}

	@Override
	public Integer getId() {
		return conceptProposalId;
	}

	@Override
	public void setId(Integer id) {
		this.conceptProposalId = id;
	}
}
