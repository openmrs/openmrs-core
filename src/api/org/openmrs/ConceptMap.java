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

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * The concept map object represents a mapping of Concept to ConceptSource.   
 * A concept can have 0 to N mappings to any and all concept sources in the database.
 */
@Root
public class ConceptMap implements java.io.Serializable {

	public static final long serialVersionUID = 754677L;

	// Fields

	private Integer conceptMapId;
	private Concept concept;
	private ConceptSource source;
	private Integer sourceId;
	private String sourceCode;
	private String comment;
	private User creator;
	private Date dateCreated;

	// Constructors

	/** default constructor */
	public ConceptMap() {
	}

	/** constructor with concept map id */
	public ConceptMap(Integer conceptMapId) {
		this.conceptMapId = conceptMapId;
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof ConceptMap) {
			ConceptMap c = (ConceptMap)obj;
			
			if (getConceptMapId() == null)
				return false;
			
			return (this.conceptMapId.equals(c.getConceptMapId()));
		}
		return false;
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		if (this.getConceptMapId() == null) return super.hashCode();
		return this.getConceptMapId().hashCode();
	}
	
	/**
     * @return the concept
     */
	@Element
	public Concept getConcept() {
    	return concept;
    }

	/**
     * @param concept the concept to set
     */
	@Element
	public void setConcept(Concept concept) {
    	this.concept = concept;
    }

	/**
	 * @return Returns the comment.
	 */
    @Element(data=true, required=false)
	public String getComment() {
		return comment;
	}

	/**
	 * @param comment The comment to set.
	 */
	@Element(data=true, required=false)
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * @return Returns the conceptMapId.
	 */
	@Attribute
	public Integer getConceptMapId() {
		return conceptMapId;
	}

	/**
	 * @param conceptMapId The conceptMapId to set.
	 */
	@Attribute
	public void setConceptMapId(Integer conceptMapId) {
		this.conceptMapId = conceptMapId;
	}

	/**
	 * @return Returns the creator.
	 */
	@Element
	public User getCreator() {
		return creator;
	}

	/**
	 * @param creator The creator to set.
	 */
	@Element
	public void setCreator(User creator) {
		this.creator = creator;
	}

	/**
	 * @return Returns the dateCreated.
	 */
	@Element
	public Date getDateCreated() {
		return dateCreated;
	}

	/**
	 * @param dateCreated The dateCreated to set.
	 */
	@Element
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	/**
	 * @return Returns the source.
	 */
	@Element
	public ConceptSource getSource() {
		return source;
	}

	/**
	 * @param source The source to set.
	 */
	@Element
	public void setSource(ConceptSource source) {
		this.source = source;
	}

	/**
	 * @return Returns the sourceCode.
	 */
	@Element
	public String getSourceCode() {
		return sourceCode;
	}

	/**
	 * @param sourceCode The sourceCode to set.
	 */
	@Element
	public void setSourceCode(String sourceCode) {
		this.sourceCode = sourceCode;
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