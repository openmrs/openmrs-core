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
package org.openmrs.web.dwr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;

/**
 * A cohort, including list its member list, for use with DWR.
 */
public class CohortListItem {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	private Integer cohortId;
	
	private String name;
	
	private String description;
	
	private Integer[] members;
	
	public CohortListItem(Cohort cohort) {
		this.cohortId = cohort.getCohortId();
		this.name = cohort.getName();
		this.description = cohort.getDescription();
		this.members = cohort.getMemberIds().toArray(new Integer[cohort.getMemberIds().size()]);
	}
	
	public Integer getCohortId() {
		return cohortId;
	}
	
	public void setCohortId(Integer cohortId) {
		this.cohortId = cohortId;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public Integer[] getMembers() {
		return members;
	}
	
	public void setMembers(Integer[] members) {
		this.members = members;
	}
	
}
