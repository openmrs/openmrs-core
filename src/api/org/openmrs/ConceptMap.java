package org.openmrs;

import java.util.Date;

import org.openmrs.synchronization.Synchronizable;

/**
 * ConceptMap 
 */
public class ConceptMap implements java.io.Serializable, Synchronizable {

	public static final long serialVersionUID = 754677L;

	// Fields

	private Integer conceptMapId;
	private ConceptSource source;
	private Integer sourceId;
	private String comment;
	private User creator;
	private Date dateCreated;
	private String guid;
    private transient String lastRecordGuid;
    
    public String getLastRecordGuid() {
        return lastRecordGuid;
    }

    public void setLastRecordGuid(String lastRecordGuid) {
        this.lastRecordGuid = lastRecordGuid;
    }

  public String getGuid() {
      return guid;
  }

  public void setGuid(String guid) {
      this.guid = guid;
  }

	// Constructors

	/** default constructor */
	public ConceptMap() {
	}

	/** constructor with id */
	public ConceptMap(Integer conceptMapId) {
		this.conceptMapId = conceptMapId;
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof ConceptMap) {
			ConceptMap c = (ConceptMap)obj;
			return (this.conceptMapId.equals(c.getConceptMapId()));
		}
		return false;
	}
	
	public int hashCode() {
		if (this.getConceptMapId() == null) return super.hashCode();
		return this.getConceptMapId().hashCode();
	}

	/**
	 * @return Returns the comment.
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @param comment The comment to set.
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * @return Returns the conceptMapId.
	 */
	public Integer getConceptMapId() {
		return conceptMapId;
	}

	/**
	 * @param conceptMapId The conceptMapId to set.
	 */
	public void setConceptMapId(Integer conceptMapId) {
		this.conceptMapId = conceptMapId;
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
	 * @return Returns the source.
	 */
	public ConceptSource getSource() {
		return source;
	}

	/**
	 * @param source The source to set.
	 */
	public void setSource(ConceptSource source) {
		this.source = source;
	}

	/**
	 * @return Returns the sourceId.
	 */
	public Integer getSourceId() {
		return sourceId;
	}

	/**
	 * @param sourceId The sourceId to set.
	 */
	public void setSourceId(Integer sourceId) {
		this.sourceId = sourceId;
	}

	
	
}