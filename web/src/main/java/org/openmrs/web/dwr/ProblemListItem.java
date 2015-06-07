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

import java.util.Date;

import org.openmrs.activelist.Problem;

/**
 *
 */
public class ProblemListItem {
	
	private Integer activeListId;
	
	private Integer problemConceptId;
	
	private String problem;
	
	private Date start;
	
	private Date end;
	
	private String status;
	
	private String comments;
	
	public ProblemListItem(Problem problem) {
		this.setActiveListId(problem.getActiveListId());
		this.setProblemConceptId(problem.getProblem().getConceptId());
		this.setProblem(problem.getProblem().getName().getName());
		this.setStart(problem.getStartDate());
		this.setEnd(problem.getEndDate());
		this.setStatus((problem.getModifier() == null) ? null : problem.getModifier().name());
		this.setComments((problem.getComments() == null) ? null : problem.getComments());
	}
	
	public void setActiveListId(Integer activeListId) {
		this.activeListId = activeListId;
	}
	
	public Integer getActiveListId() {
		return activeListId;
	}
	
	public void setProblemConceptId(Integer problemConceptId) {
		this.problemConceptId = problemConceptId;
	}
	
	public Integer getProblemConceptId() {
		return problemConceptId;
	}
	
	public void setProblem(String problem) {
		this.problem = problem;
	}
	
	public String getProblem() {
		return problem;
	}
	
	public void setStart(Date start) {
		this.start = start;
	}
	
	public Date getStart() {
		return start;
	}
	
	public void setEnd(Date end) {
		this.end = end;
	}
	
	public Date getEnd() {
		return end;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setComments(String comments) {
		this.comments = comments;
	}
	
	public String getComments() {
		return comments;
	}
}
