package org.openmrs;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * ConceptProposal 
 */
public class ConceptProposal implements java.io.Serializable {

	public static final long serialVersionUID = 57344L;
	private Log log = LogFactory.getLog(this.getClass());

	// Fields

	private Integer conceptProposalId;
	private Encounter encounter;
	private Concept obsConcept;
	private Obs obs;
	private Concept mappedConcept;
	private String originalText;
	private String finalText;
	private String state;
	private String comments;
	private User creator;
	private Date dateCreated;
	private User changedBy;
	private Date dateChanged;

	// Constructors

	/** default constructor */
	public ConceptProposal() {	}

	/** constructor with id */
	public ConceptProposal(Integer conceptProposalId) {
		this.conceptProposalId = conceptProposalId;
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof ConceptProposal) {
			ConceptProposal c = (ConceptProposal)obj;
			return (this.conceptProposalId.equals(c.getConceptProposalId()));
		}
		return false;
	}
	
	public int hashCode() {
		if (this.getConceptProposalId() == null) return super.hashCode();
		int hash = 9;
		hash = 31 * this.getConceptProposalId() + hash;
		return hash;
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
	 * @param text The final text to set.
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

	public String toString() {
		if (conceptProposalId == null)
			return "";
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

}