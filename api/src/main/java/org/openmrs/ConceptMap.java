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

import org.hibernate.search.annotations.ContainedIn;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * The concept map object represents a mapping of Concept to ConceptSource. A concept can have 0 to
 * N mappings to any and all concept sources in the database.
 */
@Root
public class ConceptMap extends BaseConceptMap implements java.io.Serializable {
	
	public static final long serialVersionUID = 754677L;
	
	// Fields
	@DocumentId
	private Integer conceptMapId;
	
	@ContainedIn
	private Concept concept;
	
	@IndexedEmbedded(includeEmbeddedObjectId = true)
	private ConceptReferenceTerm conceptReferenceTerm;
	
	// Constructors
	
	/** default constructor */
	public ConceptMap() {
	}
	
	/** constructor with concept map id */
	public ConceptMap(Integer conceptMapId) {
		this.conceptMapId = conceptMapId;
	}
	
	/**
	 * Convenience constructor that takes the term to be mapped to and the type of the map
	 *
	 * @param conceptReferenceTerm the concept reference term to map to
	 * @param conceptMapType the concept map type for this concept reference term map
	 */
	public ConceptMap(ConceptReferenceTerm conceptReferenceTerm, ConceptMapType conceptMapType) {
		this.conceptReferenceTerm = conceptReferenceTerm;
		setConceptMapType(conceptMapType);
	}
	
	/**
	 * @see org.openmrs.BaseOpenmrsObject#toString()
	 */
	@Override
	public String toString() {
		if (conceptMapId == null) {
			return "";
		}
		return conceptMapId.toString();
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
	 * Comments on concept maps are no longer supported since version 1.9, therefore a call to this
	 * methods is useless
	 *
	 * @return Returns the comment.
	 * @deprecated
	 */
	@Deprecated
	public String getComment() {
		return getConceptReferenceTerm().getDescription();
	}
	
	/**
	 * Comments on concept maps are no longer supported since version 1.9, therefore a call to this
	 * results in setting the description of the associated reference term to the specified value
	 *
	 * @param comment The comment to set.
	 * @deprecated
	 */
	@Deprecated
	public void setComment(String comment) {
		getConceptReferenceTerm().setDescription(comment);
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
	 * The conceptSource should be accessed from the associated ConceptReferenceTerm since version
	 * 1.9
	 *
	 * @return Returns the source.
	 * @deprecated
	 * @see ConceptReferenceTerm#getConceptSource()
	 */
	@Deprecated
	public ConceptSource getSource() {
		return getConceptReferenceTerm().getConceptSource();
	}
	
	/**
	 * The conceptSource should be set on the associated ConceptReferenceTerm since version 1.9
	 *
	 * @param source The source to set.
	 * @deprecated
	 * @see ConceptReferenceTerm#setConceptSource(ConceptSource)
	 */
	@Deprecated
	public void setSource(ConceptSource source) {
		getConceptReferenceTerm().setConceptSource(source);
	}
	
	/**
	 * The sourceCode should be accessed from the associated ConceptReferenceTerm since version 1.9
	 *
	 * @return Returns the sourceCode.
	 * @deprecated
	 * @see ConceptReferenceTerm#getCode()
	 */
	@Deprecated
	public String getSourceCode() {
		return getConceptReferenceTerm().getCode();
	}
	
	/**
	 * The sourceCode should be set on the associated ConceptReferenceTerm since version 1.9
	 *
	 * @param sourceCode The sourceCode to set.
	 * @deprecated
	 * @see ConceptReferenceTerm#setCode(String)
	 */
	@Deprecated
	public void setSourceCode(String sourceCode) {
		getConceptReferenceTerm().setCode(sourceCode);
	}
	
	/**
	 * @return the conceptReferenceTerm
	 * @since 1.9
	 */
	public ConceptReferenceTerm getConceptReferenceTerm() {
		if (conceptReferenceTerm == null) {
			conceptReferenceTerm = new ConceptReferenceTerm();
		}
		return conceptReferenceTerm;
	}
	
	/**
	 * @param conceptReferenceTerm the conceptReferenceTerm to set
	 * @since 1.9
	 */
	public void setConceptReferenceTerm(ConceptReferenceTerm conceptReferenceTerm) {
		this.conceptReferenceTerm = conceptReferenceTerm;
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	public Integer getId() {
		return getConceptMapId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		setConceptMapId(id);
	}
	
}
