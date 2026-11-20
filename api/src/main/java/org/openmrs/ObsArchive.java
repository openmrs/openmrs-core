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

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import org.hibernate.envers.Audited;

/**
 * Entity representing archived observations.
 *
 * @since 3.0.0
 */
@Audited
@Entity
@Table(name = "obs_archive")
public class ObsArchive extends BaseFormRecordableOpenmrsData {

	private static final long serialVersionUID = 473301L;

	@Id
	@Column(name = "obs_id")
	private Integer obsId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "person_id", nullable = false)
	private Person person;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "concept_id", nullable = false)
	private Concept concept;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "encounter_id")
	private Encounter encounter;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id")
	private Order order;

	@Column(name = "obs_datetime", nullable = false)
	private Date obsDatetime;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "location_id")
	private Location location;

	@Column(name = "obs_group_id")
	private Integer obsGroupId;

	@Column(name = "accession_number")
	private String accessionNumber;

	@Column(name = "value_group_id")
	private Integer valueGroupId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "value_coded")
	private Concept valueCoded;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "value_coded_name_id")
	private ConceptName valueCodedName;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "value_drug")
	private Drug valueDrug;

	@Column(name = "value_datetime")
	private Date valueDatetime;

	@Column(name = "value_numeric")
	private Double valueNumeric;

	@Column(name = "value_modifier")
	private String valueModifier;

	@Column(name = "value_text")
	private String valueText;

	@Column(name = "value_complex")
	private String valueComplex;

	@Column(name = "comments")
	private String comments;

	@Column(name = "previous_version")
	private Integer previousVersionId;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private Obs.Status status = Obs.Status.FINAL;

	@Enumerated(EnumType.STRING)
	@Column(name = "interpretation")
	private Obs.Interpretation interpretation;

	@OneToOne(mappedBy = "obsArchive", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private ObsArchiveReferenceRange referenceRange;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "archived_by")
	private User archivedBy;

	@Column(name = "date_archived")
	private Date dateArchived;

	public ObsArchive() {
		// required by Hibernate
	}

	public Integer getObsId() {
		return obsId;
	}

	public void setObsId(Integer obsId) {
		this.obsId = obsId;
	}

	@Override
	public Integer getId() {
		return getObsId();
	}

	@Override
	public void setId(Integer id) {
		setObsId(id);
	}

	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	public Concept getConcept() {
		return concept;
	}

	public void setConcept(Concept concept) {
		this.concept = concept;
	}

	public Encounter getEncounter() {
		return encounter;
	}

	public void setEncounter(Encounter encounter) {
		this.encounter = encounter;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public Date getObsDatetime() {
		return obsDatetime;
	}

	public void setObsDatetime(Date obsDatetime) {
		this.obsDatetime = obsDatetime;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public Integer getObsGroupId() {
		return obsGroupId;
	}

	public void setObsGroupId(Integer obsGroupId) {
		this.obsGroupId = obsGroupId;
	}

	public String getAccessionNumber() {
		return accessionNumber;
	}

	public void setAccessionNumber(String accessionNumber) {
		this.accessionNumber = accessionNumber;
	}

	public Integer getValueGroupId() {
		return valueGroupId;
	}

	public void setValueGroupId(Integer valueGroupId) {
		this.valueGroupId = valueGroupId;
	}

	public Concept getValueCoded() {
		return valueCoded;
	}

	public void setValueCoded(Concept valueCoded) {
		this.valueCoded = valueCoded;
	}

	public ConceptName getValueCodedName() {
		return valueCodedName;
	}

	public void setValueCodedName(ConceptName valueCodedName) {
		this.valueCodedName = valueCodedName;
	}

	public Drug getValueDrug() {
		return valueDrug;
	}

	public void setValueDrug(Drug valueDrug) {
		this.valueDrug = valueDrug;
	}

	public Date getValueDatetime() {
		return valueDatetime;
	}

	public void setValueDatetime(Date valueDatetime) {
		this.valueDatetime = valueDatetime;
	}

	public Double getValueNumeric() {
		return valueNumeric;
	}

	public void setValueNumeric(Double valueNumeric) {
		this.valueNumeric = valueNumeric;
	}

	public String getValueModifier() {
		return valueModifier;
	}

	public void setValueModifier(String valueModifier) {
		this.valueModifier = valueModifier;
	}

	public String getValueText() {
		return valueText;
	}

	public void setValueText(String valueText) {
		this.valueText = valueText;
	}

	public String getValueComplex() {
		return valueComplex;
	}

	public void setValueComplex(String valueComplex) {
		this.valueComplex = valueComplex;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public Integer getPreviousVersionId() {
		return previousVersionId;
	}

	public void setPreviousVersionId(Integer previousVersionId) {
		this.previousVersionId = previousVersionId;
	}

	public Obs.Status getStatus() {
		return status;
	}

	public void setStatus(Obs.Status status) {
		this.status = status;
	}

	public Obs.Interpretation getInterpretation() {
		return interpretation;
	}

	public void setInterpretation(Obs.Interpretation interpretation) {
		this.interpretation = interpretation;
	}

	public ObsArchiveReferenceRange getReferenceRange() {
		return referenceRange;
	}

	public void setReferenceRange(ObsArchiveReferenceRange referenceRange) {
		this.referenceRange = referenceRange;
	}

	public User getArchivedBy() {
		return archivedBy;
	}

	public void setArchivedBy(User archivedBy) {
		this.archivedBy = archivedBy;
	}

	public Date getDateArchived() {
		return dateArchived;
	}

	public void setDateArchived(Date dateArchived) {
		this.dateArchived = dateArchived;
	}
}
