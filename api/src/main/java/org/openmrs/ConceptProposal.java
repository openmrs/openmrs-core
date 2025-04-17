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

import java.util.Date;

import org.hibernate.envers.Audited;
import org.openmrs.util.OpenmrsConstants;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Column;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;


/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */


@Entity
@Table(name = "concept_proposal")
@Audited
public class ConceptProposal extends BaseOpenmrsObject {

	public static final long serialVersionUID = 57344L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "concept_proposal_id")
	private Integer conceptProposalId;

	@ManyToOne
	@JoinColumn(name = "concept_id")
	private Concept mappedConcept;

	@ManyToOne
	@JoinColumn(name = "encounter_id")
	private Encounter encounter;

	@Column(name = "original_text", nullable = false)
	private String originalText;

	@Column(name = "final_text")
	private String finalText;

	@ManyToOne
	@JoinColumn(name = "obs_id")
	private Obs obs;

	@ManyToOne
	@JoinColumn(name = "obs_concept_id")
	private Concept obsConcept;

	@Column(name = "state", nullable = false)
	private String state;

	@Column(name = "comments")
	private String comments;

	@ManyToOne
	@JoinColumn(name = "creator", nullable = false)
	private User creator;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "date_created", nullable = false)
	private Date dateCreated;

	@ManyToOne
	@JoinColumn(name = "changed_by")
	private User changedBy;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "date_changed")
	private Date dateChanged;

	@Column(name = "locale", nullable = false)
	private String locale;

	@Column(name = "uuid", nullable = false, unique = true, length = 38)
	private String uuid;

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

	public Encounter getEncounter() {
		return encounter;
	}

	public void setEncounter(Encounter encounter) {
		this.encounter = encounter;
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

	public Obs getObs() {
		return obs;
	}

	public void setObs(Obs obs) {
		this.obs = obs;
	}

	public Concept getObsConcept() {
		return obsConcept;
	}

	public void setObsConcept(Concept obsConcept) {
		this.obsConcept = obsConcept;
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

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	@Override
	public Integer getId() {
		return getConceptProposalId();
	}

	@Override
	public void setId(Integer id) {
		setConceptProposalId(id);
	}

	@Override
	public String toString() {
		if (conceptProposalId == null) {
			return "";
		}
		return conceptProposalId.toString();
	}

	/**
	 * Convenience method to mark this proposal as rejected. Be sure to call
	 * Context.getConceptService().saveConceptProposal(this) after calling this method
	 */
	public void rejectConceptProposal() {
		setState(OpenmrsConstants.CONCEPT_PROPOSAL_REJECT);
		setFinalText("");
	}
}
