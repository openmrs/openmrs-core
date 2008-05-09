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

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.ReportService;
import org.openmrs.cohort.CohortDefinition;
import org.openmrs.report.EvaluationContext;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

/**
 * This class represents a list of patientIds.
 * If it is generated from a CohortDefinition via {@link ReportService#evaluate(org.openmrs.report.ReportSchema, Cohort, EvaluationContext)}
 * then it will contain a link back to the CohortDefinition it came from and the
 * EvalutionContext that definition was evaluated in.
 * @see org.openmrs.cohort.CohortDefinition
 */
@Root(strict=false)
public class Cohort implements Serializable {

	public static final long serialVersionUID = 0L;
	public Log log = LogFactory.getLog(this.getClass());
	
	private Integer cohortId;
	private String name;
	private String description;
	private User creator;
	private Date dateCreated;
	private Boolean voided = false;
	private User voidedBy;
	private Date dateVoided;
	private String voidReason;
	private Set<Integer> memberIds;
	private CohortDefinition cohortDefinition;
	private EvaluationContext evaluationContext;

	public Cohort() {
		memberIds = new HashSet<Integer>();
	}
	
	/**
	 * Convenience constructor to create a Cohort object that
	 * has an primarykey/internal identifier of <code>cohortId</code>
	 * 
	 * @param cohortId the internal identifier for this cohort
	 */
	public Cohort(Integer cohortId) {
		memberIds = new HashSet<Integer>();
		this.cohortId = cohortId;
	}
	
	public Cohort(Set<Integer> memberIds) {
		this.memberIds = memberIds;
	}
	
	public Cohort(Collection<Integer> memberIds) {
		this.memberIds = new HashSet<Integer>();
		this.memberIds.addAll(memberIds);
	}
	
	public Cohort(String commaSeparatedIds) {
		memberIds = new HashSet<Integer>();
		for (StringTokenizer st = new StringTokenizer(commaSeparatedIds, ","); st.hasMoreTokens(); ) {
			String id = st.nextToken();
			memberIds.add(new Integer(id.trim()));
		}
	}
		
	public String getCommaSeparatedPatientIds() {
		StringBuilder sb = new StringBuilder();
		for (Iterator<Integer> i = getMemberIds().iterator(); i.hasNext(); ) {
			sb.append(i.next());
			if (i.hasNext()) {
				sb.append(",");
			}
		}
		return sb.toString();
	}
		
	public String toString() {
		return getMemberIds() == null ? "Cohort with null members" : (getMemberIds().size() + " patients");
	}
	
	public boolean equals(Object obj) {
		if (this.getCohortId() == null)
			return false;
		if (obj instanceof Cohort) {
			Cohort c = (Cohort)obj;
			return (this.getCohortId().equals(c.getCohortId()));
		}
		return false;
	}
	
	public int hashCode() {
		if (this.getCohortId() == null) return super.hashCode();
		int hash = 8;
		hash = 31 * this.getCohortId() + hash;
		return hash;
	}
	
	public void addMember(Integer memberId) {
		getMemberIds().add(memberId);
	}
	
	public void removeMember(Integer memberId) {
		getMemberIds().remove(memberId);
	}
	
	public int size() {
		return getMemberIds() == null ? 0 : getMemberIds().size();
	}
	
	public int getSize() {
		return size();
	}
	
	// static utility methods
	
	public static Cohort union(Cohort a, Cohort b) {
		Cohort ret = new Cohort();
		ret.setName("(" + a.getName() + " + " + b.getName() + ")");
		if (a != null)
			ret.getMemberIds().addAll(a.getMemberIds());
		if (b != null)
			ret.getMemberIds().addAll(b.getMemberIds());
		return ret;
	}
	
	public static Cohort intersect(Cohort a, Cohort b) {
		Cohort ret = new Cohort();
		ret.setName("(" + a.getName() + " * " + b.getName() + ")");
		if (a != null && b != null) {
			ret.getMemberIds().addAll(a.getMemberIds());
			ret.getMemberIds().retainAll(b.getMemberIds());
		}
		return ret;
	}
	
	public static Cohort subtract(Cohort a, Cohort b) {
		Cohort ret = new Cohort();
		ret.setName("(" + a.getName() + " - " + b.getName() + ")");
		if (a != null) {
			ret.getMemberIds().addAll(a.getMemberIds());
			if (b != null)
				ret.getMemberIds().removeAll(b.getMemberIds());
		}
		return ret;
	}
	
	// getters and setters
	
	@Attribute(required=false)
	public Integer getCohortId() {
		return cohortId;
	}
	
	@Attribute(required=false)
	public void setCohortId(Integer cohortId) {
		this.cohortId = cohortId;
	}
	
	@Attribute(required=false)
	public User getCreator() {
		return creator;
	}
	
	@Attribute(required=false)
	public void setCreator(User creator) {
		this.creator = creator;
	}
	
	@Attribute(required=false)
	public Date getDateCreated() {
		return dateCreated;
	}
	
	@Attribute(required=false)
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	
	@Attribute(required=false)
	public Date getDateVoided() {
		return dateVoided;
	}
	
	@Attribute(required=false)
	public void setDateVoided(Date dateVoided) {
		this.dateVoided = dateVoided;
	}
	
	@Element(required=false)
	public String getDescription() {
		return description;
	}
	
	@Element(required=false)
	public void setDescription(String description) {
		this.description = description;
	}
	
	@Element(required=false)
	public String getName() {
		return name;
	}
	
	@Element(required=false)
	public void setName(String name) {
		this.name = name;
	}
	
	@Attribute(required=false)
	public Boolean getVoided() {
		return voided;
	}
	
	@Attribute(required=false)
	public void setVoided(Boolean voided) {
		this.voided = voided;
	}
	
	@Attribute(required=false)
	public User getVoidedBy() {
		return voidedBy;
	}
	
	@Attribute(required=false)
	public void setVoidedBy(User voidedBy) {
		this.voidedBy = voidedBy;
	}
	
	@Attribute(required=false)
	public String getVoidReason() {
		return voidReason;
	}
	
	@Attribute(required=false)
	public void setVoidReason(String voidReason) {
		this.voidReason = voidReason;
	}

	@ElementList(required=true)
	public Set<Integer> getMemberIds() {
		return memberIds;
	}
	
	/**
	 * This method is only here for some backwards compatibility 
	 * with the PatientSet object that this Cohort object 
	 * replaced.  Do not use this method.
	 * 
	 * @deprecated use #getMemberIds()
	 * @return the memberIds
	 */
	public Set<Integer> getPatientIds() {
		return getMemberIds();
	}
	

	@ElementList(required=true)
	public void setMemberIds(Set<Integer> memberIds) {
		this.memberIds = memberIds;
	}

	/**
     * @return the cohortDefinition
     */
	@Element(required=false)
    public CohortDefinition getCohortDefinition() {
    	return cohortDefinition;
    }

	/**
     * @param cohortDefinition the cohortDefinition to set
     */
	@Element(required=false)
    public void setCohortDefinition(CohortDefinition cohortDefinition) {
    	this.cohortDefinition = cohortDefinition;
    }

	/**
	 * @return the evaluationContext
	 */
	@Element(required=false)
	public EvaluationContext getEvaluationContext() {
    	return evaluationContext;
    }

	/**
	 * @param evaluationContext the evaluationContext to set
	 */
	@Element(required=false)
	public void setEvaluationContext(EvaluationContext evaluationContext) {
    	this.evaluationContext = evaluationContext;
    }
	
}
