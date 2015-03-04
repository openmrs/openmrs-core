/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.dwr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.util.OpenmrsUtil;

public class ConceptReferenceTermListItem {
	
	private static final Log log = LogFactory.getLog(ConceptReferenceTermListItem.class);
	
	private Integer conceptReferenceTermId;
	
	private String name;
	
	private String code;
	
	private Integer conceptSourceId;
	
	private String conceptSourceName;
	
	private Boolean retired = Boolean.FALSE;
	
	public ConceptReferenceTermListItem() {
	}
	
	/**
	 * Most common constructor
	 *
	 * @param conceptReferenceTerm the search to use to construct this conceptReferenceTermListItem
	 */
	public ConceptReferenceTermListItem(ConceptReferenceTerm conceptReferenceTerm) {
		if (conceptReferenceTerm != null) {
			conceptReferenceTermId = conceptReferenceTerm.getConceptReferenceTermId();
			name = conceptReferenceTerm.getName();
			code = conceptReferenceTerm.getCode();
			if (conceptReferenceTerm.getConceptSource() != null) {
				conceptSourceId = conceptReferenceTerm.getConceptSource().getConceptSourceId();
				conceptSourceName = conceptReferenceTerm.getConceptSource().getName();
			}
			retired = conceptReferenceTerm.isRetired();
		}
	}
	
	/**
	 * @return the conceptReferenceTermId
	 */
	public Integer getConceptReferenceTermId() {
		return conceptReferenceTermId;
	}
	
	/**
	 * @param conceptReferenceTermId the conceptReferenceTermId to set
	 */
	public void setConceptReferenceTermId(Integer conceptReferenceTermId) {
		this.conceptReferenceTermId = conceptReferenceTermId;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @param name the name to set to
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}
	
	/**
	 * @param code the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}
	
	/**
	 * @return the conceptSourceId
	 */
	public Integer getConceptSourceId() {
		return conceptSourceId;
	}
	
	/**
	 * @param conceptSourceId the conceptSourceId to set
	 */
	public void setConceptSourceId(Integer conceptSourceId) {
		this.conceptSourceId = conceptSourceId;
	}
	
	/**
	 * @return the conceptSourceName
	 */
	public String getConceptSourceName() {
		return conceptSourceName;
	}
	
	/**
	 * @param conceptSourceName the conceptSourceName to set
	 */
	public void setConceptSourceName(String conceptSourceName) {
		this.conceptSourceName = conceptSourceName;
	}
	
	/**
	 * @return the retired
	 */
	public Boolean getRetired() {
		return retired;
	}
	
	/**
	 * @param retired the retired to set
	 */
	public void setRetired(Boolean retired) {
		this.retired = retired;
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ConceptReferenceTermListItem) {
			ConceptReferenceTermListItem term2 = (ConceptReferenceTermListItem) obj;
			OpenmrsUtil.nullSafeEquals(conceptReferenceTermId, term2.getConceptReferenceTermId());
		}
		return false;
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (conceptReferenceTermId != null) {
			return 31 * conceptReferenceTermId.hashCode();
		} else {
			return super.hashCode();
		}
	}
}
