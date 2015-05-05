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
import org.openmrs.Cohort;

import java.util.Arrays;

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
		if (members == null) {
			this.members = new Integer[0];
		} else {
			this.members = Arrays.copyOf(members, members.length);
		}
	}
	
}
