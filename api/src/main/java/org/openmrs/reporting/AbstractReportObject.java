/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.reporting;

import java.util.Date;

import org.openmrs.BaseOpenmrsObject;
import org.openmrs.User;

/**
 * @deprecated see reportingcompatibility module
 */
@Deprecated
public class AbstractReportObject extends BaseOpenmrsObject implements ReportObject {
	
	private Integer reportObjectId; // database primary key
	
	private String name;
	
	private String description;
	
	private String type;
	
	private String subType;
	
	public AbstractReportObject() {
		// do nothing
	}
	
	public AbstractReportObject(Integer reportObjectId, String name, String description, String type, String subType,
	    User creator, Date dateCreated, User changedBy, Date dateChanged, Boolean voided, User voidedBy, Date dateVoided,
	    String voidReason) {
		this.reportObjectId = reportObjectId;
		this.name = name;
		this.description = description;
		this.type = type;
		this.subType = subType;
	}
	
	/**
	 * @return Returns the reportObjectId.
	 */
	public Integer getReportObjectId() {
		return reportObjectId;
	}
	
	/**
	 * @param reportObjectId The reportObjectId to set.
	 */
	public void setReportObjectId(Integer reportObjectId) {
		this.reportObjectId = reportObjectId;
		
	}
	
	/**
	 * @return Returns the name;
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return Returns the description
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * @param description The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getSubType() {
		return subType;
	}
	
	public void setSubType(String subType) {
		this.subType = subType;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	/**
	 * Helper function that does a null-safe test for equality between two objects. Does not check
	 * for type-safety.
	 * 
	 * @return Whether or not two objects (of the same class) are equal.
	 */
	public static boolean equals(Object o1, Object o2) {
		return o1 == null ? o2 == null : o1.equals(o2);
	}
	
	public String toString() {
		return this.getReportObjectId() + ", " + this.getName() + ", " + this.getDescription() + ", " + this.getType()
		        + ", " + this.getSubType();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	public Integer getId() {
		return getReportObjectId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		setReportObjectId(id);
	}
}
