package org.openmrs;

import java.util.Comparator;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.BatchSize;
import org.hibernate.envers.Audited;

@Entity
@Table(name = "concept_answer")
@BatchSize(size = 25)
@Audited
public class ConceptAnswer extends BaseOpenmrsObject implements Auditable, java.io.Serializable, Comparable<ConceptAnswer> {
	
	public static final long serialVersionUID = 3744L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "concept_answer_id")
	private Integer conceptAnswerId;
	
	@ManyToOne
	@JoinColumn(name = "concept_id", nullable = false)
	private Concept concept;
	
	@ManyToOne
	@JoinColumn(name = "answer_concept", nullable = false)
	private Concept answerConcept;
	
	@ManyToOne
	@JoinColumn(name = "answer_drug")
	private Drug answerDrug;
	
	@ManyToOne
	@JoinColumn(name = "creator", nullable = false)
	private User creator;
	
	@Column(name = "date_created", nullable = false)
	private Date dateCreated;
	
	@Column(name = "sort_weight")
	private Double sortWeight;
	
	public ConceptAnswer() {
	}
	
	public ConceptAnswer(Integer conceptAnswerId) {
		this.conceptAnswerId = conceptAnswerId;
	}
	
	public ConceptAnswer(Concept answerConcept) {
		this.answerConcept = answerConcept;
	}
	
	public ConceptAnswer(Concept answerConcept, Drug d) {
		this.answerConcept = answerConcept;
		this.answerDrug = d;
	}
	
	public Concept getAnswerConcept() {
		return answerConcept;
	}
	
	public void setAnswerConcept(Concept answerConcept) {
		this.answerConcept = answerConcept;
	}
	
	public Drug getAnswerDrug() {
		return answerDrug;
	}
	
	public void setAnswerDrug(Drug answerDrug) {
		this.answerDrug = answerDrug;
	}
	
	public Concept getConcept() {
		return concept;
	}
	
	public void setConcept(Concept concept) {
		this.concept = concept;
	}
	
	public Integer getConceptAnswerId() {
		return conceptAnswerId;
	}
	
	public void setConceptAnswerId(Integer conceptAnswerId) {
		this.conceptAnswerId = conceptAnswerId;
	}
	
	@Override
	public User getCreator() {
		return creator;
	}
	
	@Override
	public void setCreator(User creator) {
		this.creator = creator;
	}
	
	@Override
	public Date getDateCreated() {
		return dateCreated;
	}
	
	@Override
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	
	@Override
	public Integer getId() {
		return getConceptAnswerId();
	}
	
	@Override
	public void setId(Integer id) {
		setConceptAnswerId(id);
	}
	
	@Override
	public User getChangedBy() {
		return null;
	}
	
	@Override
	public Date getDateChanged() {
		return null;
	}
	
	@Override
	public void setChangedBy(User changedBy) {
	}
	
	@Override
	public void setDateChanged(Date dateChanged) {
	}
	
	public Double getSortWeight() {
		return sortWeight;
	}
	
	public void setSortWeight(Double sortWeight) {
		this.sortWeight = sortWeight;
	}
	
	/**
	 * Comparator for ConceptAnswer objects based on sortWeight
	 */
	@Override
	@SuppressWarnings("squid:S1210")
	public int compareTo(ConceptAnswer other) {
		
		if (other == null) {
			return 1;
		}
		
		return Comparator
			.nullsFirst(Double::compareTo)
			.compare(this.sortWeight, other.sortWeight);
	}
}
