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
