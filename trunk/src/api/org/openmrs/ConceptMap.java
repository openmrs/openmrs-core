/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs;

import java.util.Date;

/**
 * ConceptMap 
 */
public class ConceptMap implements java.io.Serializable {

	public static final long serialVersionUID = 754677L;

	// Fields

	private Integer conceptMapId;
	private ConceptSource source;
	private Integer sourceId;
	private String comment;
	private User creator;
	private Date dateCreated;

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