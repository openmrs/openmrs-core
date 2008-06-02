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
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * Program
 */
@Root
public class Program implements java.io.Serializable {
	
	public static final long serialVersionUID = 3214567L;
	protected final Log log = LogFactory.getLog(getClass());

	// ******************
	// Properties
	// ******************
	
	private Integer programId;
	private String name;
	private String description;
	private Concept concept;
	private User creator; 
	private Date dateCreated; 
	private User changedBy;
	private Date dateChanged;
	private Boolean retired = false; 
	private Set<ProgramWorkflow> workflows = new HashSet<ProgramWorkflow>();
	
	// ******************
	// Constructors
	// ******************
	
	/** Default Constructor */
	public Program() { }
	
	/** Constructor with id */
	public Program(Integer programId) {
		setProgramId(programId);
	}
	
	// ******************
	// Instance methods
	// ******************
	
	/**
	 * Adds a new {@link ProgramWorkflow} to this Program
	 * @param workflow - the {@link ProgramWorkflow} to add
	 */
	public void addWorkflow(ProgramWorkflow workflow) {
		workflow.setProgram(this);
		getWorkflows().add(workflow);
	}

	/**
	 * Removes a {@link ProgramWorkflow} from this Program
	 * @param workflow - the {@link ProgramWorkflow} to remove
	 */
	public void removeWorkflow(ProgramWorkflow workflow) {
		if (getWorkflows().contains(workflow)) {
			getWorkflows().remove(workflow);
			workflow.setProgram(null);
		}
	}
	
	/**
	 * Retires a {@link ProgramWorkflow}
	 * @param workflow - the {@link ProgramWorkflow} to retire
	 */
	public void retireWorkflow(ProgramWorkflow workflow) {
		workflow.setRetired(true);
	}

	/**
	 * Returns a {@link ProgramWorkflow} whose {@link Concept} has any {@link ConceptName} that matches the given <code>name</name>
	 * @param name the {@link ProgramWorkflow} name, in any {@link Locale}
	 * @return a {@link ProgramWorkflow} which has the passed <code>name</code> in any {@link Locale}
	 */
	public ProgramWorkflow getWorkflowByName(String name) {
		for (ProgramWorkflow pw : getWorkflows()) {
			if (pw.getConcept().isNamed(name)) {
				return pw;
			}
		}
		return null;
	}
	
	/** @see Object#equals(Object) */
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof Program) {
			Program p = (Program)obj;
			if (this.getProgramId() == null) {
				return p.getProgramId() == null;
			}
			return (this.getProgramId().equals(p.getProgramId()));
		}
		return false;
	}

	/** @see Object#toString() */
	public String toString() {
		return "Program(id=" + getProgramId() + ", concept=" + getConcept() + ", workflows=" + getWorkflows() + ")";
	}
	
	// ******************
	// Property Access
	// ******************

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

	public Concept getConcept() {
		return concept;
	}

	public void setConcept(Concept concept) {
		this.concept = concept;
	}

	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public User getChangedBy() {
		return changedBy;
	}

	public void setChangedBy(User changedBy) {
		this.changedBy = changedBy;
	}

	public Date getDateChanged() {
		return dateChanged;
	}

	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
	}

	@Attribute(required=true)
	public Integer getProgramId() {
		return programId;
	}

	@Attribute(required=true)
	public void setProgramId(Integer programId) {
		this.programId = programId;
	}

    public Boolean getRetired() {
    	return retired;
    }
    
    public Boolean isRetired() {
    	return getRetired();
    }

    public void setRetired(Boolean retired) {
    	this.retired = retired;
    }

	public Set<ProgramWorkflow> getWorkflows() {
		return workflows;
	}

	public void setWorkflows(Set<ProgramWorkflow> workflows) {
		this.workflows = workflows;
	}
}
